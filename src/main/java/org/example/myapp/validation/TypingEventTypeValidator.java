package org.example.myapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.myapp.model.EventType;

public class TypingEventTypeValidator implements ConstraintValidator<TypingEventType, EventType> {

    @Override
    public boolean isValid(EventType value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value == EventType.TYPING_START || value == EventType.TYPING_STOP;
    }
}
