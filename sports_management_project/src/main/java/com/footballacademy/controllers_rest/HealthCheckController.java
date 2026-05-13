package com.footballacademy.controllers_rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/actuator")
public
class HealthCheckController {
    @Autowired(required = false)
    private DataSource dataSource;
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("service", "sports-management-platform");
        Map<String, Object> components = new HashMap<>();
        // Database health check
        boolean dbHealthy = checkDatabaseHealth();
        components.put("database", Map.of("status", dbHealthy ? "UP" : "DOWN", "details", dbHealthy ? "Database connection is healthy" : "Database connection failed"));
        // Memory health check
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsage =(double) usedMemory / maxMemory * 100;
        components.put("memory", Map.of("status", memoryUsage < 90 ? "UP" : "DOWN", "details", Map.of("used", formatBytes(usedMemory), "max", formatBytes(maxMemory), "usage", String.format("%.2f%%", memoryUsage))));
        // Thread health check
        int activeThreads = Thread.activeCount();
        components.put("threads", Map.of("status", activeThreads < 1000 ? "UP" : "DOWN", "details", Map.of("active", activeThreads, "max", 1000)));
        health.put("components", components);
        // Overall status
        boolean allHealthy = dbHealthy && memoryUsage < 90 && activeThreads < 1000;
        health.put("status", allHealthy ? "UP" : "DOWN");
        return ResponseEntity.ok(health);
    }
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Sports Academy Management System");
        info.put("description", "Multi-sport academy management platform");
        info.put("version", "1.0.0");
        info.put("timestamp", System.currentTimeMillis());
        Map<String, Object> build = new HashMap<>();
        build.put("java.version", System.getProperty("java.version"));
        build.put("os.name", System.getProperty("os.name"));
        build.put("os.arch", System.getProperty("os.arch"));
        build.put("os.version", System.getProperty("os.version"));
        info.put("build", build);
        return ResponseEntity.ok(info);
    }
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        Map<String, Object> metrics = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("memory.used", formatBytes(usedMemory));
        jvm.put("memory.free", formatBytes(freeMemory));
        jvm.put("memory.total", formatBytes(totalMemory));
        jvm.put("memory.max", formatBytes(maxMemory));
        jvm.put("threads.active", Thread.activeCount());
        jvm.put("processors", runtime.availableProcessors());
        metrics.put("jvm", jvm);
        metrics.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(metrics);
    }
    private boolean checkDatabaseHealth() {
        if (dataSource == null) {
            return false;
        }
        try(Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
            // 5 second timeout
        } catch (Exception e) {
            return false;
        }
    }
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp =(int)(Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE" .charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
