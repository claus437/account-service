package dk.reimer.claus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @BeforeEach
    public void cleanup() {
        repository.deleteAll();
    }

    @Test
    public void insert() {
        Transaction transaction = new Transaction("1", new Date(), "10011999", BigDecimal.valueOf(200));
        repository.insert(transaction);
        Transaction result = repository.findById("1").orElseThrow();

        assertEquals(transaction, result);
    }

    @Test
    public void duplicateInsert() {
        Transaction transaction = new Transaction("1", new Date(), "10011999", BigDecimal.valueOf(200));
        repository.insert(transaction);
        assertThrows(DataIntegrityViolationException.class, () -> {
            repository.insert(transaction);
        });
    }
}