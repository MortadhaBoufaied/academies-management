package com.footballacademy.repository;

import com.footballacademy.model.NotificationCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public
interface NotificationCampaignRepository extends JpaRepository<NotificationCampaign, Long> {
    List<NotificationCampaign> findByAcademy_IdOrderByCreatedAtDesc(Long academyId);
    List<NotificationCampaign> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    List<NotificationCampaign> findAllByOrderByCreatedAtDesc();
}
