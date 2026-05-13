package com.footballacademy.services.scouting;

import com.footballacademy.model.*;
import com.footballacademy.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ScouterDashboardService {
    private final ScouterWatchedPlayerRepository watchedRepository;
    private final ScouterRepository scouterRepository;
    private final PlayerRepository playerRepository;
    private final TalentScoreRepository talentScoreRepository;
    private final PlayerProgressionRepository progressionRepository;
    private final ScoutingReportRepository scoutingReportRepository;

    public ScouterDashboardService(
            ScouterWatchedPlayerRepository watchedRepository,
            ScouterRepository scouterRepository,
            PlayerRepository playerRepository,
            TalentScoreRepository talentScoreRepository,
            PlayerProgressionRepository progressionRepository,
            ScoutingReportRepository scoutingReportRepository
    ) {
        this.watchedRepository = watchedRepository;
        this.scouterRepository = scouterRepository;
        this.playerRepository = playerRepository;
        this.talentScoreRepository = talentScoreRepository;
        this.progressionRepository = progressionRepository;
        this.scoutingReportRepository = scoutingReportRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboard(Long scouterId, Map<String, Object> filters) {
        List<Map<String, Object>> watched = getWatchedPlayers(scouterId, filters);
        Map<String, Object> summary = summary(watched);
        return Map.of("summary", summary, "watchedPlayers", watched, "total", watched.size());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getWatchedPlayers(Long scouterId, Map<String, Object> filters) {
        List<ScouterWatchedPlayer> rows = watchedRepository.findByScouter_Id(scouterId);
        return rows.stream()
                .filter(row -> matchesFilters(row, filters))
                .map(this::toWatchedPlayerDto)
                .toList();
    }

    @Transactional
    public ScouterWatchedPlayer markPlayerAsWatched(Long scouterId, Long playerId, String status, String priority, String notes) {
        Scouter scouter = scouterRepository.findById(scouterId)
                .orElseThrow(() -> new IllegalArgumentException("Scouter not found"));
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        ScouterWatchedPlayer watch = watchedRepository.findByScouter_IdAndPlayer_Id(scouterId, playerId)
                .orElseGet(ScouterWatchedPlayer::new);
        watch.setScouter(scouter);
        watch.setPlayer(player);
        watch.setAcademy(player.getAcademy());
        watch.setSport(player.getSport());
        watch.setDivision(player.getDivision());
        watch.setWatchStatus(status);
        watch.setPriority(priority);
        watch.setNotes(notes);
        watch.setLastReviewedAt(LocalDateTime.now());
        return watchedRepository.save(watch);
    }

    @Transactional
    public ScouterWatchedPlayer updateWatchedPlayerStatus(Long scouterId, Long watchId, String status, String priority, String notes) {
        ScouterWatchedPlayer watch = watchedRepository.findById(watchId)
                .orElseThrow(() -> new IllegalArgumentException("Watched player not found"));
        assertOwner(scouterId, watch);
        if (status != null) watch.setWatchStatus(status);
        if (priority != null) watch.setPriority(priority);
        if (notes != null) watch.setNotes(notes);
        watch.setLastReviewedAt(LocalDateTime.now());
        return watchedRepository.save(watch);
    }

    @Transactional
    public void removeWatchedPlayer(Long scouterId, Long watchId) {
        ScouterWatchedPlayer watch = watchedRepository.findById(watchId)
                .orElseThrow(() -> new IllegalArgumentException("Watched player not found"));
        assertOwner(scouterId, watch);
        watchedRepository.delete(watch);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> calculatePlayerAdvancement(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));
        List<TalentScore> talentScores = talentScoreRepository.findByPlayer_IdOrderByGeneratedAtDesc(playerId);
        List<PlayerProgression> progressions = progressionRepository.findByPlayer_IdOrderByRecordedAtDesc(playerId);
        List<ScoutingReport> reports = scoutingReportRepository.findByPlayer_Id(playerId);

        double currentTalent = talentScores.isEmpty() ? safeDouble(player.getAverageRating()) * 10.0 : safeDouble(talentScores.get(0).getScore());
        double previousTalent = talentScores.size() < 2 ? currentTalent : safeDouble(talentScores.get(1).getScore());
        double change = round(currentTalent - previousTalent);
        double latestPotential = reports.stream().map(ScoutingReport::getPotentialScore).filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(currentTalent);

        boolean hasRecentReport = reports.stream().anyMatch(report -> report.getUpdatedAt() != null && report.getUpdatedAt().isAfter(LocalDateTime.now().minusDays(30)));
        boolean improvingSignal = change > 2.0 || progressions.stream().limit(3).anyMatch(PlayerProgression::isImproving);
        boolean decliningSignal = change < -2.0 || progressions.stream().limit(3).anyMatch(p -> !p.isImproving());

        String label;
        if (!hasRecentReport && talentScores.isEmpty()) label = "Needs Review";
        else if (latestPotential >= 80 && improvingSignal) label = "High Potential";
        else if (improvingSignal) label = "Improving";
        else if (decliningSignal) label = "Declining";
        else label = "Stable";

        String riskLevel = decliningSignal ? "MEDIUM" : (!hasRecentReport ? "HIGH" : "LOW");
        String action = switch (label) {
            case "High Potential" -> "Shortlist and schedule a follow-up report.";
            case "Improving" -> "Review after the next match to confirm progression.";
            case "Declining" -> "Request updated scouting and training context.";
            case "Needs Review" -> "Create a fresh scouting review.";
            default -> "Keep watching and monitor next data point.";
        };

        return Map.of(
                "playerId", playerId,
                "progressionLabel", label,
                "talentScoreChange", change,
                "currentTalentScore", round(currentTalent),
                "previousTalentScore", round(previousTalent),
                "riskLevel", riskLevel,
                "recommendedAction", action,
                "confidence", confidence(talentScores.size(), reports.size(), progressions.size()),
                "explanation", "Advancement combines talent score changes, recent scouting reports, player progression records, and average rating."
        );
    }

    private Map<String, Object> toWatchedPlayerDto(ScouterWatchedPlayer watch) {
        Player player = watch.getPlayer();
        Map<String, Object> advancement = calculatePlayerAdvancement(player.getId());
        String playerName = player.getUser() != null ? safe(player.getUser().getNom()) : "Player #" + player.getId();
        List<ScoutingReport> reports = scoutingReportRepository.findByPlayer_Id(player.getId());
        LocalDateTime lastReport = reports.stream().map(ScoutingReport::getUpdatedAt).filter(Objects::nonNull).max(LocalDateTime::compareTo).orElse(null);

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("watchId", watch.getId());
        dto.put("playerId", player.getId());
        dto.put("playerName", playerName);
        dto.put("academyId", watch.getAcademyId());
        dto.put("academyName", watch.getAcademy() != null ? safe(watch.getAcademy().getName()) : "");
        dto.put("sportId", watch.getSportId());
        dto.put("sport", watch.getSport() != null ? safe(watch.getSport().getName()) : "");
        dto.put("divisionId", watch.getDivisionId());
        dto.put("division", watch.getDivision() != null ? safe(watch.getDivision().getNom()) : "");
        dto.put("position", safe(player.getPosition()));
        dto.put("age", player.getAge() != null ? player.getAge() : 0);
        dto.put("currentTalentScore", advancement.get("currentTalentScore"));
        dto.put("previousTalentScore", advancement.get("previousTalentScore"));
        dto.put("scoreChange", advancement.get("talentScoreChange"));
        dto.put("progressionLabel", advancement.get("progressionLabel"));
        dto.put("averageRating", round(safeDouble(player.getAverageRating())));
        dto.put("scoutingStatus", watch.getWatchStatus());
        dto.put("priority", watch.getPriority());
        dto.put("lastReportDate", lastReport);
        dto.put("lastReviewedDate", watch.getLastReviewedAt());
        dto.put("recommendedAction", advancement.get("recommendedAction"));
        dto.put("riskLevel", advancement.get("riskLevel"));
        dto.put("notes", safe(watch.getNotes()));
        return dto;
    }

    private Map<String, Object> summary(List<Map<String, Object>> watched) {
        long improving = watched.stream().filter(item -> "Improving".equals(item.get("progressionLabel"))).count();
        long declining = watched.stream().filter(item -> "Declining".equals(item.get("progressionLabel"))).count();
        long highPotential = watched.stream().filter(item -> "High Potential".equals(item.get("progressionLabel"))).count();
        long needsReview = watched.stream().filter(item -> "Needs Review".equals(item.get("progressionLabel")) || "HIGH".equals(item.get("riskLevel"))).count();
        long shortlisted = watched.stream().filter(item -> "SHORTLISTED".equals(item.get("scoutingStatus"))).count();
        long missingRecent = watched.stream().filter(item -> item.get("lastReportDate") == null).count();
        return Map.of(
                "totalWatchedPlayers", watched.size(),
                "playersImproving", improving,
                "playersDeclining", declining,
                "highPotentialPlayers", highPotential,
                "playersNeedingReview", needsReview,
                "recentlyShortlistedPlayers", shortlisted,
                "playersWithInjuryRisk", 0,
                "playersWithMissingRecentData", missingRecent
        );
    }

    private boolean matchesFilters(ScouterWatchedPlayer row, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) return true;
        return matchesId(row.getSportId(), filters.get("sportId"))
                && matchesId(row.getAcademyId(), filters.get("academyId"))
                && matchesId(row.getDivisionId(), filters.get("divisionId"))
                && matchesText(row.getWatchStatus(), filters.get("watchStatus"))
                && matchesText(row.getPriority(), filters.get("priority"));
    }

    private boolean matchesId(Long actual, Object expected) {
        Long value = toLong(expected);
        return value == null || Objects.equals(actual, value);
    }

    private boolean matchesText(String actual, Object expected) {
        String value = expected == null ? null : expected.toString().trim();
        return value == null || value.isEmpty() || safe(actual).equalsIgnoreCase(value);
    }

    private void assertOwner(Long scouterId, ScouterWatchedPlayer watch) {
        if (!Objects.equals(scouterId, watch.getScouterId())) {
            throw new AccessDeniedException("Cannot modify another scouter's watched player");
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double confidence(int talent, int reports, int progressions) {
        return round(Math.min(0.95, 0.45 + talent * 0.08 + reports * 0.04 + progressions * 0.03));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
