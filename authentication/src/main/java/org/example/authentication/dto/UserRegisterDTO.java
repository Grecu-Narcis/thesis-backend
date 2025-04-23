package org.example.authentication.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String fullName;
    private String email;
    private String password;
}
