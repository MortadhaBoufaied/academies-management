package com.footballacademy.repository;

import com.footballacademy.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public
interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByDateBetween(LocalDate start, LocalDate end);
    List<Activity> findByTrainerId(Long trainerId);
    List<Activity> findByAcademy_Id(Long academyId);
    List<Activity> findByAcademy_IdAndDateBetween(Long academyId, LocalDate start, LocalDate end);
    List<Activity> findByAcademy_IdAndTrainerId(Long academyId, Long trainerId);
    // For advanced notification system
    List<Activity> findByDateBetween(LocalDateTime start, LocalDateTime end);
    // Optional: upcoming activities from today onward
    @Query("SELECT a FROM Activity a WHERE a.date >= :today ORDER BY a.date ASC") List<Activity> findUpcomingActivities(
    @Param("today") LocalDate today);
    @Query("SELECT a FROM Activity a WHERE a.academy.id = :academyId AND a.date >= :today ORDER BY a.date ASC") List<Activity> findUpcomingActivitiesForAcademy(
    @Param("academyId") Long academyId,
    @Param("today") LocalDate today);
    @Query("SELECT a FROM Activity a WHERE YEAR(a.date) = :year AND MONTH(a.date) = :month") List<Activity> findByDateYearAndMonth(
    @Param("year") int year,
    @Param("month") int month);
}
