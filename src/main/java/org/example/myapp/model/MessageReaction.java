package org.example.myapp.model;

import jakarta.persistence.*;

import java.time.Instant;

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
    private Instant createdAt = Instant.now();

    public void setReaction(String reaction) { this.reaction = reaction; }

    public void setMessage(Message message) { this.message = message; }

    public void setUser(User user) { this.user = user; }

    // Getters and setters
}
