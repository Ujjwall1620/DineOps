package com.restaurant.kitchenservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenItemResponse {
    private Long   id;
    private Long   menuItemId;
    private String menuItemName;
    private Integer quantity;
}
