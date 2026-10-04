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
    
    @org.springframework.beans.factory.annotation.Value("${jwt.secret:VGhpc0lzQVN1cGVyU2VjdXJlSldUU2VjcmV0S2V5Rm9ySFMyNTY=}")
    private String secretKey;
    
    @org.springframework.beans.factory.annotation.Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public String generateToken(String email){
        User user = repository.findByEmailCaseSensitive(email);
        return Jwts.builder()
                .setSubject(email)
                .claim("role",user.getRole())
                .claim("userId",user.getId())
                .claim("restaurantId",user.getRestaurantId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+jwtExpiration))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes())
                , SignatureAlgorithm.HS256
                ).compact();
    }


    public Claims extractAllClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(
                        Keys.hmacShaKeyFor(secretKey.getBytes())
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
