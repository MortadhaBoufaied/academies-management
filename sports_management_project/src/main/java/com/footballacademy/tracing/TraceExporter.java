package com.footballacademy.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class TraceExporter {

    private static final Logger logger = LoggerFactory.getLogger(TraceExporter.class);
    private static final List<TraceSpan> BUFFER = new CopyOnWriteArrayList<>();
    private static final int MAX_BUFFER_SIZE = 1000;

    public static void export(TraceSpan span) {
        if (span == null) {
            return;
        }
        BUFFER.add(span);
        if (BUFFER.size() > MAX_BUFFER_SIZE) {
            BUFFER.remove(0);
        }
        logger.debug("Trace exported traceId={} spanId={} method={} path={} status={} duration={}ms",
                span.getTraceId(),
                span.getSpanId(),
                span.getMethod(),
                span.getPath(),
                span.getStatusCode(),
                span.getDurationMs());
    }

    public static List<TraceSpan> getTraceBuffer() {
        return new ArrayList<>(BUFFER);
    }

    public static List<TraceSpan> getTracesByTraceId(String traceId) {
        return BUFFER.stream()
                .filter(span -> span.getTraceId() != null && span.getTraceId().equals(traceId))
                .collect(Collectors.toList());
    }

    public static void clearBuffer() {
        BUFFER.clear();
    }

    public static int getBufferSize() {
        return BUFFER.size();
    }
}
