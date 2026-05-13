package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ScoutingReportRepository extends JpaRepository<ScoutingReport, Long> { List<ScoutingReport> findByPlayer_Id(Long playerId); List<ScoutingReport> findByAcademy_Id(Long academyId); List<ScoutingReport> findByAcademy_IdAndStatus(Long academyId, ScoutingStatus status); }
