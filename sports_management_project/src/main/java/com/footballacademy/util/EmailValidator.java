package com.footballacademy.util;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public class EmailValidator {

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_LOCAL_PART_LENGTH = 64;
    private static final int MAX_DOMAIN_LENGTH = 255;

    private static final String[] FREE_EMAIL_PROVIDERS = {
            "gmail.com",
            "yahoo.com",
            "hotmail.com",
            "outlook.com",
            "aol.com",
            "mail.com",
            "protonmail.com",
            "icloud.com"
    };

    private EmailValidator() {
        // Utility class
    }

    public static ValidationResult validate(String email) {
        if (email == null || email.isEmpty()) {
            return new ValidationResult(false, "Email cannot be empty");
        }

        if (!StringUtils.hasText(email)) {
            return new ValidationResult(false, "Email cannot be blank");
        }

        String sanitizedEmail = sanitize(email);

        if (sanitizedEmail.length() > MAX_EMAIL_LENGTH) {
            return new ValidationResult(
                    false,
                    String.format("Email cannot exceed %d characters", MAX_EMAIL_LENGTH)
            );
        }

        if (containsWhitespace(sanitizedEmail)) {
            return new ValidationResult(false, "Email cannot contain whitespace");
        }

        if (!EMAIL_PATTERN.matcher(sanitizedEmail).matches()) {
            return new ValidationResult(false, "Invalid email format");
        }

        String[] parts = sanitizedEmail.split("@");
        if (parts.length != 2) {
            return new ValidationResult(false, "Invalid email format");
        }

        String localPart = parts[0];
        String domain = parts[1];

        if (localPart.length() > MAX_LOCAL_PART_LENGTH) {
            return new ValidationResult(
                    false,
                    String.format("Email local part cannot exceed %d characters", MAX_LOCAL_PART_LENGTH)
            );
        }

        if (domain.length() > MAX_DOMAIN_LENGTH) {
            return new ValidationResult(
                    false,
                    String.format("Email domain cannot exceed %d characters", MAX_DOMAIN_LENGTH)
            );
        }

        if (sanitizedEmail.contains("..")) {
            return new ValidationResult(false, "Email cannot contain consecutive dots");
        }

        if (localPart.startsWith(".") || localPart.endsWith(".")) {
            return new ValidationResult(false, "Email local part cannot start or end with a dot");
        }

        if (localPart.startsWith("-") || localPart.endsWith("-")) {
            return new ValidationResult(false, "Email local part cannot start or end with a hyphen");
        }

        if (!isValidDomain(domain)) {
            return new ValidationResult(false, "Invalid email domain");
        }

        return new ValidationResult(true, "Email is valid");
    }

    public static boolean isValid(String email) {
        return validate(email).isValid();
    }

    public static String sanitize(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    public static String extractDomain(String email) {
        if (!isValid(email)) {
            return "";
        }

        String sanitizedEmail = sanitize(email);
        int atIndex = sanitizedEmail.indexOf('@');
        if (atIndex == -1) {
            return "";
        }
        return sanitizedEmail.substring(atIndex + 1);
    }

    public static boolean isCorporateEmail(String email) {
        if (!isValid(email)) {
            return false;
        }

        String domain = extractDomain(email);
        for (String provider : FREE_EMAIL_PROVIDERS) {
            if (domain.equals(provider)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }

        String domainRegex = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return domain.matches(domainRegex);
    }

    private static boolean containsWhitespace(String value) {
        return value.matches(".*\\s+.*");
    }

    public static class ValidationResult {

        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
