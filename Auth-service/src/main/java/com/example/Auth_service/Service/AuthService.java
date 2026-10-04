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
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public String register(registerRequest request , String token){
        validateRegisterRequest(request);
        
        if (!"OWNER".equals(request.getRole())) {
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
        validateLoginRequest(request);
        
        User user = repository.findByEmailCaseSensitive(request.getEmail());
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

    private void validateRegisterRequest(registerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Register request cannot be null");
        }
        if (!StringUtils.hasText(request.getUsername())) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (!StringUtils.hasText(request.getRole())) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
    }

    private void validateLoginRequest(loginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Login request cannot be null");
        }
        if (!StringUtils.hasText(request.getEmail())) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }

    private boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email).matches();
    }
}