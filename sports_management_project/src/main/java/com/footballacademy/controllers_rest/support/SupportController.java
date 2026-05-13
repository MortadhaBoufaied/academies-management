package com.footballacademy.controllers_rest.support;

import com.footballacademy.services.support.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/support")
public class SupportController {
    private final EmailService emailService;

    public SupportController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/contact")
    public ResponseEntity<?> contact(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String name = string(body.get("name"));
        String email = string(body.get("email"));
        String subject = string(body.get("subject"));
        String message = string(body.get("message"));
        String userAgent = request.getHeader("User-Agent");

        if (name == null || name.isBlank() || subject == null || subject.isBlank() || message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name, subject, and message are required."));
        }

        emailService.sendSupportRequest(name, email, subject, message, userAgent);
        return ResponseEntity.ok(Map.of("message", "Support request sent successfully."));
    }

    private String string(Object value) {
        return value == null ? null : value.toString().trim();
    }
}
