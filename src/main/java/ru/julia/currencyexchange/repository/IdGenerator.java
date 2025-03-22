package ru.julia.currencyexchange.repository;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdGenerator {
    private final AtomicLong idGenerator = new AtomicLong();

    public Long get() {
        return idGenerator.incrementAndGet();
    }
}
