package com.footballacademy.repository;

import com.footballacademy.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public
interface TrainingRepository extends JpaRepository<Training, Long> {
    // ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ Get trainings for a specific trainer
    List<Training> findByTrainerId(Long trainerId);
    List<Training> findByAcademy_Id(Long academyId);
    List<Training> findByAcademy_IdAndTrainerId(Long academyId, Long trainerId);
    // ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ Get trainings within a date range
    List<Training> findByDateBetween(LocalDate start, LocalDate end);
    List<Training> findByAcademy_IdAndDateBetween(Long academyId, LocalDate start, LocalDate end);
    // ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ Optional: search by session type
    List<Training> findBySessionType(String sessionType);
}
