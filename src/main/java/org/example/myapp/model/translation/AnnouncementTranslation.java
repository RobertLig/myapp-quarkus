package org.example.myapp.model.translation;

import jakarta.persistence.*;
import org.example.myapp.model.Announcement;

@Entity
public class AnnouncementTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language;     // "en", "pl"
    private String title;
    private String description;

    @ManyToOne
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    // Required by JPA
    protected AnnouncementTranslation() {
    }

    // Optional convenience constructor
    public AnnouncementTranslation(String language, String title, String description) {
        this.language = language;
        this.title = title;
        this.description = description;
    }

    // Getters & setters
    public Long getId() {
        return id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Announcement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
    }
}
