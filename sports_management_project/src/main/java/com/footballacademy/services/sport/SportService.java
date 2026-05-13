package com.footballacademy.services.sport;

import com.footballacademy.model.Division;
import com.footballacademy.model.Sport;
import com.footballacademy.model.SportTheme;
import com.footballacademy.repository.DivisionRepository;
import com.footballacademy.repository.SportRepository;
import com.footballacademy.repository.SportThemeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public
class SportService {
    private final SportRepository sportRepository;
    private final SportThemeRepository sportThemeRepository;
    private final DivisionRepository divisionRepository;
    public SportService(SportRepository sportRepository, SportThemeRepository sportThemeRepository, DivisionRepository divisionRepository) {
        this.sportRepository = sportRepository;
        this.sportThemeRepository = sportThemeRepository;
        this.divisionRepository = divisionRepository;
    }
    public List<Sport> getAllSports() {
        return sportRepository.findAll();
    }
    public List<Sport> getActiveSports() {
        return sportRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }
    public Optional<Sport> getSportById(Long id) {
        return sportRepository.findById(id);
    }
    public Optional<Sport> getSportByCode(String code) {
        return sportRepository.findByCode(code);
    }
    public Sport createSport(Sport sport) {
        return createSport(sport, null, null);
    }
    public Sport createSport(Sport sport, SportTheme theme, List<Division> divisions) {
        normalizeSport(sport);
        if (sportRepository.existsByCode(sport.getCode())) {
            throw new IllegalArgumentException("Sport with code " + sport.getCode() + " already exists");
        } Sport saved = sportRepository.save(sport);
        saveThemeForSport(saved, theme);
        upsertDivisionsForSport(saved, divisions);
        return sportRepository.save(saved);
    }
    public Sport updateSport(Long id, Sport sport) {
        return updateSport(id, sport, null, null);
    }
    public Sport updateSport(Long id, Sport sport, SportTheme theme, List<Division> divisions) {
        Sport existingSport = sportRepository.findById(id) .orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + id));
        normalizeSport(sport);
        if (!existingSport.getCode() .equals(sport.getCode()) && sportRepository.existsByCode(sport.getCode())) {
            throw new IllegalArgumentException("Sport with code " + sport.getCode() + " already exists");
        } existingSport.setCode(sport.getCode());
        existingSport.setName(sport.getName());
        existingSport.setDescription(sport.getDescription());
        existingSport.setIsActive(sport.getIsActive());
        existingSport.setDisplayOrder(sport.getDisplayOrder());
        Sport saved = sportRepository.save(existingSport);
        saveThemeForSport(saved, theme);
        if (divisions != null) {
            upsertDivisionsForSport(saved, divisions);
        } return sportRepository.save(saved);
    }
    public List<Division> getDivisionsForSport(Long sportId) {
        if (sportId == null) {
            return Collections.emptyList();
        } return divisionRepository.findBySport_IdOrderByDisplayOrderAscNomAsc(sportId);
    }
    public void deleteSport(Long id) {
        if (!sportRepository.existsById(id)) {
            throw new IllegalArgumentException("Sport not found with id: " + id);
        } sportRepository.deleteById(id);
    }
    public void activateSport(Long id) {
        Sport sport = sportRepository.findById(id) .orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + id));
        sport.setIsActive(true);
        sportRepository.save(sport);
    }
    public void deactivateSport(Long id) {
        Sport sport = sportRepository.findById(id) .orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + id));
        sport.setIsActive(false);
        sportRepository.save(sport);
    }
    private void normalizeSport(Sport sport) {
        if (sport.getCode() == null || sport.getCode() .isBlank()) {
            throw new IllegalArgumentException("Sport code is required");
        }
        if (sport.getName() == null || sport.getName() .isBlank()) {
            throw new IllegalArgumentException("Sport name is required");
        } sport.setCode(sport.getCode() .trim() .toUpperCase());
        sport.setName(sport.getName() .trim());
        if (sport.getIsActive() == null) {
            sport.setIsActive(true);
        }
        if (sport.getDisplayOrder() == null) {
            sport.setDisplayOrder(0);
        }
    }
    private void saveThemeForSport(Sport sport, SportTheme incoming) {
        if (incoming == null) {
            return;
        } SportTheme target = resolveThemeTarget(sport, incoming);
        copyThemeFields(incoming, target);
        target.setSport(sport);
        if (target.getScope() == null || target.getScope() == SportTheme.ThemeScope.ACADEMY) {
            target.setScope(SportTheme.ThemeScope.GLOBAL);
        } Integer currentVersion = target.getVersion() == null ? 0 : target.getVersion();
        target.setVersion(currentVersion + 1);
        SportTheme savedTheme = sportThemeRepository.save(target);
        sport.setTheme(savedTheme);
    }
    private SportTheme resolveThemeTarget(Sport sport, SportTheme incoming) {
        if (incoming.getId() != null) {
            return sportThemeRepository.findById(incoming.getId()) .orElseThrow(() -> new IllegalArgumentException("Theme not found: " + incoming.getId()));
        }
        if (sport.getTheme() != null && sport.getTheme() .getId() != null) {
            return sportThemeRepository.findById(sport.getTheme() .getId()) .orElseGet(SportTheme::new);
        } return new SportTheme();
    }
    private void copyThemeFields(SportTheme source, SportTheme target) {
        target.setScope(source.getScope());
        target.setPrimaryColor(source.getPrimaryColor());
        target.setSecondaryColor(source.getSecondaryColor());
        target.setBackgroundColor(source.getBackgroundColor());
        target.setAccentColor(source.getAccentColor());
        target.setTextColor(source.getTextColor());
        target.setLogoUrl(source.getLogoUrl());
        target.setDefaultPlayerImageUrl(source.getDefaultPlayerImageUrl());
        target.setDefaultTrainerImageUrl(source.getDefaultTrainerImageUrl());
        target.setDefaultParentImageUrl(source.getDefaultParentImageUrl());
        target.setDefaultAdminImageUrl(source.getDefaultAdminImageUrl());
        target.setHomeBannerUrl(source.getHomeBannerUrl());
        target.setSplashImageUrl(source.getSplashImageUrl());
        target.setCardStyle(source.getCardStyle());
        target.setFontFamily(source.getFontFamily());
        target.setButtonStyle(source.getButtonStyle());
        target.setIconStyle(source.getIconStyle());
    }
    private void upsertDivisionsForSport(Sport sport, List<Division> divisions) {
        if (divisions == null) {
            return;
        }
        for (Division incoming : divisions) {
            if (incoming == null) {
                continue;
            }
            if (!hasDivisionContent(incoming)) {
                continue;
            } String normalizedName = incoming.getNom() == null ? "" : incoming.getNom() .trim();
            if (normalizedName.isBlank()) {
                throw new IllegalArgumentException("Each drafted division must have a name.");
            } String categorie = incoming.getCategorie() == null ? "" : incoming.getCategorie() .trim();
            if (categorie.isBlank()) {
                throw new IllegalArgumentException("Each drafted division must have a category.");
            } Division target = resolveDivisionTarget(sport, incoming, normalizedName, categorie);
            copyDivisionFields(incoming, target);
            target.setNom(normalizedName);
            target.setCategorie(categorie);
            target.setSport(sport);
            divisionRepository.save(target);
        }
    }
    private Division resolveDivisionTarget(Sport sport, Division incoming, String normalizedName, String categorie) {
        if (incoming.getId() != null) {
            Division existing = divisionRepository.findById(incoming.getId()) .orElseThrow(() -> new IllegalArgumentException("Division not found with id: " + incoming.getId()));
            if (existing.getSport() != null && sport.getId() != null && !sport.getId() .equals(existing.getSport() .getId())) {
                throw new IllegalArgumentException("Division " + incoming.getId() + " does not belong to this sport.");
            } return existing;
        } return divisionRepository .findBySport_IdAndNomIgnoreCaseAndCategorieIgnoreCase(sport.getId(), normalizedName, categorie) .orElseGet(Division::new);
    }
    private boolean hasDivisionContent(Division division) {
        return(division.getNom() != null && !division.getNom() .isBlank()) ||(division.getCategorie() != null && !division.getCategorie() .isBlank()) || division.getMinAge() != null || division.getMaxAge() != null ||(division.getGender() != null && !division.getGender() .isBlank()) ||(division.getLevel() != null && !division.getLevel() .isBlank()) ||(division.getCompetitionScope() != null && !division.getCompetitionScope() .isBlank());
    }
    private void copyDivisionFields(Division source, Division target) {
        target.setMinAge(source.getMinAge());
        target.setMaxAge(source.getMaxAge());
        target.setGender(source.getGender());
        target.setLevel(source.getLevel());
        target.setMinWeight(source.getMinWeight());
        target.setMaxWeight(source.getMaxWeight());
        target.setCompetitionScope(source.getCompetitionScope());
        target.setDisplayOrder(source.getDisplayOrder() == null ? 0 : source.getDisplayOrder());
        target.setActive(source.getActive() == null ? true : source.getActive());
        target.setCategory(source.getCategory());
    }
}
