package com.example.Auth_service.Service;

import com.example.Auth_service.Entity.Role;
import com.example.Auth_service.Repository.AuthRepository;
import com.example.Auth_service.DTO.AuthResponse;
import com.example.Auth_service.DTO.loginRequest;
import com.example.Auth_service.DTO.registerRequest;
import com.example.Auth_service.Entity.User;
import com.example.Auth_service.Security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String register(registerRequest request , String token){
        if (request.getRole() != "OWNER") {
            return "Owner can't created by user";
        }
        User user= User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .restaurantId(jwtUtil.extractRestaurantId(token))
                .build();

        repository.save(user);
        return "User Registered successfully!";
    }

    public AuthResponse login(loginRequest request){
        User user = repository.findByEmailCaseSensitive(request.getEmail());
        if (!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtil.genrateToken(user.getEmail());
        return new AuthResponse(token);
    }
}