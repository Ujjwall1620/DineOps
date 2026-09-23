package com.example.Auth_service.Service;

import com.example.Auth_service.DTO.OwnerInfo;
import com.example.Auth_service.DTO.OwnerRegisterRequest;
import com.example.Auth_service.DTO.RestaurantRequest;
import com.example.Auth_service.DTO.RestaurantResponse;
import com.example.Auth_service.Entity.Restaurant;
import com.example.Auth_service.Entity.Role;
import com.example.Auth_service.Entity.User;
import com.example.Auth_service.Repository.AuthRepository;
import com.example.Auth_service.Repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final AuthRepository userRepository;

    @Transactional
    public String ownerRegister(OwnerRegisterRequest request) {

        // 1. Check owner email
        if (userRepository.existsByEmailCaseSensitive(request.getOwner().getEmail())) {
            throw new RuntimeException(
                    "User with this email already exists"
            );
        }

        // 2. Create Restaurant
        RestaurantRequest restaurantRequest = request.getRestaurant();

        Restaurant restaurant = Restaurant.builder()
                .name(restaurantRequest.getName())
                .phone(restaurantRequest.getPhone())
                .email(restaurantRequest.getEmail())
                .address(restaurantRequest.getAddress())
                .city(restaurantRequest.getCity())
                .state(restaurantRequest.getState())
                .pincode(restaurantRequest.getPincode())
                .logoUrl(restaurantRequest.getLogoUrl())
                .openingTime(restaurantRequest.getOpeningTime())
                .closingTime(restaurantRequest.getClosingTime())
                .build();

        Restaurant savedRestaurant =
                restaurantRepository.save(restaurant);


        // 3. Restaurant ID mil gaya
        Long restaurantId = savedRestaurant.getId();


        // 4. Create Owner
        OwnerInfo ownerRequest = request.getOwner();

        User owner = User.builder()
                .username(ownerRequest.getUsername())
                .email(ownerRequest.getEmail())
                .password(ownerRequest.getPassword())
                .role(Role.OWNER)
                .restaurantId(restaurantId)
                .build();

        userRepository.save(owner);
        return "Restaurant registered successfully";
    }

    public List<RestaurantResponse> getAllRestaurants() {

        return restaurantRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public RestaurantResponse getRestaurantById(Long id) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Restaurant not found with id: " + id));

        return mapToResponse(restaurant);
    }

    public RestaurantResponse updateRestaurant(
            Long id,
            RestaurantRequest request) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Restaurant not found with id: " + id));

        restaurant.setName(request.getName());
        restaurant.setPhone(request.getPhone());
        restaurant.setEmail(request.getEmail());
        restaurant.setAddress(request.getAddress());
        restaurant.setCity(request.getCity());
        restaurant.setState(request.getState());
        restaurant.setPincode(request.getPincode());
        restaurant.setLogoUrl(request.getLogoUrl());
        restaurant.setOpeningTime(request.getOpeningTime());
        restaurant.setClosingTime(request.getClosingTime());

        Restaurant updatedRestaurant =
                restaurantRepository.save(restaurant);

        return mapToResponse(updatedRestaurant);
    }

    public void deleteRestaurant(Long id) {

        if (!restaurantRepository.existsById(id)) {
            throw new RuntimeException(
                    "Restaurant not found with id: " + id);
        }

        restaurantRepository.deleteById(id);
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {

        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .phone(restaurant.getPhone())
                .email(restaurant.getEmail())
                .address(restaurant.getAddress())
                .city(restaurant.getCity())
                .state(restaurant.getState())
                .pincode(restaurant.getPincode())
                .logoUrl(restaurant.getLogoUrl())
                .openingTime(restaurant.getOpeningTime())
                .closingTime(restaurant.getClosingTime())
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }
}