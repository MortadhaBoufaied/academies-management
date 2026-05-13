package com.footballacademy.util;

import java.util.regex.Pattern;

public
class ValidationUtils {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9\\-\\s()]{7,20}$");
    private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern TIME_PATTERN = Pattern.compile("^\\d{2}:\\d{2}(:\\d{2})?$");
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
    /**      * Validate phone number      */
    public static ValidationResult validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return new ValidationResult(false, "Phone number cannot be empty");
        }
        if (!PHONE_PATTERN.matcher(phone) .matches()) {
            return new ValidationResult(false, "Invalid phone number format");
        } return new ValidationResult(true, "Phone number is valid");
    }
    /**      * Validate URL      */
    public static ValidationResult validateUrl(String url) {
        if (url == null || url.isEmpty()) {
            return new ValidationResult(false, "URL cannot be empty");
        }
        if (!URL_PATTERN.matcher(url) .matches()) {
            return new ValidationResult(false, "Invalid URL format");
        } return new ValidationResult(true, "URL is valid");
    }
    /**      * Validate date format (YYYY-MM-DD)      */
    public static ValidationResult validateDate(String date) {
        if (date == null || date.isEmpty()) {
            return new ValidationResult(false, "Date cannot be empty");
        }
        if (!DATE_PATTERN.matcher(date) .matches()) {
            return new ValidationResult(false, "Invalid date format. Expected: YYYY-MM-DD");
        }
        try {
            java.time.LocalDate.parse(date);
            return new ValidationResult(true, "Date is valid");
        } catch (Exception e) {
            return new ValidationResult(false, "Invalid date");
        }
    }
    /**      * Validate time format (HH:MM or HH:MM:SS)      */
    public static ValidationResult validateTime(String time) {
        if (time == null || time.isEmpty()) {
            return new ValidationResult(false, "Time cannot be empty");
        }
        if (!TIME_PATTERN.matcher(time) .matches()) {
            return new ValidationResult(false, "Invalid time format. Expected: HH:MM or HH:MM:SS");
        } return new ValidationResult(true, "Time is valid");
    }
    /**      * Validate age      */
    public static ValidationResult validateAge(int age) {
        if (age < 0) {
            return new ValidationResult(false, "Age cannot be negative");
        }
        if (age > 150) {
            return new ValidationResult(false, "Age cannot exceed 150");
        } return new ValidationResult(true, "Age is valid");
    }
    /**      * Validate positive number      */
    public static ValidationResult validatePositive(Number number) {
        if (number == null) {
            return new ValidationResult(false, "Number cannot be null");
        }
        if (number.doubleValue() <= 0) {
            return new ValidationResult(false, "Number must be positive");
        } return new ValidationResult(true, "Number is valid");
    }
    /**      * Validate non-negative number      */
    public static ValidationResult validateNonNegative(Number number) {
        if (number == null) {
            return new ValidationResult(false, "Number cannot be null");
        }
        if (number.doubleValue() < 0) {
            return new ValidationResult(false, "Number cannot be negative");
        } return new ValidationResult(true, "Number is valid");
    }
    /**      * Validate string length      */
    public static ValidationResult validateLength(String input, int minLength, int maxLength) {
        if (input == null) {
            return new ValidationResult(false, "Input cannot be null");
        }
        if (input.length() < minLength) {
            return new ValidationResult(false, String.format("Input must be at least %d characters", minLength));
        }
        if (input.length() > maxLength) {
            return new ValidationResult(false, String.format("Input cannot exceed %d characters", maxLength));
        } return new ValidationResult(true, "Input length is valid");
    }
    /**      * Validate numeric range      */
    public static ValidationResult validateRange(Number number, double min, double max) {
        if (number == null) {
            return new ValidationResult(false, "Number cannot be null");
        } double value = number.doubleValue();
        if (value < min) {
            return new ValidationResult(false, String.format("Number must be at least %f", min));
        }
        if (value > max) {
            return new ValidationResult(false, String.format("Number cannot exceed %f", max));
        } return new ValidationResult(true, "Number is within range");
    }
    /**      * Validate required field      */
    public static ValidationResult validateRequired(Object value, String fieldName) {
        if (value == null) {
            return new ValidationResult(false, fieldName + " is required");
        }
        if (value instanceof String &&((String) value) .trim() .isEmpty()) {
            return new ValidationResult(false, fieldName + " cannot be empty");
        } return new ValidationResult(true, fieldName + " is valid");
    }
    /**      * Validate enum value      */
    public static <T extends Enum<T>> ValidationResult validateEnum(String value, Class<T> enumClass) {
        if (value == null || value.isEmpty()) {
            return new ValidationResult(false, "Enum value cannot be empty");
        }
        try {
            Enum.valueOf(enumClass, value.toUpperCase());
            return new ValidationResult(true, "Enum value is valid");
        } catch (IllegalArgumentException e) {
            return new ValidationResult(false, String.format("Invalid enum value. Valid values: %s", String.join(", ", getEnumNames(enumClass))));
        }
    }
    private static <T extends Enum<T>> String[] getEnumNames(Class<T> enumClass) {
        T[] constants = enumClass.getEnumConstants();
        String[] names = new String[constants.length];
        for (int i = 0;
        i < constants.length;
        i++) {
            names[i] = constants[i].name();
        } return names;
    }
}
