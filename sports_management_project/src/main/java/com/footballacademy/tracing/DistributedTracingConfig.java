package com.footballacademy.tracing;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Configuration
public class DistributedTracingConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TracingInterceptor()) .addPathPatterns("/api/**");
    }
    @Bean
    public TraceExporter traceExporter() {
        return new TraceExporter();
    }
    /**
    * ===========================
    * HTTP Tracing Interceptor
    * ===========================
    */
    static class TracingInterceptor implements HandlerInterceptor {
        private static final Logger logger = LoggerFactory.getLogger(TracingInterceptor.class);
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String traceId = getOrGenerate(request.getHeader("X-Trace-ID"));
            String spanId = TracingContext.generateSpanId();
            long startTime = System.currentTimeMillis();
            TracingContext.set(traceId, spanId, startTime);
            MDC.put("traceId", traceId);
            MDC.put("spanId", spanId);
            response.setHeader("X-Trace-ID", traceId);
            response.setHeader("X-Span-ID", spanId);
            return true;
        }
        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            TraceSpan span = new TraceSpan(TracingContext.getTraceId(), TracingContext.getSpanId(), request.getMethod(), request.getRequestURI(), response.getStatus(), System.currentTimeMillis() - TracingContext.getStartTime(), ex != null ? ex.getMessage() : null);
            TraceExporter.export(span);
            // âœ… CRITICAL: prevent memory leaks
            TracingContext.clear();
            MDC.clear();
        }
        private String getOrGenerate(String value) {
            return(value == null || value.isBlank()) ? TracingContext.generateTraceId() : value;
        }
    }
}
