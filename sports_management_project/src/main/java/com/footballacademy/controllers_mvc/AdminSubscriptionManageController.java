package com.footballacademy.controllers_mvc;

import com.footballacademy.model.Academy;
import com.footballacademy.model.AcademyPayment;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.academy.AcademySubscriptionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/subscription")
public
class AdminSubscriptionManageController {
    private final AcademyAccessService academyAccessService;
    private final AcademySubscriptionService academySubscriptionService;
    public AdminSubscriptionManageController(AcademyAccessService academyAccessService, AcademySubscriptionService academySubscriptionService) {
        this.academyAccessService = academyAccessService;
        this.academySubscriptionService = academySubscriptionService;
    }
    @PostMapping("/checkout")
    public String checkout(
    @RequestParam("offer") Academy.SubscriptionOffer offer, RedirectAttributes ra) {
        try {
            Academy academy = academyAccessService.currentAcademyOrThrow();
            academySubscriptionService.createCheckout(academy, offer, "Offer selected by academy admin");
            ra.addFlashAttribute("success", "Checkout created for the " + offer.name() + " offer.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        } return "redirect:/admin/view/subscription";
    }
    @PostMapping("/payments/{id}/pay")
    public String pay(
    @PathVariable Long id,
    @RequestParam(value = "method", defaultValue = "CARD") AcademyPayment.PaymentMethod method, RedirectAttributes ra) {
        try {
            Academy academy = academyAccessService.currentAcademyOrThrow();
            academySubscriptionService.markPaymentPaidForAcademy(academy.getId(), id, method, "Completed from the academy billing portal");
            ra.addFlashAttribute("success", "Subscription payment completed. Pro or regular services are now updated.");
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        } return "redirect:/admin/view/subscription";
    }
}
