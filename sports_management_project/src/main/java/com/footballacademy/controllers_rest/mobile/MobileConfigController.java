package com.footballacademy.controllers_rest.mobile;

import com.footballacademy.model.Academy;
import com.footballacademy.model.Sport;
import com.footballacademy.repository.AcademyRepository;
import com.footballacademy.services.sport.SportService;
import com.footballacademy.services.theme.SportThemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
public
class MobileConfigController {
    private final SportThemeService sportThemeService;
    private final SportService sportService;
    private final AcademyRepository academyRepository;
    public MobileConfigController(SportThemeService sportThemeService, SportService sportService, AcademyRepository academyRepository) {
        this.sportThemeService = sportThemeService;
        this.sportService = sportService;
        this.academyRepository = academyRepository;
    }
    @GetMapping("/theme")
    public ResponseEntity<Map<String, Object>> theme(
    @RequestParam(value = "academyId", required = false) Long academyId,
    @RequestParam(value = "sportId", required = false) Long sportId) {
        return ResponseEntity.ok(sportThemeService.resolveTheme(academyId, sportId));
    }
    @GetMapping("/sports")
    public ResponseEntity<List<Sport>> sports() {
        return ResponseEntity.ok(sportService.getActiveSports());
    }
    @GetMapping("/academy/{id}/sports")
    public ResponseEntity<?> academySports(
    @PathVariable Long id) {
        Academy academy = academyRepository.findById(id) .orElse(null);
        if (academy == null) {
            return ResponseEntity.notFound() .build();
        } List<Sport> sports = academy.getSport() == null ? List.of() : List.of(academy.getSport());
        if (sports.isEmpty()) {
            sports = sportService.getActiveSports();
        } return ResponseEntity.ok(sports);
    }
}
