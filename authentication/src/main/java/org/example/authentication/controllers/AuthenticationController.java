package org.example.authentication.controllers;

import org.example.authentication.business.UsersService;
import org.example.authentication.dto.UserLoginDTO;
import org.example.authentication.dto.UserRegisterDTO;
import org.example.authentication.exceptions.UserNotFoundException;
import org.example.authentication.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthenticationController {
    private final UsersService usersService;

    @Autowired
    public AuthenticationController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserRegisterDTO registerRequest) {
        if (this.usersService.existsByUsername(registerRequest.getUsername()))
            return ResponseEntity.badRequest().body("Username already exists");

        this.usersService.saveUser(
                registerRequest.getUsername(),
                registerRequest.getFullName(),
                registerRequest.getEmail(),
                registerRequest.getPassword()
        );

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO loginRequest) {
        try {
            if (!this.usersService.validateUserCredentials(loginRequest.getUsername(), loginRequest.getPassword()))
                return ResponseEntity.badRequest().body("Invalid username or password!");

            String token = JWTUtils.generateToken(loginRequest.getUsername());
            Map<String, String> response = new HashMap<>();
            response.put("token", token);

            return ResponseEntity.ok(response);
        }

        catch (UserNotFoundException e) {
            return ResponseEntity.badRequest().body("Invalid username or password!");
        }
    }
}
