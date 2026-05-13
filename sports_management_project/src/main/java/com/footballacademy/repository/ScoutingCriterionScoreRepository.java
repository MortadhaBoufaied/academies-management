package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ScoutingCriterionScoreRepository extends JpaRepository<ScoutingCriterionScore, Long> { List<ScoutingCriterionScore> findByReport_Id(Long reportId); }
