package com.footballacademy.services.trainer;

import com.footballacademy.DTO.TrainerCombinedDTO;
import com.footballacademy.model.Activity;
import com.footballacademy.model.Player;
import com.footballacademy.model.Trainer;
import com.footballacademy.model.User;
import com.footballacademy.model.Division;
import com.footballacademy.repository.ActivityRepository;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.repository.TrainerRepository;
import com.footballacademy.repository.DivisionRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.chat.ChatRoomService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Service
@Transactional
public
class TrainerService {
    private final TrainerRepository trainerRepository;
    private final ChatRoomService chatRoomService;
    private final DivisionRepository divisionRepository;
    private final PlayerRepository playerRepository;
    private final ActivityRepository activityRepository;
    private final AcademyAccessService academyAccessService;
    public TrainerService(TrainerRepository trainerRepository, ChatRoomService chatRoomService, PlayerRepository playerRepository, ActivityRepository activityRepository, DivisionRepository divisionRepository, AcademyAccessService academyAccessService) {
        this.trainerRepository = trainerRepository;
        this.chatRoomService = chatRoomService;
        this.playerRepository = playerRepository;
        this.activityRepository = activityRepository;
        this.divisionRepository = divisionRepository;
        this.academyAccessService = academyAccessService;
    }
    private TrainerCombinedDTO toDto(Trainer t) {
        User u = t.getUser();
        Long userId =(u != null ? u.getId() : t.getId());
        Long divisionId =(t.getDivision() != null ? t.getDivision() .getId() :(t.getDivisions() != null && !t.getDivisions() .isEmpty() ? t.getDivisions() .iterator() .next() .getId() : null));
        return new TrainerCombinedDTO(t.getId(), userId,(u != null ? u.getNom() : null),(u != null ? u.getEmail() : null),(u != null ? u.getTel() : null), t.getSpeciality(), t.getExperience(), t.getLicense(), t.getNotes(), divisionId);
    }
    @Transactional(readOnly = true)
    public List<TrainerCombinedDTO> getAllTrainersCombined() {
        List<Trainer> trainers = academyAccessService.isSuperAdmin() ? trainerRepository.findAllWithUser() : trainerRepository.findByAcademyIdWithUser(academyAccessService.currentAcademyOrThrow() .getId());
        if (trainers == null || trainers.isEmpty()) return Collections.emptyList();
        return trainers.stream() .map(this::toDto) .toList();
    }
    @Transactional(readOnly = true)
    public TrainerCombinedDTO getTrainerCombinedById(Long id) {
        return toDto(getTrainerById(id));
    }
    @Transactional(readOnly = true)
    public List<TrainerCombinedDTO> getTrainersBySpeciality(String speciality) {
        if (speciality == null || speciality.isBlank()) return getAllTrainersCombined();
        List<Trainer> trainers = trainerRepository.findBySpecialityWithUser(speciality);
        if (trainers == null || trainers.isEmpty()) return Collections.emptyList();
        return trainers.stream() .filter(this::isVisible) .map(this::toDto) .toList();
    }
    public Trainer getTrainerById(Long id) {
        Trainer trainer = trainerRepository.findByIdWithUser(id) .orElseThrow(() -> new RuntimeException("Trainer not found with id: " + id));
        assertVisible(trainer);
        return trainer;
    }
    public Trainer assignDivision(Long trainerId, Long divisionId) {
        Trainer trainer = getTrainerById(trainerId);
        if (divisionId != null) {
            Division division = divisionRepository.findById(divisionId) .orElseThrow(() -> new RuntimeException("Division not found with id: " + divisionId));
            assertSameAcademy(trainer, division);
            trainer.setDivision(division);
            // keep multi-division set in sync
            try {
                if (trainer.getDivisions() != null) trainer.getDivisions() .add(division);
            } catch (Exception ignored) {
            }
        } else {
            trainer.setDivision(null);
            try {
                if (trainer.getDivisions() != null) trainer.getDivisions() .clear();
            } catch (Exception ignored) {
            }
        } return trainerRepository.save(trainer);
    }
    @Transactional(readOnly = true)
    public List<Player> getTrainerPlayers(Long trainerId) {
        Trainer trainer = getTrainerById(trainerId);
        List<Player> players = playerRepository.findByTrainerId(trainer.getId());
        if (!academyAccessService.isSuperAdmin()) {
            players = players.stream() .filter(player -> academyAccessService.canAccessAcademy(player.getAcademy())) .toList();
        } return players != null ? players : Collections.emptyList();
    }
    @Transactional(readOnly = true)
    public List<Activity> getTrainerActivities(Long trainerId) {
        Trainer trainer = getTrainerById(trainerId);
        List<Activity> activities = academyAccessService.isSuperAdmin() ? activityRepository.findByTrainerId(trainer.getId()) : activityRepository.findByAcademy_IdAndTrainerId(academyAccessService.currentAcademyOrThrow() .getId(), trainer.getId());
        return activities != null ? activities : Collections.emptyList();
    }
    public Activity planActivity(Long trainerId, Activity activityDetails) {
        Trainer trainer = getTrainerById(trainerId);
        activityDetails.setTrainerId(trainer.getId());
        activityDetails.setAcademy(trainer.getAcademy());
        if (activityDetails.getDate() == null) activityDetails.setDate(LocalDate.now());
        return activityRepository.save(activityDetails);
    }
    public Player assignPlayerToTrainer(Long trainerId, Long playerId) {
        Trainer trainer = getTrainerById(trainerId);
        Player player = playerRepository.findById(playerId) .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        assertSameAcademy(trainer, player);
        player.setTrainer(trainer);
        return playerRepository.save(player);
    }
    // NEW: list divisions coached by a trainer (multi-division)
    @Transactional(readOnly = true)
    public List<Division> getTrainerDivisions(Long trainerId) {
        Trainer visibleTrainer = getTrainerById(trainerId);
        return trainerRepository.findWithDivisions(visibleTrainer.getId()) .map(t -> {
            if (t.getDivisions() != null && !t.getDivisions() .isEmpty()) {
                return t.getDivisions() .stream() .filter(this::isDivisionVisible) .toList();
            } return(t.getDivision() != null && isDivisionVisible(t.getDivision())) ? List.of(t.getDivision()) : List.<Division>of();
        }) .orElseGet(() -> {
            Trainer t = trainerRepository.findByIdWithUser(trainerId) .orElse(null);
            if (t != null) {
                if (t.getDivisions() != null && !t.getDivisions() .isEmpty()) return t.getDivisions() .stream() .filter(this::isDivisionVisible) .toList();
                if (t.getDivision() != null && isDivisionVisible(t.getDivision())) return List.of(t.getDivision());
            } return List.of();
        });
    }
    // NEW: assign multiple divisions to a trainer
    public Trainer assignDivisions(Long trainerId, List<Long> divisionIds) {
        Trainer trainer = getTrainerById(trainerId);
        Set<Division> set = new HashSet<>();
        if (divisionIds != null) {
            for (Long did : divisionIds) {
                if (did == null) continue;
                divisionRepository.findById(did) .ifPresent(division -> {
                    assertSameAcademy(trainer, division);
                    set.add(division);
                });
            }
        } trainer.setDivisions(set);
        // keep legacy primary division for existing screens
        trainer.setDivision(set.stream() .findFirst() .orElse(null));
        return trainerRepository.save(trainer);
    }
    private boolean isVisible(Trainer trainer) {
        return trainer == null || academyAccessService.isSuperAdmin() || academyAccessService.canAccessAcademy(trainer.getAcademy());
    }
    private void assertVisible(Trainer trainer) {
        if (!isVisible(trainer)) {
            throw new AccessDeniedException("You cannot access another academy's trainer");
        }
    }
    private boolean isDivisionVisible(Division division) {
        return division == null || academyAccessService.isSuperAdmin() || academyAccessService.canAccessAcademy(division.getAcademy());
    }
    private void assertSameAcademy(Trainer trainer, Division division) {
        assertVisible(trainer);
        if (!isDivisionVisible(division)) {
            throw new AccessDeniedException("Cannot assign a division from another academy");
        }
    }
    private void assertSameAcademy(Trainer trainer, Player player) {
        assertVisible(trainer);
        if (academyAccessService.isSuperAdmin()) {
            return;
        }
        if (player == null || !academyAccessService.canAccessAcademy(player.getAcademy())) {
            throw new AccessDeniedException("Cannot assign a player from another academy");
        }
    }
}
