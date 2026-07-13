package org.example.myapp.model;

import jakarta.persistence.*;

@Entity
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String place;
    private double latitude;
    private double longitude;

    @ManyToOne
    private Announcement announcement;

    // getters and setters
}
