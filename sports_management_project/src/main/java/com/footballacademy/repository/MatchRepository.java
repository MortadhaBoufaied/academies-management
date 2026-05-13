package com.footballacademy.repository;

import com.footballacademy.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public
interface MatchRepository extends JpaRepository<Match, Long> {
    // Find matches by opponent name (partial, case-insensitive)
    List<Match> findByOpponentContainingIgnoreCase(String opponent);
    // Upcoming matches: matches from today onwards
    List<Match> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date);
    List<Match> findByAcademy_Id(Long academyId);
    List<Match> findByAcademy_IdAndDateGreaterThanEqualOrderByDateAsc(Long academyId, LocalDate date);
    // Matches in a specific date range
    List<Match> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
    List<Match> findByAcademy_IdAndDateBetweenOrderByDateAsc(Long academyId, LocalDate startDate, LocalDate endDate);
}
