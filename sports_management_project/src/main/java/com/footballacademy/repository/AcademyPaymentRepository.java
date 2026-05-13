package com.footballacademy.repository;

import com.footballacademy.model.AcademyPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public
interface AcademyPaymentRepository extends JpaRepository<AcademyPayment, Long> {
    List<AcademyPayment> findAllByOrderByCreatedAtDesc();
    List<AcademyPayment> findByAcademy_IdOrderByCreatedAtDesc(Long academyId);
    Optional<AcademyPayment> findTopByAcademy_IdAndStatusOrderByCreatedAtDesc(Long academyId, AcademyPayment.PaymentStatus status);
    Optional<AcademyPayment> findTopByAcademy_IdOrderByCreatedAtDesc(Long academyId);
    long countByStatus(AcademyPayment.PaymentStatus status);
}
