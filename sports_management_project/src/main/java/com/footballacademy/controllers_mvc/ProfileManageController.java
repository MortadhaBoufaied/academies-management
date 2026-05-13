package com.footballacademy.controllers_mvc;

import com.footballacademy.model.AcademyInfo;
import com.footballacademy.model.Admin;
import com.footballacademy.repository.AdminRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.security.UserPrincipal;
import com.footballacademy.services.academy.AcademyInfoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@Controller
public
class ProfileManageController {
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final AcademyInfoService academyInfoService;
    public ProfileManageController(UserRepository userRepository, AdminRepository adminRepository, AcademyInfoService academyInfoService) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.academyInfoService = academyInfoService;
    }
    @GetMapping({
        "/admin/view/profile", "/profile/view/index"
    })
    public String profile(
    @AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("adminResponsibilities", Admin.AdminResponsibility.values());
        if (principal != null && principal.getUser() != null && principal.getUser() .hasRole("ADMIN")) {
            adminRepository.findById(principal.getId()) .ifPresent(admin -> model.addAttribute("adminProfile", admin));
            model.addAttribute("academyInfo", academyInfoService.getAcademyInfo());
        } return "pages/modules/profile/index";
    }
    @PostMapping({
        "/admin/profile", "/profile/update"
    })
    public String update(
    @AuthenticationPrincipal UserPrincipal principal,
    @RequestParam Map<String, String> form) {
        if (principal == null || principal.getUser() == null) {
            return "redirect:/login";
        } userRepository.findById(principal.getId()) .ifPresent(user -> {
            user.setNom(form.get("nom"));
            user.setEmail(form.get("email"));
            user.setTel(form.get("tel"));
            user.setBio(form.get("bio"));
            userRepository.save(user);
        });
        if (principal.getUser() .hasRole("ADMIN")) {
            adminRepository.findById(principal.getId()) .ifPresent(admin -> {
                admin.setResponsibility(form.get("responsibility"));
                adminRepository.save(admin);
            });
            if (form.containsKey("academyNom") || form.containsKey("academyName")) {
                AcademyInfo current = academyInfoService.getAcademyInfo();
                AcademyInfo info = new AcademyInfo();
                info.setNom(valueOrDefault(valueOrDefault(form.get("academyNom"), form.get("academyName")), current.getNom()));
                info.setDescription(valueOrDefault(form.get("academyDescription"), current.getDescription()));
                info.setEmail(valueOrDefault(form.get("academyEmail"), current.getEmail()));
                info.setPhone(valueOrDefault(form.get("academyPhone"), current.getPhone()));
                info.setWebsite(valueOrDefault(form.get("academyWebsite"), current.getWebsite()));
                info.setAddress(valueOrDefault(form.get("academyAddress"), current.getAddress()));
                info.setCity(valueOrDefault(form.get("academyCity"), current.getCity()));
                info.setCountry(valueOrDefault(form.get("academyCountry"), current.getCountry()));
                info.setPostalCode(valueOrDefault(form.get("academyPostalCode"), current.getPostalCode()));
                info.setSlogan(valueOrDefault(form.get("academySlogan"), current.getSlogan()));
                info.setMission(valueOrDefault(form.get("academyMission"), current.getMission()));
                info.setVision(valueOrDefault(form.get("academyVision"), current.getVision()));
                info.setEmailSupport(valueOrDefault(form.get("academyEmailSupport"), current.getEmailSupport()));
                info.setPhoneSupport(valueOrDefault(form.get("academyPhoneSupport"), current.getPhoneSupport()));
                info.setFacebook(valueOrDefault(form.get("academyFacebook"), current.getFacebook()));
                info.setInstagram(valueOrDefault(form.get("academyInstagram"), current.getInstagram()));
                info.setYoutube(valueOrDefault(form.get("academyYoutube"), current.getYoutube()));
                info.setTiktok(valueOrDefault(form.get("academyTiktok"), current.getTiktok()));
                info.setGoogleMapsUrl(valueOrDefault(form.get("academyGoogleMapsUrl"), current.getGoogleMapsUrl()));
                info.setImageUrl(valueOrDefault(form.get("academyImageUrl"), current.getImageUrl()));
                info.setFoundedYear(parseIntegerOrDefault(form.get("academyFoundedYear"), current.getFoundedYear()));
                info.setTotalPlayers(current.getTotalPlayers());
                info.setTotalCoaches(current.getTotalCoaches());
                info.setTopPlayers(current.getTopPlayers());
                info.setAchievements(current.getAchievements());
                info.setDivisionsList(current.getDivisionsList());
                academyInfoService.updateAcademyInfo(info);
            }
        } return "redirect:/profile/view/index?updated=true";
    }
    private String valueOrDefault(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
    private Integer parseIntegerOrDefault(String value, Integer fallback) {
        Integer parsed = parseInteger(value);
        return parsed != null ? parsed : fallback;
    }
}
