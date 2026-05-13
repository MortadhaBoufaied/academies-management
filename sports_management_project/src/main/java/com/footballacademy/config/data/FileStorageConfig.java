package com.footballacademy.config.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public
class FileStorageConfig {
    @Value("${file.storage.location:src/main/resources/uploads}")
    private String fileStorageLocation;
    public String fileStorageLocation() {
        return fileStorageLocation;
    }
}
