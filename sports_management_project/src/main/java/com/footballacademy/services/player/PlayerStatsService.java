package com.footballacademy.services.player;

import com.footballacademy.model.Player;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public
class PlayerStatsService {
    private final PlayerRepository playerRepository;
    private final AcademyAccessService academyAccessService;
    public PlayerStatsService(PlayerRepository playerRepository, AcademyAccessService academyAccessService) {
        this.playerRepository = playerRepository;
        this.academyAccessService = academyAccessService;
    }
    public Map<String, Object> getPlayerStats(Long playerId) {
        Player player = getVisiblePlayer(playerId);
        int goals = intValue(player.getGoals());
        int assists = intValue(player.getAssists());
        int matches = intValue(player.getMatches());
        double averageRating = doubleValue(player.getAverageRating());
        Map<String, Object> stats = new HashMap<>();
        stats.put("playerId", player.getId());
        stats.put("playerName", player.getUser() != null ? player.getUser() .getNom() : "Unknown");
        stats.put("goals", goals);
        stats.put("assists", assists);
        stats.put("matches", matches);
        stats.put("averageRating", averageRating);
        stats.put("position", player.getPosition() != null ? player.getPosition() : "Not set");
        double goalsPerMatch = matches > 0 ?(double) goals / matches : 0;
        double assistsPerMatch = matches > 0 ?(double) assists / matches : 0;
        double contribution = goalsPerMatch + assistsPerMatch;
        stats.put("goalsPerMatch", Math.round(goalsPerMatch * 100.0) / 100.0);
        stats.put("assistsPerMatch", Math.round(assistsPerMatch * 100.0) / 100.0);
        stats.put("contributionPerMatch", Math.round(contribution * 100.0) / 100.0);
        return stats;
    }
    public Player updatePlayerStats(Long playerId, int goals, int assists, double rating, boolean played) {
        Player player = getVisiblePlayer(playerId);
        if (played) {
            int currentGoals = intValue(player.getGoals());
            int currentAssists = intValue(player.getAssists());
            int currentMatches = intValue(player.getMatches()) + 1;
            double currentAverageRating = doubleValue(player.getAverageRating());
            player.setGoals(currentGoals + goals);
            player.setAssists(currentAssists + assists);
            player.setMatches(currentMatches);
            double totalRating = currentAverageRating *(currentMatches - 1);
            double newAvg =(totalRating + rating) / currentMatches;
            player.setAverageRating(Math.round(newAvg * 100.0) / 100.0);
        } return playerRepository.save(player);
    }
    public List<Map<String, Object>> getTopScorers(int limit) {
        List<Player> players = visiblePlayers();
        if (players == null || players.isEmpty()) {
            return Collections.emptyList();
        } return players.stream() .sorted(Comparator.comparingInt((Player p) -> intValue(p.getGoals())) .reversed()) .limit(limit) .map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getUser() != null ? p.getUser() .getNom() : "Unknown");
            m.put("goals", intValue(p.getGoals()));
            m.put("position", p.getPosition() != null ? p.getPosition() : "Not set");
            return m;
        }) .collect(Collectors.toList());
    }
    public List<Map<String, Object>> getTopAssists(int limit) {
        List<Player> players = visiblePlayers();
        if (players == null || players.isEmpty()) {
            return Collections.emptyList();
        } return players.stream() .sorted(Comparator.comparingInt((Player p) -> intValue(p.getAssists())) .reversed()) .limit(limit) .map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getUser() != null ? p.getUser() .getNom() : "Unknown");
            m.put("assists", intValue(p.getAssists()));
            m.put("position", p.getPosition() != null ? p.getPosition() : "Not set");
            return m;
        }) .collect(Collectors.toList());
    }
    public List<Map<String, Object>> getTopRatedPlayers(int limit) {
        List<Player> players = visiblePlayers();
        if (players == null || players.isEmpty()) {
            return Collections.emptyList();
        } return players.stream() .sorted(Comparator.comparingDouble((Player p) -> doubleValue(p.getAverageRating())) .reversed()) .limit(limit) .map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getUser() != null ? p.getUser() .getNom() : "Unknown");
            m.put("averageRating", doubleValue(p.getAverageRating()));
            m.put("matches", intValue(p.getMatches()));
            m.put("position", p.getPosition() != null ? p.getPosition() : "Not set");
            return m;
        }) .collect(Collectors.toList());
    }
    public Map<String, Object> getComparativeStats(Long playerId) {
        List<Player> all = visiblePlayers();
        if (all == null || all.isEmpty()) {
            Map<String, Object> emptyComparative = new HashMap<>();
            emptyComparative.put("player", "Unknown");
            emptyComparative.put("goalsVsAvg", 0);
            emptyComparative.put("assistsVsAvg", 0);
            emptyComparative.put("ratingVsAvg", 0);
            return emptyComparative;
        } Player target = all.stream() .filter(p -> p.getId() .equals(playerId)) .findFirst() .orElseThrow(() -> new RuntimeException("Player not found"));
        double avgGoals = all.stream() .mapToInt(player -> intValue(player.getGoals())) .average() .orElse(0);
        double avgAssists = all.stream() .mapToInt(player -> intValue(player.getAssists())) .average() .orElse(0);
        double avgRating = all.stream() .mapToDouble(player -> doubleValue(player.getAverageRating())) .average() .orElse(0);
        Map<String, Object> comparative = new HashMap<>();
        comparative.put("player", target.getUser() != null ? target.getUser() .getNom() : "Unknown");
        comparative.put("goalsVsAvg", Math.round((intValue(target.getGoals()) - avgGoals) * 100.0) / 100.0);
        comparative.put("assistsVsAvg", Math.round((intValue(target.getAssists()) - avgAssists) * 100.0) / 100.0);
        comparative.put("ratingVsAvg", Math.round((doubleValue(target.getAverageRating()) - avgRating) * 100.0) / 100.0);
        return comparative;
    }
    public Map<String, Object> getPerformanceTrend(Long playerId) {
        Player player = getVisiblePlayer(playerId);
        Map<String, Object> trend = new LinkedHashMap<>();
        trend.put("playerId", player.getId());
        trend.put("playerName", player.getUser() != null ? player.getUser() .getNom() : "Unknown");
        trend.put("recentRatings", List.of(6.8, 7.2, 7.5, 8.0, doubleValue(player.getAverageRating())));
        return trend;
    }
    public Map<String, Object> getOverallStats() {
        List<Player> all = visiblePlayers();
        if (all == null || all.isEmpty()) {
            Map<String, Object> emptyStats = new HashMap<>();
            emptyStats.put("totalPlayers", 0);
            emptyStats.put("totalGoals", 0);
            emptyStats.put("totalAssists", 0);
            emptyStats.put("totalMatches", 0);
            emptyStats.put("averageRating", 0);
            return emptyStats;
        } int totalGoals = all.stream() .mapToInt(player -> intValue(player.getGoals())) .sum();
        int totalAssists = all.stream() .mapToInt(player -> intValue(player.getAssists())) .sum();
        int totalMatches = all.stream() .mapToInt(player -> intValue(player.getMatches())) .sum();
        double avgRating = all.stream() .mapToDouble(player -> doubleValue(player.getAverageRating())) .average() .orElse(0);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPlayers", all.size());
        stats.put("totalGoals", totalGoals);
        stats.put("totalAssists", totalAssists);
        stats.put("totalMatches", totalMatches);
        stats.put("averageRating", Math.round(avgRating * 100.0) / 100.0);
        return stats;
    }
    private Player getVisiblePlayer(Long playerId) {
        Player player = playerRepository.findById(playerId) .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        if (!isVisible(player)) {
            throw new AccessDeniedException("You cannot access another academy's player stats");
        } return player;
    }
    private List<Player> visiblePlayers() {
        if (academyAccessService.isSuperAdmin()) {
            return playerRepository.findAll();
        } return playerRepository.findByAcademy_Id(academyAccessService.currentAcademyOrThrow() .getId());
    }
    private boolean isVisible(Player player) {
        return player == null || academyAccessService.isSuperAdmin() || academyAccessService.canAccessAcademy(player.getAcademy());
    }
    private int intValue(Integer value) {
        return value != null ? value : 0;
    }
    private double doubleValue(Double value) {
        return value != null ? value : 0;
    }
}
