package com.footballacademy.controllers_mvc;

import com.footballacademy.DTO.AcademyForm;
import com.footballacademy.DTO.SportForm;
import com.footballacademy.config.AppUiProperties;
import com.footballacademy.model.*;
import com.footballacademy.repository.*;
import com.footballacademy.services.academy.AcademyService;
import com.footballacademy.services.academy.AcademySubscriptionService;
import com.footballacademy.services.chatbot.ChatbotDataService;
import com.footballacademy.services.sport.SportCategoryService;
import com.footballacademy.services.sport.SportService;
import com.footballacademy.services.theme.SportThemeService;
import com.footballacademy.services.ui.MvcPaginationService;
import com.footballacademy.services.webhook.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**  * Super-admin (platform owner) portal pages.  *  * These are server-rendered MVC pages backed by real services/repositories  * (not the placeholder "single static page for every route").  */
@Controller
@RequestMapping("/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public
class SuperAdminPortalController {
    public
    record AcademyAdminContactRow(Academy academy, User ownerUser, List<User> admins) {
    }
    private static final Logger log = LoggerFactory.getLogger(SuperAdminPortalController.
    class);
    private final AcademyService academyService;
    private final AcademySubscriptionService academySubscriptionService;
    private final SportService sportService;
    private final SportCategoryService sportCategoryService;
    private final SportThemeService sportThemeService;
    private final ChatbotDataService chatbotDataService;
    private final WebhookService webhookService;
    private final MvcPaginationService mvcPaginationService;
    private final AppUiProperties appUiProperties;
    private final AcademyRepository academyRepository;
    private final SportRepository sportRepository;
    private final SportCategoryRepository sportCategoryRepository;
    private final SportThemeRepository sportThemeRepository;
    private final UserRepository userRepository;
    private final ChatbotDataRepository chatbotDataRepository;
    private final WebhookRepository webhookRepository;
    private final WebhookLogRepository webhookLogRepository;
    public SuperAdminPortalController(AcademyService academyService, AcademySubscriptionService academySubscriptionService, SportService sportService, SportCategoryService sportCategoryService, SportThemeService sportThemeService, ChatbotDataService chatbotDataService, WebhookService webhookService, MvcPaginationService mvcPaginationService, AppUiProperties appUiProperties, AcademyRepository academyRepository, SportRepository sportRepository, SportCategoryRepository sportCategoryRepository, SportThemeRepository sportThemeRepository, UserRepository userRepository, ChatbotDataRepository chatbotDataRepository, WebhookRepository webhookRepository, WebhookLogRepository webhookLogRepository) {
        this.academyService = academyService;
        this.academySubscriptionService = academySubscriptionService;
        this.sportService = sportService;
        this.sportCategoryService = sportCategoryService;
        this.sportThemeService = sportThemeService;
        this.chatbotDataService = chatbotDataService;
        this.webhookService = webhookService;
        this.mvcPaginationService = mvcPaginationService;
        this.appUiProperties = appUiProperties;
        this.academyRepository = academyRepository;
        this.sportRepository = sportRepository;
        this.sportCategoryRepository = sportCategoryRepository;
        this.sportThemeRepository = sportThemeRepository;
        this.userRepository = userRepository;
        this.chatbotDataRepository = chatbotDataRepository;
        this.webhookRepository = webhookRepository;
        this.webhookLogRepository = webhookLogRepository;
    }
    @GetMapping({
        "", "/", "/dashboard"
    })
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Platform Dashboard");
        model.addAttribute("academiesCount", safeCount("academies", academyRepository::count));
        model.addAttribute("sportsCount", safeCount("sports", sportRepository::count));
        model.addAttribute("categoriesCount", safeCount("sport categories", sportCategoryRepository::count));
        model.addAttribute("themesCount", safeCount("sport themes", sportThemeRepository::count));
        model.addAttribute("usersCount", safeCount("users", userRepository::count));
        model.addAttribute("chatbotCount", safeCount("chatbot data", chatbotDataRepository::count));
        model.addAttribute("webhooksCount", safeCount("webhooks", webhookRepository::count));
        model.addAttribute("adminContactsCount", safeCount("academy admins",() ->(long) userRepository.findByMainRole(User.UserRole.ADMIN) .size()));
        model.addAttribute("recentWebhookLogs", safeList("webhook logs",() -> webhookLogRepository.findAll() .stream() .sorted(Comparator.comparing(WebhookLog::getExecutedAt, Comparator.nullsLast(Comparator.reverseOrder()))) .limit(appUiProperties.getDashboard() .getRecentWebhookLogsLimit()) .toList()));
        return "pages/modules/super-admin/dashboard";
    }
    // -------------------- Academies --------------------
    @GetMapping("/academies/list")
    public String academiesList(
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        model.addAttribute("pageTitle", "Academies");
        var pagination = mvcPaginationService.paginate(academyService.findAll() .stream() .sorted(Comparator.comparing(Academy::getName, String.CASE_INSENSITIVE_ORDER)) .toList(), page, request);
        model.addAttribute("academies", pagination.getItems());
        model.addAttribute("pagination", pagination);
        model.addAttribute("sports", sportService.getAllSports());
        return "pages/modules/super-admin/academies/list";
    }
    @GetMapping("/academies/new")
    public String academiesNew(Model model) {
        model.addAttribute("pageTitle", "New Academy");
        model.addAttribute("academyForm", new AcademyForm());
        model.addAttribute("sports", sportService.getAllSports());
        return "pages/modules/super-admin/academies/form";
    }
    @GetMapping("/academies/{id}/edit")
    public String academiesEdit(
    @PathVariable Long id, Model model) {
        Academy academy = academyService.findById(id);
        model.addAttribute("pageTitle", "Edit Academy");
        model.addAttribute("academyForm", AcademyForm.from(academy, academyService.findOwnerUser(id) .orElse(null)));
        model.addAttribute("sports", sportService.getAllSports());
        return "pages/modules/super-admin/academies/form";
    }
    @GetMapping("/academies/{id}")
    public String academiesDetails(
    @PathVariable Long id, Model model) {
        Academy academy = academyService.findById(id);
        model.addAttribute("pageTitle", "Academy Details");
        model.addAttribute("academy", academy);
        model.addAttribute("ownerUser", academyService.findOwnerUser(id) .orElse(null));
        model.addAttribute("featureCatalog", academySubscriptionService.featureCatalog(academy));
        model.addAttribute("payments", academySubscriptionService.paymentsForAcademy(id));
        return "pages/modules/super-admin/academies/details";
    }
    @GetMapping("/academy-payments")
    public String academyPayments(
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        model.addAttribute("pageTitle", "Academy Payments");
        var pagination = mvcPaginationService.paginate(academySubscriptionService.allPayments(), page, request);
        model.addAttribute("payments", pagination.getItems());
        model.addAttribute("pagination", pagination);
        model.addAttribute("pendingPaymentsCount", academySubscriptionService.pendingPaymentsCount());
        model.addAttribute("totalCollected", academySubscriptionService.totalCollected());
        model.addAttribute("currency", academySubscriptionService.currency());
        model.addAttribute("offerPrices", academySubscriptionService.offerPrices());
        return "pages/modules/super-admin/academy-payments/index";
    }
    // -------------------- Sports --------------------
    @GetMapping("/sports/list")
    public String sportsList(
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        List<Sport> sports = sportService.getAllSports() .stream() .sorted(Comparator.comparing(Sport::getDisplayOrder) .thenComparing(Sport::getName, String.CASE_INSENSITIVE_ORDER)) .toList();
        List<SportCategory> categories = sportCategoryService.findAll(null, false);
        Map<Long, List<SportCategory>> categoriesBySport = new LinkedHashMap<>();
        for (Sport sport : sports) {
            categoriesBySport.put(sport.getId(), categories.stream() .filter(category -> category.getSport() != null && sport.getId() .equals(category.getSport() .getId())) .sorted(Comparator.comparing(SportCategory::getDisplayOrder) .thenComparing(SportCategory::getName, String.CASE_INSENSITIVE_ORDER)) .toList());
        } var pagination = mvcPaginationService.paginate(sports, page, request);
        model.addAttribute("pageTitle", "Sports");
        model.addAttribute("sports", pagination.getItems());
        model.addAttribute("pagination", pagination);
        model.addAttribute("categoriesCount", categories.size());
        model.addAttribute("categoriesBySport", categoriesBySport);
        return "pages/modules/super-admin/sports/list";
    }
    @GetMapping("/contact-admins")
    public String contactAdmins(
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        model.addAttribute("pageTitle", "Contact Admins");
        List<AcademyAdminContactRow> contacts = academyService.findAll() .stream() .sorted(Comparator.comparing(Academy::getName, String.CASE_INSENSITIVE_ORDER)) .map(academy -> new AcademyAdminContactRow(academy, academyService.findOwnerUser(academy.getId()) .orElse(null), userRepository.findByAcademy_IdAndMainRole(academy.getId(), User.UserRole.ADMIN) .stream() .sorted(Comparator.comparing(User::getNom, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))) .toList())) .toList();
        var pagination = mvcPaginationService.paginate(contacts, page, request);
        model.addAttribute("academyContacts", pagination.getItems());
        model.addAttribute("pagination", pagination);
        model.addAttribute("adminContactsCount", contacts.stream() .mapToLong(row -> row.admins() .size()) .sum());
        return "pages/modules/super-admin/contact/index";
    }
    @GetMapping("/sports/new")
    public String sportsNew(Model model) {
        model.addAttribute("pageTitle", "New Sport");
        model.addAttribute("sportForm", new SportForm());
        return "pages/modules/super-admin/sports/form";
    }
    @GetMapping("/sports/{id}/edit")
    public String sportsEdit(
    @PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Edit Sport");
        Sport sport = sportService.getSportById(id) .orElseThrow(() -> new IllegalArgumentException("Sport not found: " + id));
        model.addAttribute("sportForm", SportForm.from(sport, sportService.getDivisionsForSport(id)));
        return "pages/modules/super-admin/sports/form";
    }
    // -------------------- Categories --------------------
    @GetMapping("/sport-categories/list")
    public String categoriesList(
    @RequestParam(value = "sportId", required = false) Long sportId,
    @RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly,
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        model.addAttribute("pageTitle", "Categories");
        model.addAttribute("sportId", sportId);
        model.addAttribute("activeOnly", activeOnly);
        model.addAttribute("sports", sportService.getAllSports());
        var pagination = mvcPaginationService.paginate(sportCategoryService.findAll(sportId, activeOnly) .stream() .sorted(Comparator.comparing(SportCategory::getDisplayOrder) .thenComparing(SportCategory::getName, String.CASE_INSENSITIVE_ORDER)) .toList(), page, request);
        model.addAttribute("categories", pagination.getItems());
        model.addAttribute("pagination", pagination);
        return "pages/modules/super-admin/sport-categories/list";
    }
    @GetMapping("/sport-categories/new")
    public String categoriesNew(
    @RequestParam(value = "sportId", required = false) Long sportId, Model model) {
        model.addAttribute("pageTitle", "New Category");
        model.addAttribute("category", new SportCategory());
        model.addAttribute("sportId", sportId);
        model.addAttribute("sports", sportService.getAllSports());
        return "pages/modules/super-admin/sport-categories/form";
    }
    @GetMapping("/sport-categories/{id}/edit")
    public String categoriesEdit(
    @PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Edit Category");
        SportCategory category = sportCategoryService.findById(id);
        model.addAttribute("category", category);
        model.addAttribute("sportId", category.getSport() != null ? category.getSport() .getId() : null);
        model.addAttribute("sports", sportService.getAllSports());
        return "pages/modules/super-admin/sport-categories/form";
    }
    // -------------------- Themes --------------------
    @GetMapping("/themes/list")
    public String themesList(
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        model.addAttribute("pageTitle", "Themes");
        var pagination = mvcPaginationService.paginate(sportThemeService.findAll() .stream() .sorted(Comparator.comparing((SportTheme theme) -> theme.getSport() != null ? theme.getSport() .getName() : "", String.CASE_INSENSITIVE_ORDER) .thenComparing(theme -> theme.getScope() != null ? theme.getScope() .name() : "")) .toList(), page, request);
        model.addAttribute("themes", pagination.getItems());
        model.addAttribute("pagination", pagination);
        model.addAttribute("sports", sportService.getAllSports());
        return "pages/modules/super-admin/themes/list";
    }
    @GetMapping("/themes/new")
    public String themesNew(
    @RequestParam(value = "sportId", required = false) Long sportId, Model model) {
        model.addAttribute("pageTitle", "New Theme");
        model.addAttribute("theme", new SportTheme());
        model.addAttribute("sportId", sportId);
        model.addAttribute("sports", sportService.getAllSports());
        return "pages/modules/super-admin/themes/form";
    }
    @GetMapping("/themes/{id}/edit")
    public String themesEdit(
    @PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Edit Theme");
        SportTheme theme = sportThemeRepository.findById(id) .orElseThrow(() -> new IllegalArgumentException("Theme not found: " + id));
        model.addAttribute("theme", theme);
        model.addAttribute("sportId", theme.getSport() != null ? theme.getSport() .getId() : null);
        model.addAttribute("sports", sportService.getAllSports());
        return "pages/modules/super-admin/themes/form";
    }
    // -------------------- App Data --------------------
    @GetMapping("/app-data")
    public String appData(Model model) {
        model.addAttribute("pageTitle", "App Data");
        model.addAttribute("knowledgeBaseExists", chatbotDataService.knowledgeBaseExists());
        model.addAttribute("knowledgeBaseWebPath", chatbotDataService.knowledgeBaseWebPath());
        model.addAttribute("knowledgeBaseServerPath", chatbotDataService.knowledgeBaseServerPath());
        model.addAttribute("academiesCount", safeCount("academies", academyRepository::count));
        model.addAttribute("sportsCount", safeCount("sports", sportRepository::count));
        model.addAttribute("usersCount", safeCount("users", userRepository::count));
        model.addAttribute("chatbotCount", safeCount("chatbot data", chatbotDataRepository::count));
        return "pages/modules/super-admin/app-data/index";
    }
    // -------------------- Global Chatbot --------------------
    @GetMapping("/chatbot-global")
    public String chatbotGlobal(
    @RequestParam(value = "page", required = false) Integer page, Model model, HttpServletRequest request) {
        model.addAttribute("pageTitle", "Global Chatbot");
        var pagination = mvcPaginationService.paginate(chatbotDataService.getData(ChatbotData.Scope.GLOBAL, null, null) .stream() .sorted(Comparator.comparing(ChatbotData::getUploadedAt, Comparator.nullsLast(Comparator.reverseOrder()))) .toList(), page, request);
        model.addAttribute("entries", pagination.getItems());
        model.addAttribute("pagination", pagination);
        model.addAttribute("kbWebPath", chatbotDataService.knowledgeBaseWebPath());
        return "pages/modules/super-admin/chatbot-global/index";
    }
    // -------------------- Webhooks --------------------
    @GetMapping("/webhooks")
    public String webhooks(
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        model.addAttribute("pageTitle", "Webhooks");
        var pagination = mvcPaginationService.paginate(safeList("webhooks",() -> webhookRepository.findAll() .stream() .sorted(Comparator.comparing(Webhook::getName, String.CASE_INSENSITIVE_ORDER)) .toList()), page, request);
        model.addAttribute("webhooks", pagination.getItems());
        model.addAttribute("pagination", pagination);
        model.addAttribute("failedLogs", safeList("failed webhook logs",() -> webhookService.getFailedWebhookLogs() .stream() .limit(appUiProperties.getWebhooks() .getFailurePreviewLimit()) .toList()));
        return "pages/modules/super-admin/webhooks/index";
    }
    @GetMapping("/webhooks/{id}")
    public String webhookDetails(
    @PathVariable Long id, Model model) {
        model.addAttribute("pageTitle", "Webhook Details");
        model.addAttribute("webhook", webhookService.getWebhookById(id));
        model.addAttribute("logs", safeList("webhook detail logs",() -> webhookService.getWebhookLogs(id) .stream() .limit(appUiProperties.getWebhooks() .getDetailLogsLimit()) .toList()));
        return "pages/modules/super-admin/webhooks/details";
    }
    // -------------------- Settings --------------------
    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Settings");
        model.addAttribute("retentionPolicies", com.footballacademy.util.DataRetentionPolicy.getAllPolicies());
        return "pages/modules/super-admin/settings/index";
    }
    private long safeCount(String label, Supplier<Long> supplier) {
        try {
            Long value = supplier.get();
            return value == null ? 0L : value;
        } catch (RuntimeException ex) {
            log.warn("Super-admin page fallback: could not load {} count: {}", label, ex.getMessage());
            return 0L;
        }
    }
    private <T> List<T> safeList(String label, Supplier<List<T>> supplier) {
        try {
            List<T> value = supplier.get();
            return value == null ? Collections.emptyList() : value;
        } catch (RuntimeException ex) {
            log.warn("Super-admin page fallback: could not load {}: {}", label, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
