package com.footballacademy.repository;

import com.footballacademy.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ScouterRepository extends JpaRepository<Scouter, Long> { List<Scouter> findByAcademy_Id(Long academyId); List<Scouter> findByActiveTrue(); }
