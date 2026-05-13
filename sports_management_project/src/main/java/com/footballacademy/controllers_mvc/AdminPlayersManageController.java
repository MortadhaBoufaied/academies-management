package com.footballacademy.controllers_mvc;

import com.footballacademy.model.Division;
import com.footballacademy.model.Parent;
import com.footballacademy.model.Player;
import com.footballacademy.model.Trainer;
import com.footballacademy.model.User;
import com.footballacademy.repository.ParentRepository;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.repository.TrainerRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.auth.AuthService;
import com.footballacademy.services.division.DivisionService;
import com.footballacademy.services.player.PlayerService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
@RequestMapping("/admin")
public
class AdminPlayersManageController {
    private final AuthService authService;
    private final PlayerService playerService;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final DivisionService divisionService;
    private final TrainerRepository trainerRepository;
    private final ParentRepository parentRepository;
    private final AcademyAccessService academyAccessService;
    private final PasswordEncoder passwordEncoder;
    public AdminPlayersManageController(AuthService authService, PlayerService playerService, PlayerRepository playerRepository, UserRepository userRepository, DivisionService divisionService, TrainerRepository trainerRepository, ParentRepository parentRepository, AcademyAccessService academyAccessService, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.playerService = playerService;
        this.playerRepository = playerRepository;
        this.userRepository = userRepository;
        this.divisionService = divisionService;
        this.trainerRepository = trainerRepository;
        this.parentRepository = parentRepository;
        this.academyAccessService = academyAccessService;
        this.passwordEncoder = passwordEncoder;
    }
    @PostMapping("/players")
    public String create(
    @RequestParam String nom,
    @RequestParam String email,
    @RequestParam String mdp,
    @RequestParam String position,
    @RequestParam(required = false) String phone,
    @RequestParam(required = false) String nationality,
    @RequestParam(required = false) Integer age,
    @RequestParam(required = false) Long divisionId,
    @RequestParam(required = false) Long trainerId,
    @RequestParam(required = false) Long parentId,
    @RequestParam(required = false) Double height,
    @RequestParam(required = false) Double weight,
    @RequestParam(required = false) Integer goals,
    @RequestParam(required = false) Integer assists,
    @RequestParam(required = false) Integer matches,
    @RequestParam(required = false) Double averageRating,
    @RequestParam(defaultValue = "false") boolean paid,
    @RequestParam(required = false) String imageUrl, Model model) {
        try {
            validatePlayerInput(null, nom, email, mdp, position);
            validatePlayerMetrics(goals, assists, matches, averageRating);
            validateRelationSelections(divisionId, trainerId, parentId);
            User user = new User();
            user.setNom(nom.trim());
            user.setEmail(email.trim());
            user.setTel(trimToNull(phone));
            user.setMdp(mdp);
            user.setMainRole(User.UserRole.PLAYER);
            User created = authService.register(user);
            Player player = playerRepository.findById(created.getId()) .orElseThrow(() -> new RuntimeException("Player account was created but profile is missing."));
            applyPlayerFields(player, age, position, nationality, phone, imageUrl, paid, height, weight, goals, assists, matches, averageRating);
            playerRepository.save(player);
            playerService.assignRelations(player.getId(), divisionId, trainerId, parentId);
            return "redirect:/admin/view/players?saved=true";
        } catch (Exception exception) {
            Player draft = buildDraftPlayer(null, nom, email, position, phone, nationality, age, divisionId, trainerId, parentId, height, weight, goals, assists, matches, averageRating, paid, imageUrl);
            return renderForm(model, draft, exception.getMessage());
        }
    }
    @PostMapping("/players/{id}")
    public String update(
    @PathVariable Long id,
    @RequestParam String nom,
    @RequestParam String email,
    @RequestParam(required = false) String mdp,
    @RequestParam String position,
    @RequestParam(required = false) String phone,
    @RequestParam(required = false) String nationality,
    @RequestParam(required = false) Integer age,
    @RequestParam(required = false) Long divisionId,
    @RequestParam(required = false) Long trainerId,
    @RequestParam(required = false) Long parentId,
    @RequestParam(required = false) Double height,
    @RequestParam(required = false) Double weight,
    @RequestParam(required = false) Integer goals,
    @RequestParam(required = false) Integer assists,
    @RequestParam(required = false) Integer matches,
    @RequestParam(required = false) Double averageRating,
    @RequestParam(defaultValue = "false") boolean paid,
    @RequestParam(required = false) String imageUrl, Model model) {
        try {
            validatePlayerInput(id, nom, email, null, position);
            validatePlayerMetrics(goals, assists, matches, averageRating);
            validateRelationSelections(divisionId, trainerId, parentId);
            Player player = playerService.getPlayerById(id);
            User user = player.getUser();
            academyAccessService.assertCanAccessUser(user);
            user.setNom(nom.trim());
            user.setEmail(email.trim());
            user.setTel(trimToNull(phone));
            if (mdp != null && !mdp.isBlank()) {
                user.setMdp(passwordEncoder.encode(mdp));
            } userRepository.save(user);
            applyPlayerFields(player, age, position, nationality, phone, imageUrl, paid, height, weight, goals, assists, matches, averageRating);
            playerRepository.save(player);
            playerService.assignRelations(id, divisionId, trainerId, parentId);
            return "redirect:/admin/view/players?updated=true";
        } catch (Exception exception) {
            Player draft = buildDraftPlayer(id, nom, email, position, phone, nationality, age, divisionId, trainerId, parentId, height, weight, goals, assists, matches, averageRating, paid, imageUrl);
            return renderForm(model, draft, exception.getMessage());
        }
    }
    @PostMapping("/players/{id}/delete")
    public String delete(
    @PathVariable Long id, Model model) {
        try {
            playerService.getPlayerById(id);
            playerRepository.deleteById(id);
            userRepository.deleteById(id);
            return "redirect:/admin/view/players?deleted=true";
        } catch (Exception exception) {
            Player player = playerService.findVisiblePlayerById(id);
            if (player == null) {
                return "redirect:/admin/view/players";
            } return renderForm(model, player, exception.getMessage());
        }
    }
    private void validatePlayerInput(Long playerId, String nom, String email, String password, String position) {
        if (nom == null || nom.trim() .isEmpty()) {
            throw new IllegalArgumentException("Player name is required.");
        }
        if (email == null || email.trim() .isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (position == null || position.trim() .isEmpty()) {
            throw new IllegalArgumentException("Position is required.");
        }
        if (password != null && password.trim() .isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        } userRepository.findByEmail(email.trim()) .filter(existing -> !existing.getId() .equals(playerId)) .ifPresent(existing -> {
            throw new IllegalArgumentException("Email already exists.");
        });
    }
    private void validatePlayerMetrics(Integer goals, Integer assists, Integer matches, Double averageRating) {
        if (goals != null && goals < 0) {
            throw new IllegalArgumentException("Goals cannot be negative.");
        }
        if (assists != null && assists < 0) {
            throw new IllegalArgumentException("Assists cannot be negative.");
        }
        if (matches != null && matches < 0) {
            throw new IllegalArgumentException("Matches cannot be negative.");
        }
        if (averageRating != null &&(averageRating < 0 || averageRating > 10)) {
            throw new IllegalArgumentException("Average rating must stay between 0 and 10.");
        }
    }
    private void validateRelationSelections(Long divisionId, Long trainerId, Long parentId) {
        if (divisionId != null) {
            divisionService.getDivisionById(divisionId);
        }
        if (trainerId != null) {
            Trainer trainer = trainerRepository.findById(trainerId) .orElseThrow(() -> new IllegalArgumentException("Trainer not found."));
            academyAccessService.assertCanAccessAcademy(trainer.getAcademy());
        }
        if (parentId != null) {
            Parent parent = parentRepository.findById(parentId) .orElseThrow(() -> new IllegalArgumentException("Parent not found."));
            academyAccessService.assertCanAccessAcademy(parent.getAcademy());
        }
    }
    private void applyPlayerFields(Player player, Integer age, String position, String nationality, String phone, String imageUrl, boolean paid, Double height, Double weight, Integer goals, Integer assists, Integer matches, Double averageRating) {
        player.setAge(age);
        player.setPosition(trimToNull(position));
        player.setNationality(trimToNull(nationality));
        player.setPhone(trimToNull(phone));
        player.setImageUrl(trimToNull(imageUrl));
        player.setPaid(paid);
        player.setHeight(height);
        player.setWeight(weight);
        player.setGoals(goals != null ? goals : 0);
        player.setAssists(assists != null ? assists : 0);
        player.setMatches(matches != null ? matches : 0);
        player.setAverageRating(averageRating);
    }
    private String renderForm(Model model, Player player, String errorMessage) {
        model.addAttribute("player", player);
        model.addAttribute("divisions", divisionService.getAllDivisions());
        model.addAttribute("trainers", availableTrainers());
        model.addAttribute("parents", availableParents());
        model.addAttribute("error", errorMessage);
        return "pages/modules/data-management/players/form";
    }
    private Player buildDraftPlayer(Long id, String nom, String email, String position, String phone, String nationality, Integer age, Long divisionId, Long trainerId, Long parentId, Double height, Double weight, Integer goals, Integer assists, Integer matches, Double averageRating, boolean paid, String imageUrl) {
        Player player = new Player();
        player.setId(id);
        User user = new User();
        user.setId(id);
        user.setNom(trimToNull(nom));
        user.setEmail(trimToNull(email));
        user.setTel(trimToNull(phone));
        player.setUser(user);
        player.setPosition(trimToNull(position));
        player.setPhone(trimToNull(phone));
        player.setNationality(trimToNull(nationality));
        player.setAge(age);
        player.setHeight(height);
        player.setWeight(weight);
        player.setGoals(goals != null ? goals : 0);
        player.setAssists(assists != null ? assists : 0);
        player.setMatches(matches != null ? matches : 0);
        player.setAverageRating(averageRating);
        player.setPaid(paid);
        player.setImageUrl(trimToNull(imageUrl));
        if (divisionId != null) {
            Division division = new Division();
            division.setId(divisionId);
            player.setDivision(division);
        }
        if (trainerId != null) {
            Trainer trainer = new Trainer();
            trainer.setId(trainerId);
            player.setTrainer(trainer);
        }
        if (parentId != null) {
            Parent parent = new Parent();
            parent.setId(parentId);
            player.setParent(parent);
        } return player;
    }
    private List<Trainer> availableTrainers() {
        return academyAccessService.isSuperAdmin() ? trainerRepository.findAllWithUser() : trainerRepository.findByAcademyIdWithUser(academyAccessService.currentAcademyOrThrow() .getId());
    }
    private List<Parent> availableParents() {
        return academyAccessService.isSuperAdmin() ? parentRepository.findAllWithUser() : parentRepository.findByAcademyIdWithUser(academyAccessService.currentAcademyOrThrow() .getId());
    }
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        } String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
