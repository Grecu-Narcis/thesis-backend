package org.example.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UsersListResponse {
    List<UserResponseDTO> users;
    boolean hasMore;
}
