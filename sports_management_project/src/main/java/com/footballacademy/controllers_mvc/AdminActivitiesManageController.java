package com.footballacademy.controllers_mvc;

import com.footballacademy.model.Activity;
import com.footballacademy.services.activity.ActivityService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public
class AdminActivitiesManageController {
    private final ActivityService activityService;
    public AdminActivitiesManageController(ActivityService activityService) {
        this.activityService = activityService;
    }
    @PostMapping("/activities")
    public String create(Activity activity, RedirectAttributes redirectAttributes) {
        try {
            activityService.createActivity(activity);
            return "redirect:/admin/view/activities?saved=true";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/view/activities/new";
        }
    }
    @PostMapping("/activities/{id}")
    public String update(
    @PathVariable Long id, Activity activity, RedirectAttributes redirectAttributes) {
        try {
            activityService.updateActivity(id, activity);
            return "redirect:/admin/view/activities?updated=true";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/view/activities/" + id + "/edit";
        }
    }
    @PostMapping("/activities/{id}/delete")
    public String delete(
    @PathVariable Long id) {
        activityService.deleteActivity(id);
        return "redirect:/admin/view/activities?deleted=true";
    }
}
