package com.restaurant.billservice.service;

import com.restaurant.billservice.dto.request.PaymentRequest;
import com.restaurant.billservice.dto.response.BillResponse;
import com.restaurant.billservice.enums.BillStatus;
import com.restaurant.billservice.kafka.consumer.OrderReadyEvent;

import java.util.List;

public interface BillService {

    // ─── Internal — called by Kafka consumer ───────────────────────────────────
    BillResponse generateBill(OrderReadyEvent event);

    // ─── Payment workflow ──────────────────────────────────────────────────────
    BillResponse processPayment(Long billId, PaymentRequest request);

    // ─── Read operations ───────────────────────────────────────────────────────
    BillResponse       getBillById(Long billId);
    BillResponse       getBillByOrderId(Long orderId);
    List<BillResponse> getAllBills();
    List<BillResponse> getBillsByStatus(BillStatus status);
    List<BillResponse> getBillsByWaiterId(Long waiterId);
}
