package com.footballacademy.controllers_rest.superadmin;

import com.footballacademy.model.Academy;
import com.footballacademy.model.User;
import com.footballacademy.services.academy.AcademyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/super-admin/academies")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public
class AcademyManagementController {
    private final AcademyService academyService;
    public AcademyManagementController(AcademyService academyService) {
        this.academyService = academyService;
    }
    public
    record AcademyRequest(String name, String slug, String email, String phone, String address, String city, String country, Academy.AcademyStatus status, String logoUrl, Long sportId, List<Long> sportIds) {
    }
    public
    record AcademyAdminRequest(String name, String email, String password, String phone) {
    }
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        return ResponseEntity.ok(
            academyService.findAll().stream().map(this::toDto).collect(Collectors.toList())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> details(
    @PathVariable Long id) {
        return ResponseEntity.ok(toDto(academyService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<?> create(
    @RequestBody AcademyRequest request) {
        try {
            return ResponseEntity.status(201) .body(toDto(academyService.create(toAcademy(request), resolveSportIds(request))));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest() .body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
    @PathVariable Long id,
    @RequestBody AcademyRequest request) {
        try {
            return ResponseEntity.ok(toDto(academyService.update(id, toAcademy(request), resolveSportIds(request))));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest() .body(Map.of("error", ex.getMessage()));
        }
    }

    private Map<String, Object> toDto(Academy academy) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", academy.getId());
        dto.put("name", academy.getName());
        dto.put("slug", academy.getSlug());
        dto.put("email", academy.getEmail());
        dto.put("phone", academy.getPhone());
        dto.put("address", academy.getAddress());
        dto.put("city", academy.getCity());
        dto.put("country", academy.getCountry());
        dto.put("status", academy.getStatus() != null ? academy.getStatus().name() : null);
        dto.put("logoUrl", academy.getLogoUrl());
        dto.put("subscriptionOffer", academy.getSubscriptionOffer() != null ? academy.getSubscriptionOffer().name() : null);
        dto.put("subscriptionPaymentStatus", academy.getSubscriptionPaymentStatus() != null ? academy.getSubscriptionPaymentStatus().name() : null);
        dto.put("subscriptionActivatedAt", academy.getSubscriptionActivatedAt());
        dto.put("subscriptionUpdatedAt", academy.getSubscriptionUpdatedAt());
        dto.put("latestPerformanceScore", academy.getLatestPerformanceScore());
        dto.put("latestRankingPosition", academy.getLatestRankingPosition());
        dto.put("performanceUpdatedAt", academy.getPerformanceUpdatedAt());
        dto.put("scouterContactEnabled", academy.getScouterContactEnabled());
        dto.put("createdAt", academy.getCreatedAt());
        dto.put("updatedAt", academy.getUpdatedAt());
        if (academy.getSport() != null) {
            dto.put("sportId", academy.getSport().getId());
            dto.put("sportName", academy.getSport().getName());
        }
        return dto;
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
    @PathVariable Long id) {
        academyService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Academy deleted successfully"));
    }
    @PostMapping("/{id}/admins")
    public ResponseEntity<?> createFirstAdmin(
    @PathVariable Long id,
    @RequestBody AcademyAdminRequest request) {
        try {
            User user = academyService.createFirstAdmin(id, request.name(), request.email(), request.password(), request.phone());
            return ResponseEntity.status(201) .body(user);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest() .body(Map.of("error", ex.getMessage()));
        }
    }
    private Academy toAcademy(AcademyRequest request) {
        Academy academy = new Academy();
        academy.setName(request.name());
        academy.setSlug(request.slug());
        academy.setEmail(request.email());
        academy.setPhone(request.phone());
        academy.setAddress(request.address());
        academy.setCity(request.city());
        academy.setCountry(request.country());
        academy.setStatus(request.status());
        academy.setLogoUrl(request.logoUrl());
        return academy;
    }
    private List<Long> resolveSportIds(AcademyRequest request) {
        if (request.sportId() != null) {
            return List.of(request.sportId());
        } return request.sportIds();
    }
}
