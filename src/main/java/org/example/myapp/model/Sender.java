package org.example.myapp.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Sender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String email;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Announcement> announcements;

    // getters and setters
}
