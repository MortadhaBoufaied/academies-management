package com.footballacademy.services.sport;

import com.footballacademy.model.Sport;
import com.footballacademy.model.SportCategory;
import com.footballacademy.repository.SportCategoryRepository;
import com.footballacademy.repository.SportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public
class SportCategoryService {
    private final SportCategoryRepository sportCategoryRepository;
    private final SportRepository sportRepository;
    public SportCategoryService(SportCategoryRepository sportCategoryRepository, SportRepository sportRepository) {
        this.sportCategoryRepository = sportCategoryRepository;
        this.sportRepository = sportRepository;
    }
    public List<SportCategory> findAll(Long sportId, boolean activeOnly) {
        if (sportId != null) {
            return activeOnly ? sportCategoryRepository.findBySport_IdAndIsActiveTrueOrderByDisplayOrderAsc(sportId) : sportCategoryRepository.findAll() .stream() .filter(category -> category.getSport() != null && sportId.equals(category.getSport() .getId())) .toList();
        } return activeOnly ? sportCategoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc() : sportCategoryRepository.findAll();
    }
    public SportCategory findById(Long id) {
        return sportCategoryRepository.findById(id) .orElseThrow(() -> new IllegalArgumentException("Sport category not found: " + id));
    }
    public SportCategory create(SportCategory category, Long sportId) {
        normalize(category);
        if (sportId != null) {
            Sport sport = sportRepository.findById(sportId) .orElseThrow(() -> new IllegalArgumentException("Sport not found: " + sportId));
            category.setSport(sport);
        } Long effectiveSportId = category.getSport() != null ? category.getSport() .getId() : null;
        if (effectiveSportId != null && sportCategoryRepository.findByCodeIgnoreCaseAndSport_Id(category.getCode(), effectiveSportId) .isPresent()) {
            throw new IllegalArgumentException("Category code already exists for sport: " + category.getCode());
        } return sportCategoryRepository.save(category);
    }
    public SportCategory update(Long id, SportCategory incoming, Long sportId) {
        SportCategory existing = findById(id);
        if (incoming.getCode() != null) existing.setCode(normalizeCode(incoming.getCode()));
        if (incoming.getName() != null) existing.setName(incoming.getName());
        existing.setDescription(incoming.getDescription());
        if (incoming.getIsActive() != null) existing.setIsActive(incoming.getIsActive());
        if (incoming.getDisplayOrder() != null) existing.setDisplayOrder(incoming.getDisplayOrder());
        if (sportId != null) {
            existing.setSport(sportRepository.findById(sportId) .orElseThrow(() -> new IllegalArgumentException("Sport not found: " + sportId)));
        } return sportCategoryRepository.save(existing);
    }
    public void delete(Long id) {
        sportCategoryRepository.delete(findById(id));
    }
    private void normalize(SportCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("Sport category is required");
        } category.setCode(normalizeCode(category.getCode()));
        if (category.getName() == null || category.getName() .isBlank()) {
            category.setName(category.getCode());
        }
        if (category.getIsActive() == null) {
            category.setIsActive(true);
        }
        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }
    }
    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Category code is required");
        } return code.trim() .toUpperCase(Locale.ROOT);
    }
}
