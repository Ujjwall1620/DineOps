package com.restaurant.kitchenservice.controller;

import com.restaurant.kitchenservice.dto.request.AssignChefRequest;
import com.restaurant.kitchenservice.dto.response.ApiResponse;
import com.restaurant.kitchenservice.dto.response.KitchenStatsResponse;
import com.restaurant.kitchenservice.dto.response.KitchenTicketResponse;
import com.restaurant.kitchenservice.enums.KitchenStatus;
import com.restaurant.kitchenservice.service.KitchenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kitchen")
@RequiredArgsConstructor
@Slf4j
public class KitchenController {

    private final KitchenService kitchenService;

    // ─────────────────────────────────────────────────────────────────────────
    // DASHBOARD
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/kitchen/stats
     * Live count of tickets per status — for the kitchen display board.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<KitchenStatsResponse>> getStats() {
        log.debug("GET /api/kitchen/stats");
        return ResponseEntity.ok(ApiResponse.success("Kitchen stats retrieved",
                kitchenService.getStats()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QUEUE VIEWS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/kitchen/pending
     * All PENDING tickets sorted oldest-first — primary chef queue view.
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<KitchenTicketResponse>>> getPendingTickets() {
        log.debug("GET /api/kitchen/pending");
        return ResponseEntity.ok(ApiResponse.success("Pending tickets retrieved",
                kitchenService.getPendingTickets()));
    }

    /**
     * GET /api/kitchen/active
     * All non-terminal tickets (PENDING + IN_PREPARATION + READY) — full kitchen view.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<KitchenTicketResponse>>> getActiveTickets() {
        log.debug("GET /api/kitchen/active");
        return ResponseEntity.ok(ApiResponse.success("Active tickets retrieved",
                kitchenService.getActiveTickets()));
    }

    /**
     * GET /api/kitchen/status/{status}
     * Filter by any KitchenStatus value.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<KitchenTicketResponse>>> getByStatus(
            @PathVariable KitchenStatus status) {
        log.debug("GET /api/kitchen/status/{}", status);
        return ResponseEntity.ok(ApiResponse.success("Tickets by status retrieved",
                kitchenService.getTicketsByStatus(status)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SINGLE TICKET LOOKUP
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/kitchen/{id}
     * Fetch by internal kitchen ticket id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<KitchenTicketResponse>> getTicketById(
            @PathVariable Long id) {
        log.debug("GET /api/kitchen/{}", id);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved",
                kitchenService.getTicketById(id)));
    }

    /**
     * GET /api/kitchen/order/{orderId}
     * Fetch by Order Service orderId — useful for cross-service debugging.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<KitchenTicketResponse>> getTicketByOrderId(
            @PathVariable Long orderId) {
        log.debug("GET /api/kitchen/order/{}", orderId);
        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved for order",
                kitchenService.getTicketByOrderId(orderId)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHEF WORKFLOW
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * PUT /api/kitchen/{id}/assign-chef
     * Assign a chef to the ticket before or during preparation.
     */
    @PutMapping("/{id}/assign-chef")
    public ResponseEntity<ApiResponse<KitchenTicketResponse>> assignChef(
            @PathVariable Long id,
            @Valid @RequestBody AssignChefRequest request) {
        log.info("PUT /api/kitchen/{}/assign-chef → chef: {}", id, request.getChefName());
        return ResponseEntity.ok(ApiResponse.success("Chef assigned",
                kitchenService.assignChef(id, request)));
    }

    /**
     * PUT /api/kitchen/{id}/start
     * Chef starts cooking — PENDING → IN_PREPARATION.
     * Publishes order-cooking-started Kafka event.
     */
    @PutMapping("/{id}/start")
    public ResponseEntity<ApiResponse<KitchenTicketResponse>> startPreparation(
            @PathVariable Long id) {
        log.info("PUT /api/kitchen/{}/start", id);
        return ResponseEntity.ok(ApiResponse.success("Preparation started",
                kitchenService.startPreparation(id)));
    }

    /**
     * PUT /api/kitchen/{id}/ready
     * Chef marks food ready — IN_PREPARATION → READY.
     * Publishes order-ready Kafka event → Order Service updates its status to READY.
     */
    @PutMapping("/{id}/ready")
    public ResponseEntity<ApiResponse<KitchenTicketResponse>> markReady(
            @PathVariable Long id) {
        log.info("PUT /api/kitchen/{}/ready", id);
        return ResponseEntity.ok(ApiResponse.success("Order marked as ready",
                kitchenService.markReady(id)));
    }
}
