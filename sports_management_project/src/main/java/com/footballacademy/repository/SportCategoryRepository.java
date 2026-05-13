package com.footballacademy.repository;

import com.footballacademy.model.SportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public
interface SportCategoryRepository extends JpaRepository<SportCategory, Long> {
    List<SportCategory> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<SportCategory> findBySport_IdAndIsActiveTrueOrderByDisplayOrderAsc(Long sportId);
    Optional<SportCategory> findByCodeIgnoreCaseAndSport_Id(String code, Long sportId);
}
