package com.footballacademy.services.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final String supportTo;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:boufaiedmortadha7@gmail.com}") String from,
            @Value("${app.mail.support-to:boufaiedmortadha7@gmail.com}") String supportTo
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.supportTo = supportTo;
    }

    public boolean sendPasswordResetCode(String to, String code, int expiresMinutes) {
        String body = """
                Hello,

                You requested to reset your password.

                Your password reset code is:

                %s

                This code will expire in %d minutes.

                If you did not request this, please ignore this email.

                For support, contact:
                %s
                """.formatted(code, expiresMinutes, supportTo);
        return send(to, "Password Reset Code", body);
    }

    public boolean sendSupportRequest(String name, String email, String subject, String message, String userAgent) {
        String body = """
                Support request received.

                Name: %s
                Email: %s
                Subject: %s
                Date/time: %s
                Device/User-Agent: %s

                Message:
                %s
                """.formatted(blank(name), blank(email), blank(subject), LocalDateTime.now(), blank(userAgent), blank(message));
        return send(supportTo, subject == null || subject.isBlank() ? "Password Reset Support Request" : subject, body);
    }

    private boolean send(String to, String subject, String body) {
        try {
            JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
            if (mailSender == null) {
                logger.warn("Mail sender is not configured; email to {} skipped", to);
                return false;
            }
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
            return true;
        } catch (Exception e) {
            logger.warn("Failed to send email to {}: {}", to, e.getMessage());
            return false;
        }
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
