package com.footballacademy.controllers_mvc;

import com.footballacademy.model.AcademyInfo;
import com.footballacademy.services.academy.AcademyInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminAcademyInfoManageController {

    private final AcademyInfoService academyInfoService;

    public AdminAcademyInfoManageController(AcademyInfoService academyInfoService) {
        this.academyInfoService = academyInfoService;
    }

    // =====================================================
    // === ACADEMY INFO UPDATE
    // =====================================================

    @PostMapping("/academy-info")
    public String save(@RequestParam Map<String, String> form) {

        AcademyInfo current = academyInfoService.getAcademyInfo();
        AcademyInfo info = new AcademyInfo();

        info.setNom(valueOrDefault(form.get("nom"), current.getNom()));
        info.setSlogan(valueOrDefault(form.get("slogan"), current.getSlogan()));
        info.setDescription(valueOrDefault(form.get("description"), current.getDescription()));
        info.setEmail(valueOrDefault(form.get("email"), current.getEmail()));
        info.setPhone(valueOrDefault(form.get("phone"), current.getPhone()));
        info.setWebsite(valueOrDefault(form.get("website"), current.getWebsite()));
        info.setFoundedYear(parseIntegerOrDefault(
                form.get("foundedYear"),
                current.getFoundedYear()
        ));
        info.setAddress(valueOrDefault(form.get("address"), current.getAddress()));
        info.setCity(valueOrDefault(form.get("city"), current.getCity()));
        info.setCountry(valueOrDefault(form.get("country"), current.getCountry()));
        info.setMission(valueOrDefault(form.get("mission"), current.getMission()));

        // Preserve non-editable fields
        info.setTotalPlayers(current.getTotalPlayers());
        info.setTotalCoaches(current.getTotalCoaches());
        info.setTopPlayers(current.getTopPlayers());
        info.setAchievements(current.getAchievements());
        info.setImageUrl(current.getImageUrl());
        info.setFax(current.getFax());
        info.setPostalCode(current.getPostalCode());
        info.setGoogleMapsUrl(current.getGoogleMapsUrl());
        info.setFacebook(current.getFacebook());
        info.setInstagram(current.getInstagram());
        info.setYoutube(current.getYoutube());
        info.setTiktok(current.getTiktok());
        info.setVision(current.getVision());
        info.setEmailSupport(current.getEmailSupport());
        info.setPhoneSupport(current.getPhoneSupport());
        info.setDivisionsList(current.getDivisionsList());

        academyInfoService.updateAcademyInfo(info);

        return "redirect:/admin/view/academy-info?saved=true";
    }

    // =====================================================
    // === DIVISION ATTACH / DETACH
    // =====================================================

    @PostMapping("/academy-info/divisions/{id}/attach")
    public String attach(@PathVariable Long id) {
        academyInfoService.addDivision(id);
        return "redirect:/admin/view/divisions?attached=true";
    }

    @PostMapping("/academy-info/divisions/{id}/detach")
    public String detach(@PathVariable Long id) {
        academyInfoService.removeDivision(id);
        return "redirect:/admin/view/divisions?detached=true";
    }

    // =====================================================
    // === HELPERS
    // =====================================================

    private String valueOrDefault(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }

    private Integer parseIntegerOrDefault(String value, Integer fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
