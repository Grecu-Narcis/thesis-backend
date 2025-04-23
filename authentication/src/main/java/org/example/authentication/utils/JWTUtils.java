package org.example.authentication.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class for handling JWT generation.
 */
public class JWTUtils {

    private static final String SECRET_KEY = System.getenv("JWT_SECRET");

    /**
     * Generates a JWT token based on the provided authentication information.
     *
     * @param username Username of the user for which the token is generated.
     * @return String representing the generated JWT token.
     */
    public static String generateToken(String username) {
        return Jwts.builder()
                .claim("username", username)
                .issuedAt(new Date())
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Retrieves the signing key used for JWT token generation and validation.
     *
     * @return SecretKey used for signing JWT tokens.
     */
    private static SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}