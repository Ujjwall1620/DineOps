package com.restaurant.menuservice.dto.request;

import com.restaurant.menuservice.enums.MenuCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * All fields are optional for partial updates.
 * Only non-null fields are applied by the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMenuItemRequest {

    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private MenuCategory category;

    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 integer digits and 2 decimal places")
    private BigDecimal price;

    private Boolean available;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
}
