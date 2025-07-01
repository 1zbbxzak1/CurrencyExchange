package ru.julia.currencyexchange.infrastructure.logging.tracing;

import org.slf4j.MDC;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.util.UUID;

public class TraceIdGenerator {

    public static String getOrCreateTraceId() {
        String traceId = MDC.get(Constants.TRACE_ID_KEY);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            MDC.put(Constants.TRACE_ID_KEY, traceId);
        }
        return traceId;
    }

    public static void clearTraceId() {
        MDC.remove(Constants.TRACE_ID_KEY);
    }
}