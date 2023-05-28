package dk.reimer.claus;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = AccountNumberValidator.class)
@Documented
public @interface AccountNumber {
    String message() default "account number must be 8 digits";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
