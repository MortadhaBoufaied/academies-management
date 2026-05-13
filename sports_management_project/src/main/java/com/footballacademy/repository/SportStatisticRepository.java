package com.footballacademy.repository;

import com.footballacademy.model.SportStatistic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public
interface SportStatisticRepository extends JpaRepository<SportStatistic, Long> {
    List<SportStatistic> findBySport_IdOrderByDisplayOrderAsc(Long sportId);
    Optional<SportStatistic> findByCodeAndSport_Id(String code, Long sportId);
    boolean existsByCodeAndSport_Id(String code, Long sportId);
}
