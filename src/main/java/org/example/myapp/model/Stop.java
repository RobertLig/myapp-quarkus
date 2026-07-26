package org.example.myapp.model;

import jakarta.persistence.*;

@Entity
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;
    private double latitude;
    private double longitude;

    @Column(nullable = false)
    public int position;

    @ManyToOne
    @JoinColumn(name = "announcement_id")
    private Announcement announcement;

    // Required by JPA
    protected Stop() {
    }

    // Optional convenience constructor
    public Stop(Long id, String address, double latitude, double longitude, int position) {
        this.id = id;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.position = position;
    }

    // Getters & setters
    public Long getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Announcement getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
    }
}
