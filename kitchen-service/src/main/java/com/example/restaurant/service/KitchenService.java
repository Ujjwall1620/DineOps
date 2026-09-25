package com.example.restaurant.service;

import com.example.restaurant.dto.request.AssignChefRequest;
import com.example.restaurant.dto.response.KitchenStatsResponse;
import com.example.restaurant.dto.response.KitchenTicketResponse;
import com.example.restaurant.enums.KitchenStatus;

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
