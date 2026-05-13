package com.footballacademy.services.theme;

import com.footballacademy.model.Academy;
import com.footballacademy.model.Sport;
import com.footballacademy.model.SportTheme;
import com.footballacademy.repository.AcademyRepository;
import com.footballacademy.repository.SportRepository;
import com.footballacademy.repository.SportThemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Transactional
public
class SportThemeService {
    private final SportThemeRepository sportThemeRepository;
    private final AcademyRepository academyRepository;
    private final SportRepository sportRepository;
    public SportThemeService(SportThemeRepository sportThemeRepository, AcademyRepository academyRepository, SportRepository sportRepository) {
        this.sportThemeRepository = sportThemeRepository;
        this.academyRepository = academyRepository;
        this.sportRepository = sportRepository;
    }
    public Map<String, Object> resolveTheme(Long academyId, Long sportId) {
        Long resolvedSportId = resolveSportId(academyId, sportId);
        SportTheme resolved = resolveThemeEntity(academyId, resolvedSportId);
        return toResponse(resolved, academyId, resolvedSportId);
    }
    public SportTheme resolveThemeEntity(Long academyId, Long sportId) {
        Long resolvedSportId = resolveSportId(academyId, sportId);
        SportTheme resolved = platformDefaultTheme();
        if (resolvedSportId != null) {
            sportThemeRepository.findFirstByScopeAndSport_IdOrderByUpdatedAtDesc(SportTheme.ThemeScope.GLOBAL, resolvedSportId) .ifPresent(global -> merge(resolved, global));
        }
        if (academyId != null && resolvedSportId != null) {
            sportThemeRepository.findFirstByScopeAndAcademy_IdAndSport_IdOrderByUpdatedAtDesc(SportTheme.ThemeScope.ACADEMY, academyId, resolvedSportId) .ifPresent(override -> merge(resolved, override));
        } return resolved;
    }
    public String toCssVariables(SportTheme theme) {
        if (theme == null) {
            return "";
        } StringBuilder css = new StringBuilder();
        appendCss(css, "--ag-primary", theme.getPrimaryColor());
        appendCss(css, "--ag-primary-hover", theme.getSecondaryColor());
        appendCss(css, "--ag-secondary", theme.getSecondaryColor());
        appendCss(css, "--ag-primary-soft", theme.getAccentColor());
        appendCss(css, "--ag-primary-muted", theme.getBackgroundColor());
        appendCss(css, "--ag-bg", theme.getBackgroundColor());
        appendCss(css, "--ag-ink", theme.getTextColor());
        if (theme.getFontFamily() != null && !theme.getFontFamily() .isBlank()) {
            appendCss(css, "font-family", "'" + theme.getFontFamily() .trim() + "', system-ui, sans-serif");
        } return css.toString();
    }
    public SportTheme save(SportTheme theme, Long academyId, Long sportId) {
        if (theme == null) {
            throw new IllegalArgumentException("Theme is required");
        }
        if (academyId != null) {
            Academy academy = academyRepository.findById(academyId) .orElseThrow(() -> new IllegalArgumentException("Academy not found: " + academyId));
            theme.setAcademy(academy);
            theme.setScope(SportTheme.ThemeScope.ACADEMY);
        }
        if (sportId != null) {
            Sport sport = sportRepository.findById(sportId) .orElseThrow(() -> new IllegalArgumentException("Sport not found: " + sportId));
            theme.setSport(sport);
        }
        if (theme.getScope() == null) {
            theme.setScope(academyId == null ? SportTheme.ThemeScope.GLOBAL : SportTheme.ThemeScope.ACADEMY);
        } Integer currentVersion = theme.getVersion() == null ? 0 : theme.getVersion();
        theme.setVersion(currentVersion + 1);
        SportTheme saved = sportThemeRepository.save(theme);
        if (sportId != null) {
            Sport sport = sportRepository.findById(sportId) .orElseThrow(() -> new IllegalArgumentException("Sport not found: " + sportId));
            sport.setTheme(saved);
            sportRepository.save(sport);
        } return saved;
    }
    public java.util.List<SportTheme> findAll() {
        return sportThemeRepository.findAll();
    }
    public void delete(Long id) {
        sportRepository.findByTheme_Id(id) .forEach(sport -> {
            sport.setTheme(null);
            sportRepository.save(sport);
        });
        sportThemeRepository.deleteById(id);
    }
    private SportTheme platformDefaultTheme() {
        return sportThemeRepository.findFirstByScopeOrderByUpdatedAtDesc(SportTheme.ThemeScope.PLATFORM_DEFAULT) .map(this::copy) .orElseGet(() -> {
            SportTheme theme = new SportTheme();
            theme.setScope(SportTheme.ThemeScope.PLATFORM_DEFAULT);
            theme.setPrimaryColor("#0f766e");
            theme.setSecondaryColor("#065f46");
            theme.setBackgroundColor("#f8fafc");
            theme.setAccentColor("#f59e0b");
            theme.setTextColor("#0f172a");
            theme.setLogoUrl("/uploads/defaults/player.jpg");
            theme.setDefaultPlayerImageUrl("/uploads/defaults/player.jpg");
            theme.setDefaultTrainerImageUrl("/uploads/defaults/player.jpg");
            theme.setDefaultParentImageUrl("/uploads/defaults/player.jpg");
            theme.setDefaultAdminImageUrl("/uploads/defaults/player.jpg");
            theme.setCardStyle("compact");
            theme.setFontFamily("Public Sans");
            theme.setButtonStyle("solid");
            theme.setIconStyle("rounded");
            theme.setVersion(1);
            theme.setUpdatedAt(LocalDateTime.now());
            return theme;
        });
    }
    private SportTheme copy(SportTheme source) {
        SportTheme target = new SportTheme();
        merge(target, source);
        target.setScope(source.getScope());
        target.setAcademy(source.getAcademy());
        target.setSport(source.getSport());
        target.setVersion(source.getVersion());
        target.setUpdatedAt(source.getUpdatedAt());
        return target;
    }
    private Long resolveSportId(Long academyId, Long sportId) {
        if (sportId != null) {
            return sportId;
        }
        if (academyId == null) {
            return null;
        } return academyRepository.findById(academyId) .map(Academy::getSport) .map(Sport::getId) .orElse(null);
    }
    private void appendCss(StringBuilder css, String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        } css.append(name) .append(':') .append(value.trim()) .append(';');
    }
    private void merge(SportTheme target, SportTheme source) {
        if (source.getPrimaryColor() != null) target.setPrimaryColor(source.getPrimaryColor());
        if (source.getSecondaryColor() != null) target.setSecondaryColor(source.getSecondaryColor());
        if (source.getBackgroundColor() != null) target.setBackgroundColor(source.getBackgroundColor());
        if (source.getAccentColor() != null) target.setAccentColor(source.getAccentColor());
        if (source.getTextColor() != null) target.setTextColor(source.getTextColor());
        if (source.getLogoUrl() != null) target.setLogoUrl(source.getLogoUrl());
        if (source.getDefaultPlayerImageUrl() != null) target.setDefaultPlayerImageUrl(source.getDefaultPlayerImageUrl());
        if (source.getDefaultTrainerImageUrl() != null) target.setDefaultTrainerImageUrl(source.getDefaultTrainerImageUrl());
        if (source.getDefaultParentImageUrl() != null) target.setDefaultParentImageUrl(source.getDefaultParentImageUrl());
        if (source.getDefaultAdminImageUrl() != null) target.setDefaultAdminImageUrl(source.getDefaultAdminImageUrl());
        if (source.getHomeBannerUrl() != null) target.setHomeBannerUrl(source.getHomeBannerUrl());
        if (source.getSplashImageUrl() != null) target.setSplashImageUrl(source.getSplashImageUrl());
        if (source.getCardStyle() != null) target.setCardStyle(source.getCardStyle());
        if (source.getFontFamily() != null) target.setFontFamily(source.getFontFamily());
        if (source.getButtonStyle() != null) target.setButtonStyle(source.getButtonStyle());
        if (source.getIconStyle() != null) target.setIconStyle(source.getIconStyle());
        if (source.getVersion() != null) target.setVersion(source.getVersion());
        if (source.getUpdatedAt() != null) target.setUpdatedAt(source.getUpdatedAt());
    }
    private Map<String, Object> toResponse(SportTheme theme, Long academyId, Long sportId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("academyId", academyId);
        data.put("sportId", sportId);
        data.put("primaryColor", theme.getPrimaryColor());
        data.put("secondaryColor", theme.getSecondaryColor());
        data.put("backgroundColor", theme.getBackgroundColor());
        data.put("accentColor", theme.getAccentColor());
        data.put("textColor", theme.getTextColor());
        data.put("logoUrl", theme.getLogoUrl());
        data.put("defaultPlayerImageUrl", theme.getDefaultPlayerImageUrl());
        data.put("defaultTrainerImageUrl", theme.getDefaultTrainerImageUrl());
        data.put("defaultParentImageUrl", theme.getDefaultParentImageUrl());
        data.put("defaultAdminImageUrl", theme.getDefaultAdminImageUrl());
        data.put("homeBannerUrl", theme.getHomeBannerUrl());
        data.put("splashImageUrl", theme.getSplashImageUrl());
        data.put("cardStyle", theme.getCardStyle());
        data.put("fontFamily", theme.getFontFamily());
        data.put("buttonStyle", theme.getButtonStyle());
        data.put("iconStyle", theme.getIconStyle());
        data.put("version", theme.getVersion());
        data.put("updatedAt", theme.getUpdatedAt());
        return data;
    }
}
