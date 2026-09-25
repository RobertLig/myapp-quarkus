package org.example.myapp.dto;

public class UserResponseDTO {

    public UserResponseDTO() {}
    
    private Long id;
    private String name;
    private String email;
    private String photoUrl;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhotoUrl() { return photoUrl; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}

