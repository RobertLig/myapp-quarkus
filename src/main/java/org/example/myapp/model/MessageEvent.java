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
}
