package com.footballacademy.repository;

import com.footballacademy.model.SportPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public
interface SportPositionRepository extends JpaRepository<SportPosition, Long> {
    List<SportPosition> findBySport_IdOrderByDisplayOrderAsc(Long sportId);
    Optional<SportPosition> findByCodeAndSport_Id(String code, Long sportId);
    boolean existsByCodeAndSport_Id(String code, Long sportId);
}
