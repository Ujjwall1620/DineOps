package com.restaurant.billservice.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique bill numbers in format: BILL-YYYYMMDD-XXXX
 * e.g. BILL-20260615-1001
 * Mirrors the OrderNumberGenerator pattern from Order Service.
 */
@Component
public class BillNumberGenerator {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicLong counter = new AtomicLong(1000);

    public String generate() {
        String date = LocalDate.now().format(DATE_FORMAT);
        long sequence = counter.getAndIncrement();
        return String.format("BILL-%s-%d", date, sequence);
    }
}
