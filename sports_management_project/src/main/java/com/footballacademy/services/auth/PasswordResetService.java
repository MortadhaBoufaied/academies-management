package com.footballacademy.services.auth;

import com.footballacademy.model.PasswordResetCode;
import com.footballacademy.model.User;
import com.footballacademy.repository.PasswordResetCodeRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.support.EmailService;
import com.footballacademy.util.PasswordValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    private static final int CODE_MINUTES = 15;
    private static final int TOKEN_MINUTES = 15;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int MAX_RESENDS_PER_WINDOW = 3;
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final PasswordResetCodeRepository resetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(UserRepository userRepository, PasswordResetCodeRepository resetCodeRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.resetCodeRepository = resetCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public Map<String, String> requestResetCode(String email) {
        String normalized = normalizeEmail(email);
        String publicMessage = "If this email exists, a reset code has been sent.";
        if (normalized == null) return Map.of("message", publicMessage);

        Optional<User> userOpt = userRepository.findByEmail(normalized);
        if (userOpt.isEmpty()) return Map.of("message", publicMessage);

        long recentRequests = resetCodeRepository.countByEmailIgnoreCaseAndCreatedAtAfter(normalized, LocalDateTime.now().minusMinutes(CODE_MINUTES));
        if (recentRequests >= MAX_RESENDS_PER_WINDOW) return Map.of("message", publicMessage);

        User user = userOpt.get();
        String code = generateCode();
        PasswordResetCode resetCode = new PasswordResetCode();
        resetCode.setUserId(user.getId());
        resetCode.setEmail(normalized);
        resetCode.setCodeHash(passwordEncoder.encode(code));
        resetCode.setExpiresAt(LocalDateTime.now().plusMinutes(CODE_MINUTES));
        resetCodeRepository.save(resetCode);
        emailService.sendPasswordResetCode(normalized, code, CODE_MINUTES);
        return Map.of("message", publicMessage);
    }

    @Transactional
    public Map<String, String> verifyResetCode(String email, String code) {
        String normalized = normalizeEmail(email);
        if (normalized == null || code == null || !code.matches("\\d{8}")) {
            throw new IllegalArgumentException("Invalid reset code.");
        }

        PasswordResetCode resetCode = resetCodeRepository.findTopByEmailIgnoreCaseAndUsedFalseOrderByCreatedAtDesc(normalized)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset code."));

        if (resetCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset code expired. Please request a new code.");
        }
        if (resetCode.getAttemptCount() != null && resetCode.getAttemptCount() >= MAX_VERIFY_ATTEMPTS) {
            throw new IllegalArgumentException("Too many verification attempts. Please request a new code.");
        }

        resetCode.setAttemptCount((resetCode.getAttemptCount() == null ? 0 : resetCode.getAttemptCount()) + 1);
        if (!passwordEncoder.matches(code, resetCode.getCodeHash())) {
            resetCodeRepository.save(resetCode);
            throw new IllegalArgumentException("Invalid reset code.");
        }

        String resetToken = UUID.randomUUID() + "-" + generateCode() + "-" + UUID.randomUUID();
        resetCode.setResetTokenHash(sha256(resetToken));
        resetCode.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_MINUTES));
        resetCodeRepository.save(resetCode);
        return Map.of("resetToken", resetToken, "message", "Code verified successfully.");
    }

    @Transactional
    public Map<String, String> resetPassword(String resetToken, String newPassword, String confirmPassword) {
        if (resetToken == null || resetToken.isBlank()) {
            throw new IllegalArgumentException("Reset token is required.");
        }
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords must match.");
        }
        PasswordValidator.ValidationResult validation = PasswordValidator.validate(newPassword);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getMessage());
        }

        PasswordResetCode resetCode = resetCodeRepository.findByResetTokenHashAndUsedFalse(sha256(resetToken))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token."));
        if (resetCode.getResetTokenExpiresAt() == null || resetCode.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token expired. Please request a new code.");
        }

        User user = userRepository.findById(resetCode.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setMdp(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetCode.setUsed(true);
        resetCode.setUsedAt(LocalDateTime.now());
        resetCodeRepository.save(resetCode);
        return Map.of("message", "Password updated successfully.");
    }

    private String generateCode() {
        return String.format("%08d", secureRandom.nextInt(100_000_000));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return email.trim().toLowerCase();
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash reset token", e);
        }
    }
}
