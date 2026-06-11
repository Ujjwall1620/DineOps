package com.example.Auth_service.DTO;

import lombok.Data;

@Data
public class registerRequest {
    private String username;
    private String email;
    private String role;
    private String password;
}