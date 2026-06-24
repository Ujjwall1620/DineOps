package com.restaurant.billservice.dto.response;

import com.restaurant.billservice.enums.BillStatus;
import com.restaurant.billservice.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponse {

    private Long       id;
    private String     billNumber;
    private Long       orderId;
    private String     orderNumber;
    private Integer    tableNumber;
    private Long       waiterId;
    private String     waiterName;

    // Financial breakdown
    private BigDecimal subtotal;
    private BigDecimal gstPercentage;
    private BigDecimal gstAmount;
    private BigDecimal serviceChargePct;
    private BigDecimal serviceChargeAmt;
    private BigDecimal grandTotal;

    private BillStatus    status;
    private PaymentMethod paymentMethod;
    private String        paymentUrl;        // Razorpay link — send to customer

    private List<BillItemResponse>           items;
    private List<PaymentTransactionResponse> transactions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
