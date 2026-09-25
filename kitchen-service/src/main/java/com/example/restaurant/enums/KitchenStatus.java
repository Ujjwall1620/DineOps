package com.restaurant.kitchenservice.enums;

public enum KitchenStatus {

    /** Ticket received from Order Service, waiting for a chef. */
    PENDING,

    /** Chef has claimed the ticket and is actively cooking. */
    IN_PREPARATION,

    /** Chef has marked the food ready; waiter will collect. */
    READY,

    /** Waiter has collected the order. Terminal state. */
    COMPLETED,

    /** Order was cancelled before or during preparation. Terminal state. */
    CANCELLED
}
