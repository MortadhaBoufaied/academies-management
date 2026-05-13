package com.footballacademy.controllers_mvc;

import com.footballacademy.model.Payment;
import com.footballacademy.services.PaymentService;
import com.footballacademy.services.academy.AcademyInfoService;
import com.footballacademy.services.division.DivisionService;
import com.footballacademy.services.sport.SportService;
import com.footballacademy.services.ui.MvcPaginationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/view")
public
class AdminPagesController {
    private final AcademyInfoService academyInfoService;
    private final DivisionService divisionService;
    private final PaymentService paymentService;
    private final SportService sportService;
    private final MvcPaginationService mvcPaginationService;
    public AdminPagesController(AcademyInfoService academyInfoService, DivisionService divisionService, PaymentService paymentService, SportService sportService, MvcPaginationService mvcPaginationService) {
        this.academyInfoService = academyInfoService;
        this.divisionService = divisionService;
        this.paymentService = paymentService;
        this.sportService = sportService;
        this.mvcPaginationService = mvcPaginationService;
    }
    @GetMapping("/admins")
    public String admins() {
        return "pages/modules/admin/admins";
    }
    @GetMapping("/data-management")
    public String dataManagement() {
        return "redirect:/admin/view/dashboard";
    }
    @GetMapping("/divisions")
    public String divisions(
    @RequestParam(value = "page", required = false) Integer page,
    @RequestParam(value = "associatedPage", required = false) Integer associatedPage,
    @RequestParam(value = "availablePage", required = false) Integer availablePage, HttpServletRequest request, Model model) {
        var divisionsPagination = mvcPaginationService.paginate(divisionService.getAllDivisions() .stream() .sorted(Comparator.comparing(com.footballacademy.model.Division::getNom, String.CASE_INSENSITIVE_ORDER)) .toList(), page, request);
        model.addAttribute("divisions", divisionsPagination.getItems());
        model.addAttribute("pagination", divisionsPagination);
        addAcademyDivisionModel(model, request, associatedPage, availablePage);
        return "pages/modules/data-management/divisions/list";
    }
    @GetMapping("/divisions/new")
    public String newDivision(Model model) {
        model.addAttribute("division", new com.footballacademy.model.Division());
        model.addAttribute("sports", sportService.getAllSports());
        return "pages/modules/data-management/divisions/form";
    }
    @GetMapping("/divisions/{id}/edit")
    public String editDivision(
    @PathVariable Long id, Model model) {
        model.addAttribute("division", divisionService.getDivisionById(id));
        model.addAttribute("sports", sportService.getAllSports());
        return "pages/modules/data-management/divisions/form";
    }
    @GetMapping("/payments")
    public String payments(
    @RequestParam(required = false) String status,
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        List<Payment> allPayments = paymentService.getAllPayments();
        List<Payment> payments = allPayments;
        if ("paid" .equalsIgnoreCase(status)) {
            payments = allPayments.stream() .filter(Payment::isPaid) .toList();
        } else if ("pending" .equalsIgnoreCase(status)) {
            payments = allPayments.stream() .filter(p -> !p.isPaid()) .toList();
        }
        long paidCount = allPayments.stream() .filter(Payment::isPaid) .count();
        long pendingCount = allPayments.size() - paidCount;
        var paymentsPagination = mvcPaginationService.paginate(payments.stream() .sorted(Comparator.comparing(Payment::getMois, Comparator.nullsLast(Comparator.reverseOrder()))) .toList(), page, request);
        model.addAttribute("payments", paymentsPagination.getItems());
        model.addAttribute("pagination", paymentsPagination);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("pendingCount", pendingCount);
        return "pages/modules/data-management/payments/list";
    }
    @GetMapping("/payments/overview")
    public String paymentsOverview(
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        List<Payment> payments = paymentService.getAllPayments();
        List<Payment> unpaid = payments.stream() .filter(p -> !p.isPaid()) .toList();
        long paidCount = payments.stream() .filter(Payment::isPaid) .count();
        long pendingCount = payments.size() - paidCount;
        double totalRevenue = payments.stream() .filter(Payment::isPaid) .map(Payment::getMontant) .filter(v -> v != null) .mapToDouble(Double::doubleValue) .sum();
        double pendingRevenue = unpaid.stream() .map(Payment::getMontant) .filter(v -> v != null) .mapToDouble(Double::doubleValue) .sum();
        double collectionRate = payments.isEmpty() ? 0.0 :(paidCount * 100.0 / payments.size());
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("pendingRevenue", pendingRevenue);
        model.addAttribute("paidCount", paidCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("collectionRate", collectionRate);
        var unpaidPagination = mvcPaginationService.paginate(unpaid.stream() .sorted(Comparator.comparing(Payment::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))) .toList(), page, request);
        model.addAttribute("unpaidPlayers", unpaidPagination.getItems());
        model.addAttribute("pagination", unpaidPagination);
        return "pages/modules/data-management/payments/overview";
    }
    @GetMapping("/notifications")
    public String notifications() {
        return "pages/modules/notifications/list";
    }
    @GetMapping("/academy-info")
    public String academyInfo(Model model) {
        addAcademySettingsModel(model);
        return "pages/modules/settings/academy";
    }
    @GetMapping({
        "/chatbot-data", "/bot-knowledge"
    })
    public String chatbotData() {
        return "pages/modules/chat/bot-knowledge";
    }
    @GetMapping({
        "/reports", "/reports/index"
    })
    public String reports() {
        return "pages/modules/reports/index";
    }
    @GetMapping({
        "/settings", "/settings/academy"
    })
    public String settings(Model model) {
        addAcademySettingsModel(model);
        return "pages/modules/settings/academy";
    }
    @GetMapping("/contact")
    public String contact() {
        return "pages/modules/contact/contact";
    }
    @GetMapping("/scouters")
    public String scouters() {
        return "pages/modules/data-management/users/list";
    }
    private void addAcademySettingsModel(Model model) {
        var academyInfo = academyInfoService.getAcademyInfo();
        model.addAttribute("academyInfo", academyInfo);
    }
    private void addAcademyDivisionModel(Model model, HttpServletRequest request, Integer associatedPage, Integer availablePage) {
        var academyInfo = academyInfoService.getAcademyInfo();
        var associatedDivisions = academyInfoService.getAssociatedDivisionsForCurrentAcademy();
        var availableDivisions = academyInfoService.getAvailableDivisionsForCurrentAcademy();
        var associatedPagination = mvcPaginationService.paginate(associatedDivisions, associatedPage, request, "associatedPage");
        var availablePagination = mvcPaginationService.paginate(availableDivisions, availablePage, request, "availablePage");
        model.addAttribute("currentAcademy", academyInfo.getAcademy());
        model.addAttribute("associatedDivisions", associatedPagination.getItems());
        model.addAttribute("associatedPagination", associatedPagination);
        model.addAttribute("associatedDivisionIds", academyInfo.getDivisionsList());
        model.addAttribute("availableDivisions", availablePagination.getItems());
        model.addAttribute("availablePagination", availablePagination);
        model.addAttribute("canManageAcademyDivisions", academyInfoService.canManageAcademyDivisions());
        model.addAttribute("currentAdminResponsibilityName", academyInfoService.currentAdminResponsibilityName());
    }
}
