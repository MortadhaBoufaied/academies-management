package com.footballacademy.controllers_rest.search;

import com.footballacademy.DTO.PlayerCombinedDTO;
import com.footballacademy.model.Division;
import com.footballacademy.model.Parent;
import com.footballacademy.services.player.PlayerService;
import com.footballacademy.services.division.DivisionService;
import com.footballacademy.services.trainer.TrainerService;
import com.footballacademy.services.parent.ParentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/search")
public
class SearchController {
    private final PlayerService playerService;
    private final DivisionService divisionService;
    private final TrainerService trainerService;
    private final ParentService parentService;
    public SearchController(PlayerService playerService, DivisionService divisionService, TrainerService trainerService, ParentService parentService) {
        this.playerService = playerService;
        this.divisionService = divisionService;
        this.trainerService = trainerService;
        this.parentService = parentService;
    }
    /**      * Global search across players, divisions, trainers, and parents      * @param q Search query string      * @param type Optional: filter by entity type (player, division, trainer, parent)      *             If not specified, searches all types      * @return Map containing results grouped by entity type      */
    @GetMapping
    public ResponseEntity<?> search(
    @RequestParam String q,
    @RequestParam(required = false) String type) {
        try {
            if (q == null || q.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Search query cannot be empty"));
            } String query = q.trim() .toLowerCase();
            Map<String, Object> results = new HashMap<>();
            // Search players
            if (type == null || type.equalsIgnoreCase("player")) {
                try {
                    List<PlayerCombinedDTO> players = playerService.searchPlayersByName(query);
                    results.put("players", players != null ? players : Collections.emptyList());
                } catch (Exception e) {
                    results.put("players", Collections.emptyList());
                }
            }
            // Search divisions
            if (type == null || type.equalsIgnoreCase("division")) {
                try {
                    List<Division> divisions = divisionService.getAllDivisions() .stream() .filter(d -> d.getNom() != null && d.getNom() .toLowerCase() .contains(query)) .toList();
                    results.put("divisions", divisions);
                } catch (Exception e) {
                    results.put("divisions", Collections.emptyList());
                }
            }
            // Search trainers
            if (type == null || type.equalsIgnoreCase("trainer")) {
                try {
                    var trainers = trainerService.getAllTrainersCombined() .stream() .filter(dto -> {
                        // TrainerCombinedDTO may have userId/userName, check if accessible
                        String name = null;
                        if (dto.name() != null) {
                            name = dto.name();
                        } return name != null && name.toLowerCase() .contains(query);
                    }) .toList();
                    results.put("trainers", trainers);
                } catch (Exception e) {
                    results.put("trainers", Collections.emptyList());
                }
            }
            // Search parents
            if (type == null || type.equalsIgnoreCase("parent")) {
                try {
                    List<Parent> parents = parentService.getAllParents() .stream() .filter(p -> p.getUser() != null && p.getUser() .getNom() != null && p.getUser() .getNom() .toLowerCase() .contains(query)) .toList();
                    results.put("parents", parents);
                } catch (Exception e) {
                    results.put("parents", Collections.emptyList());
                }
            } return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }
}
