package com.restaurant.billservice.enums;

public enum BillStatus {

    /** Bill record created, awaiting full generation. */
    PENDING,

    /** Bill fully generated with all charges — awaiting payment. */
    GENERATED,

    /** Payment initiated — gateway called, awaiting callback. */
    PAYMENT_PENDING,

    /** Payment successfully completed. Terminal state. */
    PAID,

    /** Payment gateway returned failure. Retryable. */
    FAILED,

    /** Bill cancelled (order cancelled before payment). Terminal state. */
    CANCELLED,

    /** Payment was refunded after success. Terminal state. */
    REFUNDED
}
