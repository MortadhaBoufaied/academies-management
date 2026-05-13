package com.footballacademy.controllers_rest.parent;

import com.footballacademy.model.Payment;
import com.footballacademy.model.Player;
import com.footballacademy.model.Parent;
import com.footballacademy.services.parent.ParentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.footballacademy.DTO.ParentCombinedDTO;

@RestController
@RequestMapping("/api/parents")
public
class ParentController {
    private final ParentService parentService;
    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }
    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext() .getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        // In this app, username is email; Parent id == User id (shared PK)
        try {
            return parentService.findUserIdByEmail(auth.getName());
        } catch (Exception e) {
            return null;
        }
    }
    private boolean hasRole(String role) {
        var auth = SecurityContextHolder.getContext() .getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;
        return auth.getAuthorities() .stream() .anyMatch(a -> a.getAuthority() .equals("ROLE_" + role) || a.getAuthority() .equals(role));
    }
    @GetMapping
    public ResponseEntity<?> getAllParents() {
        try {
            List<ParentCombinedDTO> parents = parentService.getAllParentsCombined();
            if (parents == null || parents.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(parents);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch parents: " + e.getMessage()));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getParentById(
    @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid parent ID"));
            } Parent parent = parentService.getParentById(id);
            return ResponseEntity.ok(parent);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Parent not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch parent: " + e.getMessage()));
        }
    }
    @GetMapping("/{id}/children")
    public ResponseEntity<?> getChildren(
    @PathVariable Long id) {
        // Access control: parent can only view their own children
        if (hasRole("PARENT")) {
            Long me = currentUserId();
            if (me != null && !me.equals(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("error", "Forbidden"));
            }
        }
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid parent ID"));
            } List<Player> children = parentService.getChildren(id);
            if (children == null || children.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(children);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Parent not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch children: " + e.getMessage()));
        }
    }
    @PostMapping("/{id}/pay/{playerId}")
    public ResponseEntity<?> payForPlayer(
    @PathVariable Long id,
    @PathVariable Long playerId,
    @RequestBody Payment payment) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid parent ID"));
            }
            if (playerId == null || playerId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid player ID"));
            }
            if (payment == null) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Payment data cannot be null"));
            } Payment createdPayment = parentService.payForPlayer(id, playerId, payment);
            return ResponseEntity.status(HttpStatus.CREATED) .body(createdPayment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Payment failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to process payment: " + e.getMessage()));
        }
    }
    @GetMapping("/{id}/payments")
    public ResponseEntity<?> getPayments(
    @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid parent ID"));
            } List<Payment> payments = parentService.getPayments(id);
            if (payments == null || payments.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Parent not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch payments: " + e.getMessage()));
        }
    }
    @GetMapping("/{id}/pending-payments")
    public ResponseEntity<?> getPendingPayments(
    @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid parent ID"));
            } List<Payment> payments = parentService.getPendingPayments(id);
            if (payments == null || payments.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Parent not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch pending payments: " + e.getMessage()));
        }
    }
    @PostMapping("/{id}/children/{playerId}")
    public ResponseEntity<?> addChild(
    @PathVariable Long id,
    @PathVariable Long playerId) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid parent ID"));
            }
            if (playerId == null || playerId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid player ID"));
            } parentService.addChild(id, playerId);
            return ResponseEntity.ok(Map.of("message", "Child added successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Failed to add child: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to add child: " + e.getMessage()));
        }
    }
    @DeleteMapping("/{id}/children/{playerId}")
    public ResponseEntity<?> removeChild(
    @PathVariable Long id,
    @PathVariable Long playerId) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid parent ID"));
            }
            if (playerId == null || playerId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid player ID"));
            } parentService.removeChild(id, playerId);
            return ResponseEntity.ok(Map.of("message", "Child removed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Failed to remove child: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to remove child: " + e.getMessage()));
        }
    }
    @PostMapping
    public ResponseEntity<?> createParent(
    @RequestBody Parent parent) {
        try {
            if (parent == null) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Parent data cannot be null"));
            } Parent createdParent = parentService.createParent(parent);
            return ResponseEntity.status(HttpStatus.CREATED) .body(createdParent);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Failed to create parent: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to create parent: " + e.getMessage()));
        }
    }
}
