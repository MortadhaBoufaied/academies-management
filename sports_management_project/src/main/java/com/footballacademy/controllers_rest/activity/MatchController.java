package com.footballacademy.controllers_rest.activity;

import com.footballacademy.DTO.MatchDTO;
import com.footballacademy.model.Match;
import com.footballacademy.services.activity.MatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    // =====================================================
    // === READ OPERATIONS
    // =====================================================

    @GetMapping
    public ResponseEntity<?> getAllMatches() {
        try {
            List<Match> matches = matchService.getAllMatches();

            if (matches == null || matches.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Return DTOs to avoid exposing JPA entities
            return ResponseEntity.ok(
                    matches.stream()
                            .map(MatchDTO::from)
                            .collect(Collectors.toList())
            );

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Forbidden: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch matches: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMatchById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid match ID"));
        }

        try {
            Match match = matchService.getMatchById(id);
            return ResponseEntity.ok(MatchDTO.from(match));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Match not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch match: " + e.getMessage()));
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingMatches() {
        try {
            List<Match> matches = matchService.getUpcomingMatches();

            if (matches == null || matches.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            return ResponseEntity.ok(
                    matches.stream()
                            .map(MatchDTO::from)
                            .collect(Collectors.toList())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to fetch upcoming matches: " + e.getMessage()));
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<?> getMatchesInDateRange(
            @RequestParam String start,
            @RequestParam String end
    ) {
        try {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);

            List<Match> matches =
                    matchService.getMatchesInDateRange(startDate, endDate);

            if (matches == null || matches.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            return ResponseEntity.ok(
                    matches.stream()
                            .map(MatchDTO::from)
                            .collect(Collectors.toList())
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid date format or range: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch matches in date range: " + e.getMessage()));
        }
    }

    // =====================================================
    // === WRITE OPERATIONS
    // =====================================================

    @PostMapping
    public ResponseEntity<?> createMatch(@RequestBody Match match) {
        if (match == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Match data cannot be null"));
        }

        // Trainer restriction: current month only
        enforceTrainerMonthRestriction(match);

        try {
            Match created = matchService.createMatch(match);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(MatchDTO.from(created));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to create match: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create match: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMatch(
            @PathVariable Long id,
            @RequestBody Match match
    ) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid match ID"));
        }
        if (match == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Match payload is required"));
        }

        try {
            Match updated = matchService.updateMatch(id, match);
            return ResponseEntity.ok(MatchDTO.from(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Match not found for update: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update match: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/record-result")
    public ResponseEntity<?> recordResult(
            @PathVariable Long id,
            @RequestBody String result
    ) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid match ID"));
        }
        if (result == null || result.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Result cannot be empty"));
        }

        try {
            Match updated = matchService.recordResult(id, result);
            return ResponseEntity.ok(MatchDTO.from(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Match not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to record result: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMatch(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid match ID"));
        }

        try {
            matchService.deleteMatch(id);
            return ResponseEntity.ok(
                    Map.of("message", "Match deleted successfully")
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Match not found for deletion: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete match: " + e.getMessage()));
        }
    }

    // =====================================================
    // === SECURITY HELPERS
    // =====================================================

    private void enforceTrainerMonthRestriction(Match match) {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getAuthorities() == null) {
            return;
        }

        boolean isTrainer =
                auth.getAuthorities().stream().anyMatch(a ->
                        "ROLE_TRAINER".equals(a.getAuthority())
                                || "TRAINER".equals(a.getAuthority())
                );

        if (!isTrainer || match.getDate() == null) {
            return;
        }

        LocalDate matchDate = match.getDate();
        LocalDate now = LocalDate.now();

        if (matchDate.getYear() != now.getYear()
                || matchDate.getMonthValue() != now.getMonthValue()) {
            throw new RuntimeException(
                    "Trainer can only add matches for the current month"
            );
        }
    }
}
