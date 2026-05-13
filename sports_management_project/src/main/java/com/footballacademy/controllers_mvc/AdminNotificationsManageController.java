package com.footballacademy.controllers_mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public
class AdminNotificationsManageController {
    @PostMapping("/notifications")
    public String create() {
        return "redirect:/admin/view/notifications?saved=true";
    }
    @PostMapping("/notifications/{id}/read")
    public String markRead(
    @PathVariable String id) {
        return "redirect:/admin/view/notifications?read=true";
    }
}
