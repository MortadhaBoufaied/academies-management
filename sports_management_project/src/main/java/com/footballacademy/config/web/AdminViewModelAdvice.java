package com.footballacademy.config.web;

import com.footballacademy.repository.AcademyRepository;
import com.footballacademy.security.UserPrincipal;
import com.footballacademy.services.theme.SportThemeService;
import com.footballacademy.util.MediaUrlUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injects common model attributes for admin MVC views.
 */
@ControllerAdvice(annotations = Controller.class)
public class AdminViewModelAdvice {

    private final AcademyRepository academyRepository;
    private final SportThemeService sportThemeService;

    public AdminViewModelAdvice(
            AcademyRepository academyRepository,
            SportThemeService sportThemeService
    ) {
        this.academyRepository = academyRepository;
        this.sportThemeService = sportThemeService;
    }

    @ModelAttribute
    public void injectCommon(
            HttpServletRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            Model model
    ) {
        // Base URL for static/media assets
        model.addAttribute("baseUrl", MediaUrlUtil.baseUrl(request));

        // ================================
        // Authenticated user context
        // ================================
        if (principal != null) {
            model.addAttribute("user", principal.getUser());

            Long academyId = principal.getAcademyId();
            if (academyId != null) {
                academyRepository.findById(academyId).ifPresent(academy -> {
                    model.addAttribute("currentAcademy", academy);
                    model.addAttribute("currentAcademyName", academy.getName());

                    var themeEntity =
                            sportThemeService.resolveThemeEntity(
                                    academyId,
                                    academy.getSportId()
                            );

                    model.addAttribute(
                            "currentTheme",
                            sportThemeService.resolveTheme(
                                    academyId,
                                    academy.getSportId()
                            )
                    );

                    model.addAttribute(
                            "currentThemeCss",
                            sportThemeService.toCssVariables(themeEntity)
                    );
                });
            }
        }

        // ================================
        // Active menu resolution
        // ================================
        String uri = request.getRequestURI();
        String activeMenu = null;

        if (uri != null) {
            if (uri.startsWith("/admin/view/")) {
                activeMenu = extractFirstPathSegment(uri, "/admin/view/");
            } else if (uri.startsWith("/super-admin/")) {
                activeMenu = extractFirstPathSegment(uri, "/super-admin/");
            }
        }

        model.addAttribute("activeMenu", activeMenu);
    }

    // =====================================================
    // === Helpers
    // =====================================================

    private String extractFirstPathSegment(String uri, String prefix) {
        String rest = uri.substring(prefix.length());
        if (rest.isBlank()) {
            return null;
        }
        String[] parts = rest.split("/");
        return parts.length > 0 ? parts[0] : null;
    }
}
