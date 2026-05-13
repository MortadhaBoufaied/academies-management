package com.footballacademy.services.sport;

import com.footballacademy.model.SportStatistic;
import com.footballacademy.repository.SportStatisticRepository;
import com.footballacademy.repository.SportRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public
class SportStatisticService {
    private final SportStatisticRepository sportStatisticRepository;
    private final SportRepository sportRepository;
    private final AcademyAccessService academyAccessService;
    public SportStatisticService(SportStatisticRepository sportStatisticRepository, SportRepository sportRepository, AcademyAccessService academyAccessService) {
        this.sportStatisticRepository = sportStatisticRepository;
        this.sportRepository = sportRepository;
        this.academyAccessService = academyAccessService;
    }
    public List<SportStatistic> getAllStatistics() {
        if (!academyAccessService.isSuperAdmin()) {
            Long sportId = academyAccessService.currentSportId();
            return sportId == null ? List.of() : sportStatisticRepository.findBySport_IdOrderByDisplayOrderAsc(sportId);
        } return sportStatisticRepository.findAll();
    }
    public List<SportStatistic> getStatisticsBySport(Long sportId) {
        academyAccessService.assertCanAccessSport(sportRepository.findById(sportId) .orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + sportId)));
        return sportStatisticRepository.findBySport_IdOrderByDisplayOrderAsc(sportId);
    }
    public Optional<SportStatistic> getStatisticById(Long id) {
        return sportStatisticRepository.findById(id) .filter(statistic -> academyAccessService.canAccessSport(statistic.getSport()));
    }
    public SportStatistic createStatistic(SportStatistic statistic) {
        if (statistic.getSport() == null || statistic.getSport() .getId() == null) {
            throw new IllegalArgumentException("Sport is required");
        }
        if (sportStatisticRepository.existsByCodeAndSport_Id(statistic.getCode(), statistic.getSport() .getId())) {
            throw new IllegalArgumentException("Statistic with code " + statistic.getCode() + " already exists for this sport");
        } return sportStatisticRepository.save(statistic);
    }
    public SportStatistic updateStatistic(Long id, SportStatistic statistic) {
        SportStatistic existingStatistic = sportStatisticRepository.findById(id) .orElseThrow(() -> new IllegalArgumentException("Statistic not found with id: " + id));
        if (!existingStatistic.getCode() .equals(statistic.getCode()) && sportStatisticRepository.existsByCodeAndSport_Id(statistic.getCode(), existingStatistic.getSport() .getId())) {
            throw new IllegalArgumentException("Statistic with code " + statistic.getCode() + " already exists for this sport");
        } existingStatistic.setCode(statistic.getCode());
        existingStatistic.setName(statistic.getName());
        existingStatistic.setDescription(statistic.getDescription());
        existingStatistic.setDataType(statistic.getDataType());
        existingStatistic.setIsRequired(statistic.getIsRequired());
        existingStatistic.setDisplayOrder(statistic.getDisplayOrder());
        return sportStatisticRepository.save(existingStatistic);
    }
    public void deleteStatistic(Long id) {
        if (!sportStatisticRepository.existsById(id)) {
            throw new IllegalArgumentException("Statistic not found with id: " + id);
        } sportStatisticRepository.deleteById(id);
    }
}
