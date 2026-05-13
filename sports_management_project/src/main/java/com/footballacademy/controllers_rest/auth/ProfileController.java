package com.footballacademy.controllers_rest.auth;

import com.footballacademy.DTO.PlayerCombinedDTO;
import com.footballacademy.model.User;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.auth.AuthService;
import com.footballacademy.services.player.PlayerService;
import com.footballacademy.util.MediaUrlUtil;
import com.footballacademy.util.PlayerImageDefaults;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public
class ProfileController {
    private final AuthService authService;
    private final PlayerService playerService;
    private final UserRepository userRepository;
    public ProfileController(AuthService authService, PlayerService playerService, UserRepository userRepository) {
        this.authService = authService;
        this.playerService = playerService;
        this.userRepository = userRepository;
    }
    /** Profile by userId -> combined DTO (User+Player). */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfileByUserId(
    @PathVariable Long userId, HttpServletRequest request) {
        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid user ID"));
            } PlayerCombinedDTO dto = playerService.getPlayerCombinedById(userId);
            return ResponseEntity.ok(withAbsoluteImage(dto, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch profile: " + e.getMessage()));
        }
    }
    /** Profile for current token user -> combined DTO. */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(
    @RequestHeader(value = "Authorization", required = false) String token, HttpServletRequest request) {
        try {
            if (token == null || token.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Authorization token is required"));
            } String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
            User current = authService.getCurrentUser(jwt);
            PlayerCombinedDTO dto = playerService.getPlayerCombinedById(current.getId());
            return ResponseEntity.ok(withAbsoluteImage(dto, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Invalid or expired token: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch profile: " + e.getMessage()));
        }
    }
    /** Cookie/JWT-filter authenticated endpoint for MVC/JS clients. */
    @GetMapping("/me-lite")
    public ResponseEntity<?> meLite() {
        try {
            Authentication auth = SecurityContextHolder.getContext() .getAuthentication();
            if (auth == null || auth.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Unauthorized"));
            } User u = userRepository.findByEmail(auth.getName()) .orElse(null);
            if (u == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Unauthorized"));
            } return ResponseEntity.ok(Map.of("userId", u.getId(), "id", u.getId(), "nom", u.getNom(), "email", u.getEmail(), "role", u.getMainRole() == null ? null : u.getMainRole() .name()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", e.getMessage()));
        }
    }
    private PlayerCombinedDTO withAbsoluteImage(PlayerCombinedDTO dto, HttpServletRequest request) {
        String rel = PlayerImageDefaults.resolveRelative(dto.imageUrl());
        String abs = MediaUrlUtil.toAbsolute(request, rel);
        return new PlayerCombinedDTO(dto.id(), dto.userId(), dto.nom(), dto.email(), dto.tel(), dto.dateNaissance(), dto.position(), dto.age(), dto.nationalite(), abs, dto.paid(), dto.height(), dto.weight(), dto.goals(), dto.assists(), dto.matches(), dto.rating(), dto.divisionId(), dto.parentId(), dto.trainerId());
    }
}
