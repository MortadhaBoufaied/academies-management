package com.footballacademy.controllers_mvc;

import com.footballacademy.model.User;
import com.footballacademy.services.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;

@Controller
@RequestMapping("/admin/view/auth")
public
class AdminLoginController {
    private final AuthService authService;
    public AdminLoginController(AuthService authService) {
        this.authService = authService;
    }
    @GetMapping({
        "", "/", "/login"
    })
    public String loginPage() {
        return "pages/modules/auth/login";
    }
    @PostMapping("/login")
    public String login(
    @RequestParam("email") String email,
    @RequestParam("password") String password, HttpSession session, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> auth = authService.authenticate(email, password);
            User user =(User) auth.get("user");
            if (!authService.canAccessAdminConsole(user)) {
                redirectAttributes.addAttribute("error", "Only admin accounts can access this console.");
                return "redirect:/admin/view/auth/login";
            }
            addCookie(response, "accessToken",(String) auth.get("accessToken"), 24 * 60 * 60);
            addCookie(response, "refreshToken",(String) auth.get("refreshToken"), 7 * 24 * 60 * 60);
            session.setAttribute("adminUser", user.getNom() != null ? user.getNom() : user.getEmail());
            session.setAttribute("adminRole", user.getMainRole() != null ? user.getMainRole() .name() : "");
            return "redirect:" + authService.redirectUrlFor(user);
        } catch (RuntimeException ex) {
            redirectAttributes.addAttribute("error", "Invalid email or password.");
            return "redirect:/admin/view/auth/login";
        }
    }
    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        session.invalidate();
        addCookie(response, "accessToken", "", 0);
        addCookie(response, "refreshToken", "", 0);
        return "redirect:/admin/view/auth/login";
    }
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}
