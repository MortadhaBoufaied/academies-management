package com.footballacademy.repository;

import com.footballacademy.model.SportScoutingFilterConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SportScoutingFilterConfigRepository extends JpaRepository<SportScoutingFilterConfig, Long> {
    List<SportScoutingFilterConfig> findBySport_IdOrderByDisplayOrderAsc(Long sportId);
    List<SportScoutingFilterConfig> findBySport_IdAndActiveTrueOrderByDisplayOrderAsc(Long sportId);
}
