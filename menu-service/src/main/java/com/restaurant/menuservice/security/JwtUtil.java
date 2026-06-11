package com.restaurant.menuservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    private final String SECRET_KEY =
            "VGhpc0lzQVN1cGVyU2VjdXJlSldUU2VjcmV0S2V5Rm9ySFMyNTY=";

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractUserId(String token) {
        Object userIdClaim = extractAllClaims(token).get("userId");
        if (userIdClaim instanceof Integer) return ((Integer) userIdClaim).longValue();
        if (userIdClaim instanceof Long)    return (Long) userIdClaim;
        return Long.parseLong(userIdClaim.toString());
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        String username = (String) claims.get("username");
        return username != null ? username : claims.getSubject();
    }

    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY.getBytes()).build().parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e)     { log.error("Invalid JWT: {}", e.getMessage()); }
          catch (ExpiredJwtException e)        { log.error("JWT expired: {}", e.getMessage()); }
          catch (UnsupportedJwtException e)    { log.error("Unsupported JWT: {}", e.getMessage()); }
          catch (IllegalArgumentException e)   { log.error("Empty JWT claims: {}", e.getMessage()); }
        return false;
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

//    private Key getSigningKey() {
//        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
//    }
}
