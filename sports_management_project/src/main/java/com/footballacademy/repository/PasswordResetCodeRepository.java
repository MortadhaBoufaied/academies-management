package com.footballacademy.repository;

import com.footballacademy.model.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    Optional<PasswordResetCode> findTopByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(String email);
    Optional<PasswordResetCode> findByResetTokenHashAndUsedFalse(String resetTokenHash);
    long countByEmailIgnoreCaseAndCreatedAtAfter(String email, LocalDateTime createdAfter);
}
