package com.footballacademy.controllers_rest.player;

import com.footballacademy.DTO.PlayerRankingDTO;
import com.footballacademy.model.Player;
import com.footballacademy.model.PlayerRanking;
import com.footballacademy.services.player.PlayerRankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/player-rankings")
public
class PlayerRankingController {
    private final PlayerRankingService rankingService;
    public PlayerRankingController(PlayerRankingService rankingService) {
        this.rankingService = rankingService;
    }
    /** Returns only IDs, ordered bestÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢lower (default 10) */
    @GetMapping("/top/ids")
    public ResponseEntity<?> getTopPlayerIds(
    @RequestParam(defaultValue = "10") int limit) {
        var ids = rankingService.getTopIds(limit);
        return ResponseEntity.ok(ids);
    }
    /** Optional: ranked DTOs if your UI needs more details */
    @GetMapping("/top")
    public ResponseEntity<?> getTopPlayers(
    @RequestParam(defaultValue = "10") int limit,
    @RequestParam(required = false) Long divisionId,
    @RequestParam(required = false) String position) {
        List<PlayerRanking> top = rankingService.getTop(limit, divisionId, position);
        List<PlayerRankingDTO> dto = top.stream() .map(this::toDTO) .collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }
    /** Manual recompute (otherwise scheduled daily) */
    @PostMapping("/recompute")
    public ResponseEntity<?> triggerRecompute() {
        rankingService.recomputeAndPersist();
        return ResponseEntity.ok() .build();
    }
    private PlayerRankingDTO toDTO(PlayerRanking r) {
        Player p = r.getPlayer();
        String divisionName =(p != null && p.getDivision() != null) ? p.getDivision() .getNom() : null;
        String trainerName =(p != null && p.getTrainer() != null && p.getTrainer() .getUser() != null) ? p.getTrainer() .getUser() .getNom() : null;
        return new PlayerRankingDTO(p != null ? p.getId() : r.getPlayerId(),(p != null && p.getUser() != null) ? p.getUser() .getNom() : null, p != null ? p.getPosition() : null, p != null ? p.getAge() : null, p != null ? p.getGoals() : null, p != null ? p.getAssists() : null, p != null ? p.getMatches() : null, p != null ? p.getAverageRating() : null, divisionName, trainerName, r.getScore(), r.getTier(),(p != null ?(p.getImageUrl() != null ? p.getImageUrl() : null) : null));
    }
}
