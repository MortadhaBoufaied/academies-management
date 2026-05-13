package com.footballacademy.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public
class ResponseBuilder {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static <T> Map<String, Object> success(T data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now() .format(TIMESTAMP_FORMATTER));
        return response;
    }
    public static <T> Map<String, Object> success(String message, T data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now() .format(TIMESTAMP_FORMATTER));
        return response;
    }
    public static Map<String, Object> error(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("timestamp", LocalDateTime.now() .format(TIMESTAMP_FORMATTER));
        return response;
    }
    public static Map<String, Object> error(String code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", code);
        response.put("error", message);
        response.put("timestamp", LocalDateTime.now() .format(TIMESTAMP_FORMATTER));
        return response;
    }
    public static Map<String, Object> error(String code, String message, Object details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", code);
        response.put("error", message);
        response.put("details", details);
        response.put("timestamp", LocalDateTime.now() .format(TIMESTAMP_FORMATTER));
        return response;
    }
    public static Map<String, Object> paginated(Object data, int page, int size, long totalElements, int totalPages) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now() .format(TIMESTAMP_FORMATTER));
        Map<String, Object> pagination = new HashMap<>();
        pagination.put("page", page);
        pagination.put("size", size);
        pagination.put("totalElements", totalElements);
        pagination.put("totalPages", totalPages);
        pagination.put("first", page == 0);
        pagination.put("last", page >= totalPages - 1);
        response.put("pagination", pagination);
        return response;
    }
    public static Map<String, Object> created(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Resource created successfully");
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now() .format(TIMESTAMP_FORMATTER));
        return response;
    }
    public static Map<String, Object> updated(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Resource updated successfully");
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now() .format(TIMESTAMP_FORMATTER));
        return response;
    }
    public static Map<String, Object> deleted() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Resource deleted successfully");
        response.put("timestamp", LocalDateTime.now() .format(TIMESTAMP_FORMATTER));
        return response;
    }
    public static Map<String, Object> notFound(String resource) {
        return error("NOT_FOUND", resource + " not found");
    }
    public static Map<String, Object> badRequest(String message) {
        return error("BAD_REQUEST", message);
    }
    public static Map<String, Object> unauthorized(String message) {
        return error("UNAUTHORIZED", message);
    }
    public static Map<String, Object> forbidden(String message) {
        return error("FORBIDDEN", message);
    }
    public static Map<String, Object> conflict(String message) {
        return error("CONFLICT", message);
    }
    public static Map<String, Object> internalError(String message) {
        return error("INTERNAL_ERROR", message);
    }
}
