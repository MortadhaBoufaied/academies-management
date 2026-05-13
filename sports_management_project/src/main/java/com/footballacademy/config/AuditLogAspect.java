package com.footballacademy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public
class AuditLogAspect {
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_LOGGER");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Around("@annotation(com.footballacademy.annotation.Auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;
        String className = joinPoint.getSignature() .getDeclaringTypeName();
        String methodName = joinPoint.getSignature() .getName();
        String action = className + "." + methodName;
        // Get user information
        String username = getCurrentUsername();
        String userId = getCurrentUserId();
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            // Create audit log entry
            try {
                Map<String, Object> auditEntry = new HashMap<>();
                auditEntry.put("timestamp", LocalDateTime.now() .toString());
                auditEntry.put("action", action);
                auditEntry.put("username", username);
                auditEntry.put("userId", userId);
                auditEntry.put("duration", duration);
                auditEntry.put("status", exception == null ? "SUCCESS" : "FAILURE");
                
                if (exception != null) {
                    auditEntry.put("error", exception.getMessage());
                    auditEntry.put("errorType", exception.getClass() .getSimpleName());
                }
                
                // Add method parameters (sanitized)
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    Map<String, Object> parameters = new HashMap<>();
                    for (int i = 0; i < args.length; i++) {
                        String paramName = "param" + i;
                        Object paramValue = sanitizeParameter(args[i]);
                        parameters.put(paramName, paramValue);
                    }
                    auditEntry.put("parameters", parameters);
                }
                
                // Log the audit entry
                String auditJson = objectMapper.writeValueAsString(auditEntry);
                if (exception == null) {
                    auditLogger.info("AUDIT: {}", auditJson);
                } else {
                    auditLogger.error("AUDIT: {}", auditJson);
                }
            } catch (Exception e) {
                auditLogger.error("Failed to serialize audit log: {}", e.getMessage());
            }
        }
    }
    @Around("execution(* com.footballacademy.controllers_rest..*.*(..))")
    public Object auditControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature() .getDeclaringTypeName();
        String methodName = joinPoint.getSignature() .getName();
        // Only audit certain controller methods
        if (shouldAudit(className, methodName)) {
            return auditMethod(joinPoint);
        }
        return joinPoint.proceed();
    }
    private boolean shouldAudit(String className, String methodName) {
        // Audit methods that modify data
        String[] modifyingMethods = {
            "create", "update", "delete", "save", "remove", "add", "post", "put", "patch"
        };
        for (String modifyingMethod : modifyingMethods) {
            if (methodName.toLowerCase() .contains(modifyingMethod)) {
                return true;
            }
        }
        // Audit authentication methods
        if (className.contains("AuthController")) {
            return true;
        }
        // Audit payment methods
        if (className.contains("PaymentController")) {
            return true;
        } return false;
    }
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext() .getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignore security context errors
        } return "anonymous";
    }
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext() .getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal != null) {
                    return principal.toString();
                }
            }
        } catch (Exception e) {
            // Ignore security context errors
        } return "unknown";
    }
    private Object sanitizeParameter(Object param) {
        if (param == null) {
            return null;
        }
        if (param instanceof HttpServletRequest request) {
            Map<String, Object> requestSummary = new HashMap<>();
            requestSummary.put("method", request.getMethod());
            requestSummary.put("uri", request.getRequestURI());
            requestSummary.put("remoteAddr", request.getRemoteAddr());
            requestSummary.put("contentType", request.getContentType());
            return requestSummary;
        }
        if (param instanceof ServletRequest) {
            return "[SERVLET_REQUEST]";
        }
        if (param instanceof ServletResponse) {
            return "[SERVLET_RESPONSE]";
        }
        if (param instanceof MultipartFile file) {
            Map<String, Object> fileSummary = new HashMap<>();
            fileSummary.put("name", file.getName());
            fileSummary.put("originalFilename", file.getOriginalFilename());
            fileSummary.put("contentType", file.getContentType());
            fileSummary.put("size", file.getSize());
            return fileSummary;
        }
        // Don't log sensitive data
        String paramStr = param.toString() .toLowerCase();
        if (paramStr.contains("password") || paramStr.contains("pwd") || paramStr.contains("secret") || paramStr.contains("token") || paramStr.contains("credit") || paramStr.contains("card")) {
            return "[REDACTED]";
        }
        // Limit string length
        if (param instanceof String) {
            String str =(String) param;
            if (str.length() > 100) {
                return str.substring(0, 100) + "...";
            }
        } return param.toString();
    }
}
