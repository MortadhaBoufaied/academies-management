package com.footballacademy.controllers_mvc;

import com.footballacademy.model.Academy;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.academy.AcademyService;
import com.footballacademy.services.academy.AcademySubscriptionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/view")
public
class AdminSubscriptionViewController {
    private final AcademyAccessService academyAccessService;
    private final AcademyService academyService;
    private final AcademySubscriptionService academySubscriptionService;
    public AdminSubscriptionViewController(AcademyAccessService academyAccessService, AcademyService academyService, AcademySubscriptionService academySubscriptionService) {
        this.academyAccessService = academyAccessService;
        this.academyService = academyService;
        this.academySubscriptionService = academySubscriptionService;
    }
    @GetMapping("/subscription")
    public String subscription(
    @RequestParam(value = "locked", required = false) String lockedFeature, Model model) {
        Academy academy = academyAccessService.currentAcademyOrThrow();
        Academy persistedAcademy = academyService.findById(academy.getId());
        model.addAttribute("pageTitle", "Subscription");
        model.addAttribute("academy", persistedAcademy);
        model.addAttribute("ownerUser", academyService.findOwnerUser(persistedAcademy.getId()) .orElse(null));
        model.addAttribute("featureCatalog", academySubscriptionService.featureCatalog(persistedAcademy));
        model.addAttribute("payments", academySubscriptionService.paymentsForAcademy(persistedAcademy.getId()));
        model.addAttribute("pendingPayment", academySubscriptionService.latestPendingPayment(persistedAcademy.getId()) .orElse(null));
        model.addAttribute("regularPrice", academySubscriptionService.priceFor(Academy.SubscriptionOffer.REGULAR));
        model.addAttribute("proPrice", academySubscriptionService.priceFor(Academy.SubscriptionOffer.PRO));
        model.addAttribute("currency", academySubscriptionService.currency());
        model.addAttribute("lockedFeature", lockedFeature);
        return "pages/modules/subscription/index";
    }
}
