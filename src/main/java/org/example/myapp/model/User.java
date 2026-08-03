package org.example.myapp.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== BASIC INFO =====
    private String name;
    private String email;
    private String password;
    private String salt;

    private String ageRange;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String phone;
    private String photoUrl;

    // ===== AUTH & SECURITY =====
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    private boolean emailVerified = false;

    private String verificationToken;
    private String resetPasswordToken;

    // ===== AUDIT =====
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== RELATIONS =====
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<Announcement> announcements;

    // ===== LIFECYCLE CALLBACKS =====
    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== GETTERS & SETTERS =====
    // (same as before, plus new fields)

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getSalt() { return salt; }

    public Role getRole() { return role; }

    public boolean getEmailVerified() { return emailVerified; }

    public String getVerificationToken() { return verificationToken; }

    public String getResetPasswordToken() { return resetPasswordToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public String getAgeRange() {
        return ageRange;
    }

    public Gender getGender() {
        return gender;
    }

    public String getPhone() {
        return phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    // ===== SETTERS =====

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSalt(String salt) { this.salt = salt; }

    public void setRole(Role role) { this.role = role; }

    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setAnnouncements(List<Announcement> announcements) {
        this.announcements = announcements;
    }
}
