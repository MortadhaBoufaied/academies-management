package com.footballacademy.tracing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tracing")
public class TracingController {
    @GetMapping("/traces")
    public ResponseEntity<Map<String, Object>> getAllTraces() {
        List<TraceSpan> traces = TraceExporter.getTraceBuffer();
        Map<String, Object> response = Map.of("traces", traces, "count", traces.size(), "timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/traces/{traceId}")
    public ResponseEntity<Map<String, Object>> getTraceById(
    @PathVariable String traceId) {
        List<TraceSpan> traces = TraceExporter.getTracesByTraceId(traceId);
        if (traces.isEmpty()) {
            return ResponseEntity.notFound() .build();
        } Map<String, Object> response = Map.of("traceId", traceId, "spans", traces, "spanCount", traces.size(), "totalDuration", traces.stream() .mapToLong(TraceSpan::getDuration) .sum(), "timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/traces/{traceId}/summary")
    public ResponseEntity<Map<String, Object>> getTraceSummary(
    @PathVariable String traceId) {
        List<TraceSpan> traces = TraceExporter.getTracesByTraceId(traceId);
        if (traces.isEmpty()) {
            return ResponseEntity.notFound() .build();
        } long totalDuration = traces.stream() .mapToLong(TraceSpan::getDuration) .sum();
        long errorCount = traces.stream() .filter(span -> span.getError() != null) .count();
        Map<String, Object> response = Map.of("traceId", traceId, "spanCount", traces.size(), "totalDuration", totalDuration, "averageDuration", totalDuration / traces.size(), "errorCount", errorCount, "successRate",((traces.size() - errorCount) * 100.0 / traces.size()), "timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTracingStats() {
        List<TraceSpan> traces = TraceExporter.getTraceBuffer();
        long totalRequests = traces.size();
        long totalDuration = traces.stream() .mapToLong(TraceSpan::getDuration) .sum();
        long errorCount = traces.stream() .filter(span -> span.getError() != null) .count();
        double averageDuration = totalRequests > 0 ?(double) totalDuration / totalRequests : 0;
        Map<String, Object> response = Map.of("totalRequests", totalRequests, "totalDuration", totalDuration, "averageDuration", averageDuration, "errorCount", errorCount, "errorRate", totalRequests > 0 ?(errorCount * 100.0 / totalRequests) : 0, "bufferSize", TraceExporter.getBufferSize(), "timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/slow-requests")
    public ResponseEntity<Map<String, Object>> getSlowRequests(
    @RequestParam(defaultValue = "1000") long thresholdMs) {
        List<TraceSpan> traces = TraceExporter.getTraceBuffer();
        List<TraceSpan> slowRequests = traces.stream() .filter(span -> span.getDuration() > thresholdMs) .sorted((a, b) -> Long.compare(b.getDuration(), a.getDuration())) .limit(50) .toList();
        Map<String, Object> response = Map.of("thresholdMs", thresholdMs, "slowRequests", slowRequests, "count", slowRequests.size(), "timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/errors")
    public ResponseEntity<Map<String, Object>> getErrorTraces() {
        List<TraceSpan> traces = TraceExporter.getTraceBuffer();
        List<TraceSpan> errorTraces = traces.stream() .filter(span -> span.getError() != null) .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp())) .limit(50) .toList();
        Map<String, Object> response = Map.of("errorTraces", errorTraces, "count", errorTraces.size(), "timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/traces")
    public ResponseEntity<Map<String, Object>> clearTraces() {
        int previousSize = TraceExporter.getBufferSize();
        TraceExporter.clearBuffer();
        Map<String, Object> response = Map.of("status", "cleared", "previousSize", previousSize, "timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
