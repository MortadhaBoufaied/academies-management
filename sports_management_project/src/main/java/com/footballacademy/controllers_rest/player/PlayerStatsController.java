package com.footballacademy.controllers_rest.player;

import com.footballacademy.services.player.PlayerStatsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/player-stats")
public
class PlayerStatsController {
    private final PlayerStatsService playerStatService;
    public PlayerStatsController(PlayerStatsService playerStatService) {
        this.playerStatService = playerStatService;
    }
    @GetMapping("/{playerId}")
    public ResponseEntity<?> getPlayerStats(
    @PathVariable Long playerId) {
        try {
            if (playerId == null || playerId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid player ID"));
            } var stats = playerStatService.getPlayerStats(playerId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Player stats not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch player stats: " + e.getMessage()));
        }
    }
    @PostMapping("/{playerId}/update")
    public ResponseEntity<?> updatePlayerStats(
    @PathVariable Long playerId,
    @RequestBody Map<String, Object> statUpdate) {
        try {
            if (playerId == null || playerId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid player ID"));
            }
            if (statUpdate == null || statUpdate.isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Stat update data cannot be empty"));
            } int goals =((Number) statUpdate.getOrDefault("goals", 0)) .intValue();
            int assists =((Number) statUpdate.getOrDefault("assists", 0)) .intValue();
            double rating =((Number) statUpdate.getOrDefault("rating", 0.0)) .doubleValue();
            boolean played =(boolean) statUpdate.getOrDefault("played", true);
            var updated = playerStatService.updatePlayerStats(playerId, goals, assists, rating, played);
            return ResponseEntity.ok(updated);
        } catch (ClassCastException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Invalid data format in stat update: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Player not found for stat update: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to update player stats: " + e.getMessage()));
        }
    }
    @GetMapping("/top/scorers")
    public ResponseEntity<?> getTopScorers(
    @RequestParam(defaultValue = "10") int limit) {
        try {
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Limit must be between 1 and 100"));
            } var topScorers = playerStatService.getTopScorers(limit);
            if (topScorers == null || topScorers.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(topScorers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch top scorers: " + e.getMessage()));
        }
    }
    @GetMapping("/top/assists")
    public ResponseEntity<?> getTopAssists(
    @RequestParam(defaultValue = "10") int limit) {
        try {
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Limit must be between 1 and 100"));
            } var topAssists = playerStatService.getTopAssists(limit);
            if (topAssists == null || topAssists.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(topAssists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch top assists: " + e.getMessage()));
        }
    }
    @GetMapping("/top/rated")
    public ResponseEntity<?> getTopRated(
    @RequestParam(defaultValue = "10") int limit) {
        try {
            if (limit <= 0 || limit > 100) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Limit must be between 1 and 100"));
            } var topRated = playerStatService.getTopRatedPlayers(limit);
            if (topRated == null || topRated.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } return ResponseEntity.ok(topRated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch top rated players: " + e.getMessage()));
        }
    }
    @GetMapping("/{playerId}/comparative")
    public ResponseEntity<?> getComparative(
    @PathVariable Long playerId) {
        try {
            if (playerId == null || playerId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid player ID"));
            } var comparative = playerStatService.getComparativeStats(playerId);
            return ResponseEntity.ok(comparative);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Player not found for comparative stats: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch comparative stats: " + e.getMessage()));
        }
    }
    @GetMapping("/{playerId}/trend")
    public ResponseEntity<?> getTrend(
    @PathVariable Long playerId) {
        try {
            if (playerId == null || playerId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid player ID"));
            } var trend = playerStatService.getPerformanceTrend(playerId);
            return ResponseEntity.ok(trend);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Player not found for trend analysis: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch performance trend: " + e.getMessage()));
        }
    }
    @GetMapping("/overall")
    public ResponseEntity<?> getOverall() {
        try {
            var overallStats = playerStatService.getOverallStats();
            return ResponseEntity.ok(overallStats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch overall stats: " + e.getMessage()));
        }
    }
    @GetMapping("/rankings")
    public ResponseEntity<?> getRankings() {
        try {
            Map<String, Object> rankings = Map.of("topScorers", playerStatService.getTopScorers(10), "topAssists", playerStatService.getTopAssists(10), "topRated", playerStatService.getTopRatedPlayers(10));
            return ResponseEntity.ok(rankings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch rankings: " + e.getMessage()));
        }
    }
}
