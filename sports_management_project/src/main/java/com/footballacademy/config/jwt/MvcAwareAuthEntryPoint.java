package com.footballacademy.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import java.io.IOException;
import java.util.Map;

public
class MvcAwareAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String uri = request.getRequestURI() == null ? "" : request.getRequestURI();
        String context = request.getContextPath() == null ? "" : request.getContextPath();
        String adminPath = context + "/admin/view/";
        String superAdminPath = context + "/super-admin/";
        if (uri.startsWith(adminPath) || uri.startsWith(superAdminPath)) {
            String loginUrl = context + "/admin/view/auth/login?error=true";
            response.sendRedirect(loginUrl);
            return;
        } response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(), Map.of("error", "Unauthorized", "path", uri, "message", authException.getMessage()));
    }
}
