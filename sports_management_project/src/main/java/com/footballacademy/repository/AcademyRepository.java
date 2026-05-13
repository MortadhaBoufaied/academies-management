package com.footballacademy.repository;

import com.footballacademy.model.Academy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public
interface AcademyRepository extends JpaRepository<Academy, Long> {
    Optional<Academy> findBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCase(String slug);
    List<Academy> findByStatusOrderByNameAsc(Academy.AcademyStatus status);
    long countByStatus(Academy.AcademyStatus status);
}
