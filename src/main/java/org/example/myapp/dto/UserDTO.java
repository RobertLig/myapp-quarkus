package org.example.myapp.dto;

import jakarta.validation.constraints.*;
import org.example.myapp.model.Gender;

public class UserDTO {

    @NotBlank(message = "{validation.user.name.required}")
    private String name;

    @Email(message = "{validation.user.email.invalid}")
    @NotBlank(message = "{validation.user.email.required}")
    private String email;

    @NotBlank(message = "{validation.user.password.required}")
    private String password;

    private String ageRange;
    private Gender gender;
    private String phone;
    private String photoUrl;

    private String locale;

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

    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
}
