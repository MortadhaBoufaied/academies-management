package com.footballacademy.config.web;

import com.footballacademy.model.Academy;
import com.footballacademy.model.User;
import com.footballacademy.security.SecurityUtils;
import com.footballacademy.security.UserPrincipal;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.academy.AcademySubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public
class AcademySubscriptionAccessConfig implements WebMvcConfigurer {
    private final AcademyAccessService academyAccessService;
    private final AcademySubscriptionService academySubscriptionService;
    public AcademySubscriptionAccessConfig(AcademyAccessService academyAccessService, AcademySubscriptionService academySubscriptionService) {
        this.academyAccessService = academyAccessService;
        this.academySubscriptionService = academySubscriptionService;
    }
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AcademySubscriptionInterceptor()) .addPathPatterns("/admin/view/**", "/admin/**") .excludePathPatterns("/admin/view/auth/**");
    }
    private
    class AcademySubscriptionInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            UserPrincipal principal = SecurityUtils.getCurrentUser();
            if (principal == null) {
                return true;
            } User user = principal.getUser();
            if (user == null || user.hasRole("SUPER_ADMIN")) {
                return true;
            }
            if (!academyAccessService.roleRequiresAcademy(user.getMainRole())) {
                return true;
            } Academy academy = academyAccessService.currentAcademyOrThrow();
            if (academySubscriptionService.canAccessPath(academy, request)) {
                return true;
            } response.sendRedirect(academySubscriptionService.lockedRedirectPath(academy, request));
            return false;
        }
    }
}
