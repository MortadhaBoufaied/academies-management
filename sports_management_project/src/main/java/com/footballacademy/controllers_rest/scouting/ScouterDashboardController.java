package com.footballacademy.controllers_rest.scouting;

import com.footballacademy.model.ScouterWatchedPlayer;
import com.footballacademy.model.User;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.scouting.AcademyPerformanceRankingService;
import com.footballacademy.services.scouting.ScouterDashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/scouter")
@PreAuthorize("hasRole('SCOUTER')")
public class ScouterDashboardController {
    private final UserRepository userRepository;
    private final ScouterDashboardService dashboardService;
    private final AcademyPerformanceRankingService rankingService;

    public ScouterDashboardController(UserRepository userRepository, ScouterDashboardService dashboardService, AcademyPerformanceRankingService rankingService) {
        this.userRepository = userRepository;
        this.dashboardService = dashboardService;
        this.rankingService = rankingService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Authentication authentication, @RequestParam Map<String, Object> filters) {
        User me = currentUser(authentication);
        if (me == null) return unauthorized();
        return ResponseEntity.ok(dashboardService.getDashboard(me.getId(), filters));
    }

    @GetMapping("/watched-players")
    public ResponseEntity<?> watchedPlayers(Authentication authentication, @RequestParam Map<String, Object> filters) {
        User me = currentUser(authentication);
        if (me == null) return unauthorized();
        return ResponseEntity.ok(Map.of("items", dashboardService.getWatchedPlayers(me.getId(), filters)));
    }

    @PostMapping("/watched-players")
    public ResponseEntity<?> markWatched(Authentication authentication, @RequestBody Map<String, Object> body) {
        User me = currentUser(authentication);
        if (me == null) return unauthorized();
        Long playerId = toLong(body.get("playerId"));
        if (playerId == null) return ResponseEntity.badRequest().body(Map.of("error", "playerId is required"));
        ScouterWatchedPlayer watch = dashboardService.markPlayerAsWatched(me.getId(), playerId, string(body.get("watchStatus"), "WATCHING"), string(body.get("priority"), "MEDIUM"), string(body.get("notes"), null));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("watchId", watch.getId(), "message", "Player is now watched"));
    }

    @PutMapping("/watched-players/{watchId}")
    public ResponseEntity<?> updateWatched(Authentication authentication, @PathVariable Long watchId, @RequestBody Map<String, Object> body) {
        User me = currentUser(authentication);
        if (me == null) return unauthorized();
        ScouterWatchedPlayer watch = dashboardService.updateWatchedPlayerStatus(me.getId(), watchId, string(body.get("watchStatus"), null), string(body.get("priority"), null), string(body.get("notes"), null));
        return ResponseEntity.ok(Map.of("watchId", watch.getId(), "message", "Watched player updated"));
    }

    @DeleteMapping("/watched-players/{watchId}")
    public ResponseEntity<?> deleteWatched(Authentication authentication, @PathVariable Long watchId) {
        User me = currentUser(authentication);
        if (me == null) return unauthorized();
        dashboardService.removeWatchedPlayer(me.getId(), watchId);
        return ResponseEntity.ok(Map.of("message", "Watched player removed"));
    }

    @GetMapping("/academies/contact-list")
    public ResponseEntity<?> academyContactList(
            @RequestParam(required = false) Long sportId,
            @RequestParam(required = false) String academyName,
            @RequestParam(defaultValue = "performance") String orderBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(rankingService.searchAcademiesForScouterContact(sportId, academyName, orderBy, page, size));
    }

    @GetMapping("/academies/{academyId}/detail")
    public ResponseEntity<?> academyDetail(@PathVariable Long academyId) {
        return ResponseEntity.ok(rankingService.academyDetail(academyId));
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return null;
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String string(Object value, String fallback) {
        String text = value == null ? null : value.toString().trim();
        return text == null || text.isEmpty() ? fallback : text;
    }
}
