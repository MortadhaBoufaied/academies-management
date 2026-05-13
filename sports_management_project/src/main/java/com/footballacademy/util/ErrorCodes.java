package com.footballacademy.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ErrorCodes {
    // General errors (1000-1999)
    public static final String INTERNAL_ERROR = "ERR_1000";
    public static final String INVALID_REQUEST = "ERR_1001";
    public static final String UNAUTHORIZED = "ERR_1002";
    public static final String FORBIDDEN = "ERR_1003";
    public static final String NOT_FOUND = "ERR_1004";
    public static final String CONFLICT = "ERR_1005";
    public static final String RATE_LIMIT_EXCEEDED = "ERR_1006";
    // Validation errors (2000-2999)
    public static final String VALIDATION_ERROR = "ERR_2000";
    public static final String INVALID_EMAIL = "ERR_2001";
    public static final String INVALID_PASSWORD = "ERR_2002";
    public static final String INVALID_PHONE = "ERR_2003";
    public static final String INVALID_DATE = "ERR_2004";
    public static final String INVALID_FORMAT = "ERR_2005";
    // Authentication errors (3000-3999)
    public static final String AUTH_FAILED = "ERR_3000";
    public static final String TOKEN_EXPIRED = "ERR_3001";
    public static final String TOKEN_INVALID = "ERR_3002";
    public static final String CREDENTIALS_INVALID = "ERR_3003";
    public static final String ACCOUNT_LOCKED = "ERR_3004";
    public static final String ACCOUNT_DISABLED = "ERR_3005";
    // User errors (4000-4999)
    public static final String USER_NOT_FOUND = "ERR_4000";
    public static final String USER_EXISTS = "ERR_4001";
    public static final String USER_CREATION_FAILED = "ERR_4002";
    public static final String USER_UPDATE_FAILED = "ERR_4003";
    public static final String USER_DELETE_FAILED = "ERR_4004";
    // Payment errors (5000-5999)
    public static final String PAYMENT_FAILED = "ERR_5000";
    public static final String PAYMENT_NOT_FOUND = "ERR_5001";
    public static final String PAYMENT_ALREADY_EXISTS = "ERR_5002";
    public static final String PAYMENT_INVALID_AMOUNT = "ERR_5003";
    public static final String PAYMENT_GATEWAY_ERROR = "ERR_5004";
    // Notification errors (6000-6999)
    public static final String NOTIFICATION_FAILED = "ERR_6000";
    public static final String NOTIFICATION_NOT_FOUND = "ERR_6001";
    public static final String EMAIL_SEND_FAILED = "ERR_6002";
    public static final String SMS_SEND_FAILED = "ERR_6003";
    // Database errors (7000-7999)
    public static final String DATABASE_ERROR = "ERR_7000";
    public static final String CONNECTION_FAILED = "ERR_7001";
    public static final String QUERY_FAILED = "ERR_7002";
    public static final String CONSTRAINT_VIOLATION = "ERR_7003";
    public static final String DUPLICATE_ENTRY = "ERR_7004";
    // File upload errors (8000-8999)
    public static final String FILE_UPLOAD_FAILED = "ERR_8000";
    public static final String FILE_TOO_LARGE = "ERR_8001";
    public static final String FILE_INVALID_TYPE = "ERR_8002";
    public static final String FILE_NOT_FOUND = "ERR_8003";
    // External service errors (9000-9999)
    public static final String EXTERNAL_SERVICE_ERROR = "ERR_9000";
    public static final String EXTERNAL_SERVICE_TIMEOUT = "ERR_9001";
    public static final String EXTERNAL_SERVICE_UNAVAILABLE = "ERR_9002";
    public static String generateErrorId() {
        return "ERR_" + UUID.randomUUID() .toString() .substring(0, 8) .toUpperCase();
    }
    public static String getTimestamp() {
        return LocalDateTime.now() .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    public static
    class ErrorDetail {
        private final String code;
        private final String message;
        private final String timestamp;
        private final String errorId;
        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
            this.timestamp = getTimestamp();
            this.errorId = generateErrorId();
        }
        public String getCode() {
            return code;
        }
        public String getMessage() {
            return message;
        }
        public String getTimestamp() {
            return timestamp;
        }
        public String getErrorId() {
            return errorId;
        }
    }
}
