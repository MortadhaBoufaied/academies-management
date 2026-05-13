package com.footballacademy.repository;

import com.footballacademy.model.AcademyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public
interface AcademyInfoRepository extends JpaRepository<AcademyInfo, Long> {
    Optional<AcademyInfo> findTopByOrderByIdAsc();
    Optional<AcademyInfo> findFirstByAcademy_IdOrderByIdAsc(Long academyId);
    List<AcademyInfo> findByAcademy_IdOrderByIdAsc(Long academyId);
}
