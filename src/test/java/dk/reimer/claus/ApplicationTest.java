package dk.reimer.claus;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Just a small end to end test showing the flow works...
 *
 */
@SpringBootTest (webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.kafka.bootstrap-servers = http://localhost:9092"
})
@EmbeddedKafka(partitions = 1, topics = "transactions", brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@DirtiesContext
public class ApplicationTest {

    @Value(value="http://localhost:${local.server.port}")
    private String address;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    TransactionRepository transactions;

    @Autowired
    AccountRepository accounts;

    @KafkaListener(topics = "transactions", groupId = "junit")
    public void listen(ConsumerRecord<?, Transaction> record) {
        System.out.println(">>> " + record.value());
    }

    @Test
    public void withDrawMoney() throws InterruptedException {
        Transaction transaction = new Transaction();
        transaction.setAccountNumber("10011003");
        transaction.setAmount(BigDecimal.valueOf(-100));

        ResponseEntity<Transaction> response = rest.postForEntity(address + "/transfer", transaction, Transaction.class);
        assertEquals(200, response.getStatusCode().value());

        Transaction result = response.getBody();
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getDate());

        assertEquals(transaction.getAmount(), result.getAmount());
        assertEquals(transaction.getAccountNumber(), result.getAccountNumber());

        // waiting for the controller to pick up the message and write to db
        Thread.sleep(5000);

        Transaction stored = transactions.findById(result.getId()).orElseThrow();
        assertEquals(result, stored);

        Account account = accounts.findById(transaction.getAccountNumber()).orElseThrow();
        assertEquals(account.getBalance(), BigDecimal.valueOf(-100));
    }
}
