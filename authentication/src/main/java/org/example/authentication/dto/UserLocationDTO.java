package org.example.authentication.dto;

import lombok.Data;

@Data
public class UserLocationDTO {
    private String username;
    private double latitude;
    private double longitude;
}
