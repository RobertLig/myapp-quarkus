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

    private Date deletedForRecipientAt;

    public void setConversation(Conversation conversation) { this.conversation = conversation; }

    public void setSender(User sender) { this.sender = sender; }

    public void setContent(String content) { this.content = content; }

    public void setCreatedAt(Date date) { this.createdAt = date; }

    public Long getId() { return id;}

    public User getSender() { return sender; }

    public String getContent() { return content; }

    public Date getCreatedAt() { return createdAt; }

    public Conversation getConversation() { return conversation; }

    public void setDeletedForRecipientAt(Date date) { this.deletedForRecipientAt = date; }
}
