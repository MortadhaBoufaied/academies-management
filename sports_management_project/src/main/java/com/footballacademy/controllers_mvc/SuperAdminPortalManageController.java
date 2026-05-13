package com.footballacademy.controllers_mvc;

import com.footballacademy.DTO.AcademyForm;
import com.footballacademy.DTO.SportForm;
import com.footballacademy.model.*;
import com.footballacademy.services.academy.AcademyService;
import com.footballacademy.services.academy.AcademySubscriptionService;
import com.footballacademy.services.chatbot.ChatbotDataService;
import com.footballacademy.services.sport.SportCategoryService;
import com.footballacademy.services.sport.SportService;
import com.footballacademy.services.theme.SportThemeService;
import com.footballacademy.services.webhook.WebhookService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public
class SuperAdminPortalManageController {
    private final AcademyService academyService;
    private final AcademySubscriptionService academySubscriptionService;
    private final SportService sportService;
    private final SportCategoryService sportCategoryService;
    private final SportThemeService sportThemeService;
    private final ChatbotDataService chatbotDataService;
    private final WebhookService webhookService;
    public SuperAdminPortalManageController(AcademyService academyService, AcademySubscriptionService academySubscriptionService, SportService sportService, SportCategoryService sportCategoryService, SportThemeService sportThemeService, ChatbotDataService chatbotDataService, WebhookService webhookService) {
        this.academyService = academyService;
        this.academySubscriptionService = academySubscriptionService;
        this.sportService = sportService;
        this.sportCategoryService = sportCategoryService;
        this.sportThemeService = sportThemeService;
        this.chatbotDataService = chatbotDataService;
        this.webhookService = webhookService;
    }
    // -------------------- Academies --------------------
    @PostMapping("/academies")
    public String createAcademy(
    @ModelAttribute("academyForm") AcademyForm academyForm, Model model) {
        try {
            academyService.createFromForm(academyForm);
            return "redirect:/super-admin/academies/list?saved=true";
        } catch (RuntimeException ex) {
            return renderAcademyForm(model, academyForm, "New Academy", ex.getMessage());
        }
    }
    @PostMapping("/academies/{id}")
    public String updateAcademy(
    @PathVariable Long id,
    @ModelAttribute("academyForm") AcademyForm academyForm, Model model) {
        try {
            academyForm.setId(id);
            academyService.updateFromForm(id, academyForm);
            return "redirect:/super-admin/academies/list?updated=true";
        } catch (RuntimeException ex) {
            academyForm.setId(id);
            return renderAcademyForm(model, academyForm, "Edit Academy", ex.getMessage());
        }
    }
    @PostMapping("/academies/{id}/delete")
    public String deleteAcademy(
    @PathVariable Long id) {
        academyService.delete(id);
        return "redirect:/super-admin/academies/list?deleted=true";
    }
    @PostMapping("/academies/{id}/admins")
    public String createFirstAdmin(
    @PathVariable Long id,
    @RequestParam("name") String name,
    @RequestParam("email") String email,
    @RequestParam("password") String password,
    @RequestParam(value = "phone", required = false) String phone, RedirectAttributes ra) {
        try {
            academyService.createFirstAdmin(id, name, email, password, phone);
            return "redirect:/super-admin/academies/" + id + "?adminCreated=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/academies/" + id;
        }
    }
    @PostMapping("/academy-payments/{id}/mark-paid")
    public String markAcademyPaymentPaid(
    @PathVariable Long id,
    @RequestParam(value = "method", defaultValue = "MANUAL") AcademyPayment.PaymentMethod method, RedirectAttributes ra) {
        try {
            academySubscriptionService.markPaymentPaid(id, method, "Validated by super admin");
            return "redirect:/super-admin/academy-payments?paid=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/academy-payments";
        }
    }
    // -------------------- Sports --------------------
    @PostMapping("/sports")
    public String createSport(
    @ModelAttribute("sportForm") SportForm sportForm, Model model) {
        try {
            sportService.createSport(sportForm.toSport(), sportForm.toTheme(), sportForm.toDivisions());
            return "redirect:/super-admin/sports/list?saved=true";
        } catch (RuntimeException ex) {
            return renderSportForm(model, sportForm, "New Sport", ex.getMessage());
        }
    }
    @PostMapping("/sports/{id}")
    public String updateSport(
    @PathVariable Long id,
    @ModelAttribute("sportForm") SportForm sportForm, Model model) {
        try {
            sportForm.setId(id);
            sportService.updateSport(id, sportForm.toSport(), sportForm.toTheme(), sportForm.toDivisions());
            return "redirect:/super-admin/sports/list?updated=true";
        } catch (RuntimeException ex) {
            sportForm.setId(id);
            return renderSportForm(model, sportForm, "Edit Sport", ex.getMessage());
        }
    }
    @PostMapping("/sports/{id}/delete")
    public String deleteSport(
    @PathVariable Long id, RedirectAttributes ra) {
        try {
            sportService.deleteSport(id);
            return "redirect:/super-admin/sports/list?deleted=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/sports/list";
        }
    }
    @PostMapping("/sports/{id}/activate")
    public String activateSport(
    @PathVariable Long id) {
        sportService.activateSport(id);
        return "redirect:/super-admin/sports/list?updated=true";
    }
    @PostMapping("/sports/{id}/deactivate")
    public String deactivateSport(
    @PathVariable Long id) {
        sportService.deactivateSport(id);
        return "redirect:/super-admin/sports/list?updated=true";
    }
    private String renderSportForm(Model model, SportForm sportForm, String pageTitle, String error) {
        if (sportForm.getDivisions() == null || sportForm.getDivisions() .isEmpty()) {
            sportForm.setDivisions(new java.util.ArrayList<>(List.of(new SportForm.DivisionForm())));
        } model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("sportForm", sportForm);
        model.addAttribute("error", error);
        return "pages/modules/super-admin/sports/form";
    }
    private String renderAcademyForm(Model model, AcademyForm academyForm, String pageTitle, String error) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("academyForm", academyForm);
        model.addAttribute("sports", sportService.getAllSports());
        model.addAttribute("error", error);
        return "pages/modules/super-admin/academies/form";
    }
    // -------------------- Categories --------------------
    @PostMapping("/sport-categories")
    public String createCategory(SportCategory category,
    @RequestParam(value = "sportId", required = false) Long sportId, RedirectAttributes ra) {
        try {
            sportCategoryService.create(category, sportId);
            return "redirect:/super-admin/sport-categories/list?saved=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/sport-categories/new";
        }
    }
    @PostMapping("/sport-categories/{id}")
    public String updateCategory(
    @PathVariable Long id, SportCategory category,
    @RequestParam(value = "sportId", required = false) Long sportId, RedirectAttributes ra) {
        try {
            sportCategoryService.update(id, category, sportId);
            return "redirect:/super-admin/sport-categories/list?updated=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/sport-categories/" + id + "/edit";
        }
    }
    @PostMapping("/sport-categories/{id}/delete")
    public String deleteCategory(
    @PathVariable Long id) {
        sportCategoryService.delete(id);
        return "redirect:/super-admin/sport-categories/list?deleted=true";
    }
    // -------------------- Themes --------------------
    @PostMapping("/themes")
    public String createTheme(SportTheme theme,
    @RequestParam(value = "sportId", required = false) Long sportId, RedirectAttributes ra) {
        try {
            sportThemeService.save(theme, null, sportId);
            return "redirect:/super-admin/themes/list?saved=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/themes/new";
        }
    }
    @PostMapping("/themes/{id}")
    public String updateTheme(
    @PathVariable Long id, SportTheme theme,
    @RequestParam(value = "sportId", required = false) Long sportId, RedirectAttributes ra) {
        try {
            // Ensure binding keeps the id
            theme.setId(id);
            sportThemeService.save(theme, null, sportId);
            return "redirect:/super-admin/themes/list?updated=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/themes/" + id + "/edit";
        }
    }
    @PostMapping("/themes/{id}/delete")
    public String deleteTheme(
    @PathVariable Long id) {
        sportThemeService.delete(id);
        return "redirect:/super-admin/themes/list?deleted=true";
    }
    // -------------------- Global Chatbot --------------------
    @PostMapping("/chatbot-global")
    public String addGlobalChatbotEntry(
    @RequestParam("question") String question,
    @RequestParam("answer") String answer,
    @RequestParam(value = "tags", required = false) String tags,
    @RequestParam(value = "file", required = false) MultipartFile file, RedirectAttributes ra) {
        try {
            ChatbotData data = new ChatbotData();
            data.setQuestion(question);
            data.setAnswer(answer);
            data.setTags(tags);
            if (file != null && !file.isEmpty()) {
                data.setFilePath(chatbotDataService.storeChatbotFile(file));
            }
            chatbotDataService.applyScope(data, ChatbotData.Scope.GLOBAL, null, null, ChatbotData.SourceType.MANUAL, null);
            chatbotDataService.uploadData(data);
            return "redirect:/super-admin/chatbot-global?saved=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/chatbot-global";
        }
    }
    @PostMapping("/chatbot-global/{id}/delete")
    public String deleteGlobalChatbotEntry(
    @PathVariable Long id, RedirectAttributes ra) {
        try {
            chatbotDataService.deleteData(id);
            return "redirect:/super-admin/chatbot-global?deleted=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/chatbot-global";
        }
    }
    @PostMapping("/chatbot-global/clear")
    public String clearGlobalChatbot(RedirectAttributes ra) {
        try {
            chatbotDataService.clearAllDataAndKnowledgeBaseFile();
            return "redirect:/super-admin/chatbot-global?cleared=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/chatbot-global";
        }
    }
    // -------------------- Webhooks --------------------
    @PostMapping("/webhooks")
    public String createWebhook(Webhook webhook, RedirectAttributes ra) {
        try {
            webhookService.createWebhook(webhook);
            return "redirect:/super-admin/webhooks?saved=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/webhooks";
        }
    }
    @PostMapping("/webhooks/{id}")
    public String updateWebhook(
    @PathVariable Long id, Webhook webhook, RedirectAttributes ra) {
        try {
            webhookService.updateWebhook(id, webhook);
            return "redirect:/super-admin/webhooks/" + id + "?updated=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/webhooks/" + id;
        }
    }
    @PostMapping("/webhooks/{id}/delete")
    public String deleteWebhook(
    @PathVariable Long id, RedirectAttributes ra) {
        try {
            webhookService.deleteWebhook(id);
            return "redirect:/super-admin/webhooks?deleted=true";
        } catch (RuntimeException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/super-admin/webhooks";
        }
    }
    @PostMapping("/webhooks/{id}/activate")
    public String activateWebhook(
    @PathVariable Long id) {
        webhookService.activateWebhook(id);
        return "redirect:/super-admin/webhooks/" + id + "?updated=true";
    }
    @PostMapping("/webhooks/{id}/deactivate")
    public String deactivateWebhook(
    @PathVariable Long id) {
        webhookService.deactivateWebhook(id);
        return "redirect:/super-admin/webhooks/" + id + "?updated=true";
    }
    @PostMapping("/webhooks/{id}/test")
    public String testWebhook(
    @PathVariable Long id) {
        webhookService.testWebhook(id);
        return "redirect:/super-admin/webhooks/" + id + "?tested=true";
    }
}
