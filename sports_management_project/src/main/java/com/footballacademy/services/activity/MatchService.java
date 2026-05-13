package com.footballacademy.services.activity;

import com.footballacademy.model.Match;
import com.footballacademy.repository.MatchRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public
class MatchService {
    private final MatchRepository matchRepository;
    private final AcademyAccessService academyAccessService;
    public MatchService(MatchRepository matchRepository, AcademyAccessService academyAccessService) {
        this.matchRepository = matchRepository;
        this.academyAccessService = academyAccessService;
    }
    public Match recordResult(Long matchId, String result) {
        Match match = getMatchById(matchId);
        match.setResult(result);
        return matchRepository.save(match);
    }
    public Match createMatch(Match match) {
        if (!academyAccessService.isSuperAdmin() || match.getAcademy() == null) {
            match.setAcademy(academyAccessService.academyForWrite(match.getAcademy()));
        } else {
            academyAccessService.assertCanAccessAcademy(match.getAcademy());
        } return matchRepository.save(match);
    }
    public Match updateMatch(Long id, Match details) {
        Match existing = getMatchById(id);
        if (details.getTrainerId() != null) existing.setTrainerId(details.getTrainerId());
        if (details.getTitre() != null) existing.setTitre(details.getTitre());
        if (details.getDescription() != null) existing.setDescription(details.getDescription());
        if (details.getDate() != null) existing.setDate(details.getDate());
        if (details.getLieu() != null) existing.setLieu(details.getLieu());
        if (details.getOpponent() != null) existing.setOpponent(details.getOpponent());
        if (details.getResult() != null) existing.setResult(details.getResult());
        return matchRepository.save(existing);
    }
    public Match getMatchById(Long id) {
        Match match = matchRepository.findById(id) .orElseThrow(() -> new RuntimeException("Match not found with id: " + id));
        assertVisible(match);
        return match;
    }
    public List<Match> getAllMatches() {
        List<Match> matches = academyAccessService.isSuperAdmin() ? matchRepository.findAll() : matchRepository.findByAcademy_Id(academyAccessService.currentAcademyOrThrow() .getId());
        return matches != null ? matches : Collections.emptyList();
    }
    public List<Match> getMatchesByOpponent(String opponent) {
        List<Match> matches = matchRepository.findByOpponentContainingIgnoreCase(opponent);
        if (!academyAccessService.isSuperAdmin()) {
            matches = matches.stream() .filter(this::isVisible) .toList();
        } return matches != null ? matches : Collections.emptyList();
    }
    public void deleteMatch(Long id) {
        Match match = getMatchById(id);
        matchRepository.delete(match);
    }
    public List<Match> getUpcomingMatches() {
        LocalDate today = LocalDate.now();
        List<Match> matches = academyAccessService.isSuperAdmin() ? matchRepository.findByDateGreaterThanEqualOrderByDateAsc(today) : matchRepository.findByAcademy_IdAndDateGreaterThanEqualOrderByDateAsc(academyAccessService.currentAcademyOrThrow() .getId(), today);
        return matches != null ? matches : Collections.emptyList();
    }
    public List<Match> getMatchesInDateRange(LocalDate startDate, LocalDate endDate) {
        List<Match> matches = academyAccessService.isSuperAdmin() ? matchRepository.findByDateBetweenOrderByDateAsc(startDate, endDate) : matchRepository.findByAcademy_IdAndDateBetweenOrderByDateAsc(academyAccessService.currentAcademyOrThrow() .getId(), startDate, endDate);
        return matches != null ? matches : Collections.emptyList();
    }
    private boolean isVisible(Match match) {
        return match == null || academyAccessService.isSuperAdmin() || academyAccessService.canAccessAcademy(match.getAcademy());
    }
    private void assertVisible(Match match) {
        if (!isVisible(match)) {
            throw new AccessDeniedException("You cannot access another academy's match");
        }
    }
}
