package org.example.postsservice.exceptions;

public class AlreadyLikedPostException extends Exception {
    public AlreadyLikedPostException(String message) {
        super(message);
    }
}
