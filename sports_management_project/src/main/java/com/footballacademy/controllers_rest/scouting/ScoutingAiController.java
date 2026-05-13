package com.footballacademy.controllers_rest.scouting;

import com.footballacademy.services.scouting.ScoutingAiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/scouting")
public
class ScoutingAiController {
    private final ScoutingAiService scoutingAiService;
    public ScoutingAiController(ScoutingAiService scoutingAiService) {
        this.scoutingAiService = scoutingAiService;
    }
    @GetMapping("/players/search")
    public ResponseEntity<?> searchPlayers(Authentication authentication,
    @RequestParam(required = false) String q,
    @RequestParam(required = false) String position,
    @RequestParam(required = false) Integer age_min,
    @RequestParam(required = false) Integer age_max,
    @RequestParam(required = false) Double min_potential,
    @RequestParam(required = false) Double max_churn,
    @RequestParam(required = false) String trend_label,
    @RequestParam(required = false) Double min_avg_rating,
    @RequestParam(required = false) Integer limit) {
        if (!hasAnyRole(authentication, "SCOUTER", "ADMIN")) {
            return forbidden("SCOUTER or ADMIN role required");
        } Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("q", q);
        queryParams.put("position", position);
        queryParams.put("age_min", age_min);
        queryParams.put("age_max", age_max);
        queryParams.put("min_potential", min_potential);
        queryParams.put("max_churn", max_churn);
        queryParams.put("trend_label", trend_label);
        queryParams.put("min_avg_rating", min_avg_rating);
        queryParams.put("limit", limit);
        return scoutingAiService.searchPlayers(queryParams);
    }
    @PostMapping("/players/compare")
    public ResponseEntity<?> comparePlayers(Authentication authentication,
    @RequestBody(required = false) Map<String, Object> payload) {
        if (!hasAnyRole(authentication, "SCOUTER", "ADMIN")) {
            return forbidden("SCOUTER or ADMIN role required");
        }
        if (payload == null || !payload.containsKey("player_external_ids")) {
            return badRequest("Field 'player_external_ids' is required");
        } return scoutingAiService.comparePlayers(payload);
    }
    @PostMapping("/shortlists/generate")
    public ResponseEntity<?> generateShortlist(Authentication authentication,
    @RequestBody(required = false) Map<String, Object> payload) {
        if (!hasAnyRole(authentication, "SCOUTER", "ADMIN")) {
            return forbidden("SCOUTER or ADMIN role required");
        }
        if (payload == null || payload.isEmpty()) {
            return badRequest("Shortlist payload is required");
        } return scoutingAiService.generateShortlist(payload);
    }
    @GetMapping("/ml/potential/{playerExternalId}")
    public ResponseEntity<?> getPotential(Authentication authentication,
    @PathVariable Long playerExternalId) {
        if (!hasAnyRole(authentication, "SCOUTER", "ADMIN")) {
            return forbidden("SCOUTER or ADMIN role required");
        } return scoutingAiService.potentialScore(playerExternalId);
    }
    @GetMapping("/ml/evolution/{playerExternalId}")
    public ResponseEntity<?> getEvolution(Authentication authentication,
    @PathVariable Long playerExternalId,
    @RequestParam(required = false) Integer window) {
        if (!hasAnyRole(authentication, "SCOUTER", "ADMIN")) {
            return forbidden("SCOUTER or ADMIN role required");
        } return scoutingAiService.evolution(playerExternalId, window);
    }
    @GetMapping("/ml/churn/{playerExternalId}")
    public ResponseEntity<?> getChurn(Authentication authentication,
    @PathVariable Long playerExternalId) {
        if (!hasAnyRole(authentication, "SCOUTER", "ADMIN")) {
            return forbidden("SCOUTER or ADMIN role required");
        } return scoutingAiService.churnRisk(playerExternalId);
    }
    @PostMapping({
        "/sync/academy", "/sync/football-academy"
    })
    public ResponseEntity<?> syncAcademy(Authentication authentication,
    @RequestBody(required = false) Map<String, Object> payload) {
        if (!hasAnyRole(authentication, "ADMIN")) {
            return forbidden("ADMIN role required for sync");
        } return scoutingAiService.syncAcademy(payload == null ? Map.of() : payload);
    }
    private boolean hasAnyRole(Authentication authentication, String...roles) {
        if (authentication == null || authentication.getAuthorities() == null || roles == null) {
            return false;
        }
        for (String role : roles) {
            if (role == null || role.isBlank()) continue;
            String normalizedRole = role.trim() .toUpperCase();
            String prefixedRole = "ROLE_" + normalizedRole;
            boolean matches = authentication.getAuthorities() .stream() .anyMatch(a -> {
                String auth = a.getAuthority();
                return auth != null &&(auth.equalsIgnoreCase(normalizedRole) || auth.equalsIgnoreCase(prefixedRole));
            });
            if (matches) {
                return true;
            }
        } return false;
    }
    private ResponseEntity<Map<String, String>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("error", message));
    }
    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest() .body(Map.of("error", message));
    }
}
