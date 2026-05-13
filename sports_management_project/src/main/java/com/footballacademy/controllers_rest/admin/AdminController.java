package com.footballacademy.controllers_rest.admin;

import com.footballacademy.DTO.chatbot.ChatbotDataDTO;
import com.footballacademy.model.ChatbotData;
import com.footballacademy.model.User;
import com.footballacademy.services.admin.AdminService;
import com.footballacademy.services.auth.AuthService;
import com.footballacademy.services.chatbot.ChatbotDataService;
import com.footballacademy.util.MediaUrlUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public
class AdminController {
    private final AdminService adminService;
    @Autowired
    private final AuthService authService;
    private final ChatbotDataService chatbotDataService;
    public AdminController(AdminService adminService, AuthService authService, ChatbotDataService chatbotDataService) {
        this.adminService = adminService;
        this.authService = authService;
        this.chatbotDataService = chatbotDataService;
    }
    // ================================================== 
    // === CHATBOT DATA MANAGEMENT (ADMIN) ===
    // ==================================================
    /**
    * Upload chatbot data (multipart/form-data)
    */
    @PostMapping(value = "/chatbot-data", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadChatbotData(
    @RequestParam("question") String question,
    @RequestParam("answer") String answer,
    @RequestParam(value = "tags", required = false) String tags,
    @RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request) {
        try {
            ChatbotData data = new ChatbotData();
            data.setQuestion(question);
            data.setAnswer(answer);
            data.setTags(tags);
            if (file != null && !file.isEmpty()) {
                String relativePath = chatbotDataService.storeChatbotFile(file);
                data.setFilePath(relativePath);
            } ChatbotData created = adminService.uploadChatbotData(data);
            String absoluteFileUrl = created.getFilePath() == null ? null : MediaUrlUtil.toAbsolute(request, created.getFilePath());
            ChatbotDataDTO dto = new ChatbotDataDTO(created.getId(), created.getQuestion(), created.getAnswer(), created.getTags(), created.getScope() != null ? created.getScope() .name() : null, created.getAcademy() != null ? created.getAcademy() .getId() : null, created.getSport() != null ? created.getSport() .getId() : null, created.getSourceType() != null ? created.getSourceType() .name() : null, created.getUploadedBy() != null ? created.getUploadedBy() .getId() : null, created.getFilePath(), absoluteFileUrl);
            return ResponseEntity.status(HttpStatus.CREATED) .body(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to upload chatbot data: " + e.getMessage()));
        }
    }
    /**
    * Get all chatbot data
    */
    @GetMapping("/chatbot-data")
    public ResponseEntity<?> getAllChatbotData(HttpServletRequest request) {
        try {
            List<ChatbotDataDTO> result = adminService.getAllChatbotData() .stream() .map(d -> new ChatbotDataDTO(d.getId(), d.getQuestion(), d.getAnswer(), d.getTags(), d.getScope() != null ? d.getScope() .name() : null, d.getAcademy() != null ? d.getAcademy() .getId() : null, d.getSport() != null ? d.getSport() .getId() : null, d.getSourceType() != null ? d.getSourceType() .name() : null, d.getUploadedBy() != null ? d.getUploadedBy() .getId() : null, d.getFilePath(), d.getFilePath() == null ? null : MediaUrlUtil.toAbsolute(request, d.getFilePath()))) .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch chatbot data: " + e.getMessage()));
        }
    }
    /**
    * Delete chatbot data (also deletes stored file)
    */
    @DeleteMapping("/chatbot-data/{id}")
    public ResponseEntity<?> deleteChatbotData(
    @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid chatbot data ID"));
            } chatbotDataService.deleteData(id);
            return ResponseEntity.ok(Map.of("message", "Chatbot data deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Chatbot data not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to delete chatbot data: " + e.getMessage()));
        }
    }
    // ==================================================
    // === USER MANAGEMENT
    // ==================================================
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(adminService.getAllUsers());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch users: " + e.getMessage()));
        }
    }
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(
    @PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Invalid user ID"));
        }
        try {
            return ResponseEntity.ok(adminService.getUserWithRoleEntity(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "User not found with ID: " + id));
        }
    }
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
    @PathVariable Long id,
    @RequestBody Map<String, Object> body) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Invalid user ID"));
        }
        try {
            User update = new User();
            if (body.containsKey("nom")) update.setNom(String.valueOf(body.get("nom")));
            if (body.containsKey("email")) update.setEmail(String.valueOf(body.get("email")));
            if (body.containsKey("tel")) update.setTel(String.valueOf(body.get("tel")));
            if (body.containsKey("dateNaiss") && body.get("dateNaiss") != null) {
                update.setDateNaiss(LocalDate.parse(body.get("dateNaiss") .toString(), DateTimeFormatter.ISO_LOCAL_DATE));
            } return ResponseEntity.ok(adminService.updateUser(id, update));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to update user: " + e.getMessage()));
        }
    }
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
    @PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Invalid user ID"));
        } adminService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
    // ==================================================
    // === ADMIN (ROLE = ADMIN)
    // ==================================================
    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllUsers() .stream() .filter(u -> u.hasRole("ADMIN")) .collect(Collectors.toList()));
    }
    @PostMapping("/admins")
    public ResponseEntity<?> createAdmin(
    @RequestBody Map<String, Object> body) {
        if (!body.containsKey("email") || !body.containsKey("nom") || !body.containsKey("password")) {
            return ResponseEntity.badRequest() .body(Map.of("error", "email, nom, and password are required"));
        } User admin = new User();
        admin.setEmail(body.get("email") .toString());
        admin.setNom(body.get("nom") .toString());
        admin.setTel(String.valueOf(body.getOrDefault("tel", "")));
        admin.setMdp(body.get("password") .toString());
        admin.setMainRole(User.UserRole.ADMIN);
        return ResponseEntity.status(HttpStatus.CREATED) .body(authService.register(admin));
    }
}
