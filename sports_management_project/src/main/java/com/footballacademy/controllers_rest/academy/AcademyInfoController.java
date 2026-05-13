package com.footballacademy.controllers_rest.academy;

import com.footballacademy.model.AcademyInfo;
import com.footballacademy.model.Division;
import com.footballacademy.services.academy.AcademyInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/academy")
public
class AcademyInfoController {
    private final AcademyInfoService academyInfoService;
    public AcademyInfoController(AcademyInfoService academyInfoService) {
        this.academyInfoService = academyInfoService;
    }
    /** GET /api/academy */
    @GetMapping
    public ResponseEntity<?> getAcademyInfo() {
        var info = academyInfoService.getAcademyInfo();
        return ResponseEntity.ok(info);
    }
    /** PUT /api/academy */
    @PutMapping
    public ResponseEntity<?> updateAcademyInfo(
    @RequestBody AcademyInfo incoming) {
        var updated = academyInfoService.updateAcademyInfo(incoming);
        return ResponseEntity.ok(updated);
    }
    /** Attach division */
    @PostMapping("/divisions/{divisionId}")
    public ResponseEntity<?> addDivisionToAcademy(
    @PathVariable Long divisionId) {
        var updated = academyInfoService.addDivision(divisionId);
        return ResponseEntity.ok(updated);
    }
    /** Detach division */
    @DeleteMapping("/divisions/{divisionId}")
    public ResponseEntity<?> removeDivisionFromAcademy(
    @PathVariable Long divisionId) {
        var updated = academyInfoService.removeDivision(divisionId);
        return ResponseEntity.ok(updated);
    }
    /** Return resolved Division entities */
    @GetMapping("/divisions")
    public ResponseEntity<?> getAcademyDivisions() {
        return ResponseEntity.ok(academyInfoService.getAssociatedDivisionsForCurrentAcademy());
    }
    /** Return divisions from the current academy sport that can be associated. */
    @GetMapping("/divisions/available")
    public ResponseEntity<?> getAvailableAcademyDivisions() {
        List<DivisionOption> divisions = academyInfoService.getAvailableDivisionsForCurrentAcademy() .stream() .map(DivisionOption::from) .toList();
        return ResponseEntity.ok(divisions);
    }
    private
    record DivisionOption(Long id, String nom, String category, String categorie, Integer minAge, Integer maxAge, String gender, String level, String competitionScope, Long sportId, String sportName) {
        private static DivisionOption from(Division division) {
            return new DivisionOption(division.getId(), division.getNom(), division.getCategorie(), division.getCategorie(), division.getMinAge(), division.getMaxAge(), division.getGender(), division.getLevel(), division.getCompetitionScope(), division.getSport() != null ? division.getSport() .getId() : null, division.getSport() != null ? division.getSport() .getName() : null);
        }
    }
}
