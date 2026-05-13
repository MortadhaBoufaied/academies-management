package com.footballacademy.controllers_mvc;

import com.footballacademy.model.User;
import com.footballacademy.services.admin.AdminService;
import com.footballacademy.services.ui.MvcPaginationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/admin/view")
public
class UsersViewController {
    private final AdminService adminService;
    private final MvcPaginationService mvcPaginationService;
    public UsersViewController(AdminService adminService, MvcPaginationService mvcPaginationService) {
        this.adminService = adminService;
        this.mvcPaginationService = mvcPaginationService;
    }
    @GetMapping("/users")
    public String list(
    @RequestParam(value = "q", required = false) String q,
    @RequestParam(value = "type", required = false) String type,
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        List<User> allUsers = adminService.getAllUsers();
        long unassignedCount = allUsers.stream() .filter(u -> u.getMainRole() == null) .count();
        List<User> users = allUsers;
        String normalizedQuery = q == null ? "" : q.trim() .toLowerCase(Locale.ROOT);
        String normalizedType = type == null || type.isBlank() ? "ALL" : type.trim() .toUpperCase(Locale.ROOT);
        if ("UNASSIGNED" .equals(normalizedType)) {
            users = users.stream() .filter(user -> user.getMainRole() == null) .toList();
        } else
        if (! "ALL" .equals(normalizedType)) {
            users = users.stream() .filter(user -> user.getMainRole() != null && user.getMainRole() .name() .equals(normalizedType)) .toList();
        }
        if (!normalizedQuery.isBlank()) {
            users = users.stream() .filter(user -> contains(user.getNom(), normalizedQuery) || contains(user.getEmail(), normalizedQuery)) .toList();
        } var pagination = mvcPaginationService.paginate(users, page, request);
        model.addAttribute("users", pagination.getItems());
        model.addAttribute("pagination", pagination);
        model.addAttribute("selectedUserType", normalizedType);
        model.addAttribute("unassignedCount", unassignedCount);
        model.addAttribute("userTypes", Arrays.stream(User.UserRole.values()) .filter(role -> role != User.UserRole.SUPER_ADMIN) .toList());
        model.addAttribute("q", q);
        return "pages/modules/data-management/users/list";
    }
    @GetMapping("/users/new")
    public String create(Model model) {
        addUserFormModel(model, new User());
        return "pages/modules/data-management/users/form";
    }
    @GetMapping("/users/{id}")
    public String details(
    @PathVariable Long id, Model model) {
        var userDetails = adminService.getUserWithRoleEntity(id);
        model.addAttribute("userDetails", userDetails);
        model.addAttribute("user", userDetails.getUser());
        return "pages/modules/data-management/users/details";
    }
    @GetMapping("/users/{id}/edit")
    public String edit(
    @PathVariable Long id, Model model) {
        addUserFormModel(model, adminService.getUserWithRoleEntity(id) .getUser());
        return "pages/modules/data-management/users/form";
    }
    @GetMapping("/users/unified/list")
    public String unifiedList() {
        return "pages/modules/data-management/users/unified/list";
    }
    @GetMapping("/users/unified/form")
    public String unifiedForm() {
        return "pages/modules/data-management/users/unified/form";
    }
    @GetMapping("/users/unified/details")
    public String unifiedDetails() {
        return "pages/modules/data-management/users/unified/details";
    }
    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT) .contains(query);
    }
    private void addUserFormModel(Model model, User user) {
        model.addAttribute("user", user);
        model.addAttribute("userTypes", Arrays.stream(User.UserRole.values()) .filter(role -> role != User.UserRole.SUPER_ADMIN) .toList());
    }
}
