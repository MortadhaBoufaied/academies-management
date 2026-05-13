package com.footballacademy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private static final int CACHE_PERIOD_SECONDS = 3600;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // ==================================
        // Uploaded files (local filesystem)
        // ==================================
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(CACHE_PERIOD_SECONDS);

        // ==================================
        // Public static resources
        // ==================================
        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/")
                .setCachePeriod(CACHE_PERIOD_SECONDS);

        // ==================================
        // Application assets
        // ==================================
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(CACHE_PERIOD_SECONDS);

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/assets/")
                .setCachePeriod(CACHE_PERIOD_SECONDS);
    }
}
