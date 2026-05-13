package com.footballacademy.tracing;

import java.util.UUID;

public final class TracingContext {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> SPAN_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    private TracingContext() {
    }

    public static void set(String traceId, String spanId, long startTime) {
        TRACE_ID.set(traceId);
        SPAN_ID.set(spanId);
        START_TIME.set(startTime);
    }

    public static String getTraceId() {
        return TRACE_ID.get();
    }

    public static String getSpanId() {
        return SPAN_ID.get();
    }

    public static Long getStartTime() {
        return START_TIME.get();
    }

    public static void clear() {
        TRACE_ID.remove();
        SPAN_ID.remove();
        START_TIME.remove();
    }

    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    public static String generateSpanId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
