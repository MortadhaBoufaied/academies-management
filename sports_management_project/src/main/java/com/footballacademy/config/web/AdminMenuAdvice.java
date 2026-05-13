package com.footballacademy.config.web;

import com.footballacademy.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.ArrayList;
import java.util.List;

/**  * DEPRECATED: Menu injection for legacy Thymeleaf server-side rendering.  * The main Flutter mobile app manages its own navigation.  * This is kept for backward compatibility with any remaining server-rendered pages.  */
@ControllerAdvice(annotations = Controller.
class)
public
class AdminMenuAdvice {
    @ModelAttribute
    public void injectMenu(
    @AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (principal != null) {
            // Build menu client-side based on role, or provide default empty menu
            model.addAttribute("adminMenu", new ArrayList<>());
        }
    }
}
