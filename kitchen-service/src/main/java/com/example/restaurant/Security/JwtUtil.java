package com.example.restaurant.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final String SECRET_KEY =
            "VGhpc0lzQVN1cGVyU2VjdXJlSldUU2VjcmV0S2V5Rm9ySFMyNTY=";
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extract all claims from a JWT token.
     */
    public Claims extractAllClaims(String token) {
            return Jwts.parser()
                    .verifyWith(
                            Keys.hmacShaKeyFor(
                                    SECRET_KEY.getBytes(StandardCharsets.UTF_8)
                            )
                    )
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

    }

    /**
     * Extract userId from JWT claims.
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userIdClaim = claims.get("userId");
        if (userIdClaim instanceof Integer) {
            return ((Integer) userIdClaim).longValue();
        }
        if (userIdClaim instanceof Long) {
            return (Long) userIdClaim;
        }
        return Long.parseLong(userIdClaim.toString());
    }

    /**
     * Extract username from JWT claims.
     */
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        String username = (String) claims.get("username");
        return username != null ? username : claims.getSubject();
    }

    /**
     * Extract restaurant ID from JWT claims.
     */

    public  Long extractRestaurantID(String token) {
        Claims claims = extractAllClaims(token);
        Object restaurantIdClaim = claims.get("restaurantId");
        if (restaurantIdClaim instanceof Integer) {
            return ((Integer) restaurantIdClaim).longValue();
        }
        if (restaurantIdClaim instanceof Long) {
            return (Long) restaurantIdClaim;
        }
        return Long.parseLong(restaurantIdClaim.toString());
    }

    /**
     * Extract role from JWT claims.
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("role");
    }

    /**
     * Validate the JWT token.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Check if a token is expired.
     */
    public boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }
}
