package org.example.myapp.exception;

public class EntityNotFoundException extends RuntimeException {

    private final String messageKey;

    public EntityNotFoundException(String messageKey) {
        super(messageKey);
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}