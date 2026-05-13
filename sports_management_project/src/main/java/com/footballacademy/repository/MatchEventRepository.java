package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> { List<MatchEvent> findByMatch_IdOrderByMinuteAscSecondAsc(Long matchId); List<MatchEvent> findByPlayer_Id(Long playerId); }
