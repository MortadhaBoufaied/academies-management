package com.footballacademy.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public
class OpenApiConfig {
    @Bean
    public OpenAPI footballAcademyOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI() .info(new Info() .title("Football Academy Management API") .description("Comprehensive API for managing multi-sport academy operations including players, trainers, parents, payments, and more.") .version("1.0.0") .contact(new Contact() .name("Football Academy Team") .email("support@footballacademy.com") .url("https://footballacademy.com")) .license(new License() .name("MIT License") .url("https://opensource.org/licenses/MIT"))) .servers(List.of(new Server() .url("http://localhost:8080") .description("Development Server"), new Server() .url("https://api.footballacademy.com") .description("Production Server"))) .addSecurityItem(new SecurityRequirement() .addList(securitySchemeName)) .components(new Components() .addSecuritySchemes(securitySchemeName, new SecurityScheme() .name(securitySchemeName) .type(SecurityScheme.Type.HTTP) .scheme("bearer") .bearerFormat("JWT") .description("JWT authentication token. Format: Bearer {token}")) .addSecuritySchemes("apiKey", new SecurityScheme() .name("X-API-Key") .type(SecurityScheme.Type.APIKEY) .in(SecurityScheme.In.HEADER) .description("API key for external integrations")));
    }
}
