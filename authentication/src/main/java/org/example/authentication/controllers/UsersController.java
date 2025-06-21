package org.example.authentication.controllers;

import org.example.authentication.business.S3Service;
import org.example.authentication.business.UserLocationService;
import org.example.authentication.business.UsersNotificationTokenService;
import org.example.authentication.business.UsersService;
import org.example.authentication.dto.*;
import org.example.authentication.exceptions.UserNotFoundException;
import org.example.authentication.models.User;
import org.example.authentication.models.UserNotificationToken;
import org.example.authentication.utils.JWTUtils;
import org.example.authentication.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class UsersController {
    private final UsersService usersService;
    private final UsersNotificationTokenService usersNotificationTokenService;
    private final UserLocationService userLocationService;
    private final S3Service s3Service;
    private final JWTUtils jwtUtils;

    @Autowired
    public UsersController(UsersService usersService, UsersNotificationTokenService usersNotificationTokenService,
                           UserLocationService userLocationService, S3Service s3Service, JWTUtils jwtUtils) {
        this.usersService = usersService;
        this.usersNotificationTokenService = usersNotificationTokenService;
        this.userLocationService = userLocationService;
        this.s3Service = s3Service;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/usernames")
    public ResponseEntity<List<String>> getUsernames() {
        return ResponseEntity.ok(this.usersService.getAllUsernames());
    }

    @PostMapping("/notification-token")
    public ResponseEntity<String> addNotificationToken(@RequestBody UserNotificationToken userNotificationToken,
                                                       @RequestHeader("Authorization") String bearerToken) {
        Logger.log("Saving notification token for: " + userNotificationToken.getUsername());

        String authorizedUserId = jwtUtils.getUsernameFromBearerToken(bearerToken);

        if (!authorizedUserId.equals(userNotificationToken.getUsername()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        this.usersNotificationTokenService.saveUserNotificationToken(userNotificationToken);
        return ResponseEntity.ok("Notification token added successfully");
    }

    @PostMapping("/location")
    public ResponseEntity<String> addLocation(@RequestBody UserLocationDTO userLocationDTO,
                                              @RequestHeader("Authorization") String bearerToken) {
        Logger.log("Saving location for: " + userLocationDTO.getUsername());

        String authorizedUserId = jwtUtils.getUsernameFromBearerToken(bearerToken);

        if (!authorizedUserId.equals(userLocationDTO.getUsername()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        this.userLocationService.save(userLocationDTO.getUsername(), userLocationDTO.getLatitude(), userLocationDTO.getLongitude());

        return ResponseEntity.ok("Location added successfully");
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsersByUsername(@RequestParam String searchKey, @RequestParam String username, @RequestParam int page) {
        Page<User> usersPage = this.usersService.getUsersByUsername(searchKey, username, page);
        List<UserResponseDTO> users = usersPage.stream().map(user -> new UserResponseDTO(user.getUsername(), user.getFullName(), user.getProfileImage(), user.getBio())).toList();

        return ResponseEntity.ok(new UsersListResponse(users, usersPage.hasNext()));
    }

    @GetMapping("/notification-token/{username}")
    public ResponseEntity<?> getNotificationToken(@PathVariable String username) {
        System.out.println("Getting token for user: " + username);
        try {
            String token = this.usersNotificationTokenService.getUserNotificationToken(username);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        try {
            User requiredUser = this.usersService.getUser(username);

            return ResponseEntity.ok(new UserResponseDTO(requiredUser.getUsername(), requiredUser.getFullName(), requiredUser.getProfileImage(), requiredUser.getBio()));
        } catch (Exception e) {
            Logger.logError("Error fetching user" + e.getMessage());

            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<?> getPresignedUrl(@RequestParam String imageName, @RequestParam String imageType) {
        try {
            return ResponseEntity.ok(this.s3Service.createPresignedUrl(imageName, imageType));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/profile-image")
    public ResponseEntity<?> updateProfileImage(@RequestBody ProfileImageDTO profileImageDTO,
                                                @RequestHeader("Authorization") String bearerToken) {
        String authorizedUserId = jwtUtils.getUsernameFromBearerToken(bearerToken);

        if (!authorizedUserId.equals(profileImageDTO.getUsername()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            this.usersService.updateUserProfileImage(profileImageDTO.getUsername(), profileImageDTO.getImageKey());

            return ResponseEntity.ok("Profile image updated successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/bio")
    public ResponseEntity<?> updateUserBio(@RequestBody UserBioUpdateDTO userBioUpdateDTO,
                                           @RequestHeader("Authorization") String bearerToken) {
        String authorizedUserId = jwtUtils.getUsernameFromBearerToken(bearerToken);

        if (!authorizedUserId.equals(userBioUpdateDTO.getUsername()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            this.usersService.updateUserBio(userBioUpdateDTO.getUsername(), userBioUpdateDTO.getBio());
            return ResponseEntity.ok("User bio updated successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
