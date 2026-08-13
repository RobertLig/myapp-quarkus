package org.example.myapp.model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class ConversationParticipant {
    @Id
    @GeneratedValue
    private Long id;

    @JoinColumn(name = "conversation_id")
    @ManyToOne
    private Conversation conversation;

    @ManyToOne
    private User user;

    private Date joinedAt;
    private Date deletedAt; // soft delete for this user

    private Long lastReadMessageId;

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
}
