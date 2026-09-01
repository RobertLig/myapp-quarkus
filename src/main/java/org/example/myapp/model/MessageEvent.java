package org.example.myapp.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class MessageEvent {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Message message;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private EventType type;

    private Date createdAt;

    public void setType(EventType type) { this.type = type; }

    public void setUser(User user) { this.user = user; }

    public void setMessage(Message message) { this.message = message; }

    public void setCreatedAt(Date date) { this.createdAt = date; }

    public Long getId() { return id; }

    public EventType getType() { return type; }

    public User getUser() { return user; }

    public Message getMessage() { return message; }

    public Date getCreatedAt() { return createdAt; }
}
