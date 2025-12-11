package com.example.seckill_system.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * JWT (JSON Web Token) Utility Class
 * 
 * <p>This utility class provides methods for generating and parsing JWT tokens used for
 * user authentication in the seckill system. JWT tokens are stateless, meaning the server
 * doesn't need to store session information.</p>
 * 
 * <p><b>JWT Structure</b>:</p>
 * <p>A JWT consists of three parts separated by dots (.), which are:</p>
 * <ol>
 *   <li><b>Header</b>: Contains token type and signing algorithm (e.g., HS512)</li>
 *   <li><b>Payload</b>: Contains claims (e.g., userId, expiration time)</li>
 *   <li><b>Signature</b>: Used to verify the token hasn't been tampered with</li>
 * </ol>
 * 
 * <p><b>Security Features</b>:</p>
 * <ul>
 *   <li>Token is signed using HMAC-SHA512 algorithm</li>
 *   <li>Token expiration time is configurable (default: 24 hours)</li>
 *   <li>Secret key is stored in application configuration</li>
 * </ul>
 * 
 * <p><b>Configuration</b>:</p>
 * <p>Token settings are configured in {@code application.yml}:</p>
 * <pre>{@code
 * jwt:
 *   secret: MySeckillSecretKey_Production_eXtremeL0ng
 *   expiration: 86400  # seconds (24 hours)
 * }</pre>
 * 
 * @author Ai Yuyang
 * @see <a href="https://jwt.io/">JWT.io - JSON Web Tokens</a>
 */
@Component
public class JwtUtil {
    
    /**
     * Secret key used for signing and verifying JWT tokens.
     * Injected from application.yml via {@code @Value} annotation.
     * 
     * <p><b>Security Note</b>: In production, this should be stored in environment
     * variables or a secure configuration service, not in source code.</p>
     */
    @Value("${jwt.secret}")
    private String secret;
    
    /**
     * Token expiration time in seconds.
     * Default: 86400 (24 hours).
     * Injected from application.yml via {@code @Value} annotation.
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Generates a JWT token for the given user ID.
     * 
     * <p>The token contains:</p>
     * <ul>
     *   <li>User ID in the payload</li>
     *   <li>Expiration time (current time + expiration seconds)</li>
     *   <li>Signature using HMAC-SHA512 algorithm</li>
     * </ul>
     * 
     * <p><b>Token Format</b>:</p>
     * <pre>{@code
     * {
     *   "userId": 1001,
     *   "exp": 1733918400  // Expiration timestamp
     * }
     * }</pre>
     * 
     * @param userId The user ID to embed in the token
     * @return A signed JWT token string (e.g., "eyJhbGciOiJIUzUxMiJ9...")
     * @throws IllegalArgumentException if userId is null
     */
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact();
    }

    /**
     * Parses a JWT token and extracts the user ID from the payload.
     * 
     * <p>This method performs the following validations:</p>
     * <ul>
     *   <li>Verifies the token signature (prevents tampering)</li>
     *   <li>Checks token expiration (throws exception if expired)</li>
     *   <li>Extracts and returns the userId claim</li>
     * </ul>
     * 
     * <p><b>Exception Handling</b>:</p>
     * <p>If the token is invalid (expired, tampered, or malformed), this method
     * catches the exception and returns null. The caller should treat null as
     * an authentication failure.</p>
     * 
     * <p><b>Possible Exceptions</b> (caught internally):</p>
     * <ul>
     *   <li>{@link io.jsonwebtoken.ExpiredJwtException}: Token has expired</li>
     *   <li>{@link io.jsonwebtoken.SignatureException}: Token signature is invalid</li>
     *   <li>{@link io.jsonwebtoken.MalformedJwtException}: Token format is invalid</li>
     * </ul>
     * 
     * @param token The JWT token string to parse
     * @return The user ID extracted from the token, or null if parsing fails
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                                .setSigningKey(secret)
                                .parseClaimsJws(token)
                                .getBody();

            return Long.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            // Token is invalid (expired, tampered, or malformed)
            return null;
        }
    }
}
