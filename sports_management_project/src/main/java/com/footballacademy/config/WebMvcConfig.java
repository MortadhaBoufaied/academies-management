package com.footballacademy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public
class WebMvcConfig implements WebMvcConfigurer {
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:8080}")
    private String[] allowedOrigins;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") .allowedOriginPatterns(allowedOrigins) .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "X-API-Key") .exposedHeaders("Authorization", "Content-Type", "X-Total-Count", "X-Page-Count", "X-Trace-ID", "X-Span-ID", "X-Request-ID") .allowCredentials(true) .maxAge(3600);
        registry.addMapping("/actuator/**") .allowedOriginPatterns(allowedOrigins) .allowedMethods("GET", "OPTIONS") .allowedHeaders("Authorization", "Content-Type") .allowCredentials(false) .maxAge(3600);
    }
}
