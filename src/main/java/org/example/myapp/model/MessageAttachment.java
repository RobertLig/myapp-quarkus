package org.example.myapp.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "message_attachments")
public class MessageAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "message_id")
    private Message message;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String url; // S3/R2 URL

    @Column(nullable = false)
    private String type; // image/png, image/jpeg, application/pdf, etc.

    @Column(nullable = false)
    private Long size; // bytes

    @Column(nullable = false)
    private Date createdAt = new Date();

    public Long getId() { return id; }

    public User getUser() { return user; }

    public String getUrl() { return url; }

    public String getType() { return type; }

    public Long getSize() { return size; }

    public Date getCreatedAt() { return createdAt; }

    public void setMessage(Message message) { this.message = message; }

    public void setUser(User user) { this.user = user; }

    public void setUrl(String url) { this.url = url; }

    public void setType(String type) { this.type = type; }

    public void setSize(Long size) { this.size = size; }

    public void setCreatedAt(Date date) { this.createdAt = date; }
}
