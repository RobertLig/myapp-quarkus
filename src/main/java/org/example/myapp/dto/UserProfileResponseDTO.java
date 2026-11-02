package org.example.myapp.dto;

import org.example.myapp.model.AgeRange;
import org.example.myapp.model.Gender;
import org.example.myapp.model.Role;

public class UserProfileResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String photoUrl;
    private AgeRange ageRange;
    private Gender gender;
    private String phone;
    private Role role;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }
    public AgeRange getAgeRange() { return ageRange; }
    public Gender getGender() { return gender; }
    public String getPhone() { return phone; }
    public Role getRole() { return role; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setAgeRange(AgeRange ageRange) { this.ageRange = ageRange; }
    public void setGender(Gender gender) { this.gender = gender; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(Role role) { this.role = role; }
}
