package com.footballacademy.controllers_rest.sport;

import com.footballacademy.model.Division;
import com.footballacademy.model.Sport;
import com.footballacademy.model.SportTheme;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.sport.SportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sports")
public
class SportController {
    private final SportService sportService;
    private final AcademyAccessService academyAccessService;
    public SportController(SportService sportService, AcademyAccessService academyAccessService) {
        this.sportService = sportService;
        this.academyAccessService = academyAccessService;
    }
    public
    record SportCatalogRequest(String code, String name, String description, Boolean isActive, Integer displayOrder, SportTheme theme, List<Division> divisions) {
    }
    @GetMapping
    public ResponseEntity<List<Sport>> getAllSports() {
        if (!academyAccessService.isSuperAdmin()) {
            Sport sport = academyAccessService.currentAcademyOrThrow() .getSport();
            return ResponseEntity.ok(sport == null ? List.of() : List.of(sport));
        } return ResponseEntity.ok(sportService.getAllSports());
    }
    @GetMapping("/active")
    public ResponseEntity<List<Sport>> getActiveSports() {
        if (!academyAccessService.isSuperAdmin()) {
            Sport sport = academyAccessService.currentAcademyOrThrow() .getSport();
            return ResponseEntity.ok(sport == null ? List.of() : List.of(sport));
        } return ResponseEntity.ok(sportService.getActiveSports());
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getSportById(
    @PathVariable Long id) {
        return sportService.getSportById(id) .filter(academyAccessService::canAccessSport) .map(ResponseEntity::ok) .orElse(ResponseEntity.notFound() .build());
    }
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getSportByCode(
    @PathVariable String code) {
        return sportService.getSportByCode(code) .filter(academyAccessService::canAccessSport) .map(ResponseEntity::ok) .orElse(ResponseEntity.notFound() .build());
    }
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createSport(
    @RequestBody SportCatalogRequest request) {
        try {
            Sport saved = sportService.createSport(toSport(request), request.theme(), request.divisions());
            return ResponseEntity.status(201) .body(toCatalogResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateSport(
    @PathVariable Long id,
    @RequestBody SportCatalogRequest request) {
        try {
            Sport saved = sportService.updateSport(id, toSport(request), request.theme(), request.divisions());
            return ResponseEntity.ok(toCatalogResponse(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/{id}/divisions")
    public ResponseEntity<List<Division>> getSportDivisions(
    @PathVariable Long id) {
        Sport sport = sportService.getSportById(id) .orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + id));
        academyAccessService.assertCanAccessSport(sport);
        return ResponseEntity.ok(sportService.getDivisionsForSport(id));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteSport(
    @PathVariable Long id) {
        try {
            sportService.deleteSport(id);
            return ResponseEntity.ok(Map.of("message", "Sport deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> activateSport(
    @PathVariable Long id) {
        try {
            sportService.activateSport(id);
            return ResponseEntity.ok(Map.of("message", "Sport activated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deactivateSport(
    @PathVariable Long id) {
        try {
            sportService.deactivateSport(id);
            return ResponseEntity.ok(Map.of("message", "Sport deactivated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    private Sport toSport(SportCatalogRequest request) {
        Sport sport = new Sport();
        sport.setCode(request.code());
        sport.setName(request.name());
        sport.setDescription(request.description());
        sport.setIsActive(request.isActive());
        sport.setDisplayOrder(request.displayOrder());
        return sport;
    }
    private Map<String, Object> toCatalogResponse(Sport sport) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sport", sport);
        response.put("themeId", sport.getThemeId());
        response.put("divisions", sportService.getDivisionsForSport(sport.getId()));
        return response;
    }
}
