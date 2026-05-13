package com.footballacademy.config.data;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class DataStaticResourceConfig implements WebMvcConfigurer {

    private static final int CACHE_PERIOD_SECONDS = 3600;

    private final FileStorageConfig fileStorageConfig;

    public DataStaticResourceConfig(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // ==========================================
        // Uploaded files (dynamic filesystem path)
        // ==========================================
        Path basePath = Paths.get(fileStorageConfig.fileStorageLocation())
                .toAbsolutePath()
                .normalize();

        String uploadLocation = basePath.toUri().toString();
        if (!uploadLocation.endsWith("/")) {
            uploadLocation += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation)
                .setCachePeriod(CACHE_PERIOD_SECONDS);

        // ==========================================
        // Chatbot static files
        // ==========================================
        registry.addResourceHandler("/chatbotFiles/**")
                .addResourceLocations("file:src/main/resources/Files/chatbotFiles/")
                .setCachePeriod(CACHE_PERIOD_SECONDS);
    }
}
