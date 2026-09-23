package com.example.Auth_service.Controller;

import com.example.Auth_service.DTO.AuthResponse;
import com.example.Auth_service.DTO.loginRequest;
import com.example.Auth_service.DTO.registerRequest;
import com.example.Auth_service.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('OWNER','MANAGER')")
    public ResponseEntity<String> createEmployee(
            @RequestBody registerRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {

        // Removing the "Bearer "
        String token =
                authorizationHeader.substring(7);

        authService.register(request, token);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Employee created successfully");
    }

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody loginRequest request
    ) {

        return authService.login(request);
    }
}