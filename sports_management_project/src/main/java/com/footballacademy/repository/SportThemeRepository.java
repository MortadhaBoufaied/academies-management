package com.footballacademy.repository;

import com.footballacademy.model.SportTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public
interface SportThemeRepository extends JpaRepository<SportTheme, Long> {
    Optional<SportTheme> findFirstByScopeAndAcademy_IdAndSport_IdOrderByUpdatedAtDesc(SportTheme.ThemeScope scope, Long academyId, Long sportId);
    Optional<SportTheme> findFirstByScopeAndSport_IdOrderByUpdatedAtDesc(SportTheme.ThemeScope scope, Long sportId);
    Optional<SportTheme> findFirstByScopeOrderByUpdatedAtDesc(SportTheme.ThemeScope scope);
}
