package com.footballacademy.controllers_mvc;

import com.footballacademy.model.Parent;
import com.footballacademy.model.Player;
import com.footballacademy.model.Trainer;
import com.footballacademy.repository.ParentRepository;
import com.footballacademy.repository.TrainerRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.division.DivisionService;
import com.footballacademy.services.player.PlayerService;
import com.footballacademy.services.player.PlayerStatsService;
import com.footballacademy.services.ui.MvcPaginationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/view")
public
class PlayersViewController {
    private final PlayerService playerService;
    private final PlayerStatsService playerStatsService;
    private final DivisionService divisionService;
    private final TrainerRepository trainerRepository;
    private final ParentRepository parentRepository;
    private final AcademyAccessService academyAccessService;
    private final MvcPaginationService mvcPaginationService;
    public PlayersViewController(PlayerService playerService, PlayerStatsService playerStatsService, DivisionService divisionService, TrainerRepository trainerRepository, ParentRepository parentRepository, AcademyAccessService academyAccessService, MvcPaginationService mvcPaginationService) {
        this.playerService = playerService;
        this.playerStatsService = playerStatsService;
        this.divisionService = divisionService;
        this.trainerRepository = trainerRepository;
        this.parentRepository = parentRepository;
        this.academyAccessService = academyAccessService;
        this.mvcPaginationService = mvcPaginationService;
    }
    @GetMapping("/players")
    public String list(
    @RequestParam(value = "q", required = false) String query,
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        var pagination = mvcPaginationService.paginate(playerService.searchPlayersForView(query), page, request);
        model.addAttribute("players", pagination.getItems());
        model.addAttribute("pagination", pagination);
        return "pages/modules/data-management/players/list";
    }
    @GetMapping("/players/new")
    public String create(Model model) {
        populateFormModel(model, new Player());
        return "pages/modules/data-management/players/form";
    }
    @GetMapping("/players/{id}")
    public String details(
    @PathVariable Long id, Model model) {
        model.addAttribute("player", playerService.findVisiblePlayerById(id));
        return "pages/modules/data-management/players/details";
    }
    @GetMapping("/players/{id}/edit")
    public String edit(
    @PathVariable Long id, Model model) {
        Player player = playerService.findVisiblePlayerById(id);
        if (player == null) {
            model.addAttribute("error", "Player not found.");
            populateFormModel(model, new Player());
            return "pages/modules/data-management/players/form";
        } populateFormModel(model, player);
        return "pages/modules/data-management/players/form";
    }
    @GetMapping("/players/stats")
    public String stats(
    @RequestParam(value = "page", required = false) Integer page, HttpServletRequest request, Model model) {
        Map<String, Object> overallStats = playerStatsService.getOverallStats();
        List<Player> players = playerService.getAllPlayersForView() .stream() .sorted(Comparator .comparingInt((Player player) -> safeInt(player.getGoals())) .thenComparingInt(player -> safeInt(player.getAssists())) .thenComparingDouble(player -> safeDouble(player.getAverageRating())) .reversed()) .toList();
        var pagination = mvcPaginationService.paginate(players, page, request);
        model.addAttribute("players", pagination.getItems());
        model.addAttribute("pagination", pagination);
        model.addAttribute("rankingOffset", Math.max(0, pagination.getFromItem() - 1));
        model.addAttribute("totalGoals", overallStats.get("totalGoals"));
        model.addAttribute("totalAssists", overallStats.get("totalAssists"));
        model.addAttribute("avgRating", overallStats.get("averageRating"));
        return "pages/modules/data-management/players/stats";
    }
    @GetMapping("/players/list-sport")
    public String listSport() {
        return "pages/modules/data-management/players/list-sport";
    }
    @GetMapping("/players/form-sport")
    public String formSport() {
        return "pages/modules/data-management/players/form-sport";
    }
    @GetMapping("/players/unified/analytics")
    public String analytics() {
        return "pages/modules/data-management/players/unified/analytics";
    }
    private void populateFormModel(Model model, Player player) {
        model.addAttribute("player", player);
        model.addAttribute("divisions", divisionService.getAllDivisions());
        model.addAttribute("trainers", availableTrainers());
        model.addAttribute("parents", availableParents());
    }
    private List<Trainer> availableTrainers() {
        return academyAccessService.isSuperAdmin() ? trainerRepository.findAllWithUser() : trainerRepository.findByAcademyIdWithUser(academyAccessService.currentAcademyOrThrow() .getId());
    }
    private List<Parent> availableParents() {
        return academyAccessService.isSuperAdmin() ? parentRepository.findAllWithUser() : parentRepository.findByAcademyIdWithUser(academyAccessService.currentAcademyOrThrow() .getId());
    }
    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }
    private double safeDouble(Double value) {
        return value != null ? value : 0;
    }
}
