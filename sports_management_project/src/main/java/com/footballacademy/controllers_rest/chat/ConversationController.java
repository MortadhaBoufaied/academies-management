package com.footballacademy.controllers_rest.chat;

import com.footballacademy.DTO.chat.*;
import com.footballacademy.chat.jms.ChatJmsProducer;
import com.footballacademy.chat.jms.ChatMessageEvent;
import com.footballacademy.model.*;
import com.footballacademy.repository.*;
import com.footballacademy.services.chat.ChatAccessService;
import com.footballacademy.services.chat.ChatRoomService;
import com.footballacademy.services.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public
class ConversationController {
    private final NotificationService notificationService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final UserRepository userRepository;
    private final DivisionRepository divisionRepository;
    private final PlayerRepository playerRepository;
    private final TrainerRepository trainerRepository;
    private final ChatRoomService chatRoomService;
    private final ChatJmsProducer producer;
    private final ChatAccessService access;
    public ConversationController(NotificationService notificationService, ConversationRepository conversationRepository, MessageRepository messageRepository, MessageReadRepository messageReadRepository, UserRepository userRepository, DivisionRepository divisionRepository, PlayerRepository playerRepository, TrainerRepository trainerRepository, ChatRoomService chatRoomService, ChatJmsProducer producer, ChatAccessService access) {
        this.notificationService = notificationService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messageReadRepository = messageReadRepository;
        this.userRepository = userRepository;
        this.divisionRepository = divisionRepository;
        this.playerRepository = playerRepository;
        this.trainerRepository = trainerRepository;
        this.chatRoomService = chatRoomService;
        this.producer = producer;
        this.access = access;
    }
    /** Resolve current domain User from Spring Security (email = Authentication#getName) */
    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext() .getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()) .orElse(null);
    }
    // ---------------------------------------------------------------------     // Conversations list - ordered by last message timestamp (DESC),     // fallback to conversation.updatedAt when no messages exist.     // ---------------------------------------------------------------------
    @GetMapping("/conversations")
    public ResponseEntity<?> conversations() {
        User me = currentUser();
        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Unauthorized"));
        List<Conversation> convs = conversationRepository.findByParticipantIdFetchParticipants(me.getId());
        List<ConversationSummaryDTO> out = new ArrayList<>();
        for (Conversation c : convs) {
            if (!canUseConversation(me, c)) continue;
            Message last = messageRepository.findTopByConversationIdOrderByTimestampDesc(c.getId());
            long unread = (c.getType() == Conversation.ConversationType.DIRECT) ? messageRepository.countByConversationIdAndReceiverIdAndReadFalse(c.getId(), me.getId()) : messageReadRepository.countUnreadForUser(c.getId(), me.getId());
            List<ParticipantDTO> participants = new ArrayList<>();
            if (c.getParticipantIds() != null) {
                for (Long pid : c.getParticipantIds()) {
                    userRepository.findById(pid) .ifPresentOrElse(u -> participants.add(new ParticipantDTO(u.getId(), u.getNom(), u.getEmail())),() -> participants.add(new ParticipantDTO(pid, "User #" + pid, null)));
                }
            }
            out.add(new ConversationSummaryDTO(c.getId(), c.getTitle(), participants, last != null ? last.getContent() : "", last != null ? last.getTimestamp() : null, unread, c.getUpdatedAt()));
        }
        // Sort by lastMessageAt DESC, fallback to updatedAt DESC
        out.sort((a, b) -> {
            var ta = a.lastMessageAt() != null ? a.lastMessageAt() : a.updatedAt();
            var tb = b.lastMessageAt() != null ? b.lastMessageAt() : b.updatedAt();
            long la = toMillis(ta);
            long lb = toMillis(tb);
            return Long.compare(lb, la);
        });
        return ResponseEntity.ok(out);
    }
    private static long toMillis(LocalDateTime ldt) {
        if (ldt == null) return 0L;
        return ldt.atZone(ZoneId.systemDefault()) .toInstant() .toEpochMilli();
    }
    // ---------------------------------------------------------------------     // Create / ensure direct conversation     // ---------------------------------------------------------------------
    @PostMapping("/conversations/direct")
    public ResponseEntity<?> direct(
    @RequestBody SendDirectConversationRequest req) {
        User me = currentUser();
        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Unauthorized"));
        if (req == null || req.otherUserId() == null) {
            return ResponseEntity.badRequest() .body(Map.of("error", "otherUserId is required"));
        }
        if (!access.canContact(me, req.otherUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("error", "Forbidden"));
        } Optional<Conversation> existing = conversationRepository.findDirectBetween(me.getId(), req.otherUserId());
        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of("conversationId", existing.get() .getId()));
        } Long convId = chatRoomService.ensureDirectConversation(me.getId(), req.otherUserId());
        return ResponseEntity.status(HttpStatus.CREATED) .body(Map.of("conversationId", convId));
    }
    // ---------------------------------------------------------------------     // Create / ensure division group conversation     // ---------------------------------------------------------------------
    @PostMapping("/conversations/division/{divisionId}")
    public ResponseEntity<?> ensureDivision(
    @PathVariable Long divisionId) {
        User me = currentUser();
        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Unauthorized"));
        Division division = divisionRepository.findById(divisionId) .orElse(null);
        if (division == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) .body(Map.of("error", "Division not found"));
        }
        if (!access.canJoinDivisionGroup(me, divisionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("error", "Forbidden"));
        } Conversation c = conversationRepository.findDivisionGroup(divisionId) .orElse(null);
        if (c == null) {
            String title = "Division: " + division.getNom();
            c = Conversation.createDivisionGroup(divisionId, title);
            c.setAcademy(division.getAcademy());
            Set<Long> participants = new LinkedHashSet<>();
            playerRepository.findByDivisionId(divisionId) .forEach(p -> participants.add(p.getId()));
            trainerRepository.findCoachingDivisionWithUser(divisionId) .forEach(t -> participants.add(t.getId()));
            participants.add(me.getId());
            c.setParticipantIds(new ArrayList<>(participants));
            c.setUpdatedAt(LocalDateTime.now());
            conversationRepository.save(c);
            return ResponseEntity.status(HttpStatus.CREATED) .body(Map.of("conversationId", c.getId()));
        } boolean changed = false;
        if (c.getAcademy() == null) {
            c.setAcademy(division.getAcademy());
            changed = true;
        }
        if (c.getParticipantIds() == null) c.setParticipantIds(new ArrayList<>());
        if (!c.getParticipantIds() .contains(me.getId())) {
            c.getParticipantIds() .add(me.getId());
            c.setUpdatedAt(LocalDateTime.now());
            changed = true;
        }
        if (changed) {
            conversationRepository.save(c);
        } return ResponseEntity.ok(Map.of("conversationId", c.getId()));
    }
    // ---------------------------------------------------------------------     // Fetch messages of a conversation     // ---------------------------------------------------------------------
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> messages(
    @PathVariable Long conversationId) {
        User me = currentUser();
        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Unauthorized"));
        Conversation c = conversationRepository.findByIdFetchParticipants(conversationId) .orElse(null);
        if (!canUseConversation(me, c)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("error", "Forbidden"));
        } List<Message> msgs =(c.getType() == Conversation.ConversationType.DIRECT) ? messageRepository.findByConversationIdOrderByTimestampAsc(conversationId) : messageRepository.findByConversationIdAndReceiverIdIsNullOrderByTimestampAsc(conversationId);
        List<ChatMessageDTO> out = new ArrayList<>();
        for (Message m : msgs) {
            out.add(new ChatMessageDTO(m.getId(), m.getConversationId(), m.getSenderId(), m.getReceiverId(), m.getContent(), m.getTimestamp(), m.isRead(), m.getClientTempId()));
        }
        return ResponseEntity.ok(out);
    }
    // -----  Send message (enqueue to JMS) -----
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> send(
    @PathVariable Long conversationId,
    @RequestBody SendMessageRequest req) {
        User me = currentUser();
        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Unauthorized"));
        if (req == null || req.content() == null || req.content() .trim() .isEmpty()) {
            return ResponseEntity.badRequest() .body(Map.of("error", "content is required"));
        }
        Conversation c = conversationRepository.findByIdFetchParticipants(conversationId) .orElse(null);
        if (!canUseConversation(me, c)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("error", "Forbidden"));
        }
        Long receiverId = (c.getType() == Conversation.ConversationType.DIRECT) ? c.getParticipantIds() .stream() .filter(id -> !id.equals(me.getId())) .findFirst() .orElse(null) : null;
        String tmpId =(req.clientTempId() != null && !req.clientTempId() .isBlank()) ? req.clientTempId() : UUID.randomUUID() .toString();
        producer.enqueue(new ChatMessageEvent(conversationId, me.getId(), receiverId, req.content() .trim(), tmpId));
        return ResponseEntity.status(HttpStatus.ACCEPTED) .body(new SendMessageAck(true, tmpId));
    }
    // ---------------------------------------------------------------------     // Mark conversation as read - idempotent (no duplicate key errors)     // ---------------------------------------------------------------------
    @PutMapping("/conversations/{conversationId}/read")
    @Transactional
    public ResponseEntity<?> markRead(
    @PathVariable Long conversationId) {
        User me = currentUser();
        if (me == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED) .body(Map.of("error", "Unauthorized"));
        Conversation c = conversationRepository.findByIdFetchParticipants(conversationId) .orElse(null);
        if (!canUseConversation(me, c)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("error", "Forbidden"));
        }
        if (c.getType() == Conversation.ConversationType.DIRECT) {
            // Fast, bulk update via JPQL
            messageRepository.markDirectAsRead(conversationId, me.getId());
        } else {
            // GROUP/DIVISION: UPSERT per message to avoid duplicates
            List<Message> msgs = messageRepository.findByConversationIdAndReceiverIdIsNullOrderByTimestampAsc(conversationId);
            LocalDateTime now = LocalDateTime.now();
            for (Message m : msgs) {
                messageReadRepository.upsert(m.getId(), me.getId(), now);
                // Portable fallback (no native SQL)
                if (!messageReadRepository.existsByMessageIdAndUserId(m.getId(), me.getId())) {
                    messageReadRepository.save(new MessageRead(m.getId(), me.getId(), now));
                }
                // ---------------------------------------------------
            }
        }
        notificationService.markMessageNotificationsAsReadForConversation(conversationId, me.getId());
        return ResponseEntity.ok(Map.of("conversationId", conversationId));
    }
    private boolean canUseConversation(User me, Conversation c) {
        if (me == null || c == null || c.getParticipantIds() == null || me.getId() == null) return false;
        if (!c.getParticipantIds() .contains(me.getId())) return false;
        if (c.getType() == Conversation.ConversationType.DIRECT) {
            Long otherUserId = c.getParticipantIds() .stream() .filter(pid -> pid != null && !pid.equals(me.getId())) .findFirst() .orElse(null);
            return otherUserId != null && access.canContact(me, otherUserId);
        }
        if (c.getType() == Conversation.ConversationType.DIVISION) {
            return c.getDivisionId() != null && access.canJoinDivisionGroup(me, c.getDivisionId());
        }
        return true;
    }
}
