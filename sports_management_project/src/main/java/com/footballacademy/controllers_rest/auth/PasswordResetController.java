package com.footballacademy.controllers_rest.auth;

import com.footballacademy.services.auth.PasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(passwordResetService.requestResetCode(string(body.get("email"))));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(passwordResetService.verifyResetCode(string(body.get("email")), string(body.get("code"))));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, Object> body) {
        try {
            return ResponseEntity.ok(passwordResetService.resetPassword(string(body.get("resetToken")), string(body.get("newPassword")), string(body.get("confirmPassword"))));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String string(Object value) {
        return value == null ? null : value.toString();
    }
}
