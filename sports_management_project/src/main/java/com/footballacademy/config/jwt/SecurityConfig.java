package com.footballacademy.config.jwt;

import com.footballacademy.services.auth.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

@Configuration
@EnableMethodSecurity
public
class SecurityConfig {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    public static final List<String> PUBLIC_PATHS = List.of("/api/auth/**", "/api/support/**", "/api/mobile/**", "/admin/view/auth/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/assets/**", "/uploads/**", "/chatbotFiles/**", "/ws/**", "/error", "/favicon.ico");
    public SecurityConfig(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userDetailsService, PUBLIC_PATHS);
    }
    @Bean
    public MvcAwareAuthEntryPoint mvcAwareAuthEntryPoint() {
        return new MvcAwareAuthEntryPoint();
    }
    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http .csrf(csrf -> csrf.disable()) .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) .exceptionHandling(e -> e.authenticationEntryPoint(mvcAwareAuthEntryPoint())) .authorizeHttpRequests(auth -> auth .requestMatchers(PUBLIC_PATHS.toArray(String[]::new)) .permitAll() .requestMatchers("/admin/view/auth/**") .permitAll() .requestMatchers("/api/super-admin/**") .hasRole("SUPER_ADMIN") .requestMatchers("/super-admin/**") .hasRole("SUPER_ADMIN") .requestMatchers("/api/admin/**") .hasAnyRole("ADMIN", "SUPER_ADMIN") .requestMatchers("/admin/view/**") .hasAnyRole("ADMIN", "SUPER_ADMIN") .requestMatchers("/admin/**") .hasAnyRole("ADMIN", "SUPER_ADMIN") .requestMatchers("/api/**") .authenticated() .anyRequest() .authenticated()) .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.
        class);
        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return new ProviderManager(daoAuthenticationProvider());
    }
}
