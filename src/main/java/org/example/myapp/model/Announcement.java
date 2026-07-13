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
}
