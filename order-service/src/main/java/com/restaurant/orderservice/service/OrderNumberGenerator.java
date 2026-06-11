package com.restaurant.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique order numbers in the format: ORD-YYYYMMDD-XXXX
 * e.g. ORD-20260608-1001
 *
 * Uses an AtomicLong counter seeded from a base to ensure uniqueness
 * within the JVM. For multi-instance deployments, use a distributed
 * counter (Redis INCR or DB sequence) instead.
 */
@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private static final String PREFIX = "ORD";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicLong counter = new AtomicLong(1000);

    public String generate() {
        String date = LocalDate.now().format(DATE_FORMAT);
        long sequence = counter.getAndIncrement();
        return String.format("%s-%s-%d", PREFIX, date, sequence);
    }
}
