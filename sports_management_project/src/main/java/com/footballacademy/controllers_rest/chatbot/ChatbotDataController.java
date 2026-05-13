package com.footballacademy.controllers_rest.chatbot;

import com.footballacademy.DTO.chatbot.ChatbotDataDTO;
import com.footballacademy.model.ChatbotData;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.chatbot.ChatbotDataService;
import com.footballacademy.util.MediaUrlUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chatbot")
public
class ChatbotDataController {
    private final ChatbotDataService chatbotDataService;
    private final AcademyAccessService academyAccessService;
    public ChatbotDataController(ChatbotDataService chatbotDataService, AcademyAccessService academyAccessService) {
        this.chatbotDataService = chatbotDataService;
        this.academyAccessService = academyAccessService;
    }
    @GetMapping
    public ResponseEntity<?> getAllData(
    @RequestParam(value = "scope", required = false) String scope,
    @RequestParam(value = "academyId", required = false) Long academyId,
    @RequestParam(value = "sportId", required = false) Long sportId, HttpServletRequest request) {
        try {
            List<ChatbotData> data = chatbotDataService.getData(parseScope(scope), academyId, sportId);
            List<ChatbotDataDTO> dto = data.stream() .map(d -> toDTO(d, request)) .collect(Collectors.toList());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch chatbot data: " + e.getMessage()));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getDataById(
    @PathVariable Long id, HttpServletRequest request) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid data ID"));
            } ChatbotData data = chatbotDataService.getDataById(id);
            return ResponseEntity.ok(toDTO(data, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Chatbot data not found: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch chatbot data: " + e.getMessage()));
        }
    }
    @PostMapping(value = "/upload", consumes = {
        "multipart/form-data"
    })
    public ResponseEntity<?> uploadData(
    @RequestParam("question") String question,
    @RequestParam("answer") String answer,
    @RequestParam(value = "tags", required = false) String tags,
    @RequestParam(value = "scope", required = false) String scope,
    @RequestParam(value = "academyId", required = false) Long academyId,
    @RequestParam(value = "sportId", required = false) Long sportId,
    @RequestParam(value = "sourceType", required = false) String sourceType,
    @RequestParam(value = "uploadedById", required = false) Long uploadedById,
    @RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request) {
        try {
            if (question == null || question.isBlank()) return ResponseEntity.badRequest() .body(Map.of("error", "question is required"));
            if (answer == null || answer.isBlank()) return ResponseEntity.badRequest() .body(Map.of("error", "answer is required"));
            ChatbotData data = new ChatbotData();
            data.setQuestion(question);
            data.setAnswer(answer);
            data.setTags(tags);
            if (file != null && !file.isEmpty()) {
                String relPath = chatbotDataService.storeChatbotFile(file);
                data.setFilePath(relPath);
            } chatbotDataService.applyScope(data, parseScope(scope), academyId, sportId, parseSourceType(sourceType), uploadedById);
            ChatbotData created = chatbotDataService.uploadData(data);
            return ResponseEntity.status(HttpStatus.CREATED) .body(toDTO(created, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Failed to upload data: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to upload data: " + e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteData(
    @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid data ID"));
            } chatbotDataService.deleteData(id);
            return ResponseEntity.ok(Map.of("message", "Data deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Chatbot data not found for deletion: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to delete chatbot data: " + e.getMessage()));
        }
    }
    @PostMapping("/{id}/analyze")
    public ResponseEntity<?> analyzeData(
    @PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest() .body(Map.of("error", "Invalid data ID"));
            } String analysis = chatbotDataService.analyzeData(id);
            return ResponseEntity.ok(Map.of("analysis", analysis));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Chatbot data not found for analysis: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to analyze data: " + e.getMessage()));
        }
    }
    @PostMapping(value = "/ingest-csv", consumes = {
        "multipart/form-data"
    })
    public ResponseEntity<?> ingestCsv(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "scope", required = false) String scope,
    @RequestParam(value = "academyId", required = false) Long academyId,
    @RequestParam(value = "sportId", required = false) Long sportId,
    @RequestParam(value = "uploadedById", required = false) Long uploadedById) {
        try {
            chatbotDataService.ingestCsv(file, parseScope(scope), academyId, sportId, uploadedById);
            return ResponseEntity.ok(Map.of("message", "Chatbot CSV ingested successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/answer")
    public ResponseEntity<?> answer(
    @RequestParam("question") String question,
    @RequestParam(value = "academyId", required = false) Long academyId,
    @RequestParam(value = "sportId", required = false) Long sportId) {
        return ResponseEntity.ok(chatbotDataService.answer(question, academyId, sportId));
    }
    @GetMapping("/admin/bootstrap")
    public ResponseEntity<?> adminBootstrap(
    @RequestParam(value = "academyId", required = false) Long academyId,
    @RequestParam(value = "sportId", required = false) Long sportId) {
        try {
            Long effectiveAcademyId = academyAccessService.isSuperAdmin() ? academyId : academyAccessService.currentAcademyId();
            Long effectiveSportId = sportId != null ? sportId : academyAccessService.currentSportId();
            return ResponseEntity.ok(Map.of("mode", academyAccessService.isSuperAdmin() ? "SUPER_ADMIN" : "ACADEMY_ADMIN", "defaultScope", academyAccessService.isSuperAdmin() ? "GLOBAL" : "ACADEMY", "canManageGlobal", academyAccessService.isSuperAdmin(), "academyId", effectiveAcademyId, "sportId", effectiveSportId, "knowledgeBasePath", chatbotDataService.knowledgeBaseWebPath(), "recentEntries", chatbotDataService.getRecentEntries(8, effectiveAcademyId, effectiveSportId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to load chatbot console data: " + e.getMessage()));
        }
    }
    @PostMapping("/admin/ask")
    public ResponseEntity<?> adminAsk(
    @RequestBody Map<String, Object> payload) {
        try {
            String question = payload.get("question") == null ? null : String.valueOf(payload.get("question"));
            Long academyId = toLong(payload.get("academyId"));
            Long sportId = toLong(payload.get("sportId"));
            return ResponseEntity.ok(chatbotDataService.answerForAdmin(question, academyId, sportId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to ask chatbot: " + e.getMessage()));
        }
    }
    @PostMapping("/admin/teach")
    public ResponseEntity<?> adminTeach(
    @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        try {
            String question = payload.get("question") == null ? null : String.valueOf(payload.get("question"));
            String answer = payload.get("answer") == null ? null : String.valueOf(payload.get("answer"));
            String tags = payload.get("tags") == null ? null : String.valueOf(payload.get("tags"));
            Long academyId = toLong(payload.get("academyId"));
            Long sportId = toLong(payload.get("sportId"));
            Long replaceEntryId = toLong(payload.get("replaceEntryId"));
            Long uploadedById = toLong(payload.get("uploadedById"));
            ChatbotData.Scope scope = parseScope(payload.get("scope") == null ? null : String.valueOf(payload.get("scope")));
            ChatbotData saved = chatbotDataService.saveAdminResponse(question, answer, tags, scope, academyId, sportId, replaceEntryId, uploadedById);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Chatbot response saved successfully");
            response.put("entry", toDTO(saved, request));
            response.put("recentEntries", chatbotDataService.getRecentEntries(8, academyId, sportId));
            return ResponseEntity.status(HttpStatus.CREATED) .body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", "Failed to save chatbot response: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to save chatbot response: " + e.getMessage()));
        }
    }
    private ChatbotDataDTO toDTO(ChatbotData d, HttpServletRequest request) {
        String rel = d.getFilePath();
        String abs = rel == null || rel.isBlank() ? null : MediaUrlUtil.toAbsolute(request, rel);
        return new ChatbotDataDTO(d.getId(), d.getQuestion(), d.getAnswer(), d.getTags(), d.getScope() != null ? d.getScope() .name() : null, d.getAcademy() != null ? d.getAcademy() .getId() : null, d.getSport() != null ? d.getSport() .getId() : null, d.getSourceType() != null ? d.getSourceType() .name() : null, d.getUploadedBy() != null ? d.getUploadedBy() .getId() : null, rel, abs);
    }
    private ChatbotData.Scope parseScope(String scope) {
        if (scope == null || scope.isBlank()) return null;
        return ChatbotData.Scope.valueOf(scope.trim() .toUpperCase(Locale.ROOT));
    }
    private ChatbotData.SourceType parseSourceType(String sourceType) {
        if (sourceType == null || sourceType.isBlank()) return null;
        return ChatbotData.SourceType.valueOf(sourceType.trim() .toUpperCase(Locale.ROOT));
    }
    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}
