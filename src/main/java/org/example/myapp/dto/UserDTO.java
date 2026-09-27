package org.example.myapp.dto;

import jakarta.validation.constraints.*;
import org.example.myapp.model.AgeRange;
import org.example.myapp.model.Gender;
import org.example.myapp.model.Locale;

public class UserDTO {

    private String name;

    @Email(message = "{validation.user.email.invalid}")
    private String email;

    private String password;

    private AgeRange ageRange;
    private Gender gender;
    private String phone;
    private String photoUrl;

    // Locale is set automatically by frontend (navigator.language)
    private Locale locale;

    // ===== GETTERS =====

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public AgeRange getAgeRange() {
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

    // ===== SETTERS =====

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAgeRange(AgeRange ageRange) {
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

    public Locale getLocale() { return locale; }
    public void setLocale(Locale locale) { this.locale = locale; }
}
