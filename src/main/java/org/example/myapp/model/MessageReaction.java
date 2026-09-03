package org.example.myapp.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "message_reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "user_id"}))
public class MessageReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "message_id")
    private Message message;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String reaction; // ❤️ 👍 😂 etc.

    @Column(nullable = false)
    private Date createdAt = new Date();

    public void setReaction(String reaction) { this.reaction = reaction; }

    public void setMessage(Message message) { this.message = message; }

    public void setUser(User user) { this.user = user; }

    public Long getId() { return id; }

    public User getUser() { return user; }

    public String getReaction() { return reaction; }

    public Date getCreatedAt() { return createdAt; }

    // Getters and setters
}
