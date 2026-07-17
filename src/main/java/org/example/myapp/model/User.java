package org.example.myapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // REQUIRED
    private String name;
    private String email;
    private String password;

    // OPTIONAL
    private String ageRange; // "< 20", "20–29", "30–39", etc.

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String phone;
    private String photoUrl;

    // RELATIONSHIP
    @OneToMany(mappedBy = "user")
    private java.util.List<Announcement> announcements;

    // getters and setters...
}
