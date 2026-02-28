package com.project.MyEplPredictor.services;

import com.project.MyEplPredictor.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private String secretKey = "";
    //  Used to sign the JWT so nobody can fake it.

    public JwtService(){
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            // KeyGenerator with "HmacSHA256" → Generates a secure random key for signing tokens.

            SecretKey sk = keyGen.generateKey(); // returns an object of SecretKey

            secretKey = Base64.getEncoder().encodeToString(sk.getEncoded()); // encodes to a byte array
            // Base64.getEncoder() → Converts the binary secret key into a text string so it can be stored/used easily.

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 100)) // That’s just 360,000 ms or 360 seconds or 6 minutes
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
