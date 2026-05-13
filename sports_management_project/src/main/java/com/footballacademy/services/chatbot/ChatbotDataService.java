package com.footballacademy.services.chatbot;

import com.footballacademy.model.ChatbotData;
import com.footballacademy.repository.AcademyRepository;
import com.footballacademy.repository.ChatbotDataRepository;
import com.footballacademy.repository.SportRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatbotDataService {

    private final ChatbotDataRepository repository;
    private final AcademyRepository academyRepository;
    private final SportRepository sportRepository;
    private final UserRepository userRepository;
    private final AcademyAccessService academyAccessService;

    /** Physical storage for chatbot-related files */
    private final Path storageDir =
            Paths.get("src/main/resources/Files/chatbotFiles")
                    .toAbsolutePath()
                    .normalize();

    /** Web path exposed by static resource config */
    private static final String KB_WEB_PATH = "/chatbotFiles/knowledge_base.csv";
    private static final String KB_FILE_NAME = "knowledge_base.csv";

    public ChatbotDataService(
            ChatbotDataRepository repository,
            AcademyRepository academyRepository,
            SportRepository sportRepository,
            UserRepository userRepository,
            AcademyAccessService academyAccessService
    ) {
        this.repository = repository;
        this.academyRepository = academyRepository;
        this.sportRepository = sportRepository;
        this.userRepository = userRepository;
        this.academyAccessService = academyAccessService;

        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create chatbot storage directory", e);
        }
    }

    /* =====================================================
       Knowledge base helpers
       ===================================================== */

    public Path knowledgeBasePath() {
        return storageDir.resolve(KB_FILE_NAME).normalize();
    }

    public String knowledgeBaseWebPath() {
        return KB_WEB_PATH;
    }

    /* =====================================================
       Data CRUD
       ===================================================== */

    public List<ChatbotData> getAllData() {
        return repository.findAll();
    }

    public ChatbotData getDataById(Long id) {
        ChatbotData data = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chatbot data not found"));
        assertVisible(data);
        return data;
    }

    public ChatbotData uploadData(ChatbotData data) {
        if (data == null) throw new RuntimeException("Data cannot be null");
        if (data.getQuestion() == null || data.getQuestion().isBlank())
            throw new RuntimeException("Question is required");
        if (data.getAnswer() == null || data.getAnswer().isBlank())
            throw new RuntimeException("Answer is required");

        if (data.getFilePath() == null || data.getFilePath().isBlank()) {
            data.setFilePath(KB_WEB_PATH);
        }
        if (data.getFileName() == null || data.getFileName().isBlank()) {
            data.setFileName(KB_FILE_NAME);
        }
        if (data.getScope() == null) {
            data.setScope(ChatbotData.Scope.GLOBAL);
        }
        if (data.getSourceType() == null) {
            data.setSourceType(ChatbotData.SourceType.MANUAL);
        }

        enforceWriteScope(data);
        return repository.save(data);
    }

    public void deleteData(Long id) {
        ChatbotData existing = getDataById(id);
        if (!academyAccessService.isSuperAdmin()
                && existing.getScope() == ChatbotData.Scope.GLOBAL) {
            throw new AccessDeniedException("Cannot delete global chatbot data");
        }
        repository.deleteById(id);
    }

    /* =====================================================
       CSV ingestion
       ===================================================== */

    public void ingestCsv(MultipartFile csvFile) {
        if (csvFile == null || csvFile.isEmpty()) {
            throw new RuntimeException("CSV file is required");
        }

        try {
            Files.copy(
                    csvFile.getInputStream(),
                    knowledgeBasePath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to store knowledge base CSV", e);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Files.newInputStream(knowledgeBasePath()),
                        StandardCharsets.UTF_8
                )
        )) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] cols = line.split("[;,]", -1);
                if (cols.length < 2) continue;

                String question = cols[0].trim();
                String answer = cols[1].trim();
                if (question.isEmpty() || answer.isEmpty()) continue;

                ChatbotData row = new ChatbotData();
                row.setQuestion(question);
                row.setAnswer(answer);
                row.setFileName(KB_FILE_NAME);
                row.setFilePath(KB_WEB_PATH);
                row.setSourceType(ChatbotData.SourceType.CSV);
                row.setScope(ChatbotData.Scope.GLOBAL);

                if (cols.length >= 3 && !cols[2].isBlank()) {
                    row.setTags(cols[2].trim());
                }

                repository.save(row);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse knowledge base CSV", e);
        }
    }

    /* =====================================================
       Answering
       ===================================================== */

    public Map<String, Object> answer(String question) {
        if (question == null || question.isBlank()) {
            return Map.of("answer", "Please ask a question.");
        }

        String q = question.trim().toLowerCase(Locale.ROOT);
        List<ChatbotData> candidates = repository.findAll();

        Match best = bestMatch(q, candidates);

        if (best.data == null) {
            return Map.of(
                    "answer", "Sorry, I don't have an answer for that yet.",
                    "confidence", "LOW",
                    "found", false
            );
        }

        return Map.of(
                "answer", best.data.getAnswer(),
                "matchedQuestion", best.data.getQuestion(),
                "score", best.score,
                "confidence", confidenceFor(best.score),
                "found", true
        );
    }

    /* =====================================================
       Helpers
       ===================================================== */

    private Match bestMatch(String q, List<ChatbotData> data) {
        ChatbotData best = null;
        int bestScore = 0;

        for (ChatbotData d : data) {
            if (d.getQuestion() == null) continue;
            int score = score(q, d.getQuestion().toLowerCase(Locale.ROOT));
            if (score > bestScore) {
                bestScore = score;
                best = d;
            }
        }
        return new Match(best, bestScore);
    }

    private int score(String q, String dq) {
        if (dq.equals(q)) return 100;
        if (dq.contains(q) || q.contains(dq)) return 60;

        Set<String> a = new HashSet<>(Arrays.asList(q.split("\\s+")));
        Set<String> b = new HashSet<>(Arrays.asList(dq.split("\\s+")));
        a.retainAll(b);
        return a.isEmpty() ? 0 : a.size() * 10;
    }

    private String confidenceFor(int score) {
        if (score >= 80) return "HIGH";
        if (score >= 40) return "MEDIUM";
        return "LOW";
    }

    private void enforceWriteScope(ChatbotData data) {
        if (!academyAccessService.isSuperAdmin()) {
            data.setScope(ChatbotData.Scope.ACADEMY);
            data.setAcademy(academyAccessService.currentAcademyOrThrow());
        }
    }

    private void assertVisible(ChatbotData data) {
        if (academyAccessService.isSuperAdmin()) return;
        if (data.getScope() == ChatbotData.Scope.GLOBAL) return;
        if (!academyAccessService.canAccessAcademy(data.getAcademy())) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    // ==================== MISSING METHODS ====================
    public boolean knowledgeBaseExists() {
        return Files.exists(storageDir.resolve(KB_FILE_NAME));
    }

    public String knowledgeBaseServerPath() {
        return KB_WEB_PATH;
    }

    public List<ChatbotData> getData(ChatbotData.Scope scope, Long academyId, Long userId) {
        List<ChatbotData> result = new ArrayList<>();
        if (scope != null) {
            result.addAll(repository.findAll().stream()
                    .filter(d -> scope.equals(d.getScope()))
                    .collect(Collectors.toList()));
        } else {
            result.addAll(getAllData());
        }
        return result;
    }

    // ==================== ADDITIONAL MISSING METHODS ====================
    public String storeChatbotFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }
        ingestCsv(file);
        return KB_WEB_PATH;
    }

    public void ingestCsv(MultipartFile csvFile, ChatbotData.Scope scope, Long academyId, Long sportId, Long userId) {
        ingestCsv(csvFile);
        if (scope != null) {
            List<ChatbotData> data = repository.findAll();
            for (ChatbotData d : data) {
                d.setScope(scope);
                if (scope == ChatbotData.Scope.ACADEMY && academyId != null) {
                    d.setAcademy(academyRepository.findById(academyId).orElse(null));
                }
                repository.save(d);
            }
        }
    }

    public void applyScope(ChatbotData data, ChatbotData.Scope scope, Long academyId, Long sportId, ChatbotData.SourceType sourceType, Long userId) {
        if (data != null) {
            data.setScope(scope);
            if (scope == ChatbotData.Scope.ACADEMY && academyId != null) {
                data.setAcademy(academyRepository.findById(academyId).orElse(null));
            }
            if (sourceType != null) {
                data.setSourceType(sourceType);
            }
            repository.save(data);
        }
    }

    public void clearAllDataAndKnowledgeBaseFile() {
        repository.deleteAll();
        try {
            Files.deleteIfExists(knowledgeBasePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete knowledge base file", e);
        }
    }

    public String analyzeData(Long academyId) {
        if (academyId != null) {
            long count = repository.findAll().stream()
                    .filter(d -> d.getScope() == ChatbotData.Scope.GLOBAL || 
                               (d.getAcademy() != null && d.getAcademy().getId().equals(academyId)))
                    .count();
            return "Analyzed " + count + " knowledge base entries";
        }
        return "Analyzed " + repository.findAll().size() + " knowledge base entries";
    }

    public Map<String, Object> answer(String question, Long academyId, Long userId) {
        return answer(question);
    }

    public List<ChatbotData> getRecentEntries(int limit, Long academyId, Long userId) {
        return repository.findAll().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, Object> answerForAdmin(String question, Long academyId, Long userId) {
        return answer(question);
    }

    public ChatbotData saveAdminResponse(String question, String answer, String notes, ChatbotData.Scope scope, Long academyId, Long sportId, Long userId, Long responseId) {
        ChatbotData data = new ChatbotData();
        data.setQuestion(question);
        data.setAnswer(answer);
        data.setScope(scope);
        if (scope == ChatbotData.Scope.ACADEMY && academyId != null) {
            data.setAcademy(academyRepository.findById(academyId).orElse(null));
        }
        data.setSourceType(ChatbotData.SourceType.MANUAL);
        return repository.save(data);
    }

    private static record Match(ChatbotData data, int score) {}
}
