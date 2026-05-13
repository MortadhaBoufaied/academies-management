package com.footballacademy.controllers_mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public
class AdminTrainersManageController {
    @PostMapping("/trainers")
    public String create() {
        return "redirect:/admin/view/trainers?saved=true";
    }
    @PostMapping("/trainers/{id}")
    public String update(
    @PathVariable String id) {
        return "redirect:/admin/view/trainers?updated=true";
    }
    @PostMapping("/trainers/{id}/delete")
    public String delete(
    @PathVariable String id) {
        return "redirect:/admin/view/trainers?deleted=true";
    }
}
