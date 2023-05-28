package dk.reimer.claus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

/**
 * Validator for account numbers.
 */
public class AccountNumberValidator implements ConstraintValidator<AccountNumber, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return Objects.toString(s, "").matches("^\\d{8}$");
    }
}
