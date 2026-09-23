package com.example.Auth_service.Security;

import com.example.Auth_service.Entity.User;
import com.example.Auth_service.Repository.AuthRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final AuthRepository repository;
    private final String SECRET_KEY =
            "VGhpc0lzQVN1cGVyU2VjdXJlSldUU2VjcmV0S2V5Rm9ySFMyNTY=";

    public String genrateToken(String email){
        User user = repository.findByEmailCaseSensitive(email);
        return Jwts.builder()
                .setSubject(email)
                .claim("role",user.getRole())
                .claim("userId",user.getId())
                .claim("restaurantId",user.getRestaurantId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+86400000))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()
                ), SignatureAlgorithm.HS256
                ).compact();
    }


    public Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(
                        Keys.hmacShaKeyFor(SECRET_KEY.getBytes())
                )
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long extractRestaurantId(String token) {

        Claims claims = extractAllClaims(token);

        return claims.get("restaurantId", Long.class);
    }

    public Integer extractUserId(String token) {

        Claims claims = extractAllClaims(token);

        return claims.get("userId", Integer.class);
    }

    public String extractRole(String token) {

        Claims claims = extractAllClaims(token);

        return claims.get("role", String.class);
    }

}
