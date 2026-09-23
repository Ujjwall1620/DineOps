package com.example.Auth_service.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class RestaurantResponse {

    private Long id;

    private String name;
    private String phone;
    private String email;

    private String address;
    private String city;
    private String state;
    private String pincode;

    private String logoUrl;

    private LocalTime openingTime;
    private LocalTime closingTime;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}