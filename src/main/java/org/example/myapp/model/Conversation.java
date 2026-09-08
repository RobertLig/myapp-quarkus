package org.example.myapp.model;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class Conversation {
    @Id
    @GeneratedValue
    private Long id;

    private Long announcementId;

    private Date createdAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<ConversationParticipant> participants;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<Message> messages;

    @Enumerated(EnumType.STRING)
    private ConversationType type;

    public void setId(Long id) { this.id = id; }

    public Long getId() { return id; }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setParticipants(List<ConversationParticipant> participants) { this.participants = participants; }

    public List<ConversationParticipant> getParticipants() { return participants; }

    public void setType(ConversationType conversationType) { this.type = conversationType; }

    public ConversationType getType() { return type; }
}
