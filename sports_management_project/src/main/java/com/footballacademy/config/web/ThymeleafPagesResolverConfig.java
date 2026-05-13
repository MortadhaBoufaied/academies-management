package com.footballacademy.config.web;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Configuration
public
class ThymeleafPagesResolverConfig {
    @Bean
    public ITemplateResolver pagesTemplateResolver(ApplicationContext applicationContext) {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setApplicationContext(applicationContext);
        resolver.setPrefix("classpath:/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCheckExistence(true);
        resolver.setCacheable(false);
        resolver.setOrder(0);
        resolver.setResolvablePatterns(Set.of("pages/*", "pages/**"));
        return resolver;
    }
}
