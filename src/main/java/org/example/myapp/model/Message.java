package org.example.myapp.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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

    private Date deletedAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<MessageReaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<MessageAttachment> attachments = new ArrayList<>();

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

    public Date getDeletedForRecipientAt() { return deletedForRecipientAt; }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<MessageReaction> getReactions() { return reactions; }

    public List<MessageAttachment> getAttachments() {
        return attachments;
    }
}
