package com.footballacademy.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public
class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**") .addResourceLocations("file:uploads/");
        registry.addResourceHandler("/chatbotFiles/**") .addResourceLocations("file:src/main/resources/Files/chatbotFiles/");
    }
}
