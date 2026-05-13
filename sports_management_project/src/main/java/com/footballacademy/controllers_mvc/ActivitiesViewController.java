package com.footballacademy.controllers_mvc;

import com.footballacademy.DTO.TrainerCombinedDTO;
import com.footballacademy.model.Activity;
import com.footballacademy.services.activity.ActivityService;
import com.footballacademy.services.trainer.TrainerService;
import com.footballacademy.services.ui.MvcPaginationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/view")
public
class ActivitiesViewController {
    private final ActivityService activityService;
    private final TrainerService trainerService;
    private final MvcPaginationService mvcPaginationService;
    public ActivitiesViewController(ActivityService activityService, TrainerService trainerService, MvcPaginationService mvcPaginationService) {
        this.activityService = activityService;
        this.trainerService = trainerService;
        this.mvcPaginationService = mvcPaginationService;
    }
    @GetMapping("/activities")
    public String list(
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        List<Activity> activities = activityService.getAllActivities() .stream() .sorted(Comparator.comparing(Activity::getDate, Comparator.nullsLast(Comparator.reverseOrder()))) .toList();
        var pagination = mvcPaginationService.paginate(activities, page, request);
        model.addAttribute("activities", pagination.getItems());
        model.addAttribute("pagination", pagination);
        model.addAttribute("trainerNames", trainerNameMap());
        return "pages/modules/data-management/activities/list";
    }
    @GetMapping("/activities/new")
    public String create(Model model) {
        model.addAttribute("activity", new Activity());
        model.addAttribute("trainers", trainerService.getAllTrainersCombined());
        return "pages/modules/data-management/activities/form";
    }
    @GetMapping("/activities/{id}/edit")
    public String edit(
    @PathVariable Long id, Model model) {
        model.addAttribute("activity", activityService.getActivityById(id));
        model.addAttribute("trainers", trainerService.getAllTrainersCombined());
        return "pages/modules/data-management/activities/form";
    }
    @GetMapping("/activities/calendar")
    public String calendar(Model model) {
        List<Activity> activities = activityService.getUpcomingActivities() .stream() .sorted(Comparator.comparing(Activity::getDate, Comparator.nullsLast(Comparator.naturalOrder()))) .toList();
        model.addAttribute("activities", activities);
        model.addAttribute("calendarEvents", activities.stream() .map(activity -> new CalendarEventView(activity.getId(), activity.getTitre(), activity.getDate() != null ? activity.getDate() .toString() : null, activity.getLieu(), activity.getDescription())) .toList());
        return "pages/modules/data-management/activities/calendar";
    }
    @GetMapping("/activities/matches")
    public String matches() {
        return "pages/modules/data-management/matches/list";
    }
    private Map<Long, String> trainerNameMap() {
        Map<Long, String> map = new LinkedHashMap<>();
        for (TrainerCombinedDTO trainer : trainerService.getAllTrainersCombined()) {
            map.put(trainer.id(), trainer.name() != null ? trainer.name() :("Trainer " + trainer.id()));
        } return map;
    }
    private
    record CalendarEventView(Long id, String title, String start, String location, String description) {
    }
}
