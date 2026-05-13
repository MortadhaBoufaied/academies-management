package com.footballacademy.util;

import java.util.regex.Pattern;

public class InputSanitizer {
    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]*>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("('(''|[^'])*')|(;)|(\b(ALTER|CREATE|DELETE|DROP|EXEC(UTE){0,1}|INSERT( +INTO){0,1}|MERGE|SELECT|UPDATE|UNION( +ALL){0,1})\b)", Pattern.CASE_INSENSITIVE);
    public static String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String sanitized = SCRIPT_PATTERN.matcher(input).replaceAll("");
        sanitized = HTML_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = sanitized.trim();
        return sanitized;
    }
    public static String sanitizeForSql(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            throw new SecurityException("Potential SQL injection detected");
        }
        return input.replace("'", "''");
    }
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "file";
        }
        String sanitized = filename.replaceAll(".*[/\\\\]", "");
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (sanitized.length() > 255) {
            sanitized = sanitized.substring(0, 255);
        }
        return sanitized.isEmpty() ? "file" : sanitized;
    }
    public static String sanitizeEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }
        return email.trim().toLowerCase();
    }
    public static String sanitizePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }
        return phone.replaceAll("[^0-9+]", "");
    }
    public static String sanitizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        String sanitized = url.trim();
        if (!sanitized.matches("^(http|https)://.*")) {
            sanitized = "https://" + sanitized;
        }
        return sanitized;
    }
    public static String truncate(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        if (input.length() <= maxLength) {
            return input;
        }
        return input.substring(0, maxLength);
    }
    public static String removeControlCharacters(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[\\x00-\\x1F\\x7F]", "");
    }
    public static boolean containsOnlySafeCharacters(String input, String allowedPattern) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        return input.matches(allowedPattern);
    }
    public static String sanitizeJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        String sanitized = removeControlCharacters(json);
        sanitized = sanitized.trim();
        return sanitized;
    }
    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
    public static String escapeJavaScript(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
