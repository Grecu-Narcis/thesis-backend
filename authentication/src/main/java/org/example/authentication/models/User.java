package org.example.authentication.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

/**
 * Represents a user entity with basic information.
 * This entity is mapped to the "users" table in the database.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
public class User {
    @Id
    @Column(name = "username", updatable = false)
    private String username;

    @Column(name = "fullName", nullable = false, length = 50)
    private String fullName;

    @Column(name="email", nullable = false)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="createdAt", nullable = false)
    private Date createdAt;

    public User(String username, String fullName, String email, String password) {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.createdAt = new Date();
    }

    /**
     * Compares this User object with another object for equality based on ID.
     *
     * @param obj The object to compare with.
     * @return true if the objects are equal (i.e., have the same ID), otherwise false.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User userToCompare))
            return false;

        return this.getUsername().equals(userToCompare.getUsername());
    }
}