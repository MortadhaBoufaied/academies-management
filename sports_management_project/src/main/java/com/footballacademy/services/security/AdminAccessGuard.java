package com.footballacademy.services.security;

import com.footballacademy.model.User;
import com.footballacademy.security.SecurityUtils;
import com.footballacademy.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public
class AdminAccessGuard {
    public User requireAuthenticatedUser() {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        if (principal == null || principal.getUser() == null) {
            throw new AccessDeniedException("Authentication is required");
        } return principal.getUser();
    }
    public User requireAdminOrSuperAdmin() {
        User user = requireAuthenticatedUser();
        if (!user.hasRole("ADMIN") && !user.hasRole("SUPER_ADMIN")) {
            throw new AccessDeniedException("Admin access is required");
        } return user;
    }
    public User requireSuperAdmin() {
        User user = requireAuthenticatedUser();
        if (!user.hasRole("SUPER_ADMIN")) {
            throw new AccessDeniedException("Super admin access is required");
        } return user;
    }
    public Long requireVisibleAcademy(Long requestedAcademyId) {
        User user = requireAdminOrSuperAdmin();
        if (user.hasRole("SUPER_ADMIN")) {
            return requestedAcademyId;
        } Long ownAcademyId = user.getAcademyId();
        if (ownAcademyId == null) {
            throw new AccessDeniedException("Current admin is not assigned to an academy");
        }
        if (requestedAcademyId != null && !ownAcademyId.equals(requestedAcademyId)) {
            throw new AccessDeniedException("You cannot access another academy's data");
        } return ownAcademyId;
    }
}
