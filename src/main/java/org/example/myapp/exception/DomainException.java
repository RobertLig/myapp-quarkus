package org.example.myapp.exception;

public class DomainException extends RuntimeException {

    private final String messageKey;
    private final int status;

    public DomainException(String messageKey, int status) {
        super(messageKey);
        this.messageKey = messageKey;
        this.status = status;
    }

    public static DomainException unauthorized(String messageKey) {
        return new DomainException(messageKey, 401);
    }

    public static DomainException forbidden(String messageKey) {
        return new DomainException(messageKey, 403);
    }

    public static DomainException tooManyRequests(String messageKey) {
        return new DomainException(messageKey, 429);
    }

    public String getMessageKey() {
        return messageKey;
    }

    public int getStatus() {
        return status;
    }
}