package com.footballacademy.controllers_mvc;

import com.footballacademy.model.Role;
import com.footballacademy.model.User;
import com.footballacademy.repository.RoleRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.admin.AdminService;
import com.footballacademy.services.auth.AuthService;
import com.footballacademy.services.roles.RoleService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public
class AdminUsersManageController {
    private final AuthService authService;
    private final AdminService adminService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AcademyAccessService academyAccessService;
    public AdminUsersManageController(AuthService authService, AdminService adminService, UserRepository userRepository, RoleRepository roleRepository, RoleService roleService, PasswordEncoder passwordEncoder, AcademyAccessService academyAccessService) {
        this.authService = authService;
        this.adminService = adminService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.academyAccessService = academyAccessService;
    }
    @PostMapping("/users")
    public String create(
    @RequestParam Map<String, String> form, RedirectAttributes redirectAttributes) {
        try {
            User.UserRole role = parseRole(form.get("mainRole"));
            ensureRole(role);
            User user = new User();
            applyEditableFields(user, form);
            user.setMainRole(role);
            user.setMdp(required(form.get("mdp"), "Password is required"));
            authService.register(user);
            return "redirect:/admin/view/users?saved=true";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/view/users/new";
        }
    }
    @PostMapping("/users/{id}")
    public String update(
    @PathVariable Long id,
    @RequestParam Map<String, String> form, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id) .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
            academyAccessService.assertCanAccessUser(user);
            User.UserRole role = parseRole(form.get("mainRole"));
            applyEditableFields(user, form);
            if (form.get("mdp") != null && !form.get("mdp") .isBlank()) {
                user.setMdp(passwordEncoder.encode(form.get("mdp")));
            } user.setMainRole(role);
            syncBaseRole(user, role);
            user = userRepository.save(user);
            roleService.createRoleSpecificEntity(user);
            return "redirect:/admin/view/users?updated=true";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/view/users/" + id + "/edit";
        }
    }
    @PostMapping("/users/{id}/assign-role")
    public String assignRole(
    @PathVariable Long id,
    @RequestParam("mainRole") String mainRole, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id) .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
            academyAccessService.assertCanAccessUser(user);
            // only role assignment (no editing other fields)             
            User.UserRole role = parseRole(mainRole);
            user.setMainRole(role);
            syncBaseRole(user, role);
            user = userRepository.save(user);
            roleService.createRoleSpecificEntity(user);
            return "redirect:/admin/view/users?updated=true";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/view/users";
        }
    }
    @PostMapping("/users/{id}/delete")
    public String delete(
    @PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteUser(id);
            return "redirect:/admin/view/users?deleted=true";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/view/users";
        }
    }
    private void applyEditableFields(User user, Map<String, String> form) {
        user.setNom(required(form.get("nom"), "Full name is required"));
        user.setEmail(required(form.get("email"), "Email is required"));
        user.setTel(blankToNull(form.get("tel")));
        user.setDateNaiss(parseDate(form.get("dateNaiss")));
        user.setActive(form.containsKey("active"));
        user.setProfilePhotoUrl(blankToNull(form.get("profilePhotoUrl")));
        user.setBio(blankToNull(form.get("bio")));
    }
    private User.UserRole parseRole(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("Role is required");
        } User.UserRole role = User.UserRole.valueOf(raw.trim() .toUpperCase());
        if (role == User.UserRole.SUPER_ADMIN && !academyAccessService.isSuperAdmin()) {
            throw new RuntimeException("Only a super admin can create or assign super admin accounts");
        } return role;
    }
    private void syncBaseRole(User user, User.UserRole role) {
        Role roleEntity = ensureRole(role);
        user.getRoles() .clear();
        user.addRole(roleEntity);
    }
    private Role ensureRole(User.UserRole role) {
        return roleRepository.findByName(role.name()) .orElseGet(() -> roleRepository.save(new Role(role.name())));
    }
    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        } return LocalDate.parse(raw);
    }
    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        } return value.trim();
    }
    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
