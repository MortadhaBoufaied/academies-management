package com.footballacademy.controllers_mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/view")
public
class AdminModulesController {
    @GetMapping("/sections")
    public String sections() {
        return "pages/modules/admin/page-index";
    }
    @GetMapping("/{section}")
    public String section(
    @PathVariable String section) {
        if ("dashboard" .equals(section)) return "redirect:/admin/view/dashboard";
        if ("data-management" .equals(section)) return "redirect:/admin/view/dashboard";
        if ("admins" .equals(section)) return "redirect:/admin/view/admins";
        if ("users" .equals(section)) return "redirect:/admin/view/users";
        if ("players" .equals(section)) return "redirect:/admin/view/players";
        if ("trainers" .equals(section)) return "redirect:/admin/view/trainers";
        if ("parents" .equals(section)) return "redirect:/admin/view/parents";
        if ("divisions" .equals(section)) return "redirect:/admin/view/divisions";
        if ("activities" .equals(section)) return "redirect:/admin/view/activities";
        if ("payments" .equals(section)) return "redirect:/admin/view/payments";
        if ("notifications" .equals(section)) return "redirect:/admin/view/notifications";
        if ("chat" .equals(section)) return "redirect:/admin/view/chat";
        if ("chatbot" .equals(section)) return "redirect:/admin/view/chatbot";
        if ("profile" .equals(section)) return "redirect:/profile/view/index";
        if ("reports" .equals(section)) return "redirect:/reports/view/index";
        if ("settings" .equals(section)) return "redirect:/settings/view/academy";
        return "pages/modules/errors/404";
    }
}
