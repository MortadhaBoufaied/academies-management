package com.footballacademy.services;

import com.footballacademy.DTO.SendNotificationRequest;
import com.footballacademy.DTO.notification.NotificationCampaignStatsDto;
import com.footballacademy.DTO.notification.NotificationViewDto;
import com.footballacademy.model.Academy;
import com.footballacademy.model.Notification;
import com.footballacademy.model.NotificationCampaign;
import com.footballacademy.model.Parent;
import com.footballacademy.model.Player;
import com.footballacademy.model.User;
import com.footballacademy.repository.AcademyRepository;
import com.footballacademy.repository.NotificationCampaignRepository;
import com.footballacademy.repository.NotificationRepository;
import com.footballacademy.repository.ParentRepository;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public
class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.
    class);
    private final NotificationRepository notificationRepository;
    private final NotificationCampaignRepository notificationCampaignRepository;
    private final UserRepository userRepository;
    private final ParentRepository parentRepository;
    private final PlayerRepository playerRepository;
    private final AcademyRepository academyRepository;
    private final AcademyAccessService academyAccessService;
    private final SimpMessageSendingOperations messaging;
    public NotificationService(NotificationRepository notificationRepository, NotificationCampaignRepository notificationCampaignRepository, UserRepository userRepository, ParentRepository parentRepository, PlayerRepository playerRepository, AcademyRepository academyRepository, AcademyAccessService academyAccessService, SimpMessageSendingOperations messaging) {
        this.notificationRepository = notificationRepository;
        this.notificationCampaignRepository = notificationCampaignRepository;
        this.userRepository = userRepository;
        this.parentRepository = parentRepository;
        this.playerRepository = playerRepository;
        this.academyRepository = academyRepository;
        this.academyAccessService = academyAccessService;
        this.messaging = messaging;
    }
    @Transactional(rollbackFor = Exception.
    class)
    public List<Notification> sendNotification(SendNotificationRequest request, Long adminId) {
        validateRequest(request, adminId);
        Long targetAcademyId = resolveTargetAcademyId(request);
        List<Long> targetUserIds;
        if (Boolean.TRUE.equals(request.getSendToAll())) {
            targetUserIds = getUserIdsByCategory(request.getCategory(), targetAcademyId);
        } else {
            targetUserIds = filterUserIdsByAcademy(request.getUserIds(), targetAcademyId);
        }
        if (targetUserIds.isEmpty()) {
            logger.info("No recipients resolved for notification request '{}'", request.getTitle());
            return Collections.emptyList();
        } NotificationCampaign campaign = createCampaign(request.getTitle(), request.getContent(), request.getContent(), request.getCategory(), Boolean.TRUE.equals(request.getSendToAll()) ? "CATEGORY_ALL" : "USER_LIST", audienceSummaryFor(request.getCategory(), targetUserIds.size()), adminId, targetAcademyId);
        return createNotificationsForCampaign(campaign, targetUserIds);
    }
    public List<NotificationViewDto> getNotificationViewsForUser(Long userId) {
        return toViewDtos(getNotifications(userId));
    }
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId) .stream() .filter(this::isNotificationVisible) .toList();
    }
    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId) .stream() .filter(this::isNotificationVisible) .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())) .reversed()) .toList();
    }
    @Transactional(rollbackFor = Exception.
    class)
    public int markAllAsRead(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        } Long currentUserId = academyAccessService.currentUserId();
        if (!academyAccessService.isSuperAdmin() && currentUserId != null && !currentUserId.equals(userId)) {
            throw new AccessDeniedException("You can only mark your own notifications as read");
        } List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId) .stream() .filter(this::isNotificationVisible) .toList();
        if (unreadNotifications.isEmpty()) {
            return 0;
        } LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(now);
        });
        notificationRepository.saveAll(unreadNotifications);
        publishUnreadCount(userId);
        unreadNotifications.stream() .map(Notification::getCampaignId) .filter(Objects::nonNull) .distinct() .forEach(this::publishCampaignStats);
        return unreadNotifications.size();
    }
    public long getUnreadCount(Long userId) {
        return getUnreadNotifications(userId) .size();
    }
    public long countUnreadNotifications(Long userId) {
        return getUnreadCount(userId);
    }
    public List<Notification> getNotificationsForUser(Long userId) {
        return getNotifications(userId);
    }
    public List<User> searchUsersInCategory(Notification.Category category, String searchTerm) {
        Long academyId = academyAccessService.isSuperAdmin() ? null : academyAccessService.currentAcademyId();
        List<Long> userIds = getUserIdsByCategory(category, academyId);
        String normalized = searchTerm == null ? "" : searchTerm.trim() .toLowerCase();
        return userRepository.findAll() .stream() .filter(user -> userIds.contains(user.getId())) .filter(user -> normalized.isBlank() ||(user.getEmail() != null && user.getEmail() .toLowerCase() .contains(normalized)) ||(user.getNom() != null && user.getNom() .toLowerCase() .contains(normalized))) .toList();
    }
    @Transactional(rollbackFor = Exception.
    class)
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId) .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        assertCanMutateNotification(notification);
        notificationRepository.delete(notification);
        if (notification.getUserId() != null) {
            publishUnreadCount(notification.getUserId());
        }
        if (notification.getCampaignId() != null) {
            publishCampaignStats(notification.getCampaignId());
        }
    }
    public Notification saveNotification(Notification notification) {
        normalizeNotification(notification);
        Notification saved = notificationRepository.save(notification);
        publishNotification(saved);
        if (saved.getUserId() != null) {
            publishUnreadCount(saved.getUserId());
        }
        if (saved.getCampaignId() != null) {
            publishCampaignStats(saved.getCampaignId());
        } return saved;
    }
    public Notification addNotification(String title, String content, Long userId) {
        Notification notification = new Notification(title, content, Notification.Category.GENERAL, userId, 1L);
        notification.setContentHtml(content);
        assignNotificationAcademyFromUser(notification, userId);
        return saveNotification(notification);
    }
    @Transactional(rollbackFor = Exception.
    class)
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId) .orElse(null);
        if (notification == null) {
            return null;
        } assertCanMutateNotification(notification);
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
            if (notification.getUserId() != null) {
                publishUnreadCount(notification.getUserId());
            }
            if (notification.getCampaignId() != null) {
                publishCampaignStats(notification.getCampaignId());
            }
        } return notification;
    }
    public Notification addMessageNotification(Long userId, Long conversationId, Long senderId, String preview) {
        String senderName = userRepository.findById(senderId) .map(user -> user.getNom() != null && !user.getNom() .isBlank() ? user.getNom() : user.getEmail()) .orElse("User #" + senderId);
        Notification notification = new Notification("New message from " + senderName, preview, Notification.Category.GENERAL, userId, senderId);
        notification.setContentHtml(preview);
        notification.setConversationId(conversationId);
        assignNotificationAcademyFromUser(notification, userId);
        return saveNotification(notification);
    }
    @Transactional(rollbackFor = Exception.
    class)
    public void markMessageNotificationsAsReadForConversation(Long conversationId, Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByConversationIdAndUserId(conversationId, userId) .stream() .filter(notification -> !notification.isRead()) .toList();
        if (unreadNotifications.isEmpty()) {
            return;
        } LocalDateTime now = LocalDateTime.now();
        unreadNotifications.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(now);
        });
        notificationRepository.saveAll(unreadNotifications);
        publishUnreadCount(userId);
    }
    @Transactional(readOnly = true)
    public List<NotificationCampaignStatsDto> getCampaigns(boolean mineOnly) {
        List<NotificationCampaign> campaigns;
        if (mineOnly) {
            Long currentUserId = academyAccessService.currentUserId();
            if (currentUserId == null) {
                return List.of();
            } campaigns = notificationCampaignRepository.findByCreatedByOrderByCreatedAtDesc(currentUserId);
        } else
        if (academyAccessService.isSuperAdmin()) {
            campaigns = notificationCampaignRepository.findAllByOrderByCreatedAtDesc();
        } else {
            campaigns = notificationCampaignRepository.findByAcademy_IdOrderByCreatedAtDesc(academyAccessService.currentAcademyOrThrow() .getId());
        } return campaigns.stream() .filter(this::isCampaignVisible) .map(this::buildCampaignStats) .toList();
    }
    @Transactional(readOnly = true)
    public NotificationCampaignStatsDto getCampaignStats(Long campaignId) {
        NotificationCampaign campaign = notificationCampaignRepository.findById(campaignId) .orElseThrow(() -> new IllegalArgumentException("Notification campaign not found: " + campaignId));
        assertCampaignVisible(campaign);
        return buildCampaignStats(campaign);
    }
    @Transactional(rollbackFor = Exception.
    class)
    public NotificationCampaignStatsDto sendTargetedCampaign(String title, String contentHtml, String targetingMode, String audienceSummary, Notification.Category category, Long targetAcademyId, Collection<Long> recipientIds, Long actorId) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            throw new IllegalArgumentException("At least one recipient is required");
        } String plainContent = stripHtml(contentHtml);
        NotificationCampaign campaign = createCampaign(title, plainContent, contentHtml, category, targetingMode, audienceSummary, actorId, targetAcademyId);
        createNotificationsForCampaign(campaign, recipientIds);
        return buildCampaignStats(campaign);
    }
    private NotificationCampaign createCampaign(String title, String plainContent, String contentHtml, Notification.Category category, String targetingMode, String audienceSummary, Long actorId, Long targetAcademyId) {
        NotificationCampaign campaign = new NotificationCampaign();
        campaign.setTitle(title);
        campaign.setContent(plainContent);
        campaign.setContentHtml(contentHtml);
        campaign.setCategory(category == null ? Notification.Category.GENERAL : category);
        campaign.setTargetingMode(targetingMode == null || targetingMode.isBlank() ? "GENERAL" : targetingMode);
        campaign.setAudienceSummary(audienceSummary);
        campaign.setCreatedBy(actorId != null ? actorId : 1L);
        if (targetAcademyId != null) {
            campaign.setAcademy(academyRepository.findById(targetAcademyId) .orElseThrow(() -> new IllegalArgumentException("Academy not found: " + targetAcademyId)));
        } else
        if (!academyAccessService.isSuperAdmin()) {
            campaign.setAcademy(academyAccessService.currentAcademyOrThrow());
        } return notificationCampaignRepository.save(campaign);
    }
    private List<Notification> createNotificationsForCampaign(NotificationCampaign campaign, Collection<Long> recipientIds) {
        List<Long> normalizedRecipientIds = recipientIds.stream() .filter(Objects::nonNull) .filter(id -> id > 0) .distinct() .toList();
        if (normalizedRecipientIds.isEmpty()) {
            return List.of();
        } Academy academy = campaign.getAcademy();
        List<Notification> notifications = new ArrayList<>();
        for (Long userId : normalizedRecipientIds) {
            Optional<User> targetUser = userRepository.findById(userId);
            if (targetUser.isEmpty()) {
                continue;
            } Notification notification = new Notification(campaign.getTitle(), campaign.getContent(), campaign.getCategory(), userId, campaign.getCreatedBy());
            notification.setCampaignId(campaign.getId());
            notification.setContentHtml(campaign.getContentHtml());
            notification.setAcademy(academy != null ? academy : targetUser.get() .getAcademy());
            notifications.add(notification);
        } List<Notification> saved = notificationRepository.saveAll(notifications);
        saved.forEach(this::publishNotification);
        normalizedRecipientIds.forEach(this::publishUnreadCount);
        publishCampaignStats(campaign.getId());
        return saved;
    }
    private void validateRequest(SendNotificationRequest request, Long adminId) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (adminId == null) {
            throw new IllegalArgumentException("Admin ID cannot be null");
        }
        if (request.getTitle() == null || request.getTitle() .trim() .isEmpty()) {
            throw new IllegalArgumentException("Notification title cannot be empty");
        }
        if (request.getContent() == null || request.getContent() .trim() .isEmpty()) {
            throw new IllegalArgumentException("Notification content cannot be empty");
        }
    }
    private List<Long> getUserIdsByCategory(Notification.Category category, Long academyId) {
        if (category == null) {
            return List.of();
        } return
        switch (category) {
            case PARENTS ->(academyId == null ? parentRepository.findAll() : parentRepository.findByAcademy_Id(academyId)) .stream() .map(Parent::getUser) .filter(Objects::nonNull) .map(User::getId) .filter(Objects::nonNull) .toList();
            case FOOTBALLERS ->(academyId == null ? playerRepository.findAll() : playerRepository.findByAcademy_Id(academyId)) .stream() .map(Player::getUser) .filter(Objects::nonNull) .map(User::getId) .filter(Objects::nonNull) .toList();
            case TRAINERS -> usersByRoleAndAcademy(User.UserRole.TRAINER, academyId) .stream() .map(User::getId) .toList();
            case ADMIN -> usersByRoleAndAcademy(User.UserRole.ADMIN, academyId) .stream() .map(User::getId) .toList();
            case GENERAL ->(academyId == null ? userRepository.findAll() : userRepository.findByAcademy_Id(academyId)) .stream() .map(User::getId) .filter(Objects::nonNull) .toList();
        };
    }
    private Long resolveTargetAcademyId(SendNotificationRequest request) {
        if (academyAccessService.isSuperAdmin()) {
            if (Boolean.TRUE.equals(request.getGlobal())) {
                return null;
            }
            if (request.getAcademyId() != null) {
                academyAccessService.assertCanAccessAcademy(request.getAcademyId());
                return request.getAcademyId();
            } return null;
        } return academyAccessService.currentAcademyOrThrow() .getId();
    }
    private List<User> usersByRoleAndAcademy(User.UserRole role, Long academyId) {
        return academyId == null ? userRepository.findByMainRole(role) : userRepository.findByAcademy_IdAndMainRole(academyId, role);
    }
    private List<Long> filterUserIdsByAcademy(List<Long> userIds, Long academyId) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        } return userIds.stream() .filter(Objects::nonNull) .distinct() .filter(userId -> userRepository.findById(userId) .map(user -> academyId == null ||(user.getAcademy() != null && academyId.equals(user.getAcademy() .getId()))) .orElse(false)) .toList();
    }
    private void assignNotificationAcademyFromUser(Notification notification, Long userId) {
        if (notification == null || userId == null) {
            return;
        } userRepository.findById(userId) .map(User::getAcademy) .ifPresent(notification::setAcademy);
    }
    private boolean isNotificationVisible(Notification notification) {
        return notification != null &&(academyAccessService.isSuperAdmin() || academyAccessService.canAccessAcademy(notification.getAcademy()));
    }
    private boolean isCampaignVisible(NotificationCampaign campaign) {
        return campaign != null &&(academyAccessService.isSuperAdmin() || academyAccessService.canAccessAcademy(campaign.getAcademy()));
    }
    private void assertCampaignVisible(NotificationCampaign campaign) {
        if (!isCampaignVisible(campaign)) {
            throw new AccessDeniedException("You cannot access another academy's notification campaign");
        }
    }
    private void assertCanMutateNotification(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }
        if (academyAccessService.isSuperAdmin()) {
            return;
        } Long currentUserId = academyAccessService.currentUserId();
        if (currentUserId != null && currentUserId.equals(notification.getUserId())) {
            return;
        } throw new AccessDeniedException("You can only update your own notifications");
    }
    private void normalizeNotification(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }
        if (notification.getContentHtml() == null || notification.getContentHtml() .isBlank()) {
            notification.setContentHtml(notification.getContent());
        }
        if ((notification.getContent() == null || notification.getContent() .isBlank()) && notification.getContentHtml() != null) {
            notification.setContent(stripHtml(notification.getContentHtml()));
        }
        if (notification.getAcademy() == null && !academyAccessService.isSuperAdmin()) {
            notification.setAcademy(academyAccessService.currentAcademyOrThrow());
        }
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }
        if (notification.getCategory() == null) {
            notification.setCategory(Notification.Category.GENERAL);
        }
    }
    private List<NotificationViewDto> toViewDtos(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return List.of();
        } Set<Long> senderIds = notifications.stream() .map(Notification::getCreatedBy) .filter(Objects::nonNull) .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, String> senderNames = userRepository.findAllById(senderIds) .stream() .collect(Collectors.toMap(User::getId, user -> user.getNom() != null && !user.getNom() .isBlank() ? user.getNom() : user.getEmail()));
        return notifications.stream() .map(notification -> NotificationViewDto.from(notification, senderNames.getOrDefault(notification.getCreatedBy(), "System"))) .toList();
    }
    private NotificationCampaignStatsDto buildCampaignStats(NotificationCampaign campaign) {
        long totalRecipients = notificationRepository.countByCampaignId(campaign.getId());
        long readCount = notificationRepository.countByCampaignIdAndIsReadTrue(campaign.getId());
        long unreadCount = Math.max(0, totalRecipients - readCount);
        double readPercentage = totalRecipients == 0 ? 0.0 :(readCount * 100.0) / totalRecipients;
        String senderName = userRepository.findById(campaign.getCreatedBy()) .map(user -> user.getNom() != null && !user.getNom() .isBlank() ? user.getNom() : user.getEmail()) .orElse("System");
        return new NotificationCampaignStatsDto(campaign.getId(), campaign.getAcademy() != null ? campaign.getAcademy() .getId() : null, campaign.getCreatedBy(), senderName, campaign.getTitle(), buildPreview(campaign.getContent()), campaign.getTargetingMode(), campaign.getAudienceSummary(), campaign.getCategory() != null ? campaign.getCategory() .name() : null,(int) totalRecipients,(int) readCount,(int) unreadCount, Math.round(readPercentage * 10.0) / 10.0, campaign.getCreatedAt());
    }
    private String audienceSummaryFor(Notification.Category category, int totalRecipients) {
        String base = category == null ? "GENERAL" : category.name();
        return totalRecipients + " recipients Ã‚Â· " + base;
    }
    private void publishNotification(Notification notification) {
        if (notification == null || notification.getUserId() == null) {
            return;
        } String senderName = userRepository.findById(notification.getCreatedBy()) .map(user -> user.getNom() != null && !user.getNom() .isBlank() ? user.getNom() : user.getEmail()) .orElse("System");
        NotificationViewDto dto = NotificationViewDto.from(notification, senderName);
        messaging.convertAndSend("/topic/notifications/" + notification.getUserId(), dto);
    }
    private void publishUnreadCount(Long userId) {
        if (userId == null) {
            return;
        } messaging.convertAndSend("/topic/notification-count/" + userId, Map.of("userId", userId, "unreadCount", getUnreadCount(userId)));
    }
    private void publishCampaignStats(Long campaignId) {
        if (campaignId == null) {
            return;
        } notificationCampaignRepository.findById(campaignId) .ifPresent(campaign -> {
            NotificationCampaignStatsDto stats = buildCampaignStats(campaign);
            messaging.convertAndSend("/topic/notification-stats/" + campaign.getCreatedBy(), stats);
        });
    }
    private String buildPreview(String value) {
        if (value == null || value.isBlank()) {
            return "";
        } String normalized = value.replaceAll("\\s+", " ") .trim();
        return normalized.length() > 140 ? normalized.substring(0, 140) + "..." : normalized;
    }
    private String stripHtml(String raw) {
        if (raw == null) {
            return "";
        } return raw.replaceAll("(?is)<br\\s*/?>", "\n") .replaceAll("(?is)<[^>]*>", " ") .replace("Â ", " ") .replace("&", "&") .replace("<", "<") .replace(">", ">") .replaceAll("[\\t\\f\\r ]+", " ") .replaceAll("\\n{3,}", "\n\n") .trim();
    }
}
