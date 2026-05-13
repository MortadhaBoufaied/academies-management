package com.footballacademy.controllers_mvc;

import com.footballacademy.model.Trainer;
import com.footballacademy.repository.TrainerRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.ui.MvcPaginationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/view")
public
class TrainersViewController {
    private final TrainerRepository trainerRepository;
    private final AcademyAccessService academyAccessService;
    private final MvcPaginationService mvcPaginationService;
    public TrainersViewController(TrainerRepository trainerRepository, AcademyAccessService academyAccessService, MvcPaginationService mvcPaginationService) {
        this.trainerRepository = trainerRepository;
        this.academyAccessService = academyAccessService;
        this.mvcPaginationService = mvcPaginationService;
    }
    @GetMapping("/trainers")
    public String list(
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        List<Trainer> trainers = academyAccessService.isSuperAdmin() ? trainerRepository.findAllWithUser() : trainerRepository.findByAcademyIdWithUser(academyAccessService.currentAcademyOrThrow() .getId());
        trainers = trainers.stream() .sorted(Comparator.comparing(trainer -> trainer.getUser() != null && trainer.getUser() .getNom() != null ? trainer.getUser() .getNom() : "", String.CASE_INSENSITIVE_ORDER)) .toList();
        var pagination = mvcPaginationService.paginate(trainers, page, request);
        model.addAttribute("trainers", pagination.getItems());
        model.addAttribute("pagination", pagination);
        return "pages/modules/data-management/trainers/list";
    }
    @GetMapping("/trainers/new")
    public String create() {
        return "pages/modules/data-management/trainers/form";
    }
    @GetMapping("/trainers/{id}")
    public String details(
    @PathVariable String id) {
        return "pages/modules/data-management/trainers/details";
    }
    @GetMapping("/trainers/{id}/edit")
    public String edit(
    @PathVariable String id) {
        return "pages/modules/data-management/trainers/form";
    }
}
