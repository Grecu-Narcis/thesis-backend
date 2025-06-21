package org.example.postsservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class for handling JWT generation.
 */
@Component
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

    /**
     * Retrieves the username from a given JWT token.
     *
     * @param token JWT token from which to extract the username.
     * @return String representing the username extracted from the token.
     */
    public String getUsernameFromJWT(String token) {
        Claims jwtBody = Jwts.parser()
                .verifyWith(JWTUtils.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return (String) jwtBody.get("username");
    }

    public String getUsernameFromBearerToken(String bearerToken) {
        String jwtToken = bearerToken.substring(7);
        System.out.println(jwtToken);
        return this.getUsernameFromJWT(jwtToken);
    }

    /**
     * Validates the provided JWT token.
     *
     * @param token JWT token to be validated.
     * @return boolean indicating whether the token is valid (true) or not (false).
     * @throws AuthenticationCredentialsNotFoundException if the token validation fails (e.g., expired or incorrect token).
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(JWTUtils.getSigningKey()).build().parseSignedClaims(token);
            return true;
        }

        catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("JWT was expired  or incorrect!");
        }
    }
}
