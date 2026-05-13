package com.footballacademy.controllers_mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public
class AdminSettingsManageController {
    @PostMapping({
        "/settings", "/settings/academy"
    })
    public String saveSettings() {
        return "redirect:/settings/view/academy?saved=true";
    }
}
