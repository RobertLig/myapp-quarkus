package org.example.myapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.util.Date;

@Entity
public class UserBlock {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User blocker;

    @ManyToOne
    private User blocked;

    private Date createdAt;

    public void setId(Long id) { this.id = id; }

    public Long getId() { return id; }

    public void setBlocker(User blocker) { this.blocker = blocker; }

    public User getBlocker() { return blocker; }

    public void setBlocked(User blocked) { this.blocked = blocked; }

    public User getBlocked() { return blocked; }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
