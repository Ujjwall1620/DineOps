package com.example.Auth_service.Service;

import com.example.Auth_service.Entity.Role;
import com.example.Auth_service.Repository.AuthRepository;
import com.example.Auth_service.DTO.AuthResponse;
import com.example.Auth_service.DTO.LogMessage;
import com.example.Auth_service.DTO.loginRequest;
import com.example.Auth_service.DTO.registerRequest;
import com.example.Auth_service.Entity.User;
import com.example.Auth_service.Kafka.LogProducer;
import com.example.Auth_service.Security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LogProducer logProducer;

    public String register(registerRequest request){
        User user= User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole().toUpperCase()))
                .build();

       repository.save(user);
        logProducer.sendLog(
                new LogMessage(
                        "AUTH-SERVICE",
                        "INFO",
                        "User Registered Successful",
                        LocalDateTime.now()
                )
        );
        return "User Registered successfully!";
    }

    public AuthResponse login(loginRequest request){
        User user = repository.findByEmail(request.getEmail());
        if (!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            logProducer.sendLog(new LogMessage(  "AUTH-SERVICE",
                    "WARN",
                    "Invalid user ",
                    LocalDateTime.now()));
        }
        String token = jwtUtil.genrateToken(user.getEmail());
        logProducer.sendLog(
                new LogMessage(
                        "AUTH-SERVICE",
                        "INFO",
                        "User Login Successful",
                        LocalDateTime.now()
                )
        );

        return new AuthResponse(token);
    }
}