package com.footballacademy.util;

import java.util.regex.Pattern;

public
class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");
    public static
    class ValidationResult {
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
    public static ValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password cannot be empty");
        }
        if (password.length() < MIN_LENGTH) {
            return new ValidationResult(false, String.format("Password must be at least %d characters long", MIN_LENGTH));
        }
        if (password.length() > MAX_LENGTH) {
            return new ValidationResult(false, String.format("Password must not exceed %d characters", MAX_LENGTH));
        }
        if (!UPPERCASE.matcher(password) .find()) {
            return new ValidationResult(false, "Password must contain at least one uppercase letter");
        }
        if (!LOWERCASE.matcher(password) .find()) {
            return new ValidationResult(false, "Password must contain at least one lowercase letter");
        }
        if (!DIGIT.matcher(password) .find()) {
            return new ValidationResult(false, "Password must contain at least one digit");
        }
        if (!SPECIAL.matcher(password) .find()) {
            return new ValidationResult(false, "Password must contain at least one special character");
        }
        // Check for common weak passwords
        if (isCommonPassword(password)) {
            return new ValidationResult(false, "Password is too common. Please choose a stronger password");
        } return new ValidationResult(true, "Password is valid");
    }
    private static boolean isCommonPassword(String password) {
        String lowerPassword = password.toLowerCase();
        String[] commonPasswords = {
            "password", "123456", "12345678", "qwerty", "abc123", "monkey", "letmein", "dragon", "111111", "baseball", "iloveyou", "trustno1", "sunshine", "master", "hello", "freedom", "whatever", "qazwsx", "trustno1", "000000"
        };
        for (String common : commonPasswords) {
            if (lowerPassword.equals(common) || lowerPassword.contains(common)) {
                return true;
            }
        }
        // Check for sequential patterns
        if (hasSequentialPattern(password)) {
            return true;
        }
        // Check for repeated characters
        if (hasRepeatedCharacters(password)) {
            return true;
        } return false;
    }
    private static boolean hasSequentialPattern(String password) {
        String lowerPassword = password.toLowerCase();
        for (int i = 0;
        i < lowerPassword.length() - 2;
        i++) {
            char c1 = lowerPassword.charAt(i);
            char c2 = lowerPassword.charAt(i + 1);
            char c3 = lowerPassword.charAt(i + 2);
            // Check for sequential letters or numbers
            if (c1 + 1 == c2 && c2 + 1 == c3) {
                return true;
            }
            if (c1 - 1 == c2 && c2 - 1 == c3) {
                return true;
            }
        } return false;
    }
    private static boolean hasRepeatedCharacters(String password) {
        for (int i = 0;
        i < password.length() - 2;
        i++) {
            char c = password.charAt(i);
            if (password.charAt(i + 1) == c && password.charAt(i + 2) == c) {
                return true;
            }
        } return false;
    }
    public static String generatePasswordRequirements() {
        return String.format("Password must be between %d and %d characters long, " + "contain at least one uppercase letter, one lowercase letter, " + "one digit, and one special character", MIN_LENGTH, MAX_LENGTH);
    }
}
