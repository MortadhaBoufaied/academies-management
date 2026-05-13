package com.footballacademy.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.IOException;
import java.util.UUID;

@Configuration
public
class RequestValidationConfig implements WebMvcConfigurer {
    private static final long MAX_REQUEST_SIZE_BYTES = 10 * 1024 * 1024;
    // 10 MB
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestSizeInterceptor()) .addPathPatterns("/api/**");
    }
    /**
    * ===========================
    * Request size validation
    * ===========================
    */
    static
    class RequestSizeInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            long contentLength = request.getContentLengthLong();
            if (contentLength > MAX_REQUEST_SIZE_BYTES) {
                response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter() .write("{\"error\":\"Request entity too large (max 10MB)\"}");
                return false;
            } return true;
        }
    }
    /**
    * ===========================
    * Security headers filter
    * ===========================
    */
    @Bean
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
                // Security headers
                response.setHeader("X-Content-Type-Options", "nosniff");
                response.setHeader("X-Frame-Options", "DENY");
                response.setHeader("X-XSS-Protection", "1; mode=block");
                if (request.isSecure()) {
                    response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                } response.setHeader("Content-Security-Policy", "default-src 'self'; " + "script-src 'self' https://cdn.tailwindcss.com https://js.stripe.com; " + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " + "img-src 'self' data: https:; " + "font-src 'self' https://fonts.gstatic.com; " + "connect-src 'self' https://api.stripe.com; " + "frame-src 'self' https://js.stripe.com;");
                // API metadata
                response.setHeader("X-API-Version", "1.0.0");
                response.setHeader("X-Request-ID", UUID.randomUUID() .toString());
                filterChain.doFilter(request, response);
            }
        };
    }
}
