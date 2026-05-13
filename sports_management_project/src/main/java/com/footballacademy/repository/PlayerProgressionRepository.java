package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PlayerProgressionRepository extends JpaRepository<PlayerProgression, Long> { List<PlayerProgression> findByPlayer_IdOrderByRecordedAtDesc(Long playerId); }
