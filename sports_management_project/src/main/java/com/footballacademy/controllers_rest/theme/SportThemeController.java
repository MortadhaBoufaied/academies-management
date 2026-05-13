package com.footballacademy.controllers_rest.theme;

import com.footballacademy.model.SportTheme;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.theme.SportThemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
public
class SportThemeController {
    private final SportThemeService sportThemeService;
    private final AcademyAccessService academyAccessService;
    public SportThemeController(SportThemeService sportThemeService, AcademyAccessService academyAccessService) {
        this.sportThemeService = sportThemeService;
        this.academyAccessService = academyAccessService;
    }
    @GetMapping("/api/super-admin/themes")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<SportTheme>> list() {
        return ResponseEntity.ok(sportThemeService.findAll());
    }
    @PostMapping("/api/super-admin/themes")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> saveGlobalOrPlatformTheme(
    @RequestBody SportTheme theme,
    @RequestParam(value = "sportId", required = false) Long sportId) {
        try {
            return ResponseEntity.status(201) .body(sportThemeService.save(theme, null, sportId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest() .body(Map.of("error", ex.getMessage()));
        }
    }
    @DeleteMapping("/api/super-admin/themes/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> delete(
    @PathVariable Long id) {
        sportThemeService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Theme deleted successfully"));
    }
    @GetMapping("/api/theme/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> currentTheme() {
        Long academyId = academyAccessService.currentAcademyId();
        Long sportId = academyId != null ? academyAccessService.currentSportId() : null;
        return ResponseEntity.ok(sportThemeService.resolveTheme(academyId, sportId));
    }
    @PostMapping("/api/admin/themes/academy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> saveAcademyOverride(
    @RequestBody SportTheme theme,
    @RequestParam("sportId") Long sportId) {
        try {
            Long academyId = academyAccessService.currentAcademyOrThrow() .getId();
            return ResponseEntity.status(201) .body(sportThemeService.save(theme, academyId, sportId));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest() .body(Map.of("error", ex.getMessage()));
        }
    }
}
