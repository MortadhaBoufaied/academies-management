package com.footballacademy.repository;

import com.footballacademy.model.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public
interface SportRepository extends JpaRepository<Sport, Long> {
    List<Sport> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<Sport> findByTheme_Id(Long themeId);
    Optional<Sport> findByCode(String code);
    boolean existsByCode(String code);
}
