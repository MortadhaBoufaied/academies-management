package com.footballacademy.services.sport;

import com.footballacademy.model.SportPosition;
import com.footballacademy.repository.SportPositionRepository;
import com.footballacademy.repository.SportRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public
class SportPositionService {
    private final SportPositionRepository sportPositionRepository;
    private final SportRepository sportRepository;
    private final AcademyAccessService academyAccessService;
    public SportPositionService(SportPositionRepository sportPositionRepository, SportRepository sportRepository, AcademyAccessService academyAccessService) {
        this.sportPositionRepository = sportPositionRepository;
        this.sportRepository = sportRepository;
        this.academyAccessService = academyAccessService;
    }
    public List<SportPosition> getAllPositions() {
        if (!academyAccessService.isSuperAdmin()) {
            Long sportId = academyAccessService.currentSportId();
            return sportId == null ? List.of() : sportPositionRepository.findBySport_IdOrderByDisplayOrderAsc(sportId);
        } return sportPositionRepository.findAll();
    }
    public List<SportPosition> getPositionsBySport(Long sportId) {
        academyAccessService.assertCanAccessSport(sportRepository.findById(sportId) .orElseThrow(() -> new IllegalArgumentException("Sport not found with id: " + sportId)));
        return sportPositionRepository.findBySport_IdOrderByDisplayOrderAsc(sportId);
    }
    public Optional<SportPosition> getPositionById(Long id) {
        return sportPositionRepository.findById(id) .filter(position -> academyAccessService.canAccessSport(position.getSport()));
    }
    public SportPosition createPosition(SportPosition position) {
        if (position.getSport() == null || position.getSport() .getId() == null) {
            throw new IllegalArgumentException("Sport is required");
        }
        if (sportPositionRepository.existsByCodeAndSport_Id(position.getCode(), position.getSport() .getId())) {
            throw new IllegalArgumentException("Position with code " + position.getCode() + " already exists for this sport");
        } return sportPositionRepository.save(position);
    }
    public SportPosition updatePosition(Long id, SportPosition position) {
        SportPosition existingPosition = sportPositionRepository.findById(id) .orElseThrow(() -> new IllegalArgumentException("Position not found with id: " + id));
        if (!existingPosition.getCode() .equals(position.getCode()) && sportPositionRepository.existsByCodeAndSport_Id(position.getCode(), existingPosition.getSport() .getId())) {
            throw new IllegalArgumentException("Position with code " + position.getCode() + " already exists for this sport");
        } existingPosition.setCode(position.getCode());
        existingPosition.setName(position.getName());
        existingPosition.setDescription(position.getDescription());
        existingPosition.setDisplayOrder(position.getDisplayOrder());
        return sportPositionRepository.save(existingPosition);
    }
    public void deletePosition(Long id) {
        if (!sportPositionRepository.existsById(id)) {
            throw new IllegalArgumentException("Position not found with id: " + id);
        } sportPositionRepository.deleteById(id);
    }
}
