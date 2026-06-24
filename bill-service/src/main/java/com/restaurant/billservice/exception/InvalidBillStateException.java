package com.restaurant.billservice.exception;

import com.restaurant.billservice.enums.BillStatus;

public class InvalidBillStateException extends RuntimeException {
    public InvalidBillStateException(BillStatus current, String action) {
        super("Cannot perform [" + action + "] on bill with status: " + current);
    }
}
