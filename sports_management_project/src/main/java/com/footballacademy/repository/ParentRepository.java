package com.footballacademy.repository;

import com.footballacademy.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public
interface ParentRepository extends JpaRepository<Parent, Long> {
    @Query("select p from Parent p join fetch p.user") List<Parent> findAllWithUser();
    @Query("select p from Parent p join fetch p.user where p.academy.id = :academyId") List<Parent> findByAcademyIdWithUser(
    @Param("academyId") Long academyId);
    List<Parent> findByAcademy_Id(Long academyId);
    @Query("select p from Parent p join fetch p.user where p.id = :id") Optional<Parent> findByIdWithUser(
    @Param("id") Long id);
}
