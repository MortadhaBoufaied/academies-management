package com.footballacademy.repository;

import com.footballacademy.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public
interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByAcademy_Id(Long academyId);
    long countByUserIdAndIsReadFalse(Long userId);
    List<Notification> findByCategory(Notification.Category category);
    List<Notification> findByAcademy_IdAndCategory(Long academyId, Notification.Category category);
    List<Notification> findByIsScheduledTrueAndScheduledForBefore(LocalDateTime dateTime);
    @Query("SELECT n FROM Notification n WHERE n.category = :category AND (n.userId IS NULL OR n.userId IN :userIds)") List<Notification> findByUsersInCategory(
    @Param("category") Notification.Category category,
    @Param("userIds") List<Long> userIds);
    List<Notification> findByConversationIdAndUserId(Long conversationId, Long userId);
    // For advanced notification system
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByCampaignIdOrderByCreatedAtDesc(Long campaignId);
    long countByCampaignId(Long campaignId);
    long countByCampaignIdAndIsReadTrue(Long campaignId);
}
