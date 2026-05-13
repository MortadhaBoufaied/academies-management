package com.footballacademy.controllers_mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public
class AdminParentsManageController {
    @PostMapping("/parents")
    public String create() {
        return "redirect:/admin/view/parents?saved=true";
    }
    @PostMapping("/parents/{id}")
    public String update(
    @PathVariable String id) {
        return "redirect:/admin/view/parents?updated=true";
    }
    @PostMapping("/parents/{id}/delete")
    public String delete(
    @PathVariable String id) {
        return "redirect:/admin/view/parents?deleted=true";
    }
}
