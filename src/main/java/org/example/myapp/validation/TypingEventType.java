package org.example.myapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

@Target({FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TypingEventTypeValidator.class)
@Documented
public @interface TypingEventType {
    String message() default "{error.typingevent.invalid}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
