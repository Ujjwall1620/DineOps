package com.restaurant.kitchenservice.service;

import com.restaurant.kitchenservice.dto.request.AssignChefRequest;
import com.restaurant.kitchenservice.dto.response.KitchenStatsResponse;
import com.restaurant.kitchenservice.dto.response.KitchenTicketResponse;
import com.restaurant.kitchenservice.enums.KitchenStatus;

import java.util.List;

public interface KitchenService {

    // ─── Chef workflow ─────────────────────────────────────────────────────────
    KitchenTicketResponse startPreparation(Long ticketId);
    KitchenTicketResponse markReady(Long ticketId);
    KitchenTicketResponse assignChef(Long ticketId, AssignChefRequest request);

    // ─── Read operations ───────────────────────────────────────────────────────
    KitchenTicketResponse      getTicketById(Long ticketId);
    KitchenTicketResponse      getTicketByOrderId(Long orderId);
    List<KitchenTicketResponse> getPendingTickets();
    List<KitchenTicketResponse> getTicketsByStatus(KitchenStatus status);
    List<KitchenTicketResponse> getActiveTickets();

    // ─── Dashboard ─────────────────────────────────────────────────────────────
    KitchenStatsResponse getStats();
}
