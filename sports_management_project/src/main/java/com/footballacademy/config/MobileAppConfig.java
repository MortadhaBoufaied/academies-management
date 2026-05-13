package com.footballacademy.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public
class MobileAppConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(MobileAppConfig.class);
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MobileRequestInterceptor()) .addPathPatterns("/api/**");
    }
    public static
    class MobileRequestInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String userAgent = request.getHeader("User-Agent");
            logger.debug("API Request - Method: {}, Path: {}, User-Agent: {}", request.getMethod(), request.getRequestURI(), userAgent != null ? userAgent : "Unknown");
            return true;
        }
    }
}
