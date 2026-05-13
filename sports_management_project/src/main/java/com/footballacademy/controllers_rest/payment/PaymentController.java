package com.footballacademy.controllers_rest.payment;

import com.footballacademy.DTO.PaymentDto;
import com.footballacademy.model.Payment;
import com.footballacademy.services.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Validated
public
class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.
    class);
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @GetMapping
    public ResponseEntity<?> getAllPayments() {
        try {
            List<Payment> payments = paymentService.getAllPayments();
            return ResponseEntity.ok(payments != null ? payments : Collections.emptyList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch payments: " + e.getMessage()));
        }
    }
    @GetMapping("/player/{playerId}")
    public ResponseEntity<?> getPaymentsForPlayer(
    @PathVariable
    @NotNull
    @Positive Long playerId) {
        try {
            List<Payment> payments = paymentService.getPaymentsForPlayer(playerId);
            return ResponseEntity.ok(payments != null ? payments : Collections.emptyList());
        } catch (Exception e) {
            logger.error("Failed to fetch payments for player {}", playerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch player payments"));
        }
    }
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<?> getPaymentsForParent(
    @PathVariable
    @NotNull
    @Positive Long parentId) {
        try {
            List<Payment> payments = paymentService.getPaymentsForParent(parentId);
            return ResponseEntity.ok(payments != null ? payments : Collections.emptyList());
        } catch (Exception e) {
            logger.error("Failed to fetch payments for parent {}", parentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch parent payments"));
        }
    }
    @PostMapping
    public ResponseEntity<?> createPayment(
    @Valid
    @RequestBody PaymentDto paymentDto) {
        try {
            if (paymentDto == null) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Payment payload is required"));
            } Payment created = paymentService.createPayment(paymentDto.getPlayerId(), paymentDto.getMontant(), paymentDto.getMois());
            return ResponseEntity.status(HttpStatus.CREATED) .body(created);
        } catch (Exception e) {
            logger.error("Failed to create payment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", "Failed to create payment"));
        }
    }
    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<?> markAsPaid(
    @PathVariable
    @NotNull
    @Positive Long id) {
        try {
            Payment updated = paymentService.markAsPaid(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.warn("Failed to mark payment {} as paid: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error marking payment {} as paid", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to mark payment as paid"));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePayment(
    @PathVariable
    @NotNull
    @Positive Long id) {
        try {
            paymentService.deletePayment(id);
            return ResponseEntity.ok(Map.of("message", "Payment deleted successfully"));
        } catch (Exception e) {
            logger.error("Failed to delete payment {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to delete payment"));
        }
    }
    @GetMapping("/overdue")
    public ResponseEntity<?> getOverduePayments() {
        try {
            List<Payment> overduePayments = paymentService.getOverduePayments();
            return ResponseEntity.ok(overduePayments != null ? overduePayments : Collections.emptyList());
        } catch (Exception e) {
            logger.error("Failed to fetch overdue payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch overdue payments"));
        }
    }
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingPayments() {
        try {
            List<Payment> pendingPayments = paymentService.getPendingPayments();
            return ResponseEntity.ok(pendingPayments != null ? pendingPayments : Collections.emptyList());
        } catch (Exception e) {
            logger.error("Failed to fetch pending payments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch pending payments"));
        }
    }
    @GetMapping("/revenue/monthly")
    public ResponseEntity<?> getMonthlyRevenue() {
        try {
            Double revenue = paymentService.getMonthlyRevenue();
            return ResponseEntity.ok(Map.of("monthlyRevenue", revenue != null ? revenue : 0.0));
        } catch (Exception e) {
            logger.error("Failed to fetch monthly revenue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch monthly revenue"));
        }
    }
}
