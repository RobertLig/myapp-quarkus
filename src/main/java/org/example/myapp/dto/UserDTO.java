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
}
