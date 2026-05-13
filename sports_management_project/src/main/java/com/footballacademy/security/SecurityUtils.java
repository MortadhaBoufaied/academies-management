package com.footballacademy.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public
class SecurityUtils {
    public static UsernamePasswordAuthenticationToken buildAuthentication(UserDetails userDetails, HttpServletRequest request) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
    public static UserPrincipal getCurrentUser() {
        var auth = SecurityContextHolder.getContext() .getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        } return null;
    }
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
    public static boolean isSuperAdmin() {
        return hasRole("SUPER_ADMIN");
    }
    public static boolean hasRole(String role) {
        if (role == null || role.isBlank()) {
            return false;
        } String normalized = role.trim() .toUpperCase();
        String prefixed = normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
        var auth = SecurityContextHolder.getContext() .getAuthentication();
        return auth != null && auth.getAuthorities() .stream() .anyMatch(grantedAuthority -> grantedAuthority.getAuthority() .equals(prefixed));
    }
    public static Long currentAcademyId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getAcademyId() : null;
    }
}
