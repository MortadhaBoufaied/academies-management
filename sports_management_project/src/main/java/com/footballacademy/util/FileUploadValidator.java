package com.footballacademy.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class FileUploadValidator {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;      // 10 MB
    private static final long MAX_IMAGE_SIZE = 5L * 1024L * 1024L;      // 5 MB
    private static final long MAX_DOCUMENT_SIZE = 10L * 1024L * 1024L;  // 10 MB

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(
            Arrays.asList(
                    "image/jpeg",
                    "image/jpg",
                    "image/png",
                    "image/gif",
                    "image/webp",
                    "image/svg+xml"
            )
    );

    private static final Set<String> ALLOWED_DOCUMENT_TYPES = new HashSet<>(
            Arrays.asList(
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "text/plain",
                    "text/csv"
            )
    );

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList(
                    "jpg", "jpeg", "png", "gif", "webp", "svg",
                    "pdf", "doc", "docx", "xls", "xlsx",
                    "txt", "csv"
            )
    );

    private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(
            Arrays.asList(
                    "exe", "bat", "cmd", "sh", "ps1", "vbs", "js",
                    "jar", "com", "scr", "pif", "vbe", "wsf", "wsc"
            )
    );

    private FileUploadValidator() {
        // Utility class
    }

    // =====================================================
    // === PUBLIC VALIDATION METHODS
    // =====================================================

    public static ValidationResult validateImage(MultipartFile file) {
        return validateFile(file, MAX_IMAGE_SIZE, ALLOWED_IMAGE_TYPES);
    }

    public static ValidationResult validateDocument(MultipartFile file) {
        return validateFile(file, MAX_DOCUMENT_SIZE, ALLOWED_DOCUMENT_TYPES);
    }

    public static ValidationResult validateFile(MultipartFile file) {
        return validateFile(file, MAX_FILE_SIZE, null);
    }

    // =====================================================
    // === CORE VALIDATION LOGIC
    // =====================================================

    private static ValidationResult validateFile(
            MultipartFile file,
            long maxSize,
            Set<String> allowedContentTypes
    ) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.invalid("File is empty or not provided");
        }

        if (file.getSize() > maxSize) {
            return ValidationResult.invalid(
                    String.format("File size exceeds maximum allowed size of %d bytes", maxSize)
            );
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return ValidationResult.invalid("File name is empty");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ValidationResult.invalid(
                    String.format("File extension '.%s' is not allowed", extension)
            );
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return ValidationResult.invalid("File content type is not specified");
        }

        if (allowedContentTypes != null && !allowedContentTypes.contains(contentType)) {
            return ValidationResult.invalid(
                    String.format("File content type '%s' is not allowed", contentType)
            );
        }

        if (containsMaliciousPatterns(originalFilename)) {
            return ValidationResult.invalid("File name contains malicious patterns");
        }

        return ValidationResult.valid();
    }

    // =====================================================
    // === HELPERS
    // =====================================================

    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    private static boolean containsMaliciousPatterns(String filename) {
        String lower = filename.toLowerCase();

        // Path traversal
        if (lower.contains("..") || lower.contains("/") || lower.contains("\\")) {
            return true;
        }

        // Null byte injection
        if (lower.contains("\0")) {
            return true;
        }

        // Executable file extensions
        String extension = getFileExtension(lower);
        return DANGEROUS_EXTENSIONS.contains(extension);
    }

    public static String sanitizeFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }

        String sanitized = filename.replaceAll(".*[/\\\\]", "");
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "_");

        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }

        return sanitized.isBlank() ? "file" : sanitized;
    }

    // =====================================================
    // === CONSTANT ACCESSORS
    // =====================================================

    public static long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    public static long getMaxImageSize() {
        return MAX_IMAGE_SIZE;
    }

    public static long getMaxDocumentSize() {
        return MAX_DOCUMENT_SIZE;
    }

    // =====================================================
    // === RESULT TYPE
    // =====================================================

    public static class ValidationResult {

        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, "File is valid");
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
