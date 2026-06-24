package com.restaurant.billservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillItemResponse {
    private Long       id;
    private Long       menuItemId;
    private String     menuItemName;
    private Integer    quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal subtotal;
}
