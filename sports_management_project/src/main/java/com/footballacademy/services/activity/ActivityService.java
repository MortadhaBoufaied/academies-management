package com.footballacademy.services.activity;

import com.footballacademy.model.Activity;
import com.footballacademy.model.Player;
import com.footballacademy.repository.ActivityRepository;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public
class ActivityService {
    private final ActivityRepository activityRepository;
    private final PlayerRepository playerRepository;
    private final AcademyAccessService academyAccessService;
    public ActivityService(ActivityRepository activityRepository, PlayerRepository playerRepository, AcademyAccessService academyAccessService) {
        this.activityRepository = activityRepository;
        this.playerRepository = playerRepository;
        this.academyAccessService = academyAccessService;
    }
    public List<Activity> getAllActivities() {
        List<Activity> activities = academyAccessService.isSuperAdmin() ? activityRepository.findAll() : activityRepository.findByAcademy_Id(academyAccessService.currentAcademyOrThrow() .getId());
        return activities != null ? activities : Collections.emptyList();
    }
    public Activity getActivityById(Long id) {
        Activity activity = activityRepository.findById(id) .orElseThrow(() -> new RuntimeException("Activity not found with id: " + id));
        assertVisible(activity);
        return activity;
    }
    public Activity createActivity(Activity activity) {
        validateActivityData(activity);
        if (!academyAccessService.isSuperAdmin() || activity.getAcademy() == null) {
            activity.setAcademy(academyAccessService.academyForWrite(activity.getAcademy()));
        } else {
            academyAccessService.assertCanAccessAcademy(activity.getAcademy());
        } return activityRepository.save(activity);
    }
    public Activity updateActivity(Long id, Activity activityDetails) {
        Activity activity = getActivityById(id);
        activity.setTitre(activityDetails.getTitre());
        activity.setDescription(activityDetails.getDescription());
        activity.setDate(activityDetails.getDate());
        activity.setLieu(activityDetails.getLieu());
        activity.setTrainerId(activityDetails.getTrainerId());
        if (activityDetails.getAcademy() != null) {
            activity.setAcademy(academyAccessService.academyForWrite(activityDetails.getAcademy()));
        } return activityRepository.save(activity);
    }
    public void deleteActivity(Long id) {
        Activity activity = getActivityById(id);
        activityRepository.delete(activity);
    }
    public void addParticipant(Long activityId, Long playerId) {
        Activity activity = getActivityById(activityId);
        Player player = playerRepository.findById(playerId) .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        assertSameAcademy(activity, player);
        // Simulate participant addition (real implementation would require a join table)
    }
    public void removeParticipant(Long activityId, Long playerId) {
        Activity activity = getActivityById(activityId);
        Player player = playerRepository.findById(playerId) .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        assertSameAcademy(activity, player);
        // Simulate participant removal (real implementation would require a join table)
    }
    public List<Activity> getActivitiesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Activity> activities = academyAccessService.isSuperAdmin() ? activityRepository.findByDateBetween(startDate, endDate) : activityRepository.findByAcademy_IdAndDateBetween(academyAccessService.currentAcademyOrThrow() .getId(), startDate, endDate);
        return activities != null ? activities : Collections.emptyList();
    }
    public List<Activity> getActivitiesByTrainer(Long trainerId) {
        List<Activity> activities = academyAccessService.isSuperAdmin() ? activityRepository.findByTrainerId(trainerId) : activityRepository.findByAcademy_IdAndTrainerId(academyAccessService.currentAcademyOrThrow() .getId(), trainerId);
        return activities != null ? activities : Collections.emptyList();
    }
    public List<Activity> getUpcomingActivities() {
        List<Activity> activities = academyAccessService.isSuperAdmin() ? activityRepository.findUpcomingActivities(LocalDate.now()) : activityRepository.findUpcomingActivitiesForAcademy(academyAccessService.currentAcademyOrThrow() .getId(), LocalDate.now());
        return activities != null ? activities : Collections.emptyList();
    }
    private void validateActivityData(Activity activity) {
        if (activity.getDate() == null) throw new RuntimeException("Activity date is required");
        if (activity.getDate() .isBefore(LocalDate.now())) throw new RuntimeException("Activity date cannot be in the past");
        if (activity.getTitre() == null || activity.getTitre() .trim() .isEmpty()) throw new RuntimeException("Activity title is required");
    }
    private void assertVisible(Activity activity) {
        if (academyAccessService.isSuperAdmin()) {
            return;
        }
        if (activity == null || !academyAccessService.canAccessAcademy(activity.getAcademy())) {
            throw new AccessDeniedException("You cannot access another academy's activity");
        }
    }
    private void assertSameAcademy(Activity activity, Player player) {
        assertVisible(activity);
        if (academyAccessService.isSuperAdmin()) {
            return;
        }
        if (player == null || !academyAccessService.canAccessAcademy(player.getAcademy())) {
            throw new AccessDeniedException("Cannot attach a player from another academy");
        }
    }
}
