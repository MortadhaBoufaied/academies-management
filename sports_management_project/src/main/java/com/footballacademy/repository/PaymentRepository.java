package com.footballacademy.repository;

import com.footballacademy.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public
interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPlayerId(Long playerId);
    List<Payment> findByParentId(Long parentId);
    List<Payment> findByAcademy_Id(Long academyId);
    // find payments of a parent that are not paid
    List<Payment> findByIsPaidFalseAndParentId(Long parentId);
    // all payments not paid
    List<Payment> findByIsPaidFalse();
    List<Payment> findByAcademy_IdAndIsPaidFalse(Long academyId);
    // payments by year and month (uses JPQL)
    @Query("SELECT p FROM Payment p WHERE YEAR(p.mois) = :year AND MONTH(p.mois) = :month") List<Payment> findByMonth(
    @Param("year") int year,
    @Param("month") int month);
    @Query("SELECT p FROM Payment p WHERE p.academy.id = :academyId AND YEAR(p.mois) = :year AND MONTH(p.mois) = :month") List<Payment> findByAcademyAndMonth(
    @Param("academyId") Long academyId,
    @Param("year") int year,
    @Param("month") int month);
    // payments not paid for a specific month
    @Query("SELECT p FROM Payment p WHERE YEAR(p.mois) = :year AND MONTH(p.mois) = :month AND p.isPaid = false") List<Payment> findUnpaidByMonth(
    @Param("year") int year,
    @Param("month") int month);
    @Query("SELECT p FROM Payment p WHERE p.academy.id = :academyId AND YEAR(p.mois) = :year AND MONTH(p.mois) = :month AND p.isPaid = false") List<Payment> findUnpaidByAcademyAndMonth(
    @Param("academyId") Long academyId,
    @Param("year") int year,
    @Param("month") int month);
    // payments by parent for a specific month
    List<Payment> findByParentIdAndMois(Long parentId, LocalDate mois);
    // all payments for a specific month
    List<Payment> findByMois(LocalDate mois);
    List<Payment> findByAcademy_IdAndMois(Long academyId, LocalDate mois);
    // For advanced notification system
    List<Payment> findByStatusAndDueDateBefore(String status, LocalDateTime dueDate);
    List<Payment> findByStatusAndDueDateBetween(String status, LocalDateTime start, LocalDateTime end);
    // Additional methods for advanced features
    List<Payment> findByDueDateBeforeAndIsPaidFalse(LocalDateTime dueDate);
    List<Payment> findByDueDateBetweenAndIsPaidFalse(LocalDateTime start, LocalDateTime end);
}
