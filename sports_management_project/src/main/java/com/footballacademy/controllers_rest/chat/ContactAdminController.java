package com.footballacademy.controllers_rest.chat;

import com.footballacademy.model.User;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.chat.AcademyAdminContactService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ContactAdminController {
    private final UserRepository userRepository;
    private final AcademyAdminContactService academyAdminContactService;

    public ContactAdminController(UserRepository userRepository, AcademyAdminContactService academyAdminContactService) {
        this.userRepository = userRepository;
        this.academyAdminContactService = academyAdminContactService;
    }

    @PostMapping("/contact-admin")
    public ResponseEntity<?> contactAdmin(Authentication authentication, @RequestBody(required = false) Map<String, Object> body) {
        User requester = currentUser(authentication);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        try {
            Long academyId = toLong(body != null ? body.get("academyId") : null);
            return ResponseEntity.ok(academyAdminContactService.contactAdmin(requester, academyId));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    private User currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) return null;
        return userRepository.findByEmail(authentication.getName()).orElse(null);
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
