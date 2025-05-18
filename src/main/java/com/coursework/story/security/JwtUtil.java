package com.coursework.story.security;

import com.coursework.story.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${story.app.jwtSecret}")
    private String secretKey;
    private Key key;

    @PostConstruct
    public void init() {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("JWT secret key is not set in the application properties.");
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token is expired.", e);
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid JWT token.", e);
        }
    }

    public String generateAccessToken(String username) {
        // 15 minutes
        long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15;
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        // 7 days
        long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean validateToken(String token, UserDetails userDetails) {
        Claims claims = extractAllClaims(token);

        String username = claims.getSubject();
        Date expiration = claims.getExpiration();

        if (!userDetails.getUsername().equals(username)) {
            throw new InvalidTokenException("Token username does not match.");
        }

        if (expiration.before(new Date())) {
            throw new InvalidTokenException("Token is expired.");
        }

        return true;
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
}