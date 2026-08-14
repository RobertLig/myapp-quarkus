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
}
