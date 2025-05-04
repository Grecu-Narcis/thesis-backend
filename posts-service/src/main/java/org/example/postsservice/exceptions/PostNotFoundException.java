package org.example.postsservice.exceptions;

public class PostNotFoundException extends Exception {
    public PostNotFoundException(String message) {
        super(message);
    }
}