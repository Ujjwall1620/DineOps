package com.example.Auth_service.DTO;

import lombok.Data;

@Data
public class OwnerRegisterRequest {
    private OwnerInfo owner;
    private RestaurantRequest restaurant;
}