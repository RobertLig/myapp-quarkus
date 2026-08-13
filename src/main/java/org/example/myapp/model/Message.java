package org.example.myapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.Date;

@Entity
public class Message {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Conversation conversation;

    @ManyToOne
    private User sender;

    private String content;
    private Date createdAt;

    private Date deletedForSenderAt;
    private Date deletedForRecipientAt;
}
