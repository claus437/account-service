package dk.reimer.claus;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest
@DirtiesContext
public class AccountValidatorTest {

    @Autowired
    Validator validator;

    @Test
    public void incorrectAccountNumber() {
        Account account = new Account("2");
        Set<ConstraintViolation<Account>> violations = validator.validate(account);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void correctAccountNumber() {
        Account account = new Account("12345678");
        Set<ConstraintViolation<Account>> violations = validator.validate(account);
        assertTrue(violations.isEmpty());
    }
}
