package org.example.myapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "width", column = @Column(name = "dimensions_width")),
            @AttributeOverride(name = "height", column = @Column(name = "dimensions_height")),
            @AttributeOverride(name = "length", column = @Column(name = "dimensions_length")),
            @AttributeOverride(name = "unit", column = @Column(name = "dimensions_unit"))
    })
    private AnnouncementDimensions dimensions;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "weight_value")),
            @AttributeOverride(name = "unit", column = @Column(name = "weight_unit"))
    })
    private AnnouncementWeight weight;

    private String postingPlace;
    private String receptionPlace;

    private LocalDateTime postingDateTime;
    private LocalDateTime receptionDateTime;

    @ManyToOne
    private Sender sender;

    @ManyToOne
    private Courier courier;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL)
    private List<Stop> stops;

    @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL)
    private List<Photo> photos;

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getPostingDateTime() {
        return postingDateTime;
    }

    public void setPostingDateTime(LocalDateTime postingDateTime) {
        this.postingDateTime = postingDateTime;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public Courier getCourier() {
        return courier;
    }

    public void setCourier(Courier courier) {
        this.courier = courier;
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
