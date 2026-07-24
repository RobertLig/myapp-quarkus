package org.example.myapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import org.example.myapp.model.translation.AnnouncementTranslation;

@Entity
public class Announcement {

    // Required by JPA
    protected Announcement() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type; // "sender" or "courier"

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "width", column = @Column(name = "dimensions_width")),
            @AttributeOverride(name = "height", column = @Column(name = "dimensions_height")),
            @AttributeOverride(name = "length", column = @Column(name = "dimensions_length"))
    })
    private AnnouncementDimensions dimensions;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "weight_value"))
    })
    private AnnouncementWeight weight;

    private String postingPlace;
    private String receptionPlace;

    private LocalDateTime postingDateTime;
    private LocalDateTime receptionDateTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL)
    private List<AnnouncementTranslation> translations;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL)
    private List<Stop> stops;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL)
    private List<Photo> photos;

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AnnouncementDimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(AnnouncementDimensions dimensions) {
        this.dimensions = dimensions;
    }

    public AnnouncementWeight getWeight() {
        return weight;
    }

    public void setWeight(AnnouncementWeight weight) {
        this.weight = weight;
    }

    public String getPostingPlace() {
        return postingPlace;
    }

    public void setPostingPlace(String postingPlace) {
        this.postingPlace = postingPlace;
    }

    public String getReceptionPlace() {
        return receptionPlace;
    }

    public void setReceptionPlace(String receptionPlace) {
        this.receptionPlace = receptionPlace;
    }

    public LocalDateTime getPostingDateTime() {
        return postingDateTime;
    }

    public void setPostingDateTime(LocalDateTime postingDateTime) {
        this.postingDateTime = postingDateTime;
    }

    public LocalDateTime getReceptionDateTime() {
        return receptionDateTime;
    }

    public void setReceptionDateTime(LocalDateTime receptionDateTime) {
        this.receptionDateTime = receptionDateTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<AnnouncementTranslation> getTranslations() {
        return translations;
    }

    public void setTranslations(List<AnnouncementTranslation> translations) {
        this.translations = translations;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }
}
