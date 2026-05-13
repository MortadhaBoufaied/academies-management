package com.footballacademy.services.dashboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballacademy.model.Player;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.scouting.ScoutingAiService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardTopPlayersService {
    private final PlayerRepository playerRepository;
    private final AcademyAccessService academyAccessService;
    private final ScoutingAiService scoutingAiService;
    private final ObjectMapper objectMapper;

    public DashboardTopPlayersService(
            PlayerRepository playerRepository,
            AcademyAccessService academyAccessService,
            ScoutingAiService scoutingAiService,
            ObjectMapper objectMapper
    ) {
        this.playerRepository = playerRepository;
        this.academyAccessService = academyAccessService;
        this.scoutingAiService = scoutingAiService;
        this.objectMapper = objectMapper;
    }

    public List<TopPlayerCard> topPlayersForCurrentAcademy(int limit) {
        if (limit <= 0) return List.of();
        if (academyAccessService.isSuperAdmin()) return List.of();

        Long academyId = academyAccessService.currentAcademyOrThrow().getId();

        // 1) Preferred path: use Scouting AI ranking (potential_score desc).
        List<ScouterPlayerCard> scoutingCards = fetchTopFromScouting(limit);
        if (!scoutingCards.isEmpty()) {
            List<Long> ids = scoutingCards.stream()
                    .map(ScouterPlayerCard::player_external_id)
                    .filter(Objects::nonNull)
                    .map(Long::valueOf)
                    .toList();

            if (!ids.isEmpty()) {
                Map<Long, Player> playersById = playerRepository
                        .findByAcademyIdAndIdInWithUserAndRefs(academyId, ids)
                        .stream()
                        .collect(Collectors.toMap(Player::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));

                List<TopPlayerCard> out = new ArrayList<>();
                for (ScouterPlayerCard card : scoutingCards) {
                    Long playerId = card.player_external_id() != null ? Long.valueOf(card.player_external_id()) : null;
                    if (playerId == null) continue;

                    Player p = playersById.get(playerId);
                    if (p == null) continue; // Ignore players not in this academy

                    out.add(TopPlayerCard.from(p, card));
                }

                if (!out.isEmpty()) {
                    // If the scouting service returns IDs outside this academy (multi-academy sync),
                    // top up the list from local stats so we always show up to `limit` cards.
                    if (out.size() < limit) {
                        var picked = out.stream().map(TopPlayerCard::id).filter(Objects::nonNull).collect(Collectors.toSet());
                        playerRepository.findByAcademyIdWithUserAndRefs(academyId).stream()
                                .sorted(localTopComparator())
                                .filter(p -> p.getId() != null && !picked.contains(p.getId()))
                                .limit(limit - out.size())
                                .map(p -> TopPlayerCard.from(p, null))
                                .filter(Objects::nonNull)
                                .forEach(out::add);
                    }
                    return out.size() > limit ? out.subList(0, limit) : out;
                }
            }
        }

        // 2) Fallback path: use local stats (averageRating desc, goals desc, assists desc, matches desc).
        return playerRepository.findByAcademyIdWithUserAndRefs(academyId).stream()
                .sorted(localTopComparator())
                .limit(limit)
                .map(p -> TopPlayerCard.from(p, null))
                .toList();
    }

    private Comparator<Player> localTopComparator() {
        return Comparator
                .comparing((Player p) -> p.getAverageRating() != null ? p.getAverageRating() : 0.0, Comparator.reverseOrder())
                .thenComparing((Player p) -> p.getGoals() != null ? p.getGoals() : 0, Comparator.reverseOrder())
                .thenComparing((Player p) -> p.getAssists() != null ? p.getAssists() : 0, Comparator.reverseOrder())
                .thenComparing((Player p) -> p.getMatches() != null ? p.getMatches() : 0, Comparator.reverseOrder());
    }

    private List<ScouterPlayerCard> fetchTopFromScouting(int limit) {
        try {
            ResponseEntity<?> resp = scoutingAiService.searchPlayers(Map.of("limit", Math.min(100, Math.max(1, limit))));
            Object body = resp != null ? resp.getBody() : null;
            if (body == null) return List.of();

            ScouterSearchResponse parsed = objectMapper.convertValue(body, ScouterSearchResponse.class);
            if (parsed == null || parsed.items == null) return List.of();
            return parsed.items.stream().filter(Objects::nonNull).toList();
        } catch (Exception ignored) {
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ScouterSearchResponse {
        public Integer total;
        public List<ScouterPlayerCard> items;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScouterPlayerCard(
            Integer player_external_id,
            String full_name,
            String position,
            Integer age,
            String division_name,
            Double potential_score,
            Double churn_risk,
            String trend_label,
            Double avg_rating
    ) {
    }

    public record TopPlayerCard(
            Long id,
            String fullName,
            String position,
            Integer age,
            String divisionName,
            String imageUrl,
            Integer goals,
            Integer assists,
            Integer matches,
            Double averageRating,
            Double potentialScore,
            Double churnRisk,
            String trendLabel
    ) {
        public static TopPlayerCard from(Player player, ScouterPlayerCard scouting) {
            if (player == null) return null;
            String name = player.getUser() != null ? player.getUser().getNom() : null;
            String division = player.getDivision() != null ? player.getDivision().getNom() : null;

            return new TopPlayerCard(
                    player.getId(),
                    name,
                    player.getPosition(),
                    player.getAge(),
                    division,
                    (player.getImageUrl() != null && !player.getImageUrl().isBlank()) ? player.getImageUrl() : null,
                    player.getGoals(),
                    player.getAssists(),
                    player.getMatches(),
                    player.getAverageRating(),
                    scouting != null ? scouting.potential_score() : null,
                    scouting != null ? scouting.churn_risk() : null,
                    scouting != null ? scouting.trend_label() : null
            );
        }
    }
}
