package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ScoutingCriterionRepository extends JpaRepository<ScoutingCriterion, Long> { List<ScoutingCriterion> findByActiveTrue(); List<ScoutingCriterion> findByDomainIgnoreCaseAndActiveTrue(String domain); }
