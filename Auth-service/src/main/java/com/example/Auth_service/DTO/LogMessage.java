package com.example.Auth_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogMessage {
    private String service;
    private String level;
    private String message;
    private LocalDateTime timestamp;
}