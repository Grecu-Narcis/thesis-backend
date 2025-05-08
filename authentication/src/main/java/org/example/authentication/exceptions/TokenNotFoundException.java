package org.example.authentication.exceptions;

public class TokenNotFoundException extends Exception {
    public TokenNotFoundException(String message) {
        super(message);
    }
}
