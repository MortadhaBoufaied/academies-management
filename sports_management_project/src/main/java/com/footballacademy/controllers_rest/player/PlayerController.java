package com.footballacademy.controllers_rest.player;

import com.footballacademy.DTO.PlayerCombinedDTO;
import com.footballacademy.model.Division;
import com.footballacademy.model.Parent;
import com.footballacademy.model.Player;
import com.footballacademy.model.Trainer;
import com.footballacademy.model.User;
import com.footballacademy.model.Sport;
import com.footballacademy.model.SportPosition;
import com.footballacademy.repository.DivisionRepository;
import com.footballacademy.repository.ParentRepository;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.repository.TrainerRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.repository.SportRepository;
import com.footballacademy.repository.SportPositionRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.player.PlayerService;
import com.footballacademy.util.MediaUrlUtil;
import com.footballacademy.util.PlayerImageDefaults;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/players")
@Validated
public
class PlayerController {
    private static final Logger logger = LoggerFactory.getLogger(PlayerController.
    class);
    private final PlayerService playerService;
    private final com.footballacademy.services.player.PlayerStatsService playerStatsService;
    private final UserRepository userRepository;
    private final DivisionRepository divisionRepository;
    private final ParentRepository parentRepository;
    private final TrainerRepository trainerRepository;
    private final PlayerRepository playerRepository;
    private final SportRepository sportRepository;
    private final SportPositionRepository sportPositionRepository;
    private final AcademyAccessService academyAccessService;
    public PlayerController(PlayerService playerService, com.footballacademy.services.player.PlayerStatsService playerStatsService, UserRepository userRepository, DivisionRepository divisionRepository, ParentRepository parentRepository, TrainerRepository trainerRepository, PlayerRepository playerRepository, SportRepository sportRepository, SportPositionRepository sportPositionRepository, AcademyAccessService academyAccessService) {
        this.playerService = playerService;
        this.playerStatsService = playerStatsService;
        this.userRepository = userRepository;
        this.divisionRepository = divisionRepository;
        this.parentRepository = parentRepository;
        this.trainerRepository = trainerRepository;
        this.playerRepository = playerRepository;
        this.sportRepository = sportRepository;
        this.sportPositionRepository = sportPositionRepository;
        this.academyAccessService = academyAccessService;
    }
    @GetMapping
    public ResponseEntity<?> getAllPlayers(HttpServletRequest request) {
        try {
            List<PlayerCombinedDTO> players = playerService.getAllPlayersCombined();
            if (players == null || players.isEmpty()) return ResponseEntity.ok(Collections.emptyList());
            List<PlayerCombinedDTO> mapped = players.stream() .map(dto -> toAbsolute(dto, request)) .toList();
            return ResponseEntity.ok(mapped);
        } catch (Exception e) {
            logger.error("Failed to fetch all players", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch players"));
        }
    }
    @PostMapping
    public ResponseEntity<?> createPlayer(
    @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            if (body == null || body.isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Player payload is required"));
            } Map<String, Object> userMap = asMap(body.get("user"));
            Long userId = parseLong(userMap.get("id"));
            if (userId == null) userId = parseLong(body.get("userId"));
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Valid user.id or userId is required"));
            } final Long resolvedUserId = userId;
            User user = userRepository.findById(resolvedUserId) .orElseThrow(() -> new RuntimeException("User not found with id: " + resolvedUserId));
            academyAccessService.assertCanAccessUser(user);
            Player player = playerRepository.findById(resolvedUserId) .orElseGet(() -> {
                Player p = new Player();
                p.setUser(user);
                p.setPaid(false);
                p.setAcademy(user.getAcademy() != null ? user.getAcademy() : academyAccessService.academyForWrite(null));
                return p;
            });
            if (player.getUser() == null) {
                player.setUser(user);
            }
            if (player.getAcademy() == null) {
                player.setAcademy(user.getAcademy() != null ? user.getAcademy() : academyAccessService.academyForWrite(null));
            } applyPlayerFields(player, body);
            Player saved = playerRepository.save(player);
            PlayerCombinedDTO dto = playerService.getPlayerCombinedById(saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED) .body(toAbsolute(dto, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to create player: " + e.getMessage()));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlayerById(
    @PathVariable
    @NotNull
    @Positive Long id, HttpServletRequest request) {
        try {
            PlayerCombinedDTO dto = playerService.getPlayerCombinedById(id);
            return ResponseEntity.ok(toAbsolute(dto, request));
        } catch (Exception e) {
            logger.warn("Player not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Player not found"));
        }
    }
    @GetMapping("/division/{divisionId}")
    public ResponseEntity<?> getPlayersByDivision(
    @PathVariable("divisionId")
    @NotNull
    @Positive Long divisionId, HttpServletRequest request) {
        try {
            List<PlayerCombinedDTO> players = playerService.getPlayersCombinedByDivision(divisionId);
            return ResponseEntity.ok(players.stream() .map(dto -> toAbsolute(dto, request)) .toList());
        } catch (Exception e) {
            logger.error("Failed to fetch players for division {}", divisionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch players for division"));
        }
    }
    @PutMapping("/{id}/relations")
    public ResponseEntity<?> updateRelations(
    @PathVariable
    @NotNull
    @Positive Long id,
    @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Long divisionId = body.get("divisionId") == null ? null : Long.valueOf(body.get("divisionId") .toString());
            Long trainerId = body.get("trainerId") == null ? null : Long.valueOf(body.get("trainerId") .toString());
            Long parentId = body.get("parentId") == null ? null : Long.valueOf(body.get("parentId") .toString());
            playerService.assignRelations(id, divisionId, trainerId, parentId);
            return ResponseEntity.ok(toAbsolute(playerService.getPlayerCombinedById(id), request));
        } catch (Exception e) {
            logger.error("Failed to update relations for player {}", id, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", "Failed to update player relations"));
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlayer(
    @PathVariable
    @NotNull
    @Positive Long id,
    @RequestBody Map<String, Object> body, HttpServletRequest request) {
        try {
            Player player = playerService.getPlayerById(id);
            User user = player.getUser();
            Map<String, Object> userMap = asMap(body.get("user"));
            if (user != null && !userMap.isEmpty()) {
                if (userMap.containsKey("nom")) user.setNom(asString(userMap.get("nom")));
                if (userMap.containsKey("email")) user.setEmail(asString(userMap.get("email")));
                if (userMap.containsKey("tel")) user.setTel(asString(userMap.get("tel")));
                userRepository.save(user);
            }
            if (user != null) {
                if (body.containsKey("nom")) user.setNom(asString(body.get("nom")));
                if (body.containsKey("email")) user.setEmail(asString(body.get("email")));
                if (body.containsKey("tel")) user.setTel(asString(body.get("tel")));
                userRepository.save(user);
            } applyPlayerFields(player, body);
            playerRepository.save(player);
            return ResponseEntity.ok(toAbsolute(playerService.getPlayerCombinedById(id), request));
        } catch (RuntimeException e) {
            logger.warn("Failed to update player {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Player not found"));
        } catch (Exception e) {
            logger.error("Failed to update player {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to update player"));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlayer(
    @PathVariable
    @NotNull
    @Positive Long id) {
        try {
            playerService.getPlayerById(id);
            playerRepository.deleteById(id);
            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Player deleted successfully"));
        } catch (Exception e) {
            logger.error("Failed to delete player {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to delete player"));
        }
    }
    @GetMapping("/unassigned")
    public ResponseEntity<?> getUnassignedPlayers(HttpServletRequest request) {
        try {
            List<PlayerCombinedDTO> players = playerService.getPlayersWithoutDivisionCombined();
            return ResponseEntity.ok(players.stream() .map(dto -> toAbsolute(dto, request)) .toList());
        } catch (Exception e) {
            logger.error("Failed to fetch unassigned players", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch unassigned players"));
        }
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchPlayersByName(
    @RequestParam
    @NotNull String name, HttpServletRequest request) {
        try {
            if (name == null || name.trim() .isEmpty()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Search term cannot be empty"));
            } List<PlayerCombinedDTO> players = playerService.searchPlayersByName(name);
            return ResponseEntity.ok(players.stream() .map(dto -> toAbsolute(dto, request)) .toList());
        } catch (Exception e) {
            logger.error("Failed to search players by name: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Search failed"));
        }
    }
    @PostMapping("/search/advanced")
    public ResponseEntity<?> searchPlayersAdvanced(
    @RequestParam(required = false) String name,
    @RequestParam(required = false) String position,
    @RequestParam(required = false) Boolean paid,
    @RequestParam(required = false) Long divisionId, HttpServletRequest request) {
        try {
            List<PlayerCombinedDTO> players = playerService.searchPlayersAdvanced(name, position, paid, divisionId);
            return ResponseEntity.ok(players.stream() .map(dto -> toAbsolute(dto, request)) .toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Advanced search failed: " + e.getMessage()));
        }
    }
    @PutMapping("/{playerId}/stats")
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
            var updated = playerStatsService.updatePlayerStats(playerId, goals, assists, rating, played);
            return ResponseEntity.ok(updated);
        } catch (ClassCastException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Invalid data format in stat update: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Player not found for stat update: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to update player stats: " + e.getMessage()));
        }
    }
    private PlayerCombinedDTO toAbsolute(PlayerCombinedDTO dto, HttpServletRequest request) {
        String rel = PlayerImageDefaults.resolveRelative(dto.imageUrl());
        String abs = MediaUrlUtil.toAbsolute(request, rel);
        return new PlayerCombinedDTO(dto.id(), dto.userId(), dto.nom(), dto.email(), dto.tel(), dto.dateNaissance(), dto.position(), dto.age(), dto.nationalite(), abs, dto.paid(), dto.height(), dto.weight(), dto.goals(), dto.assists(), dto.matches(), dto.rating(), dto.divisionId(), dto.parentId(), dto.trainerId());
    }
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return(Map<String, Object>) map;
        } return Collections.emptyMap();
    }
    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
    private Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
    private Integer parseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
    private Double parseDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
    private Boolean parseBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() != 0;
        String s = String.valueOf(value) .trim() .toLowerCase();
        if (s.equals("true") || s.equals("1") || s.equals("yes")) return true;
        if (s.equals("false") || s.equals("0") || s.equals("no")) return false;
        return null;
    }
    private void applyPlayerFields(Player player, Map<String, Object> body) {
        if (body.containsKey("position")) player.setPosition(asString(body.get("position")));
        if (body.containsKey("age")) player.setAge(parseInteger(body.get("age")));
        if (body.containsKey("nationality")) {
            player.setNationality(asString(body.get("nationality")));
        } else
        if (body.containsKey("nationalite")) {
            player.setNationality(asString(body.get("nationalite")));
        }
        if (body.containsKey("phone")) {
            player.setPhone(asString(body.get("phone")));
        } else
        if (body.containsKey("tel")) {
            player.setPhone(asString(body.get("tel")));
        }
        if (body.containsKey("imageUrl")) player.setImageUrl(asString(body.get("imageUrl")));
        if (body.containsKey("height")) player.setHeight(parseDouble(body.get("height")));
        if (body.containsKey("weight")) player.setWeight(parseDouble(body.get("weight")));
        if (body.containsKey("goals")) player.setGoals(parseInteger(body.get("goals")));
        if (body.containsKey("assists")) player.setAssists(parseInteger(body.get("assists")));
        if (body.containsKey("matches")) player.setMatches(parseInteger(body.get("matches")));
        if (body.containsKey("customStats")) player.setCustomStats(asString(body.get("customStats")));
        if (body.containsKey("rating")) {
            player.setAverageRating(parseDouble(body.get("rating")));
        } else
        if (body.containsKey("averageRating")) {
            player.setAverageRating(parseDouble(body.get("averageRating")));
        }
        if (body.containsKey("isPaid")) {
            Boolean paid = parseBoolean(body.get("isPaid"));
            if (paid != null) player.setPaid(paid);
        } else
        if (body.containsKey("paid")) {
            Boolean paid = parseBoolean(body.get("paid"));
            if (paid != null) player.setPaid(paid);
        } Long divisionId = null;
        if (body.containsKey("divisionId")) divisionId = parseLong(body.get("divisionId"));
        if (body.containsKey("division")) {
            Map<String, Object> divMap = asMap(body.get("division"));
            if (!divMap.isEmpty()) divisionId = parseLong(divMap.get("id"));
        }
        if (body.containsKey("divisionId") || body.containsKey("division")) {
            if (divisionId == null) {
                player.setDivision(null);
            } else {
                Division division = divisionRepository.findById(divisionId) .orElse(null);
                if (division != null) academyAccessService.assertCanAccessAcademy(division.getAcademy());
                player.setDivision(division);
            }
        }
        if (body.containsKey("parentId")) {
            Long parentId = parseLong(body.get("parentId"));
            Parent parent = parentId == null ? null : parentRepository.findById(parentId) .orElse(null);
            if (parent != null) academyAccessService.assertCanAccessAcademy(parent.getAcademy());
            player.setParent(parent);
        }
        if (body.containsKey("trainerId")) {
            Long trainerId = parseLong(body.get("trainerId"));
            Trainer trainer = trainerId == null ? null : trainerRepository.findById(trainerId) .orElse(null);
            if (trainer != null) academyAccessService.assertCanAccessAcademy(trainer.getAcademy());
            player.setTrainer(trainer);
        }
        if (body.containsKey("sportId")) {
            Long sportId = parseLong(body.get("sportId"));
            Sport sport = sportId == null ? null : sportRepository.findById(sportId) .orElse(null);
            player.setSport(sport);
        }
        if (body.containsKey("sportPositionId")) {
            Long sportPositionId = parseLong(body.get("sportPositionId"));
            SportPosition position = sportPositionId == null ? null : sportPositionRepository.findById(sportPositionId) .orElse(null);
            if (position != null && player.getSport() != null && position.getSport() != null && !position.getSport() .getId() .equals(player.getSport() .getId())) {
                throw new RuntimeException("Sport position does not belong to selected sport");
            } player.setSportPosition(position);
            if (position != null) {
                player.setPosition(position.getName());
            }
        }
    }
}
