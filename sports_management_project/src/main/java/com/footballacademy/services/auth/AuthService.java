package com.footballacademy.services.auth;

import com.footballacademy.model.Academy;
import com.footballacademy.model.Role;
import com.footballacademy.model.User;
import com.footballacademy.repository.AcademyRepository;
import com.footballacademy.repository.RoleRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.security.SecurityUtils;
import com.footballacademy.security.UserPrincipal;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.roles.RoleService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public
class AuthService {
    // =============================
    // Repositories & Services
    // =============================
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AcademyRepository academyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleService roleService;
    private final AcademyAccessService academyAccessService;
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, AcademyRepository academyRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, RoleService roleService, AcademyAccessService academyAccessService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.academyRepository = academyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.roleService = roleService;
        this.academyAccessService = academyAccessService;
    }
    // ==================================================
    // === AUTHENTICATION
    // ==================================================
    public Map<String, Object> authenticate(String email, String rawPassword) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, rawPassword));
        User user = userRepository.findByEmail(email) .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRegistrationDate() == null) {
            user.setRegistrationDate(LocalDateTime.now());
        }
        if (user.getLoginCount() == null) {
            user.setLoginCount(0L);
        } user.setLastLogin(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userRepository.save(user);
        UserPrincipal principal = new UserPrincipal(user, null);
        String accessToken = jwtService.generateToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("redirectUrl", redirectUrlFor(user));
        return response;
    }
    // ==================================================
    // === REGISTRATION
    // ==================================================
    public User register(User user) {
        return register(user, null);
    }
    public User register(User user, Long academyId) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (user.getMainRole() == null) {
            throw new RuntimeException("User role is required");
        } assignAcademyForRegistration(user, academyId);
        if (user.getActive() == null) {
            user.setActive(true);
        }
        if (user.getRegistrationDate() == null) {
            user.setRegistrationDate(LocalDateTime.now());
        }
        if (user.getLoginCount() == null) {
            user.setLoginCount(0L);
        }
        // Encode password
        user.setMdp(passwordEncoder.encode(user.getMdp()));
        User savedUser = userRepository.save(user);
        // Attach base role entity (populate user_roles table)
        User finalSavedUser = savedUser;
        Role baseRole = roleRepository.findByName(savedUser.getMainRole() .name()) .orElseThrow(() -> new RuntimeException("Role not found: " + finalSavedUser.getMainRole() .name()));
        savedUser.getRoles() .add(baseRole);
        savedUser = userRepository.save(savedUser);
        // Create role-specific entity (Admin, Trainer, Player, etc.)
        roleService.createRoleSpecificEntity(savedUser);
        return savedUser;
    }
    // ==================================================
    // === ACADEMY ASSIGNMENT LOGIC
    // ==================================================
    private void assignAcademyForRegistration(User user, Long academyId) {
        // Super admin never belongs to an academy
        if (user.getMainRole() == User.UserRole.SUPER_ADMIN) {
            user.setAcademy(null);
            return;
        }
        // Roles that do not require academy
        if (!academyAccessService.roleRequiresAcademy(user.getMainRole())) {
            return;
        }
        // Already assigned
        if (user.getAcademy() != null) {
            return;
        } Long resolvedAcademyId = academyId != null ? academyId : academyAccessService.currentAcademyId();
        if (resolvedAcademyId == null && academyRepository.count() == 1) {
            resolvedAcademyId = academyRepository.findAll() .get(0) .getId();
        }
        if (resolvedAcademyId == null) {
            throw new RuntimeException("Academy is required for role: " + user.getMainRole());
        }
        if (SecurityUtils.getCurrentUser() != null) {
            academyAccessService.assertCanAccessAcademy(resolvedAcademyId);
        } Long finalResolvedAcademyId = resolvedAcademyId;
        Academy academy = academyRepository.findById(resolvedAcademyId) .orElseThrow(() -> new RuntimeException("Academy not found: " + finalResolvedAcademyId));
        user.setAcademy(academy);
    }
    // ==================================================
    // === JWT / SESSION UTILITIES
    // ==================================================
    public Map<String, String> refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null) {
            throw new RuntimeException("Invalid refresh token");
        } User user = userRepository.findByEmail(userEmail) .orElseThrow(() -> new RuntimeException("User not found"));
        UserPrincipal principal = new UserPrincipal(user, null);
        if (!jwtService.isTokenValid(refreshToken, principal)) {
            throw new RuntimeException("Invalid refresh token");
        } String newAccessToken = jwtService.generateToken(principal);
        return Map.of("accessToken", newAccessToken);
    }
    public void logout(String token) {
        if (!jwtService.isTokenValid(token)) {
            throw new RuntimeException("Invalid token");
        }
        // Stateless JWT → nothing to do
    }
    public User getCurrentUser(String token) {
        String email = jwtService.extractUsername(token);
        return userRepository.findByEmail(email) .orElseThrow(() -> new RuntimeException("User not found"));
    }
    // ==================================================
    // === ROLE / NAVIGATION HELPERS
    // ==================================================
    public String redirectUrlFor(User user) {
        if (user != null && user.hasRole("SUPER_ADMIN")) {
            return "/super-admin/dashboard";
        }
        if (user != null && user.hasRole("ADMIN")) {
            return "/admin/view/dashboard";
        } return "/api/profile/me-lite";
    }
    public boolean canAccessAdminConsole(User user) {
        return user != null &&(user.hasRole("ADMIN") || user.hasRole("SUPER_ADMIN"));
    }
}
