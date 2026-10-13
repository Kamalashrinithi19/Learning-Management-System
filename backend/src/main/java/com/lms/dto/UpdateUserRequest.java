package com.lms.dto;

import com.lms.entity.Role;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private String password;
    private Role role;
}
