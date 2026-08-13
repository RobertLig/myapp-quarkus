package org.example.myapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.Date;

@Entity
public class UserBlock {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User blocker;

    @ManyToOne
    private User blocked;

    private Date createdAt;
}
