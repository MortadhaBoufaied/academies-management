package com.footballacademy.repository;

import com.footballacademy.model.AcademyPerformanceScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AcademyPerformanceScoreRepository extends JpaRepository<AcademyPerformanceScore, Long> {
    List<AcademyPerformanceScore> findByAcademy_Id(Long academyId);
    List<AcademyPerformanceScore> findBySport_Id(Long sportId);
    Optional<AcademyPerformanceScore> findTopByAcademy_IdOrderByGeneratedAtDesc(Long academyId);
    List<AcademyPerformanceScore> findBySport_IdOrderByOverallScoreDesc(Long sportId);
    List<AcademyPerformanceScore> findAllByOrderByOverallScoreDesc();

    default List<AcademyPerformanceScore> findByAcademyId(Long academyId) {
        return findByAcademy_Id(academyId);
    }

    default List<AcademyPerformanceScore> findBySportId(Long sportId) {
        return findBySport_Id(sportId);
    }

    default Optional<AcademyPerformanceScore> findTopByAcademyIdOrderByGeneratedAtDesc(Long academyId) {
        return findTopByAcademy_IdOrderByGeneratedAtDesc(academyId);
    }
}
