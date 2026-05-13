package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface InjuryRecordRepository extends JpaRepository<InjuryRecord, Long> { List<InjuryRecord> findByPlayer_Id(Long playerId); List<InjuryRecord> findByPlayer_IdAndRecoveredFalse(Long playerId); }
