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
    private boolean ageRangeProvided;
    private boolean genderProvided;
    private boolean phoneProvided;

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

    public boolean isAgeRangeProvided() {
        return ageRangeProvided;
    }

    public boolean isGenderProvided() {
        return genderProvided;
    }

    public boolean isPhoneProvided() {
        return phoneProvided;
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
        this.ageRangeProvided = true;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
        this.genderProvided = true;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        this.phoneProvided = true;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Locale getLocale() { return locale; }
    public void setLocale(Locale locale) { this.locale = locale; }
}
