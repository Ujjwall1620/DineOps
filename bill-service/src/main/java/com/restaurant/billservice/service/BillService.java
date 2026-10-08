package com.restaurant.billservice.service;

import com.restaurant.billservice.dto.request.PaymentRequest;
import com.restaurant.billservice.dto.response.BillResponse;
import com.restaurant.billservice.enums.BillStatus;
import com.restaurant.billservice.kafka.consumer.OrderCreatedEvent;

import java.util.List;
import java.util.Map;

public interface BillService {

    // ─── Internal — called by Kafka consumer ───────────────────────────────────
    BillResponse generateBill(OrderCreatedEvent event);

    // ─── Payment workflow ──────────────────────────────────────────────────────
    BillResponse processPayment(Long billId, PaymentRequest request);

    // ─── Read operations ───────────────────────────────────────────────────────
    BillResponse       getBillById(Long billId);
    BillResponse       getBillByOrderId(Long orderId);
    List<BillResponse> getAllBills();
    List<BillResponse> getBillsByStatus(BillStatus status);
    List<BillResponse> getBillsByWaiterId(Long waiterId);
    void processOrderCreatedEvents(List<OrderCreatedEvent> events);
}
