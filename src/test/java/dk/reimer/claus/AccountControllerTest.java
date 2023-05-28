package dk.reimer.claus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.concurrent.ListenableFuture;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = {AccountController.class})
@ContextConfiguration(classes={Application.class})
public class AccountControllerTest {
    @MockBean AccountRepository accounts;
    @MockBean TransactionRepository transactions;
    @MockBean KafkaTemplate<String, Transaction> kafka;

    @Autowired MockMvc mock;

    @Test
    public void getBalance() throws Exception {
        when(accounts.findById("10011999")).thenReturn(
            Optional.of(new Account("10011999", BigDecimal.valueOf(100)))
        );

        mock.perform(
            MockMvcRequestBuilders.get("/balance?account=10011999"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("100"));
    }

    @Test
    public void getBalanceOfUnknownAccount() throws Exception {
        when(accounts.findById("10011999")).thenReturn(Optional.empty());

        mock.perform(MockMvcRequestBuilders.get("/balance?account=10011999"))
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("no such account 10011999"));
    }

    @Test
    public void transfer() throws Exception {
        Transaction transaction = new Transaction(null, null, "10001999", BigDecimal.valueOf(150));

        ListenableFuture<SendResult<String, Transaction>> future = Mockito.mock(ListenableFuture.class);
        when(future.get()).thenReturn(null);
        when(kafka.send(eq("transactions"), refEq(transaction, "id", "date"))).thenReturn(future);

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(transaction);

        mock.perform(MockMvcRequestBuilders.post("/transfer").contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.id").isNotEmpty()
        );
    }

    @Test
    public void getLatestTransactions() throws Exception {
        List<Transaction> result = Arrays.asList(
            new Transaction(), new Transaction(), new Transaction()
        );

        when(transactions.findAllByAccountNumber(eq("10011999"), any())).thenReturn(result);

        mock.perform(MockMvcRequestBuilders.get("/list?count=3&account=10011999"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(3))
        );
    }

    @Test
    public void getLatestTransactionsFewerThanRequested() throws Exception {
        List<Transaction> result = Arrays.asList(
                new Transaction(), new Transaction()
        );

        when(transactions.findAllByAccountNumber(eq("10011999"), any())).thenReturn(result);

        mock.perform(MockMvcRequestBuilders.get("/list?count=3&account=10011999"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2))
        );
    }
}
