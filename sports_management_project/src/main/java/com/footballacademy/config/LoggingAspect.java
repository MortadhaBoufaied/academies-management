package com.footballacademy.config;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Aspect
@Component
public
class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private static final int MAX_PREVIEW_LENGTH = 160;
    @Around("execution(* com.footballacademy.controllers_rest..*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        long startTime = System.currentTimeMillis();
        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";
        String className = joinPoint.getSignature() .getDeclaringTypeName();
        String methodName = joinPoint.getSignature() .getName();
        if (request != null) {
            MDC.put("request.method", method);
            MDC.put("request.uri", uri);
            MDC.put("request.remoteAddr", request.getRemoteAddr());
            MDC.put("request.userAgent", safeHeader(request.getHeader("User-Agent")));
        }
        try {
            logger.info("ENTER: {}.{}({})", className, methodName, sanitizeArgs(joinPoint.getArgs()));
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("EXIT: {}.{} status=success resultType={} executionTimeMs={}", className, methodName, result == null ? "void" : result.getClass() .getSimpleName(), executionTime);
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("ERROR: {}.{} status=failure errorType={} message={} executionTimeMs={}", className, methodName, e.getClass() .getSimpleName(), safeText(e.getMessage()), executionTime, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
    @Around("execution(* com.footballacademy.services..*.*(..))")
    public Object logServiceLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature() .getDeclaringTypeName();
        String methodName = joinPoint.getSignature() .getName();
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            if (executionTime > 1000) {
                logger.warn("SLOW SERVICE: {}.{} took {}ms", className, methodName, executionTime);
            } else {
                logger.debug("SERVICE: {}.{} completed in {}ms", className, methodName, executionTime);
            } return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("SERVICE ERROR: {}.{} failed after {}ms: {}", className, methodName, executionTime, safeText(e.getMessage()), e);
            throw e;
        }
    }
    @Around("execution(* com.footballacademy.repository..*.*(..))")
    public Object logRepositoryLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature() .getDeclaringTypeName();
        String methodName = joinPoint.getSignature() .getName();
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            if (executionTime > 500) {
                logger.warn("SLOW QUERY: {}.{} took {}ms", className, methodName, executionTime);
            } return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("REPOSITORY ERROR: {}.{} failed after {}ms: {}", className, methodName, executionTime, safeText(e.getMessage()));
            throw e;
        }
    }
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =(ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (Exception e) {
            return null;
        }
    }
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        } return Arrays.stream(args) .map(this::sanitizeArg) .collect(Collectors.toList()) .toString();
    }
    private String sanitizeArg(Object arg) {
        if (arg == null) {
            return "null";
        }
        if (arg instanceof ServletRequest) {
            return "[SERVLET_REQUEST]";
        }
        if (arg instanceof ServletResponse) {
            return "[SERVLET_RESPONSE]";
        }
        if (arg instanceof MultipartFile) {
            MultipartFile file =(MultipartFile) arg;
            return "[FILE name=" + safeText(file.getOriginalFilename()) + ", size=" + file.getSize() + "]";
        } String value = String.valueOf(arg);
        String lowered = value.toLowerCase(Locale.ROOT);
        if (lowered.contains("password") || lowered.contains("pwd") || lowered.contains("secret") || lowered.contains("token") || lowered.contains("authorization") || lowered.contains("credit") || lowered.contains("card") || lowered.contains("mdp")) {
            return "[REDACTED]";
        } return safeText(value);
    }
    private String safeHeader(String value) {
        return value == null ? null : safeText(value);
    }
    private String safeText(String value) {
        if (value == null) {
            return null;
        } String compact = value.replaceAll("[\\r\\n\\t]+", " ") .trim();
        return compact.length() > MAX_PREVIEW_LENGTH ? compact.substring(0, MAX_PREVIEW_LENGTH) + "..." : compact;
    }
}
