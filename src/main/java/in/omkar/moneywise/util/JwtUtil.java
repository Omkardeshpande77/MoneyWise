package in.omkar.moneywise.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Expiration in milliseconds (for example 86400000 = 1 day)
     */
    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    private Key getSigningKey() {
        if (secret == null || secret.getBytes().length < 32) {
            // It's important that the key be at least 256 bits (32 bytes) for HS256
            throw new IllegalStateException("JWT secret is missing or too short. Must be at least 32 bytes.");
        }
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generate token with username (no extra claims)
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    // Generate token with extra claims
    public String generateToken(Map<String, Object> extraClaims, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // Extract username (subject)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract any claim using a resolver
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse token and return Claims.
     * Works with jjwt 0.13.0 using the new parser API.
     */
    private Claims extractAllClaims(String token) {
        if (token == null) {
            throw new JwtException("JWT token is null");
        }
        String jwt = token.trim();
        if (jwt.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }

        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())   // works only with full jjwt setup
                .build()
                .parseClaimsJws(jwt)           // ✅ correct for jjwt 0.13.0
                .getBody();
    }


    private Boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    // Validate the token against username
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername != null && extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (RuntimeException e) {
            return false;
        }
    }
}
