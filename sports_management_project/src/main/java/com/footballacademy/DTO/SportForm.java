package com.footballacademy.DTO;

import com.footballacademy.model.Division;
import com.footballacademy.model.Sport;
import com.footballacademy.model.SportTheme;
import java.util.ArrayList;
import java.util.List;

public
class SportForm {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isActive = true;
    private Integer displayOrder = 0;
    private ThemeForm theme = new ThemeForm();
    private List<DivisionForm> divisions = new ArrayList<>();
    public SportForm() {
        this.divisions.add(new DivisionForm());
    }
    public static SportForm from(Sport sport, List<Division> sourceDivisions) {
        SportForm form = new SportForm();
        form.setDivisions(new ArrayList<>());
        if (sport != null) {
            form.setId(sport.getId());
            form.setCode(sport.getCode());
            form.setName(sport.getName());
            form.setDescription(sport.getDescription());
            form.setIsActive(sport.getIsActive());
            form.setDisplayOrder(sport.getDisplayOrder());
            form.setTheme(ThemeForm.from(sport.getTheme()));
        }
        if (sourceDivisions != null) {
            for (Division division : sourceDivisions) {
                form.getDivisions() .add(DivisionForm.from(division));
            }
        }
        if (form.getDivisions() .isEmpty()) {
            form.getDivisions() .add(new DivisionForm());
        } return form;
    }
    public Sport toSport() {
        Sport sport = new Sport();
        sport.setId(id);
        sport.setCode(code);
        sport.setName(name);
        sport.setDescription(description);
        sport.setIsActive(isActive);
        sport.setDisplayOrder(displayOrder);
        return sport;
    }
    public List<Division> toDivisions() {
        if (divisions == null || divisions.isEmpty()) {
            return List.of();
        } return divisions.stream() .map(DivisionForm::toDivision) .toList();
    }
    public SportTheme toTheme() {
        return theme != null ? theme.toTheme() : null;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean active) {
        isActive = active;
    }
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    public ThemeForm getTheme() {
        return theme;
    }
    public void setTheme(ThemeForm theme) {
        this.theme = theme != null ? theme : new ThemeForm();
    }
    public List<DivisionForm> getDivisions() {
        return divisions;
    }
    public void setDivisions(List<DivisionForm> divisions) {
        this.divisions = divisions != null ? divisions : new ArrayList<>();
    }
    public static
    class DivisionForm {
        private Long id;
        private String nom;
        private String categorie;
        private Integer minAge;
        private Integer maxAge;
        private String gender;
        private String level;
        private String competitionScope;
        private Integer displayOrder = 0;
        private Boolean isActive = true;
        public static DivisionForm from(Division division) {
            DivisionForm form = new DivisionForm();
            if (division == null) {
                return form;
            } form.setId(division.getId());
            form.setNom(division.getNom());
            form.setCategorie(division.getCategorie());
            form.setMinAge(division.getMinAge());
            form.setMaxAge(division.getMaxAge());
            form.setGender(division.getGender());
            form.setLevel(division.getLevel());
            form.setCompetitionScope(division.getCompetitionScope());
            form.setDisplayOrder(division.getDisplayOrder());
            form.setIsActive(division.getActive());
            return form;
        }
        public Division toDivision() {
            Division division = new Division();
            division.setNom(nom);
            division.setCategorie(categorie);
            division.setMinAge(minAge);
            division.setMaxAge(maxAge);
            division.setGender(gender);
            division.setLevel(level);
            division.setCompetitionScope(competitionScope);
            division.setDisplayOrder(displayOrder);
            division.setActive(isActive);
            return division;
        }
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public String getNom() {
            return nom;
        }
        public void setNom(String nom) {
            this.nom = nom;
        }
        public String getCategorie() {
            return categorie;
        }
        public void setCategorie(String categorie) {
            this.categorie = categorie;
        }
        public Integer getMinAge() {
            return minAge;
        }
        public void setMinAge(Integer minAge) {
            this.minAge = minAge;
        }
        public Integer getMaxAge() {
            return maxAge;
        }
        public void setMaxAge(Integer maxAge) {
            this.maxAge = maxAge;
        }
        public String getGender() {
            return gender;
        }
        public void setGender(String gender) {
            this.gender = gender;
        }
        public String getLevel() {
            return level;
        }
        public void setLevel(String level) {
            this.level = level;
        }
        public String getCompetitionScope() {
            return competitionScope;
        }
        public void setCompetitionScope(String competitionScope) {
            this.competitionScope = competitionScope;
        }
        public Integer getDisplayOrder() {
            return displayOrder;
        }
        public void setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
        }
        public Boolean getIsActive() {
            return isActive;
        }
        public void setIsActive(Boolean active) {
            isActive = active;
        }
    }
    public static
    class ThemeForm {
        private Long id;
        private String primaryColor;
        private String secondaryColor;
        private String accentColor;
        private String backgroundColor;
        private String textColor;
        private String fontFamily;
        private String logoUrl;
        private String homeBannerUrl;
        public static ThemeForm from(SportTheme theme) {
            ThemeForm form = new ThemeForm();
            if (theme == null) {
                return form;
            } form.setId(theme.getId());
            form.setPrimaryColor(theme.getPrimaryColor());
            form.setSecondaryColor(theme.getSecondaryColor());
            form.setAccentColor(theme.getAccentColor());
            form.setBackgroundColor(theme.getBackgroundColor());
            form.setTextColor(theme.getTextColor());
            form.setFontFamily(theme.getFontFamily());
            form.setLogoUrl(theme.getLogoUrl());
            form.setHomeBannerUrl(theme.getHomeBannerUrl());
            return form;
        }
        public SportTheme toTheme() {
            boolean hasContent =(primaryColor != null && !primaryColor.isBlank()) ||(secondaryColor != null && !secondaryColor.isBlank()) ||(accentColor != null && !accentColor.isBlank()) ||(backgroundColor != null && !backgroundColor.isBlank()) ||(textColor != null && !textColor.isBlank()) ||(fontFamily != null && !fontFamily.isBlank()) ||(logoUrl != null && !logoUrl.isBlank()) ||(homeBannerUrl != null && !homeBannerUrl.isBlank()) || id != null;
            if (!hasContent) {
                return null;
            } SportTheme theme = new SportTheme();
            theme.setId(id);
            theme.setPrimaryColor(primaryColor);
            theme.setSecondaryColor(secondaryColor);
            theme.setAccentColor(accentColor);
            theme.setBackgroundColor(backgroundColor);
            theme.setTextColor(textColor);
            theme.setFontFamily(fontFamily);
            theme.setLogoUrl(logoUrl);
            theme.setHomeBannerUrl(homeBannerUrl);
            return theme;
        }
        public Long getId() {
            return id;
        }
        public void setId(Long id) {
            this.id = id;
        }
        public String getPrimaryColor() {
            return primaryColor;
        }
        public void setPrimaryColor(String primaryColor) {
            this.primaryColor = primaryColor;
        }
        public String getSecondaryColor() {
            return secondaryColor;
        }
        public void setSecondaryColor(String secondaryColor) {
            this.secondaryColor = secondaryColor;
        }
        public String getAccentColor() {
            return accentColor;
        }
        public void setAccentColor(String accentColor) {
            this.accentColor = accentColor;
        }
        public String getBackgroundColor() {
            return backgroundColor;
        }
        public void setBackgroundColor(String backgroundColor) {
            this.backgroundColor = backgroundColor;
        }
        public String getTextColor() {
            return textColor;
        }
        public void setTextColor(String textColor) {
            this.textColor = textColor;
        }
        public String getFontFamily() {
            return fontFamily;
        }
        public void setFontFamily(String fontFamily) {
            this.fontFamily = fontFamily;
        }
        public String getLogoUrl() {
            return logoUrl;
        }
        public void setLogoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
        }
        public String getHomeBannerUrl() {
            return homeBannerUrl;
        }
        public void setHomeBannerUrl(String homeBannerUrl) {
            this.homeBannerUrl = homeBannerUrl;
        }
    }
}
