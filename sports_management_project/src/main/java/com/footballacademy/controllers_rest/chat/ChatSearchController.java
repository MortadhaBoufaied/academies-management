package com.footballacademy.controllers_rest.chat;

import com.footballacademy.DTO.chat.ContactDTO;
import com.footballacademy.model.User;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.chat.ChatAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public
class ChatSearchController {
    public
    record UserBrief(Long id, String nom, String email, String role) {
    }
    private final UserRepository userRepository;
    private final ChatAccessService accessService;
    public ChatSearchController(UserRepository userRepository, ChatAccessService accessService) {
        this.userRepository = userRepository;
        this.accessService = accessService;
    }
    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext() .getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()) .orElse(null);
    }
    /**      * Legacy endpoint kept for backward compatibility.      * Now enforced through ChatAccessService (role-scoped).      */
    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(
    @RequestParam(name = "q", defaultValue = "") String q) {
        User me = currentUser();
        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Unauthorized"));
        String query =(q == null) ? "" : q.trim();
        if (query.length() < 2) return ResponseEntity.ok(List.of());
        List<ContactDTO> allowed = accessService.listContacts(me, null, query);
        List<UserBrief> users = allowed.stream() .filter(c -> c != null && c.kind() != null && c.kind() .equalsIgnoreCase("USER")) .map(c -> new UserBrief(c.id(), c.name(), c.email(), c.role())) .toList();
        return ResponseEntity.ok(users);
    }
}
