package com.footballacademy.controllers_rest.scouting;

import com.footballacademy.model.SportScoutingFilterConfig;
import com.footballacademy.services.scouting.SportScoutingFilterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/scouting-ai")
public class ScoutingAiSportFilterController {
    private final SportScoutingFilterService filterService;

    public ScoutingAiSportFilterController(SportScoutingFilterService filterService) {
        this.filterService = filterService;
    }

    @GetMapping("/sports")
    @PreAuthorize("hasAnyRole('SCOUTER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> sports() {
        return ResponseEntity.ok(filterService.sports());
    }

    @GetMapping("/sports/{sportId}/divisions")
    @PreAuthorize("hasAnyRole('SCOUTER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> divisions(@PathVariable Long sportId) {
        return ResponseEntity.ok(filterService.divisions(sportId));
    }

    @GetMapping("/sports/{sportId}/filters")
    @PreAuthorize("hasAnyRole('SCOUTER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> filters(@PathVariable Long sportId) {
        return ResponseEntity.ok(filterService.filterConfig(sportId));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('SCOUTER','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> search(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(filterService.search(body));
    }

    @PostMapping("/filters")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> createFilter(@RequestBody Map<String, Object> body) {
        SportScoutingFilterConfig saved = filterService.saveConfig(null, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", saved.getId(), "message", "Filter created"));
    }

    @PutMapping("/filters/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> updateFilter(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SportScoutingFilterConfig saved = filterService.saveConfig(id, body);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "message", "Filter updated"));
    }

    @DeleteMapping("/filters/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<?> deleteFilter(@PathVariable Long id) {
        filterService.deleteConfig(id);
        return ResponseEntity.ok(Map.of("message", "Filter deleted"));
    }
}
