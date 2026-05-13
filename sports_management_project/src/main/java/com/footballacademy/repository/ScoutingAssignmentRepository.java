package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ScoutingAssignmentRepository extends JpaRepository<ScoutingAssignment, Long> { List<ScoutingAssignment> findByScouter_Id(Long scouterId); List<ScoutingAssignment> findByScouter_IdAndAcademy_Id(Long scouterId, Long academyId); List<ScoutingAssignment> findByAcademy_IdAndDivision_Id(Long academyId, Long divisionId); }
