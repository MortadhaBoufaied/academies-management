package com.footballacademy.services.academy;

import com.footballacademy.model.Academy;
import com.footballacademy.model.Admin;
import com.footballacademy.model.Division;
import com.footballacademy.model.Sport;
import com.footballacademy.model.User;
import com.footballacademy.repository.AcademyRepository;
import com.footballacademy.repository.AdminRepository;
import com.footballacademy.security.SecurityUtils;
import com.footballacademy.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public
class AcademyAccessService {
    private final AcademyRepository academyRepository;
    private final AdminRepository adminRepository;
    public AcademyAccessService(AcademyRepository academyRepository, AdminRepository adminRepository) {
        this.academyRepository = academyRepository;
        this.adminRepository = adminRepository;
    }
    public boolean isSuperAdmin() {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        return principal != null && principal.getUser() != null && principal.getUser() .hasRole("SUPER_ADMIN");
    }
    public User currentUser() {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        return principal != null ? principal.getUser() : null;
    }
    public Long currentUserId() {
        User user = currentUser();
        return user != null ? user.getId() : null;
    }
    public Long currentAcademyId() {
        User user = currentUser();
        return user != null ? user.getAcademyId() : null;
    }
    public Academy currentAcademyOrThrow() {
        Long academyId = currentAcademyId();
        if (academyId == null) {
            throw new AccessDeniedException("Current user is not assigned to an academy");
        } return academyRepository.findById(academyId) .orElseThrow(() -> new AccessDeniedException("Current user's academy no longer exists"));
    }
    public Long currentSportId() {
        if (isSuperAdmin()) {
            return null;
        } Academy academy = currentAcademyOrThrow();
        return academy.getSport() != null ? academy.getSport() .getId() : null;
    }
    public Long resolveAcademyId(Long requestedAcademyId) {
        if (isSuperAdmin()) {
            return requestedAcademyId;
        } Long currentAcademyId = currentAcademyId();
        if (currentAcademyId == null) {
            throw new AccessDeniedException("Academy-scoped users must belong to an academy");
        }
        if (requestedAcademyId != null && !requestedAcademyId.equals(currentAcademyId)) {
            throw new AccessDeniedException("You cannot access another academy's data");
        } return currentAcademyId;
    }
    public void assertCanAccessAcademy(Long academyId) {
        if (academyId == null) {
            if (!isSuperAdmin()) {
                throw new AccessDeniedException("Academy id is required");
            } return;
        } resolveAcademyId(academyId);
    }
    public boolean canAccessAcademy(Academy academy) {
        if (isSuperAdmin()) {
            return true;
        } Long academyId = academy != null ? academy.getId() : null;
        Long currentAcademyId = currentAcademyId();
        return academyId != null && academyId.equals(currentAcademyId);
    }
    public void assertCanAccessAcademy(Academy academy) {
        if (!canAccessAcademy(academy)) {
            throw new AccessDeniedException("You cannot access another academy's data");
        }
    }
    public boolean canAccessSport(Sport sport) {
        if (isSuperAdmin()) {
            return true;
        } Long sportId = sport != null ? sport.getId() : null;
        Long currentSportId = currentSportId();
        return sportId != null && sportId.equals(currentSportId);
    }
    public void assertCanAccessSport(Sport sport) {
        if (!canAccessSport(sport)) {
            throw new AccessDeniedException("You cannot access data for another sport");
        }
    }
    public boolean canAccessDivision(Division division) {
        if (division == null) {
            return false;
        }
        if (isSuperAdmin()) {
            return true;
        }
        if (division.getAcademy() != null) {
            return canAccessAcademy(division.getAcademy());
        } return canAccessSport(division.getSport());
    }
    public void assertCanAccessDivision(Division division) {
        if (!canAccessDivision(division)) {
            throw new AccessDeniedException("You cannot access this division");
        }
    }
    public Admin.AdminResponsibility currentAdminResponsibility() {
        User user = currentUser();
        if (user == null) {
            return null;
        } return adminRepository.findByUser_Id(user.getId()) .map(Admin::getResponsibility) .orElse(null);
    }
    public boolean canManageAcademyDivisions() {
        if (isSuperAdmin()) {
            return true;
        } Admin.AdminResponsibility responsibility = currentAdminResponsibility();
        return responsibility == Admin.AdminResponsibility.ACADEMY_DIRECTOR || responsibility == Admin.AdminResponsibility.OPERATIONS_MANAGER || responsibility == Admin.AdminResponsibility.SPORTS_COORDINATOR;
    }
    public void assertCanManageAcademyDivisions() {
        if (!canManageAcademyDivisions()) {
            throw new AccessDeniedException("Only academy leadership or sports operations admins can associate divisions to the academy");
        }
    }
    public void assertCanAccessUser(User user) {
        if (user == null || isSuperAdmin()) {
            return;
        }
        if (user.getMainRole() == User.UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Academy users cannot manage platform owners");
        } assertCanAccessAcademy(user.getAcademy());
    }
    public Academy academyForWrite(Academy requestedAcademy) {
        if (isSuperAdmin()) {
            return requestedAcademy;
        }
        if (requestedAcademy != null) {
            assertCanAccessAcademy(requestedAcademy);
            return requestedAcademy;
        } return currentAcademyOrThrow();
    }
    public boolean roleRequiresAcademy(User.UserRole role) {
        return role != null && role != User.UserRole.SUPER_ADMIN;
    }
}
