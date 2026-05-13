package com.footballacademy.controllers_mvc;

import com.footballacademy.model.Parent;
import com.footballacademy.repository.ParentRepository;
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
class ParentsViewController {
    private final ParentRepository parentRepository;
    private final AcademyAccessService academyAccessService;
    private final MvcPaginationService mvcPaginationService;
    public ParentsViewController(ParentRepository parentRepository, AcademyAccessService academyAccessService, MvcPaginationService mvcPaginationService) {
        this.parentRepository = parentRepository;
        this.academyAccessService = academyAccessService;
        this.mvcPaginationService = mvcPaginationService;
    }
    @GetMapping("/parents")
    public String list(
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        List<Parent> parents = academyAccessService.isSuperAdmin() ? parentRepository.findAllWithUser() : parentRepository.findByAcademyIdWithUser(academyAccessService.currentAcademyOrThrow() .getId());
        parents = parents.stream() .sorted(Comparator.comparing(parent -> parent.getUser() != null && parent.getUser() .getNom() != null ? parent.getUser() .getNom() : "", String.CASE_INSENSITIVE_ORDER)) .toList();
        var pagination = mvcPaginationService.paginate(parents, page, request);
        model.addAttribute("parents", pagination.getItems());
        model.addAttribute("pagination", pagination);
        return "pages/modules/data-management/parents/list";
    }
    @GetMapping("/parents/new")
    public String create() {
        return "pages/modules/data-management/parents/form";
    }
    @GetMapping("/parents/{id}")
    public String details(
    @PathVariable String id) {
        return "pages/modules/data-management/parents/details";
    }
    @GetMapping("/parents/{id}/edit")
    public String edit(
    @PathVariable String id) {
        return "pages/modules/data-management/parents/form";
    }
}
