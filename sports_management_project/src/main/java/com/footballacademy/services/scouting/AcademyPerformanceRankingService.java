package com.footballacademy.services.scouting;

import com.footballacademy.model.*;
import com.footballacademy.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AcademyPerformanceRankingService {
    private final AcademyRepository academyRepository;
    private final PlayerRepository playerRepository;
    private final TrainerRepository trainerRepository;
    private final DivisionRepository divisionRepository;
    private final PaymentRepository paymentRepository;
    private final ScoutingReportRepository scoutingReportRepository;
    private final TalentScoreRepository talentScoreRepository;
    private final AcademyPerformanceScoreRepository scoreRepository;

    public AcademyPerformanceRankingService(
            AcademyRepository academyRepository,
            PlayerRepository playerRepository,
            TrainerRepository trainerRepository,
            DivisionRepository divisionRepository,
            PaymentRepository paymentRepository,
            ScoutingReportRepository scoutingReportRepository,
            TalentScoreRepository talentScoreRepository,
            AcademyPerformanceScoreRepository scoreRepository
    ) {
        this.academyRepository = academyRepository;
        this.playerRepository = playerRepository;
        this.trainerRepository = trainerRepository;
        this.divisionRepository = divisionRepository;
        this.paymentRepository = paymentRepository;
        this.scoutingReportRepository = scoutingReportRepository;
        this.talentScoreRepository = talentScoreRepository;
        this.scoreRepository = scoreRepository;
    }

    @Transactional
    public AcademyPerformanceScore generateAcademyScore(Long academyId) {
        Academy academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new IllegalArgumentException("Academy not found"));
        return generateAcademyScore(academy);
    }

    @Transactional
    public List<AcademyPerformanceScore> recomputeAllAcademyScores() {
        List<AcademyPerformanceScore> scores = academyRepository.findAll().stream()
                .map(this::generateAcademyScore)
                .sorted(Comparator.comparing(AcademyPerformanceScore::getOverallScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        Map<Long, List<AcademyPerformanceScore>> bySport = scores.stream()
                .collect(Collectors.groupingBy(score -> score.getSportId() == null ? -1L : score.getSportId()));

        for (List<AcademyPerformanceScore> sportScores : bySport.values()) {
            sportScores.sort(Comparator.comparing(AcademyPerformanceScore::getOverallScore, Comparator.nullsLast(Comparator.reverseOrder())));
            for (int i = 0; i < sportScores.size(); i++) {
                AcademyPerformanceScore score = sportScores.get(i);
                score.setRankingPosition(i + 1);
                Academy academy = score.getAcademy();
                academy.setLatestPerformanceScore(score.getOverallScore());
                academy.setLatestRankingPosition(score.getRankingPosition());
                academy.setPerformanceUpdatedAt(score.getGeneratedAt());
                academyRepository.save(academy);
                scoreRepository.save(score);
            }
        }

        return scores;
    }

    @Transactional(readOnly = true)
    public List<AcademyPerformanceScore> rankAcademiesBySport(Long sportId) {
        if (sportId != null) {
            List<AcademyPerformanceScore> scores = scoreRepository.findBySport_IdOrderByOverallScoreDesc(sportId);
            if (!scores.isEmpty()) return scores;
        } else {
            List<AcademyPerformanceScore> scores = scoreRepository.findAllByOrderByOverallScoreDesc();
            if (!scores.isEmpty()) return scores;
        }
        return List.of();
    }

    @Transactional(readOnly = true)
    public AcademyPerformanceScore getAcademyRankingDetails(Long academyId) {
        return scoreRepository.findTopByAcademy_IdOrderByGeneratedAtDesc(academyId).orElse(null);
    }

    @Transactional
    public Map<String, Object> searchAcademiesForScouterContact(Long sportId, String academyName, String orderBy, int page, int size) {
        List<Academy> academies = academyRepository.findAll().stream()
                .filter(academy -> academy.getStatus() == null || academy.getStatus() == Academy.AcademyStatus.ACTIVE)
                .filter(academy -> sportId == null || (academy.getSport() != null && Objects.equals(academy.getSport().getId(), sportId)))
                .filter(academy -> academyName == null || academyName.isBlank() || safe(academy.getName()).toLowerCase().contains(academyName.trim().toLowerCase()))
                .toList();

        List<Map<String, Object>> items = academies.stream()
                .map(this::toAcademyContactDto)
                .sorted(contactComparator(orderBy))
                .toList();

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 100));
        int from = Math.min(safePage * safeSize, items.size());
        int to = Math.min(from + safeSize, items.size());

        return Map.of("items", items.subList(from, to), "total", items.size());
    }

    @Transactional
    public Map<String, Object> academyDetail(Long academyId) {
        Academy academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new IllegalArgumentException("Academy not found"));
        AcademyPerformanceScore score = latestOrGenerate(academy);
        List<Player> players = playerRepository.findByAcademy_Id(academyId);
        List<Trainer> trainers = trainerRepository.findByAcademy_Id(academyId);

        return new LinkedHashMap<>(Map.ofEntries(
                Map.entry("academyId", academy.getId()),
                Map.entry("academyName", safe(academy.getName())),
                Map.entry("sportId", academy.getSport() != null ? academy.getSport().getId() : nullValue()),
                Map.entry("sportName", academy.getSport() != null ? safe(academy.getSport().getName()) : ""),
                Map.entry("city", safe(academy.getCity())),
                Map.entry("country", safe(academy.getCountry())),
                Map.entry("playersCount", players.size()),
                Map.entry("trainersCount", trainers.size()),
                Map.entry("overallScore", round(score != null ? score.getOverallScore() : 0.0)),
                Map.entry("rankingPosition", score != null && score.getRankingPosition() != null ? score.getRankingPosition() : 0),
                Map.entry("rankingExplanation", score != null ? safe(score.getExplanation()) : ""),
                Map.entry("mainStrengths", score != null ? safe(score.getMainStrengths()) : ""),
                Map.entry("mainWeaknesses", score != null ? safe(score.getMainWeaknesses()) : ""),
                Map.entry("canContact", academy.getScouterContactEnabled() == null || Boolean.TRUE.equals(academy.getScouterContactEnabled()))
        ));
    }

    private AcademyPerformanceScore generateAcademyScore(Academy academy) {
        List<Player> players = playerRepository.findByAcademy_Id(academy.getId());
        List<Trainer> trainers = trainerRepository.findByAcademy_Id(academy.getId());
        List<Division> divisions = divisionRepository.findByAcademy_Id(academy.getId());
        List<Payment> payments = paymentRepository.findByAcademy_Id(academy.getId());
        List<ScoutingReport> reports = scoutingReportRepository.findByAcademy_Id(academy.getId());

        double averageRating = players.stream().map(Player::getAverageRating).filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().orElse(0.0);
        double playerDevelopmentScore = clamp(averageRating * 10.0 + Math.min(players.size(), 20) * 0.5);

        double reportAverage = reports.stream().mapToDouble(ScoutingReport::calculateOverallScore).average().orElse(0.0);
        long approvedReports = reports.stream().filter(report -> report.getStatus() == ScoutingStatus.APPROVED || report.getStatus() == ScoutingStatus.SHORTLISTED).count();
        double scoutingScore = clamp(reportAverage * 10.0 + (reports.isEmpty() ? 0 : (approvedReports * 100.0 / reports.size()) * 0.25));

        double talentProductionScore = players.stream()
                .map(player -> talentScoreRepository.findByPlayer_IdOrderByGeneratedAtDesc(player.getId()).stream().findFirst().map(TalentScore::getScore).orElse(player.getAverageRating() != null ? player.getAverageRating() * 10.0 : 0.0))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double activityScore = clamp(45.0 + Math.min(divisions.size(), 8) * 5.0 + Math.min(trainers.size(), 10) * 2.0);

        long paid = payments.stream().filter(Payment::isPaid).count();
        double paymentHealthScore = payments.isEmpty() ? 75.0 : clamp(paid * 100.0 / payments.size());

        double academyGrowthScore = clamp(Math.min(players.size(), 80) * 0.75 + Math.min(trainers.size(), 20) * 1.5 + Math.min(divisions.size(), 12) * 2.0);

        double overall = round(
                playerDevelopmentScore * 0.25
                        + scoutingScore * 0.20
                        + talentProductionScore * 0.20
                        + activityScore * 0.15
                        + paymentHealthScore * 0.10
                        + academyGrowthScore * 0.10
        );

        AcademyPerformanceScore score = new AcademyPerformanceScore();
        score.setAcademy(academy);
        score.setSport(academy.getSport());
        score.setOverallScore(overall);
        score.setPlayerDevelopmentScore(round(playerDevelopmentScore));
        score.setScoutingScore(round(scoutingScore));
        score.setActivityScore(round(activityScore));
        score.setPaymentHealthScore(round(paymentHealthScore));
        score.setTalentProductionScore(round(talentProductionScore));
        score.setConfidence(confidence(players.size(), reports.size(), payments.size()));
        score.setGeneratedAt(LocalDateTime.now());
        score.setMainStrengths(strengths(playerDevelopmentScore, scoutingScore, talentProductionScore, paymentHealthScore, academyGrowthScore));
        score.setMainWeaknesses(weaknesses(playerDevelopmentScore, scoutingScore, talentProductionScore, paymentHealthScore, academyGrowthScore));
        score.setExplanation("Weighted performance score using player development, scouting quality, talent production, activity consistency, payment health, and academy growth.");
        return scoreRepository.save(score);
    }

    private Map<String, Object> toAcademyContactDto(Academy academy) {
        AcademyPerformanceScore score = latestOrGenerate(academy);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("academyId", academy.getId());
        item.put("academyName", safe(academy.getName()));
        item.put("sportId", academy.getSport() != null ? academy.getSport().getId() : null);
        item.put("sportName", academy.getSport() != null ? safe(academy.getSport().getName()) : "");
        item.put("city", safe(academy.getCity()));
        item.put("country", safe(academy.getCountry()));
        item.put("overallScore", score != null ? round(score.getOverallScore()) : 0.0);
        item.put("rankingPosition", score != null && score.getRankingPosition() != null ? score.getRankingPosition() : 0);
        item.put("explanation", score != null ? safe(score.getExplanation()) : "Ranking not generated yet.");
        item.put("canContact", academy.getScouterContactEnabled() == null || Boolean.TRUE.equals(academy.getScouterContactEnabled()));
        return item;
    }

    private AcademyPerformanceScore latestOrGenerate(Academy academy) {
        return scoreRepository.findTopByAcademy_IdOrderByGeneratedAtDesc(academy.getId()).orElseGet(() -> generateAcademyScore(academy));
    }

    private Comparator<Map<String, Object>> contactComparator(String orderBy) {
        String normalized = orderBy == null ? "performance" : orderBy.trim().toLowerCase();
        Comparator<Map<String, Object>> byPerformance = Comparator.comparing(item -> toDouble(item.get("overallScore")), Comparator.reverseOrder());
        return switch (normalized) {
            case "academy_name", "name" -> Comparator.comparing(item -> safe(item.get("academyName")), String.CASE_INSENSITIVE_ORDER);
            case "sport" -> Comparator.comparing(item -> safe(item.get("sportName")), String.CASE_INSENSITIVE_ORDER);
            case "city", "city_country", "country" -> Comparator.comparing(item -> (safe(item.get("city")) + safe(item.get("country"))), String.CASE_INSENSITIVE_ORDER);
            case "ai_ranking_score", "ranking", "score" -> byPerformance;
            default -> byPerformance.thenComparing(item -> safe(item.get("academyName")), String.CASE_INSENSITIVE_ORDER);
        };
    }

    private Object nullValue() {
        return "";
    }

    private String strengths(double development, double scouting, double talent, double payment, double growth) {
        List<String> parts = new ArrayList<>();
        if (development >= 70) parts.add("Strong player development");
        if (scouting >= 70) parts.add("High scouting quality");
        if (talent >= 70) parts.add("Good talent production");
        if (payment >= 80) parts.add("Healthy payments");
        if (growth >= 60) parts.add("Solid academy growth");
        return parts.isEmpty() ? "Balanced operational baseline" : String.join(", ", parts);
    }

    private String weaknesses(double development, double scouting, double talent, double payment, double growth) {
        List<String> parts = new ArrayList<>();
        if (development < 50) parts.add("Player development needs attention");
        if (scouting < 50) parts.add("Scouting report coverage is limited");
        if (talent < 50) parts.add("Talent score production is low");
        if (payment < 60) parts.add("Payment health needs follow-up");
        if (growth < 45) parts.add("Academy growth indicators are thin");
        return parts.isEmpty() ? "No critical weaknesses detected" : String.join(", ", parts);
    }

    private double confidence(int players, int reports, int payments) {
        return round(Math.min(0.95, 0.45 + Math.min(players, 50) * 0.006 + Math.min(reports, 30) * 0.008 + Math.min(payments, 50) * 0.003));
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    private double round(Double value) {
        if (value == null) return 0.0;
        return Math.round(value * 100.0) / 100.0;
    }

    private double toDouble(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
