package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TalentScoreRepository extends JpaRepository<TalentScore, Long> { List<TalentScore> findByPlayer_IdOrderByGeneratedAtDesc(Long playerId); }
