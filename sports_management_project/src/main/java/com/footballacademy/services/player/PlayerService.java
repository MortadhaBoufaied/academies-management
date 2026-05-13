package com.footballacademy.services.player;

import com.footballacademy.DTO.PlayerCombinedDTO;
import com.footballacademy.model.*;
import com.footballacademy.repository.DivisionRepository;
import com.footballacademy.repository.ParentRepository;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.repository.TrainerRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.chat.ChatRoomService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public
class PlayerService {
    private final PlayerRepository playerRepository;
    private final ChatRoomService chatRoomService;
    private final DivisionRepository divisionRepository;
    private final ParentRepository parentRepository;
    private final TrainerRepository trainerRepository;
    private final AcademyAccessService academyAccessService;
    public PlayerService(PlayerRepository playerRepository, ChatRoomService chatRoomService, DivisionRepository divisionRepository, ParentRepository parentRepository, TrainerRepository trainerRepository, AcademyAccessService academyAccessService) {
        this.playerRepository = playerRepository;
        this.chatRoomService = chatRoomService;
        this.divisionRepository = divisionRepository;
        this.parentRepository = parentRepository;
        this.trainerRepository = trainerRepository;
        this.academyAccessService = academyAccessService;
    }
    private PlayerCombinedDTO toDto(Player p) {
        User u = p.getUser();
        Long userId =(u != null ? u.getId() : p.getId());
        return new PlayerCombinedDTO(p.getId(), userId,(u != null ? u.getNom() : null),(u != null ? u.getEmail() : null),(u != null ? u.getTel() : null),(u != null ? u.getDateNaiss() : null), p.getPosition(), p.getAge(), p.getNationality(),((p.getImageUrl() != null && !p.getImageUrl() .isBlank()) ? p.getImageUrl() : ""), p.isPaid(), p.getHeight(), p.getWeight(), p.getGoals(), p.getAssists(), p.getMatches(), p.getAverageRating(),(p.getDivision() != null ? p.getDivision() .getId() : null),(p.getParent() != null ? p.getParent() .getId() : null),(p.getTrainer() != null ? p.getTrainer() .getId() : null));
    }
    public List<Player> getAllPlayers() {
        List<Player> players = academyAccessService.isSuperAdmin() ? playerRepository.findAll() : playerRepository.findByAcademy_Id(academyAccessService.currentAcademyOrThrow() .getId());
        if (players == null) return Collections.emptyList();
        return players;
    }
    @Transactional(readOnly = true)
    public List<Player> getAllPlayersForView() {
        List<Player> players = academyAccessService.isSuperAdmin() ? playerRepository.findAllWithUserAndRefs() : playerRepository.findByAcademyIdWithUserAndRefs(academyAccessService.currentAcademyOrThrow() .getId());
        if (players == null || players.isEmpty()) {
            return Collections.emptyList();
        } return players;
    }
    @Transactional(readOnly = true)
    public List<PlayerCombinedDTO> getAllPlayersCombined() {
        List<Player> players = academyAccessService.isSuperAdmin() ? playerRepository.findAllWithUserAndRefs() : playerRepository.findByAcademyIdWithUserAndRefs(academyAccessService.currentAcademyOrThrow() .getId());
        if (players == null || players.isEmpty()) return Collections.emptyList();
        return players.stream() .map(this::toDto) .toList();
    }
    @Transactional(readOnly = true)
    public List<PlayerCombinedDTO> getPlayersCombinedByDivision(Long divisionId) {
        divisionRepository.findById(divisionId) .ifPresent(academyAccessService::assertCanAccessDivision);
        List<Player> players = playerRepository.findByDivisionIdWithUserAndRefs(divisionId);
        if (players == null || players.isEmpty()) return Collections.emptyList();
        return players.stream() .filter(this::isVisible) .map(this::toDto) .toList();
    }
    @Transactional(readOnly = true)
    public List<PlayerCombinedDTO> getPlayersWithoutDivisionCombined() {
        List<Player> players = playerRepository.findByDivisionIsNullWithUserAndRefs();
        if (players == null || players.isEmpty()) return Collections.emptyList();
        return players.stream() .filter(this::isVisible) .map(this::toDto) .toList();
    }
    public Player getPlayerById(Long id) {
        Player player = playerRepository.findByIdWithUserAndRefs(id) .orElseThrow(() -> new RuntimeException("Player not found with id: " + id));
        assertVisible(player);
        return player;
    }
    @Transactional(readOnly = true)
    public Player findVisiblePlayerById(Long id) {
        if (id == null) {
            return null;
        } return playerRepository.findByIdWithUserAndRefs(id) .filter(this::isVisible) .orElse(null);
    }
    @Transactional(readOnly = true)
    public PlayerCombinedDTO getPlayerCombinedById(Long id) {
        return toDto(getPlayerById(id));
    }
    public Player assignRelations(Long playerId, Long divisionId, Long trainerId, Long parentId) {
        Player player = getPlayerById(playerId);
        if (divisionId != null) {
            Division division = divisionRepository.findById(divisionId) .orElseThrow(() -> new RuntimeException("Division not found"));
            academyAccessService.assertCanAccessDivision(division);
            player.setDivision(division);
        } else {
            player.setDivision(null);
        }
        if (trainerId != null) {
            Trainer trainer = trainerRepository.findById(trainerId) .orElseThrow(() -> new RuntimeException("Trainer not found"));
            assertSameAcademy(player, trainer.getAcademy());
            player.setTrainer(trainer);
        } else {
            player.setTrainer(null);
        }
        if (parentId != null) {
            Parent parent = parentRepository.findById(parentId) .orElseThrow(() -> new RuntimeException("Parent not found"));
            assertSameAcademy(player, parent.getAcademy());
            player.setParent(parent);
        } else {
            player.setParent(null);
        } return playerRepository.save(player);
    }
    @Transactional(readOnly = true)
    public List<PlayerCombinedDTO> searchPlayersByName(String name) {
        if (name == null || name.trim() .isEmpty()) {
            return getAllPlayersCombined();
        } List<Player> players = playerRepository.findByUserNomContainingIgnoreCase(name);
        if (players == null || players.isEmpty()) return Collections.emptyList();
        return players.stream() .filter(this::isVisible) .map(this::toDto) .toList();
    }
    @Transactional(readOnly = true)
    public List<PlayerCombinedDTO> searchPlayersAdvanced(String name, String position, Boolean paid, Long divisionId) {
        List<Player> allPlayers = playerRepository.findAllWithUserAndRefs();
        if (allPlayers == null || allPlayers.isEmpty()) return Collections.emptyList();
        // Apply filters
        return allPlayers.stream() .filter(this::isVisible) .filter(p -> name == null || name.isEmpty() ||(p.getUser() != null && p.getUser() .getNom() .toLowerCase() .contains(name.toLowerCase()))) .filter(p -> position == null || position.isEmpty() ||(p.getPosition() != null && p.getPosition() .equalsIgnoreCase(position))) .filter(p -> paid == null || p.isPaid() == paid) .filter(p -> divisionId == null ||(p.getDivision() != null && p.getDivision() .getId() .equals(divisionId))) .map(this::toDto) .toList();
    }
    @Transactional(readOnly = true)
    public List<Player> searchPlayersForView(String query) {
        if (query == null || query.trim() .isEmpty()) {
            return getAllPlayersForView();
        } String normalized = query.trim() .toLowerCase();
        return getAllPlayersForView() .stream() .filter(player -> {
            User user = player.getUser();
            if (user == null) {
                return false;
            } String name = user.getNom() != null ? user.getNom() .toLowerCase() : "";
            String email = user.getEmail() != null ? user.getEmail() .toLowerCase() : "";
            return name.contains(normalized) || email.contains(normalized);
        }) .toList();
    }
    private boolean isVisible(Player player) {
        if (player == null || academyAccessService.isSuperAdmin()) {
            return true;
        } Long currentAcademyId = academyAccessService.currentAcademyId();
        Long playerAcademyId = player.getAcademy() != null ? player.getAcademy() .getId() : null;
        return currentAcademyId != null && currentAcademyId.equals(playerAcademyId);
    }
    private void assertVisible(Player player) {
        if (!isVisible(player)) {
            throw new AccessDeniedException("You cannot access another academy's player");
        }
    }
    private void assertSameAcademy(Player player, Academy relatedAcademy) {
        if (academyAccessService.isSuperAdmin() || relatedAcademy == null) {
            return;
        } Long playerAcademyId = player.getAcademy() != null ? player.getAcademy() .getId() : null;
        if (!relatedAcademy.getId() .equals(playerAcademyId)) {
            throw new AccessDeniedException("Cannot assign data from another academy");
        }
    }
}
