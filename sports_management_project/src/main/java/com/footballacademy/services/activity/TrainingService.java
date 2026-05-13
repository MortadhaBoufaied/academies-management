package com.footballacademy.services.activity;

import com.footballacademy.model.Training;
import com.footballacademy.repository.TrainingRepository;
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
class TrainingService {
    private final TrainingRepository trainingRepository;
    private final AcademyAccessService academyAccessService;
    public TrainingService(TrainingRepository trainingRepository, AcademyAccessService academyAccessService) {
        this.trainingRepository = trainingRepository;
        this.academyAccessService = academyAccessService;
    }
    public Training recordAttendance(Long trainingId, List<Long> playerIds) {
        Training training = getTrainingById(trainingId);
        // In a real app, attendance would be stored in a join table         //
        training.setAttendeeIds(playerIds);
        return trainingRepository.save(training);
    }
    public Training createTraining(Training training) {
        if (!academyAccessService.isSuperAdmin() || training.getAcademy() == null) {
            training.setAcademy(academyAccessService.academyForWrite(training.getAcademy()));
        } else {
            academyAccessService.assertCanAccessAcademy(training.getAcademy());
        } return trainingRepository.save(training);
    }
    public Training getTrainingById(Long id) {
        Training training = trainingRepository.findById(id) .orElseThrow(() -> new RuntimeException("Training not found with id: " + id));
        assertVisible(training);
        return training;
    }
    public List<Training> getAllTrainings() {
        List<Training> trainings = academyAccessService.isSuperAdmin() ? trainingRepository.findAll() : trainingRepository.findByAcademy_Id(academyAccessService.currentAcademyOrThrow() .getId());
        return trainings != null ? trainings : Collections.emptyList();
    }
    public List<Training> getTrainingsBySessionType(String sessionType) {
        List<Training> trainings = trainingRepository.findBySessionType(sessionType);
        if (!academyAccessService.isSuperAdmin()) {
            trainings = trainings.stream() .filter(this::isVisible) .toList();
        } return trainings != null ? trainings : Collections.emptyList();
    }
    public void deleteTraining(Long id) {
        Training training = getTrainingById(id);
        trainingRepository.delete(training);
    }
    public List<Training> getTrainingsByTrainer(Long trainerId) {
        List<Training> trainings = academyAccessService.isSuperAdmin() ? trainingRepository.findByTrainerId(trainerId) : trainingRepository.findByAcademy_IdAndTrainerId(academyAccessService.currentAcademyOrThrow() .getId(), trainerId);
        return trainings != null ? trainings : Collections.emptyList();
    }
    public List<Training> getTrainingsInDateRange(LocalDate start, LocalDate end) {
        List<Training> trainings = academyAccessService.isSuperAdmin() ? trainingRepository.findByDateBetween(start, end) : trainingRepository.findByAcademy_IdAndDateBetween(academyAccessService.currentAcademyOrThrow() .getId(), start, end);
        return trainings != null ? trainings : Collections.emptyList();
    }
    private boolean isVisible(Training training) {
        return training == null || academyAccessService.isSuperAdmin() || academyAccessService.canAccessAcademy(training.getAcademy());
    }
    private void assertVisible(Training training) {
        if (!isVisible(training)) {
            throw new AccessDeniedException("You cannot access another academy's training");
        }
    }
}
