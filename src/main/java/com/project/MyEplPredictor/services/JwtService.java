package com.project.MyEplPredictor.services;

import com.project.MyEplPredictor.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final String secretKey;
    private final long jwtExpirationMs;

    public JwtService(@Value("${jwt.secret}") String secretKey,
                      @Value("${jwt.expiration-ms:3600000}") long jwtExpirationMs) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("jwt.secret must be configured with a non-empty value");
        }
        this.secretKey = secretKey;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getEmail())
            .issuedAt(new Date(now))
            .expiration(new Date(now + jwtExpirationMs))
                .and()
                .signWith(getKey())
                .compact();
    }

    /*
    Explaining the generateToken method

    1. claims map → This is where you could put extra data like user roles, permissions, etc. (currently empty).

    2. Jwts.builder() → Starts building a JWT token using the jjwt library.

    3. .claims().add(claims) → Adds your payload claims (in this case, just empty extra data).

    4. .subject(username) → Sets the sub-claim — the main identifier (username here)

    5. .issuedAt(new Date(...)) → Adds the iat (issued at) timestamp.

    6. .expiration(new Date(...)) → Adds the exp claim — token expiry time.

    7. .signWith(getKey()) → Signs the token using your generated secret key.

    8. .compact() → Finalizes the token and returns it as a string.

    */

    private SecretKey getKey() {
        byte[] bkey = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(bkey); // takes an array of bytes

        /*
        Takes your Base64 string secretKey, decodes it back to bytes, and makes an HmacSHA256 key from it.
        This is used to sign tokens (so the server can later verify them).
        */
    }

    public String extractUsername(String token) {
        // Extracts the 'sub' (subject) claim, which you're using as the username.
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims,T> claimResolver){
        // Get all claims from the token (after verifying signature), then apply a resolver function
        // to pick the one you want (e.g., getSubject, getExpiration, custom claim, etc.).
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        // Build a parser that will VERIFY the signature with your symmetric HMAC key.
        return Jwts.parser()
                .verifyWith(getKey())   // sets the verification key; invalid sig => exception
                .build()
                .parseSignedClaims(token) // parses and verifies; returns a Jws<Claims>
                .getPayload();            // the actual Claims (payload) if verification passed
        /*
         * Notes:
         * - If the token is malformed, signed with the wrong key, or tampered, this throws.
         * - If the token is expired, jjwt may throw ExpiredJwtException (depending on version/config).
         *   You can catch it to return a cleaner 401 message.
         */
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        // Pull the username from the token and check two things:
        // 1) token subject matches the user you loaded
        // 2) token is not expired
        final String userName = extractUsername(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token){
        // Compare the exp claim to "now"—if exp is before now, it's expired.
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token){
        // Grab the exp claim from the token's payload.
        return extractClaim(token, Claims::getExpiration);
    }


}
