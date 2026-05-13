package com.footballacademy.services.scouting;

import com.footballacademy.model.*;
import com.footballacademy.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SportScoutingFilterService {
    private final SportRepository sportRepository;
    private final DivisionRepository divisionRepository;
    private final SportScoutingFilterConfigRepository configRepository;
    private final PlayerRepository playerRepository;
    private final TalentScoreRepository talentScoreRepository;
    private final ScoutingReportRepository scoutingReportRepository;

    public SportScoutingFilterService(
            SportRepository sportRepository,
            DivisionRepository divisionRepository,
            SportScoutingFilterConfigRepository configRepository,
            PlayerRepository playerRepository,
            TalentScoreRepository talentScoreRepository,
            ScoutingReportRepository scoutingReportRepository
    ) {
        this.sportRepository = sportRepository;
        this.divisionRepository = divisionRepository;
        this.configRepository = configRepository;
        this.playerRepository = playerRepository;
        this.talentScoreRepository = talentScoreRepository;
        this.scoutingReportRepository = scoutingReportRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> sports() {
        return sportRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::sportDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> divisions(Long sportId) {
        return divisionRepository.findBySport_IdAndActiveTrueOrderByDisplayOrderAscNomAsc(sportId).stream()
                .map(division -> Map.<String, Object>of(
                        "divisionId", division.getId(),
                        "divisionName", safe(division.getNom()),
                        "category", safe(division.getCategorie())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> filterConfig(Long sportId) {
        Sport sport = sportRepository.findById(sportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found"));
        List<Map<String, Object>> filters = configRepository.findBySport_IdAndActiveTrueOrderByDisplayOrderAsc(sportId).stream()
                .map(this::filterDto)
                .toList();
        return Map.of("sportId", sport.getId(), "sportName", sport.getName(), "filters", filters);
    }

    @Transactional
    public SportScoutingFilterConfig saveConfig(Long id, Map<String, Object> body) {
        Long sportId = toLong(body.get("sportId"));
        Sport sport = sportRepository.findById(sportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found"));
        SportScoutingFilterConfig config = id == null
                ? new SportScoutingFilterConfig()
                : configRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Filter config not found"));
        config.setSport(sport);
        config.setFilterKey(string(body.get("filterKey"), config.getFilterKey()));
        config.setFilterLabel(string(body.get("filterLabel"), config.getFilterLabel()));
        config.setFilterType(string(body.get("filterType"), config.getFilterType()));
        config.setAllowedValues(string(body.get("allowedValues"), config.getAllowedValues()));
        config.setMinValue(toDouble(body.get("minValue"), config.getMinValue()));
        config.setMaxValue(toDouble(body.get("maxValue"), config.getMaxValue()));
        config.setActive(toBoolean(body.get("isActive"), config.getActive()));
        config.setDisplayOrder(toInteger(body.get("displayOrder"), config.getDisplayOrder()));
        return configRepository.save(config);
    }

    @Transactional
    public void deleteConfig(Long id) {
        configRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> search(Map<String, Object> body) {
        Long sportId = toLong(body.get("sportId"));
        if (sportId == null) {
            throw new IllegalArgumentException("sportId is required");
        }
        Long divisionId = toLong(body.get("divisionId"));
        Map<String, Object> filters = body.get("filters") instanceof Map<?, ?> raw
                ? new LinkedHashMap<>((Map<String, Object>) raw)
                : Map.of();
        String orderBy = string(body.get("orderBy"), "potentialScore");
        int page = Math.max(0, toInteger(body.get("page"), 0));
        int size = Math.max(1, Math.min(100, toInteger(body.get("size"), 20)));

        Sport sport = sportRepository.findById(sportId)
                .orElseThrow(() -> new IllegalArgumentException("Sport not found"));

        List<Map<String, Object>> items = playerRepository.findAllWithUserAndRefs().stream()
                .filter(player -> player.getSport() != null && Objects.equals(player.getSport().getId(), sportId))
                .filter(player -> divisionId == null || (player.getDivision() != null && Objects.equals(player.getDivision().getId(), divisionId)))
                .filter(player -> matchesPlayerFilters(player, filters))
                .map(this::playerResultDto)
                .sorted(playerComparator(orderBy))
                .toList();

        int from = Math.min(page * size, items.size());
        int to = Math.min(from + size, items.size());
        return Map.of("sportId", sport.getId(), "sportName", sport.getName(), "items", items.subList(from, to), "total", items.size());
    }

    private boolean matchesPlayerFilters(Player player, Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) return true;
        if (!matchesText(player.getPosition(), filters.get("position"))) return false;
        if (!matchesNumberMin(player.getAge(), filters.get("ageMin"))) return false;
        if (!matchesNumberMax(player.getAge(), filters.get("ageMax"))) return false;
        if (!matchesNumberMin(player.getAverageRating(), filters.get("minAvgRating"))) return false;
        if (!matchesNumberMin(currentTalentScore(player), filters.get("minTalentScore"))) return false;
        if (!matchesNumberMin(currentPotentialScore(player), filters.get("minPotentialScore"))) return false;
        if (!matchesText(player.getNationality(), filters.get("nationality"))) return false;
        if (!matchesNumberMin(player.getHeight(), filters.get("heightMin"))) return false;
        if (!matchesNumberMax(player.getHeight(), filters.get("heightMax"))) return false;
        if (!matchesNumberMin(player.getWeight(), filters.get("weightMin"))) return false;
        if (!matchesNumberMax(player.getWeight(), filters.get("weightMax"))) return false;
        if (!matchesBoolean(player.isPaid(), filters.get("paymentStatus"))) return false;
        return true;
    }

    private Map<String, Object> playerResultDto(Player player) {
        double talent = currentTalentScore(player);
        double potential = currentPotentialScore(player);
        String progression = progressionLabel(talent, potential);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("playerId", player.getId());
        item.put("playerName", player.getUser() != null ? safe(player.getUser().getNom()) : "Player #" + player.getId());
        item.put("academyName", player.getAcademy() != null ? safe(player.getAcademy().getName()) : "");
        item.put("divisionName", player.getDivision() != null ? safe(player.getDivision().getNom()) : "");
        item.put("sport", player.getSport() != null ? safe(player.getSport().getName()) : "");
        item.put("position", safe(player.getPosition()));
        item.put("age", player.getAge());
        item.put("rating", round(safeDouble(player.getAverageRating())));
        item.put("talentScore", round(talent));
        item.put("potentialScore", round(potential));
        item.put("progression", progression);
        item.put("scoutingStatus", scoutingStatus(player));
        item.put("recommendation", recommendation(progression, potential));
        item.put("explanation", "Sport-first AI search result filtered only within the selected sport.");
        return item;
    }

    private double currentTalentScore(Player player) {
        return talentScoreRepository.findByPlayer_IdOrderByGeneratedAtDesc(player.getId()).stream()
                .findFirst()
                .map(TalentScore::getScore)
                .orElse(safeDouble(player.getAverageRating()) * 10.0);
    }

    private double currentPotentialScore(Player player) {
        return scoutingReportRepository.findByPlayer_Id(player.getId()).stream()
                .map(ScoutingReport::getPotentialScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(currentTalentScore(player));
    }

    private String scoutingStatus(Player player) {
        return scoutingReportRepository.findByPlayer_Id(player.getId()).stream()
                .map(ScoutingReport::getStatus)
                .filter(Objects::nonNull)
                .map(Enum::name)
                .findFirst()
                .orElse("WATCHING");
    }

    private String progressionLabel(double talent, double potential) {
        if (potential >= 82 && potential >= talent) return "High Potential";
        if (potential - talent > 4) return "Improving";
        if (talent - potential > 4) return "Declining";
        return "Stable";
    }

    private String recommendation(String progression, double potential) {
        if ("High Potential".equals(progression)) return "High-potential player with strong sport-specific profile.";
        if ("Improving".equals(progression)) return "Monitor next match and request follow-up scouting report.";
        if ("Declining".equals(progression)) return "Review training, injury, and recent performance context.";
        return potential >= 70 ? "Good profile for continued observation." : "Keep in broad monitoring pool.";
    }

    private Comparator<Map<String, Object>> playerComparator(String orderBy) {
        String normalized = orderBy == null ? "potentialScore" : orderBy.trim();
        return switch (normalized) {
            case "talentScore" -> Comparator.comparing(item -> toDoubleValue(item.get("talentScore")), Comparator.reverseOrder());
            case "rating" -> Comparator.comparing(item -> toDoubleValue(item.get("rating")), Comparator.reverseOrder());
            case "name", "playerName" -> Comparator.comparing(item -> safe(item.get("playerName")), String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(item -> toDoubleValue(item.get("potentialScore")), Comparator.reverseOrder());
        };
    }

    private Map<String, Object> sportDto(Sport sport) {
        return Map.of("sportId", sport.getId(), "sportName", sport.getName(), "code", safe(sport.getCode()));
    }

    private Map<String, Object> filterDto(SportScoutingFilterConfig config) {
        return new LinkedHashMap<>(Map.of(
                "filterKey", safe(config.getFilterKey()),
                "filterLabel", safe(config.getFilterLabel()),
                "filterType", safe(config.getFilterType()),
                "allowedValues", splitAllowedValues(config.getAllowedValues()),
                "minValue", config.getMinValue() == null ? "" : config.getMinValue(),
                "maxValue", config.getMaxValue() == null ? "" : config.getMaxValue(),
                "isActive", config.getActive() == null || Boolean.TRUE.equals(config.getActive()),
                "displayOrder", config.getDisplayOrder() == null ? 0 : config.getDisplayOrder()
        ));
    }

    private List<String> splitAllowedValues(String values) {
        if (values == null || values.isBlank()) return List.of();
        return Arrays.stream(values.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private boolean matchesText(String actual, Object expected) {
        String value = expected == null ? "" : expected.toString().trim();
        return value.isEmpty() || safe(actual).equalsIgnoreCase(value);
    }

    private boolean matchesNumberMin(Number actual, Object expected) {
        Double value = toDouble(expected, null);
        return value == null || (actual != null && actual.doubleValue() >= value);
    }

    private boolean matchesNumberMax(Number actual, Object expected) {
        Double value = toDouble(expected, null);
        return value == null || (actual != null && actual.doubleValue() <= value);
    }

    private boolean matchesBoolean(boolean actual, Object expected) {
        String value = expected == null ? "" : expected.toString().trim().toUpperCase();
        if (value.isEmpty()) return true;
        if ("PAID".equals(value)) return actual;
        if ("UNPAID".equals(value) || "PENDING".equals(value)) return !actual;
        return true;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try { return Long.parseLong(value.toString()); } catch (Exception ignored) { return null; }
    }

    private Integer toInteger(Object value, Integer fallback) {
        if (value == null) return fallback;
        if (value instanceof Number number) return number.intValue();
        try { return Integer.parseInt(value.toString()); } catch (Exception ignored) { return fallback; }
    }

    private Double toDouble(Object value, Double fallback) {
        if (value == null || value.toString().isBlank()) return fallback;
        if (value instanceof Number number) return number.doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (Exception ignored) { return fallback; }
    }

    private double toDoubleValue(Object value) {
        Double parsed = toDouble(value, 0.0);
        return parsed == null ? 0.0 : parsed;
    }

    private Boolean toBoolean(Object value, Boolean fallback) {
        if (value == null) return fallback;
        if (value instanceof Boolean bool) return bool;
        return Boolean.parseBoolean(value.toString());
    }

    private String string(Object value, String fallback) {
        String text = value == null ? null : value.toString().trim();
        return text == null || text.isEmpty() ? fallback : text;
    }

    private double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
