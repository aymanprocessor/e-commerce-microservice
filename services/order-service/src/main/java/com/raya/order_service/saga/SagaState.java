package com.raya.order_service.saga;

public enum SagaState {

    STARTED,               // saga initiated, no commands sent yet

    INVENTORY_RESERVING,   // ReserveInventoryCommand sent, awaiting result
    INVENTORY_RESERVED,    // reservation confirmed — triggers ProcessPaymentCommand
    INVENTORY_FAILED,      // reservation failed — no payment attempt (terminal)

    PAYMENT_PROCESSING,    // ProcessPaymentCommand sent, awaiting result
    PAYMENT_COMPLETED,     // payment confirmed (terminal → COMPLETED)
    PAYMENT_FAILED,        // payment failed — triggers ReleaseInventoryCommand

    COMPENSATING,          // ReleaseInventoryCommand sent, awaiting confirmation
    INVENTORY_RELEASED,    // inventory released (terminal → CANCELLED)

    COMPLETED,             // saga ended successfully
    CANCELLED
}
