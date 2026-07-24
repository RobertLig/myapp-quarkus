package org.example.myapp.model;

import jakarta.persistence.*;

@Entity
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    private Integer position;

    @ManyToOne
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    // Required by JPA
    protected Photo() {
    }

    public Photo(String url, int position) {
        this.url = url;
        this.position = position;
    }

    // Optional convenience constructor
    public Photo(Long id, String url, int position) {
        this.id = id;
        this.url = url;
        this.position = position;
    }

    // Getters & setters
    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Announcement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
    }
}
