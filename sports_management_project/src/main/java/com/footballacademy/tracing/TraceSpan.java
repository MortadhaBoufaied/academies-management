package com.footballacademy.tracing;

public class TraceSpan {

    private final String traceId;
    private final String spanId;
    private final String method;
    private final String path;
    private final int statusCode;
    private final long durationMs;
    private final String error;
    private final long timestamp;

    public TraceSpan(String traceId,
                     String spanId,
                     String method,
                     String path,
                     int statusCode,
                     long durationMs,
                     String error) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.method = method;
        this.path = path;
        this.statusCode = statusCode;
        this.durationMs = durationMs;
        this.error = error;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public long getDuration() {
        return durationMs;
    }

    public String getError() {
        return error;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
