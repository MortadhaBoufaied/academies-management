package com.footballacademy.controllers_rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public
class MobileTestController {
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Mobile API connection successful");
        response.put("timestamp", LocalDateTime.now() .toString());
        response.put("server", "Football Academy API");
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now() .toString());
        response.put("services", Map.of("auth", "UP", "database", "UP", "api", "UP"));
        return ResponseEntity.ok(response);
    }
    @PostMapping("/echo")
    public ResponseEntity<Map<String, Object>> echoRequest(
    @RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("received", payload);
        response.put("timestamp", LocalDateTime.now() .toString());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getMobileConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("apiVersion", "1.0.0");
        response.put("supportedFeatures", Map.of("authentication", true, "registration", true, "passwordReset", true, "profileManagement", true, "notifications", true));
        response.put("endpoints", Map.of("login", "/api/auth/login", "register", "/api/auth/signup", "refresh", "/api/auth/refresh", "logout", "/api/auth/logout", "validate", "/api/auth/validate"));
        response.put("requirements", Map.of("minPasswordLength", 8, "passwordRequirements", "Must contain uppercase, lowercase, digit, and special character", "emailFormat", "Standard email format required"));
        return ResponseEntity.ok(response);
    }
}
