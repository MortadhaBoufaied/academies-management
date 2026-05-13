package com.footballacademy.services.chat;

import com.footballacademy.model.Conversation;
import com.footballacademy.model.Division;
import com.footballacademy.model.User;
import com.footballacademy.repository.ConversationRepository;
import com.footballacademy.repository.DivisionRepository;
import com.footballacademy.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public
class ChatRoomService {
    private final ConversationRepository conversationRepository;
    private final DivisionRepository divisionRepository;
    private final UserRepository userRepository;
    public ChatRoomService(ConversationRepository conversationRepository, DivisionRepository divisionRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.divisionRepository = divisionRepository;
        this.userRepository = userRepository;
    }
    public Long ensureDivisionGroup(Long divisionId, Long requesterId) {
        if (divisionId == null || divisionId <= 0) return null;
        Conversation conv = conversationRepository .findFirstByTypeAndDivisionId(Conversation.ConversationType.DIVISION, divisionId) .orElse(null);
        if (conv == null) {
            Division d = divisionRepository.findById(divisionId) .orElse(null);
            String title = d != null ?("Group chat - " + d.getNom()) :("Group chat (Division " + divisionId + ")");
            conv = Conversation.createDivisionGroup(divisionId, title);
            if (d != null) {
                conv.setAcademy(d.getAcademy());
            }
        } else
        if (conv.getAcademy() == null) {
            divisionRepository.findById(divisionId) .map(Division::getAcademy) .ifPresent(conv::setAcademy);
        }
        if (requesterId != null) {
            List<Long> ids = conv.getParticipantIds();
            if (ids == null) ids = new ArrayList<>();
            if (!ids.contains(requesterId)) ids.add(requesterId);
            conv.setParticipantIds(ids);
        } conv = conversationRepository.save(conv);
        return conv.getId();
    }
    public Long ensureDivisionGroup(Long divisionId) {
        return ensureDivisionGroup(divisionId, null);
    }
    public Long ensureDirectConversation(Long userA, Long userB) {
        if (userA == null || userB == null) return null;
        Long a = Math.min(userA, userB);
        Long b = Math.max(userA, userB);
        Conversation existing = conversationRepository.findFirstDirectBetween(a, b) .orElse(null);
        if (existing != null) return existing.getId();
        Conversation conv = Conversation.createDirect(a, b);
        User ua = userRepository.findById(a) .orElse(null);
        User ub = userRepository.findById(b) .orElse(null);
        if (ua != null && ub != null && ua.getAcademy() != null && ub.getAcademy() != null) {
            if (!ua.getAcademy() .getId() .equals(ub.getAcademy() .getId())) {
                throw new AccessDeniedException("Cannot create chat across academies");
            } conv.setAcademy(ua.getAcademy());
        } conv = conversationRepository.save(conv);
        return conv.getId();
    }
    public Long ensureAcademyAdminConversation(Long requesterId, Long adminId, Long academyId) {
        if (requesterId == null || adminId == null) return null;
        Long a = Math.min(requesterId, adminId);
        Long b = Math.max(requesterId, adminId);
        Conversation existing = conversationRepository.findFirstDirectBetween(a, b).orElse(null);
        if (existing != null) return existing.getId();
        Conversation conv = Conversation.createDirect(a, b);
        if (academyId != null) {
            userRepository.findById(adminId)
                    .map(User::getAcademy)
                    .filter(academy -> academy.getId().equals(academyId))
                    .ifPresent(conv::setAcademy);
        }
        conv = conversationRepository.save(conv);
        return conv.getId();
    }
}
