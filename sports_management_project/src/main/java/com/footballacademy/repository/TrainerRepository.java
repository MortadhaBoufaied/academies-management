package com.footballacademy.repository;

import com.footballacademy.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public
interface TrainerRepository extends JpaRepository<Trainer, Long> {
    List<Trainer> findBySpeciality(String speciality);
    @Query("select t from Trainer t join fetch t.user") List<Trainer> findAllWithUser();
    @Query("select t from Trainer t join fetch t.user where t.academy.id = :academyId") List<Trainer> findByAcademyIdWithUser(
    @Param("academyId") Long academyId);
    List<Trainer> findByAcademy_Id(Long academyId);
    @Query("select t from Trainer t join fetch t.user where lower(t.speciality) = lower(:speciality)") List<Trainer> findBySpecialityWithUser(
    @Param("speciality") String speciality);
    @Query("select distinct t from Trainer t join fetch t.user left join t.divisions ds where (t.division is not null and t.division.id = :divisionId) or (ds is not null and ds.id = :divisionId)") List<Trainer> findCoachingDivisionWithUser(
    @Param("divisionId") Long divisionId);
    @Query("select t from Trainer t join fetch t.user where t.id = :id") Optional<Trainer> findByIdWithUser(
    @Param("id") Long id);
    @Query("select distinct t from Trainer t left join fetch t.divisions ds where t.id = :id") Optional<Trainer> findWithDivisions(
    @Param("id") Long id);
}
