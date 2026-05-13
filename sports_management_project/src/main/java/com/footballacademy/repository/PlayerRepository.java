package com.footballacademy.repository;

import com.footballacademy.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

    // =====================================================
    // === SIMPLE FINDERS
    // =====================================================

    List<Player> findByParentId(Long parentId);

    List<Player> findByTrainerId(Long trainerId);

    List<Player> findByDivisionId(Long divisionId);

    List<Player> findByAcademy_Id(Long academyId);

    List<Player> findByDivisionIsNull();

    // =====================================================
    // === SEARCH BY USER NAME
    // =====================================================

    @Query("SELECT p FROM Player p WHERE p.user.nom LIKE %:nom%")
    List<Player> findByUserNomContaining(@Param("nom") String nom);

    @Query("""
        SELECT p
          FROM Player p
         WHERE LOWER(p.user.nom) LIKE LOWER(CONCAT('%', :nom, '%'))
    """)
    List<Player> findByUserNomContainingIgnoreCase(@Param("nom") String nom);

    // =====================================================
    // === FETCH JOIN VARIANTS (OPTIMIZED)
    // =====================================================

    @Query("""
        SELECT DISTINCT p
          FROM Player p
          JOIN FETCH p.user
          LEFT JOIN FETCH p.division
          LEFT JOIN FETCH p.parent
          LEFT JOIN FETCH p.trainer
    """)
    List<Player> findAllWithUserAndRefs();

    @Query("""
        SELECT DISTINCT p
          FROM Player p
          JOIN FETCH p.user
          LEFT JOIN FETCH p.division
          LEFT JOIN FETCH p.parent
          LEFT JOIN FETCH p.trainer
         WHERE p.academy.id = :academyId
    """)
    List<Player> findByAcademyIdWithUserAndRefs(
            @Param("academyId") Long academyId
    );

    @Query("""
        SELECT DISTINCT p
          FROM Player p
          JOIN FETCH p.user
          LEFT JOIN FETCH p.division
          LEFT JOIN FETCH p.parent
          LEFT JOIN FETCH p.trainer
         WHERE p.division.id = :divisionId
    """)
    List<Player> findByDivisionIdWithUserAndRefs(
            @Param("divisionId") Long divisionId
    );

    @Query("""
        SELECT DISTINCT p
          FROM Player p
          JOIN FETCH p.user
          LEFT JOIN FETCH p.parent
          LEFT JOIN FETCH p.trainer
         WHERE p.division IS NULL
    """)
    List<Player> findByDivisionIsNullWithUserAndRefs();

    @Query("""
        SELECT p
          FROM Player p
          JOIN FETCH p.user
          LEFT JOIN FETCH p.division
          LEFT JOIN FETCH p.parent
          LEFT JOIN FETCH p.trainer
         WHERE p.id = :id
    """)
    Optional<Player> findByIdWithUserAndRefs(@Param("id") Long id);

    @Query("""
        SELECT DISTINCT p
          FROM Player p
          JOIN FETCH p.user
          LEFT JOIN FETCH p.division
          LEFT JOIN FETCH p.parent
          LEFT JOIN FETCH p.trainer
         WHERE p.academy.id = :academyId
           AND p.id IN :ids
    """)
    List<Player> findByAcademyIdAndIdInWithUserAndRefs(
            @Param("academyId") Long academyId,
            @Param("ids") List<Long> ids
    );
}
