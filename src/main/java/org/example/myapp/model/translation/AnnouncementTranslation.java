package org.example.myapp.model.translation;

import jakarta.persistence.*;
import org.example.myapp.model.Announcement;

@Entity
public class AnnouncementTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language; // "en", "pl"
    private String title;
    private String description;

    @ManyToOne
    private Announcement announcement;

    // getters and setters
}
