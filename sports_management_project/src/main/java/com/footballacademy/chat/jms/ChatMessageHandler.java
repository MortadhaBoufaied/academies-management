package com.footballacademy.chat.jms;

import com.footballacademy.DTO.chat.ChatMessageDTO;
import com.footballacademy.DTO.chat.ConversationSummaryDTO;
import com.footballacademy.DTO.chat.ParticipantDTO;
import com.footballacademy.model.Conversation;
import com.footballacademy.model.Message;
import com.footballacademy.model.User;
import com.footballacademy.repository.ConversationRepository;
import com.footballacademy.repository.MessageReadRepository;
import com.footballacademy.repository.MessageRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.NotificationService;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**  * Handles chat message persistence + websocket fanout.  *  * Supports:  *  - DIRECT: receiverId is required, Message.read is used.  *  - GROUP/DIVISION: receiverId is null, per-user unread tracked in message_reads.  */
@Component
public
class ChatMessageHandler {
    private final NotificationService notificationService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final UserRepository userRepository;
    private final SimpMessageSendingOperations messaging;
    public ChatMessageHandler(NotificationService notificationService, ConversationRepository conversationRepository, MessageRepository messageRepository, MessageReadRepository messageReadRepository, UserRepository userRepository, SimpMessageSendingOperations messaging) {
        this.notificationService = notificationService;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messageReadRepository = messageReadRepository;
        this.userRepository = userRepository;
        this.messaging = messaging;
    }
    @Transactional
    public void handle(ChatMessageEvent event) {
        Conversation conv = conversationRepository.findByIdFetchParticipants(event.conversationId()) .orElseThrow(() -> new RuntimeException("Conversation not found: " + event.conversationId()));
        // Copy participants while still inside transaction/session.
        List<Long> participantIds = new ArrayList<>(conv.getParticipantIds());
        // For group/division messages, receiverId is null.
        Long receiverId =(conv.getType() == Conversation.ConversationType.DIRECT) ? event.receiverId() : null;
        Message msg = new Message(event.conversationId(), event.senderId(), receiverId, event.content(), event.clientTempId());
        messageRepository.save(msg);
        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);
        // notifications
        String preview = msg.getContent();
        if (preview != null && preview.length() > 80) preview = preview.substring(0, 80) + "...";
        if (conv.getType() == Conversation.ConversationType.DIRECT) {
            if (receiverId != null) {
                notificationService.addMessageNotification(receiverId, conv.getId(), msg.getSenderId(), preview);
            }
        } else {
            for (Long pid : participantIds) {
                if (pid != null && !pid.equals(msg.getSenderId())) {
                    notificationService.addMessageNotification(pid, conv.getId(), msg.getSenderId(), preview);
                }
            }
        } ChatMessageDTO msgDto = new ChatMessageDTO(msg.getId(), msg.getConversationId(), msg.getSenderId(), msg.getReceiverId(), msg.getContent(), msg.getTimestamp(), msg.isRead(), msg.getClientTempId());
        // fanout message
        messaging.convertAndSend("/topic/messages/" + conv.getId(), msgDto);
        // conversation summaries to each participant
        for (Long participantId : participantIds) {
            long unread = 0;
            if (participantId != null) {
                if (conv.getType() == Conversation.ConversationType.DIRECT) {
                    unread = messageRepository.countByConversationIdAndReceiverIdAndReadFalse(conv.getId(), participantId);
                } else {
                    unread = messageReadRepository.countUnreadForUser(conv.getId(), participantId);
                }
            } List<ParticipantDTO> participants = new ArrayList<>();
            for (Long pid : participantIds) {
                User u = userRepository.findById(pid) .orElse(null);
                if (u != null) participants.add(new ParticipantDTO(u.getId(), u.getNom(), u.getEmail()));
                else participants.add(new ParticipantDTO(pid, "User #" + pid, null));
            } ConversationSummaryDTO summary = new ConversationSummaryDTO(conv.getId(), conv.getTitle(), participants, msg.getContent(), msg.getTimestamp(), unread, conv.getUpdatedAt());
            messaging.convertAndSend("/topic/conversations/" + participantId, summary);
        }
    }
}
