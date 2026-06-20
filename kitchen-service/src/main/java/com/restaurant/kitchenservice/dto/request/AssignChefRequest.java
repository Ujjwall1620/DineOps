package com.restaurant.kitchenservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignChefRequest {

    @NotNull(message = "Chef ID is required")
    @Positive(message = "Chef ID must be a positive integer")
    private Long chefId;

    @NotBlank(message = "Chef name is required")
    private String chefName;
}
