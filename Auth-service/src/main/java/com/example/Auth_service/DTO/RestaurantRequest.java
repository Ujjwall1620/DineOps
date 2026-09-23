package com.example.Auth_service.DTO;

import lombok.Data;
import java.time.LocalTime;

@Data
public class RestaurantRequest {

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
}