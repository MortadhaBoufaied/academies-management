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
class ChatContactsController {
    private final UserRepository userRepository;
    private final ChatAccessService accessService;
    public ChatContactsController(UserRepository userRepository, ChatAccessService accessService) {
        this.userRepository = userRepository;
        this.accessService = accessService;
    }
    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext() .getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()) .orElse(null);
    }
    @GetMapping("/contacts")
    public ResponseEntity<?> contacts(
    @RequestParam(required = false) String category,
    @RequestParam(required = false) String q) {
        User me = currentUser();
        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Unauthorized"));
        List<ContactDTO> list = accessService.listContacts(me, category, q);
        return ResponseEntity.ok(list);
    }
}
