package com.footballacademy.services.player;

import com.footballacademy.model.Player;
import com.footballacademy.model.PlayerRanking;
import com.footballacademy.model.PlayerRanking.Tier;
import com.footballacademy.repository.PlayerRankingRepository;
import com.footballacademy.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlayerRankingService {
    private final PlayerRepository playerRepository;
    private final PlayerRankingRepository rankingRepository;
    // Weights (can be set in application.properties)
    @Value("${ranking.weight.goals:0.30}")
    private double W_GOALS;
    @Value("${ranking.weight.assists:0.20}")
    private double W_ASSISTS;
    @Value("${ranking.weight.rating:0.30}")
    private double W_RATING;
    @Value("${ranking.weight.matches:0.15}")
    private double W_MATCHES;
    @Value("${ranking.weight.age:0.05}")
    private double W_AGE;
    // Position multipliers
    @Value("${ranking.pos.FWD:1.00}")
    private double POS_FWD;
    @Value("${ranking.pos.MID:1.00}")
    private double POS_MID;
    @Value("${ranking.pos.DEF:1.00}")
    private double POS_DEF;
    @Value("${ranking.pos.GK:1.00}")
    private double POS_GK;
    // Small penalty if not paid
    @Value("${ranking.penalty.unpaid:0.05}")
    private double PENALTY_UNPAID;
    public PlayerRankingService(PlayerRepository playerRepository, PlayerRankingRepository rankingRepository) {
        this.playerRepository = playerRepository;
        this.rankingRepository = rankingRepository;
    }
    /** Recompute all rankings and persist them. Safe to call anytime. */
    public void recomputeAndPersist() {
        List<Player> players = playerRepository.findAll();
        if (players.isEmpty()) return;
        // Maxima for normalization
        int maxGoals = players.stream() .mapToInt(p -> safeInt(p.getGoals())) .max() .orElse(1);
        int maxAssists = players.stream() .mapToInt(p -> safeInt(p.getAssists())) .max() .orElse(1);
        int maxMatches = players.stream() .mapToInt(p -> safeInt(p.getMatches())) .max() .orElse(1);
        double maxRating = players.stream() .mapToDouble(p -> safeDouble(p.getAverageRating())) .max() .orElse(10.0);
        // Raw scores
        Map<Long, Double> rawScores = new HashMap<>();
        for (Player p : players) {
            double g = normalize(safeInt(p.getGoals()), maxGoals);
            double a = normalize(safeInt(p.getAssists()), maxAssists);
            double m = normalize(safeInt(p.getMatches()), maxMatches);
            double r = normalize(safeDouble(p.getAverageRating()), maxRating);
            double ageFactor = ageScore(safeInt(p.getAge()));
            double posMult = positionMultiplier(p.getPosition());
            double score =(W_GOALS*g + W_ASSISTS*a + W_RATING*r + W_MATCHES*m + W_AGE*ageFactor) * posMult;
            if (!p.isPaid()) score = score *(1.0 - PENALTY_UNPAID);
            // dues penalty
            rawScores.put(p.getId(), score);
        }
        // Percentiles for tiers
        List<Double> sorted = rawScores.values() .stream() .sorted() .collect(Collectors.toList());
        double p90 = percentile(sorted, 90);
        double p60 = percentile(sorted, 60);
        LocalDateTime now = LocalDateTime.now();
        for (Player p : players) {
            double score = rawScores.get(p.getId());
            Tier tier =(score >= p90) ? Tier.ELITE :(score >= p60 ? Tier.CORE : Tier.DEVELOPING);
            PlayerRanking ranking = rankingRepository.findByPlayerId(p.getId()) .orElseGet(PlayerRanking::new);
            ranking.setPlayerId(p.getId());
            ranking.setScore(round(score, 4));
            ranking.setTier(tier);
            ranking.setLastUpdated(now);
            rankingRepository.save(ranking);
        }
    }
    /** Daily schedule: 02:05 local time (Africa/Tunis). Adjust if needed. */
    @Scheduled(cron = "0 5 2 * * *", zone = "Africa/Tunis")
    public void dailyRecomputeJob() {
        recomputeAndPersist();
    }
    /** Top player IDs ordered by score desc */
    @Transactional(readOnly = true)
    public List<Long> getTopIds(int limit) {
        int cap = Math.max(1, Math.min(limit, 100));
        return rankingRepository.findAllByOrderByScoreDesc() .stream() .limit(cap) .map(PlayerRanking::getPlayerId) .toList();
    }
    /** Optional: full ranking objects (for DTO endpoint) */
    @Transactional(readOnly = true)
    public List<PlayerRanking> getTop(int limit, Long divisionId, String position) {
        List<PlayerRanking> all = rankingRepository.findAllByOrderByScoreDesc();
        return all.stream() .filter(r -> filterByDivision(r, divisionId)) .filter(r -> filterByPosition(r, position)) .limit(Math.max(1, Math.min(limit, 100))) .collect(Collectors.toList());
    }
    // --- helpers ---
    private boolean filterByDivision(PlayerRanking r, Long divisionId) {
        if (divisionId == null) return true;
        Player p = r.getPlayer();
        return p != null && p.getDivision() != null && Objects.equals(p.getDivision() .getId(), divisionId);
    }
    private boolean filterByPosition(PlayerRanking r, String position) {
        if (position == null || position.isBlank()) return true;
        Player p = r.getPlayer();
        return p != null && position.equalsIgnoreCase(p.getPosition());
    }
    private int safeInt(Integer v) {
        return v == null ? 0 : v;
    }
    private double safeDouble(Double v) {
        return v == null ? 0.0 : v;
    }
    private double normalize(double x, double max) {
        return max <= 0 ? 0.0 :(x / max);
    }
    private double ageScore(int age) {
        // Peak window example: 18-28 gets 1.0 and tapered outside
        if (age <= 0) return 0.5;
        if (age >= 18 && age <= 28) return 1.0;
        if (age < 18) return 0.8 -(18 - age) * 0.02;
        // gentle taper
        return Math.max(0.6, 1.0 -(age - 28) * 0.02);
    }
    private double positionMultiplier(String pos) {
        if (pos == null) return 1.0;
        switch (pos.toUpperCase()) {
            case "FWD" : return POS_FWD;
            case "MID" : return POS_MID;
            case "DEF" : return POS_DEF;
            case "GK" : return POS_GK;
            default: return 1.0;
        }
    }
    private double percentile(List<Double> sortedAsc, int pct) {
        if (sortedAsc.isEmpty()) return 0.0;
        double rank =(pct / 100.0) *(sortedAsc.size() - 1);
        int lo =(int) Math.floor(rank), hi =(int) Math.ceil(rank);
        if (lo == hi) return sortedAsc.get(lo);
        double w = rank - lo;
        return sortedAsc.get(lo) *(1 - w) + sortedAsc.get(hi) * w;
    }
    private double round(double x, int digits) {
        double m = Math.pow(10, digits);
        return Math.round(x * m) / m;
    }
}
