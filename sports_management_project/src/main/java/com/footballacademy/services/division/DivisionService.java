package com.footballacademy.services.division;

import com.footballacademy.model.Division;
import com.footballacademy.model.Player;
import com.footballacademy.model.Sport;
import com.footballacademy.repository.DivisionRepository;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.repository.SportRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.academy.AcademyInfoService;
import com.footballacademy.services.chat.ChatRoomService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public
class DivisionService {
    private final DivisionRepository divisionRepository;
    private final ChatRoomService chatRoomService;
    private final PlayerRepository playerRepository;
    private final AcademyInfoService academyInfoService;
    private final AcademyAccessService academyAccessService;
    private final SportRepository sportRepository;
    public DivisionService(DivisionRepository divisionRepository, ChatRoomService chatRoomService, PlayerRepository playerRepository, AcademyInfoService academyInfoService, AcademyAccessService academyAccessService, SportRepository sportRepository) {
        this.divisionRepository = divisionRepository;
        this.chatRoomService = chatRoomService;
        this.playerRepository = playerRepository;
        this.academyInfoService = academyInfoService;
        this.academyAccessService = academyAccessService;
        this.sportRepository = sportRepository;
    }
    @Transactional(readOnly = true)
    public List<Division> getAllDivisions() {
        List<Division> divisions = divisionRepository.findAll();
        if (divisions == null) {
            return Collections.emptyList();
        } return divisions.stream() .filter(this::isVisible) .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public Division getDivisionById(Long id) {
        Division division = divisionRepository.findById(id) .orElseThrow(() -> new RuntimeException("Division not found with id: " + id));
        assertVisible(division);
        return division;
    }
    public Division createDivision(Division division) {
        if (division == null) {
            throw new RuntimeException("Division data is required");
        } assignSportIfMissing(division);
        assignAcademyIfMissing(division);
        validateDivisionData(division);
        validateSportAccess(division);
        Division saved = divisionRepository.save(division);
        try {
            chatRoomService.ensureDivisionGroup(saved.getId());
        } catch (Exception ignored) {
            // Chat room creation must not break division creation.
        } return saved;
    }
    public Division updateDivision(Long id, Division divisionDetails) {
        if (divisionDetails == null) {
            throw new RuntimeException("Division data is required");
        } Division division = getDivisionById(id);
        division.setNom(divisionDetails.getNom());
        division.setCategorie(divisionDetails.getCategorie());
        division.setMinAge(divisionDetails.getMinAge());
        division.setMaxAge(divisionDetails.getMaxAge());
        division.setGender(divisionDetails.getGender());
        division.setLevel(divisionDetails.getLevel());
        division.setMinWeight(divisionDetails.getMinWeight());
        division.setMaxWeight(divisionDetails.getMaxWeight());
        division.setCompetitionScope(divisionDetails.getCompetitionScope());
        division.setDisplayOrder(divisionDetails.getDisplayOrder());
        division.setActive(divisionDetails.getActive());
        if (divisionDetails.getSport() != null) {
            division.setSport(divisionDetails.getSport());
        }
        if (divisionDetails.getCategory() != null) {
            division.setCategory(divisionDetails.getCategory());
        } validateDivisionData(division);
        validateSportAccess(division);
        return divisionRepository.save(division);
    }
    /**
    * Soft detach behavior:
    * - Keep the division row in the divisions table.
    * - Remove the division from AcademyInfo.divisionsList.
    * - Set division_id = NULL for all assigned players.
    */
    public void deleteDivision(Long id) {
        Division division = getDivisionById(id);
        assertVisible(division);
        academyInfoService.removeDivision(id);
        List<Player> players = playerRepository.findByDivisionId(id);
        if (players != null && !players.isEmpty()) {
            for (Player player : players) {
                player.setDivision(null);
            } playerRepository.saveAll(players);
        }
    }
    public void addPlayer(Long divisionId, Long playerId) {
        Division division = getDivisionById(divisionId);
        Player player = playerRepository.findById(playerId) .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        assertSameAcademy(division, player);
        division.addPlayer(player);
        divisionRepository.save(division);
    }
    public void removePlayer(Long divisionId, Long playerId) {
        Division division = getDivisionById(divisionId);
        Player player = playerRepository.findById(playerId) .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        assertSameAcademy(division, player);
        if (division.getPlayers() == null || !division.getPlayers() .contains(player)) {
            throw new RuntimeException("Player is not in this division");
        } division.removePlayer(player);
        divisionRepository.save(division);
    }
    @Transactional(readOnly = true)
    public List<Player> getDivisionPlayers(Long divisionId) {
        Division division = getDivisionById(divisionId);
        return division.getPlayers() != null ? division.getPlayers() : Collections.emptyList();
    }
    @Transactional(readOnly = true)
    public List<Division> getDivisionsByCategory(String category) {
        List<Division> divisions = divisionRepository.findByCategorie(category);
        if (divisions == null) {
            return Collections.emptyList();
        } return divisions.stream() .filter(this::isVisible) .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<Division> getDivisionsBySport(Long sportId) {
        if (sportId == null) {
            return Collections.emptyList();
        } Sport sport = sportRepository.findById(sportId) .orElseThrow(() -> new RuntimeException("Sport not found with id: " + sportId));
        academyAccessService.assertCanAccessSport(sport);
        List<Division> divisions = divisionRepository.findBySport_IdOrderByDisplayOrderAscNomAsc(sportId);
        if (divisions == null) {
            return Collections.emptyList();
        } return divisions.stream() .filter(this::isVisible) .collect(Collectors.toList());
    }
    private void validateDivisionData(Division division) {
        if (division.getNom() == null || division.getNom() .trim() .isEmpty()) {
            throw new RuntimeException("Division name is required");
        }
        if (division.getCategorie() == null || division.getCategorie() .trim() .isEmpty()) {
            throw new RuntimeException("Division category is required");
        }
        if (division.getSport() == null) {
            throw new RuntimeException("Division sport is required");
        }
        if (division.getMinAge() != null && division.getMaxAge() != null && division.getMinAge() > division.getMaxAge()) {
            throw new RuntimeException("Division minAge cannot be greater than maxAge");
        }
        if (division.getMinWeight() != null && division.getMaxWeight() != null && division.getMinWeight() .compareTo(division.getMaxWeight()) > 0) {
            throw new RuntimeException("Division minWeight cannot be greater than maxWeight");
        }
    }
    private void assignSportIfMissing(Division division) {
        if (division.getSport() != null) {
            return;
        }
        if (!academyAccessService.isSuperAdmin()) {
            Sport academySport = academyAccessService.currentAcademyOrThrow() .getSport();
            if (academySport != null) {
                division.setSport(academySport);
                return;
            }
        }
        if (sportRepository.count() == 1) {
            List<Sport> sports = sportRepository.findAll();
            if (sports != null && !sports.isEmpty()) {
                division.setSport(sports.get(0));
            }
        }
    }
    private void assignAcademyIfMissing(Division division) {
        if (!academyAccessService.isSuperAdmin() && division.getAcademy() == null) {
            division.setAcademy(academyAccessService.currentAcademyOrThrow());
        }
    }
    private void validateSportAccess(Division division) {
        if (division.getSport() != null) {
            academyAccessService.assertCanAccessSport(division.getSport());
        }
    }
    private boolean isVisible(Division division) {
        return academyAccessService.canAccessDivision(division);
    }
    private void assertVisible(Division division) {
        if (!isVisible(division)) {
            throw new AccessDeniedException("You cannot access another academy's division");
        }
    }
    private void assertSameAcademy(Division division, Player player) {
        if (academyAccessService.isSuperAdmin()) {
            return;
        } Long divisionAcademyId = division.getAcademy() != null ? division.getAcademy() .getId() : null;
        Long divisionSportId = division.getSport() != null ? division.getSport() .getId() : null;
        Long currentSportId = academyAccessService.currentSportId();
        if (divisionAcademyId == null && divisionSportId != null && divisionSportId.equals(currentSportId)) {
            return;
        } Long playerAcademyId = player.getAcademy() != null ? player.getAcademy() .getId() : null;
        if (divisionAcademyId == null || !divisionAcademyId.equals(playerAcademyId)) {
            throw new AccessDeniedException("Cannot assign a player from another academy");
        }
    }
}
