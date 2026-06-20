package com.restaurant.kitchenservice.dto.response;

import com.restaurant.kitchenservice.enums.KitchenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenTicketResponse {
    private Long          id;
    private Long          orderId;
    private String        orderNumber;
    private Integer       tableNumber;
    private Long          chefId;
    private String        chefName;
    private KitchenStatus status;
    private List<KitchenItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
