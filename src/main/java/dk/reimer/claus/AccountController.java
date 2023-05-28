package dk.reimer.claus;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@Validated
public class AccountController {
    private final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final TransactionRepository transactions;
    private final AccountRepository accounts;
    private final KafkaTemplate<String, Transaction> kafka;

    @Autowired
    public AccountController(KafkaTemplate<String, Transaction> kafka, AccountRepository accounts, TransactionRepository transactions) {
        this.kafka = kafka;
        this.transactions = transactions;
        this.accounts = accounts;
    }

    /**
     * Executes the given transaction.
     *
     * If this is the first transaction, an account will be created for the given customer.
     *
     * Positive amounts is added to the given account, while negative amounts is
     * withdrawn from the account.
     *
     * @param transaction The transaction to be executed.
     * @return The transaction.
     *
     * @Throws InterruptedException If the thread is interrupted, the transaction may
     *                              or may not have been published.
     * @Throws ExecutionException   The transaction could not be published and was not settled.
     */
    @RequestMapping(value = "transfer", method = RequestMethod.POST)
    public Transaction transfer(@Valid @RequestBody Transaction transaction) throws InterruptedException, ExecutionException {
        transaction.setId(UUID.randomUUID().toString());
        transaction.setDate(new Date());

        logger.info("execution transaction " + transaction);

        kafka.send("transactions", transaction).get();

        return transaction;
    }

    /**
     * Gets the balance of the account.
     *
     * @return The balance of the account.
     */
    @RequestMapping(value = "balance", method = RequestMethod.GET)
    public BigDecimal getBalance(@RequestParam @AccountNumber String account) {
        return accounts.findById(account).orElseThrow(() -> new AccountServiceException(HttpStatus.BAD_REQUEST, "no such account " + account)).getBalance();
    }


    /**
     * Gets the given count of transactions or as many that is available if less
     * than requested.
     *
     * @param count The count of transactions to receive.
     *
     * @return The list of transactions
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public List<Transaction> getLatestTransactions(@RequestParam @AccountNumber String account, @RequestParam int count) {
        Pageable p = PageRequest.of(0, count, Sort.by("date").descending());
        List<Transaction> result = transactions.findAllByAccountNumber(account, p);

        return result;
    }

    /**
     * Listens fro transaction comming from kafka.
     *
     * @param record The kafka record holding the transaction.
     */
    @KafkaListener(topics = "transactions", groupId = "${account-service.instance.id}")
    public void transactionListener(ConsumerRecord<?, Transaction> record) {
        try {
            doTransaction(record.value());
        } catch (RuntimeException x) {
            logger.error("failed to store " + record.value(), x);
        }
    }

    /**
     * Adds the transaction to the database and updates the balance of the given account.
     *
     * @param transaction The transaction to be settled.
     */
    @Transactional
    public void doTransaction(Transaction transaction) {
        logger.info("settling transaction " + transaction);

        String accountNumber = transaction.getAccountNumber();

        // we don't want to allow another client to alter the balance after we read it as
        // it would lead to an incorrect value - however it's fine if another instance
        // is updating the balance simultaneously or even before as we will get the event
        // and bring things back in sync.
        synchronized (accountNumber.intern()) {
            Account account = accounts.findById(transaction.getAccountNumber()).orElse(new Account(accountNumber));

            BigDecimal balance = account.getBalance();
            balance = balance.add(transaction.getAmount());
            account.setBalance(balance);

            accounts.save(account);
            transactions.insert(transaction);
        }
    }
}
