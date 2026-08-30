package org.example.myapp.dto;

public class DeleteMessageRequest {
    public Long messageId;
    public Long userId;
    public String mode; // "sender", "recipient", "global"
}
