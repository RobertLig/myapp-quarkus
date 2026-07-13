package org.example.myapp.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;

    @OneToMany(mappedBy = "courier", cascade = CascadeType.ALL)
    private List<Announcement> announcements;

    // getters and setters
}
