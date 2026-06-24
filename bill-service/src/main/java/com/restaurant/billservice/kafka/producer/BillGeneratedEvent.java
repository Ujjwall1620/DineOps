package com.restaurant.billservice.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillGeneratedEvent {

    private Long       billId;
    private String     billNumber;
    private Long       orderId;
    private String     orderNumber;
    private Integer    tableNumber;
    private BigDecimal subtotal;
    private BigDecimal gstAmount;
    private BigDecimal serviceChargeAmt;
    private BigDecimal grandTotal;
    private String     status;          // "GENERATED"
    private String     eventType;       // "BILL_GENERATED"
    private LocalDateTime eventTimestamp;
}
