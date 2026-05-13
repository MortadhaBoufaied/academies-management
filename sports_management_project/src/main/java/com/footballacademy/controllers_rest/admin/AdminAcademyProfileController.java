package com.footballacademy.controllers_rest.admin;

import com.footballacademy.controllers_rest.superadmin.AcademyManagementController.AcademyRequest;
import com.footballacademy.model.Academy;
import com.footballacademy.model.AcademyInfo;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.academy.AcademyInfoService;
import com.footballacademy.services.academy.AcademyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/academy")
@PreAuthorize("hasRole('ADMIN')")
public
class AdminAcademyProfileController {
    private final AcademyAccessService academyAccessService;
    private final AcademyService academyService;
    private final AcademyInfoService academyInfoService;
    public AdminAcademyProfileController(AcademyAccessService academyAccessService, AcademyService academyService, AcademyInfoService academyInfoService) {
        this.academyAccessService = academyAccessService;
        this.academyService = academyService;
        this.academyInfoService = academyInfoService;
    }
    @GetMapping
    public ResponseEntity<Academy> currentAcademy() {
        return ResponseEntity.ok(academyAccessService.currentAcademyOrThrow());
    }
    @GetMapping("/info")
    public ResponseEntity<AcademyInfo> currentAcademyInfo() {
        return ResponseEntity.ok(academyInfoService.getAcademyInfo());
    }
    @PutMapping
    public ResponseEntity<?> updateCurrentAcademy(
    @RequestBody AcademyRequest request) {
        try {
            Academy incoming = new Academy();
            incoming.setName(request.name());
            incoming.setSlug(request.slug());
            incoming.setEmail(request.email());
            incoming.setPhone(request.phone());
            incoming.setAddress(request.address());
            incoming.setCity(request.city());
            incoming.setCountry(request.country());
            incoming.setLogoUrl(request.logoUrl());
            Long academyId = academyAccessService.currentAcademyOrThrow() .getId();
            return ResponseEntity.ok(academyService.update(academyId, incoming, null));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest() .body(Map.of("error", ex.getMessage()));
        }
    }
    @PutMapping("/info")
    public ResponseEntity<?> updateCurrentAcademyInfo(
    @RequestBody AcademyInfo incoming) {
        try {
            return ResponseEntity.ok(academyInfoService.updateAcademyInfo(incoming));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest() .body(Map.of("error", ex.getMessage()));
        }
    }
}
