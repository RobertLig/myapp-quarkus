package org.example.myapp.exception;

public class FieldValidationException extends RuntimeException {

    private final String field;
    private final String messageKey;

    public FieldValidationException(String field, String messageKey) {
        super(messageKey);
        this.field = field;
        this.messageKey = messageKey;
    }

    public String getField() {
        return field;
    }

    public String getMessageKey() {
        return messageKey;
    }
}