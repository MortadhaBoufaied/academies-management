package com.footballacademy.controllers_mvc;

import com.footballacademy.model.Division;
import com.footballacademy.services.division.DivisionService;
import com.footballacademy.services.sport.SportService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public
class AdminDivisionsManageController {
    private final DivisionService divisionService;
    private final SportService sportService;
    public AdminDivisionsManageController(DivisionService divisionService, SportService sportService) {
        this.divisionService = divisionService;
        this.sportService = sportService;
    }
    @PostMapping("/divisions")
    public String create(Division division,
    @RequestParam(required = false) Long sportId, RedirectAttributes redirectAttributes) {
        try {
            if (sportId != null) {
                sportService.getSportById(sportId) .ifPresent(division::setSport);
            } divisionService.createDivision(division);
            return "redirect:/admin/view/divisions?saved=true";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/view/divisions/new";
        }
    }
    @PostMapping("/divisions/{id}")
    public String update(
    @PathVariable Long id, Division division,
    @RequestParam(required = false) Long sportId, RedirectAttributes redirectAttributes) {
        try {
            if (sportId != null) {
                sportService.getSportById(sportId) .ifPresent(division::setSport);
            } divisionService.updateDivision(id, division);
            return "redirect:/admin/view/divisions?updated=true";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/view/divisions/" + id + "/edit";
        }
    }
    @PostMapping("/divisions/{id}/delete")
    public String delete(
    @PathVariable Long id) {
        divisionService.deleteDivision(id);
        return "redirect:/admin/view/divisions?deleted=true";
    }
}
