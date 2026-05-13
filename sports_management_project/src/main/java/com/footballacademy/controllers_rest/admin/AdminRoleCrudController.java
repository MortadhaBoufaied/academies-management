package com.footballacademy.controllers_rest.admin;

import com.footballacademy.model.*;
import com.footballacademy.repository.*;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.auth.AuthService;
import com.footballacademy.services.player.PlayerService;
import com.footballacademy.services.trainer.TrainerService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Transactional
public
class AdminRoleCrudController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final TrainerRepository trainerRepository;
    private final ParentRepository parentRepository;
    private final DivisionRepository divisionRepository;
    private final SportRepository sportRepository;
    private final SportPositionRepository sportPositionRepository;
    private final PlayerService playerService;
    private final TrainerService trainerService;
    private final AcademyAccessService academyAccessService;
    public AdminRoleCrudController(AuthService authService, UserRepository userRepository, PlayerRepository playerRepository, TrainerRepository trainerRepository, ParentRepository parentRepository, DivisionRepository divisionRepository, SportRepository sportRepository, SportPositionRepository sportPositionRepository, PlayerService playerService, TrainerService trainerService, AcademyAccessService academyAccessService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.trainerRepository = trainerRepository;
        this.parentRepository = parentRepository;
        this.divisionRepository = divisionRepository;
        this.sportRepository = sportRepository;
        this.sportPositionRepository = sportPositionRepository;
        this.playerService = playerService;
        this.trainerService = trainerService;
        this.academyAccessService = academyAccessService;
    }
    // -------- PLAYERS --------
    public static
    class PlayerUpsert {
        public User user;
        public Integer age;
        public String position;
        public String nationality;
        public String phone;
        public String imageUrl;
        public Boolean paid;
        public Double height;
        public Double weight;
        public Long divisionId;
        public Long parentId;
        public Long trainerId;
        public Long sportId;
        public Long sportPositionId;
        public String customStats;
    }
    @PostMapping("/players")
    public ResponseEntity<?> createPlayer(
    @RequestBody PlayerUpsert req) {
        try {
            if (req == null || req.user == null) return ResponseEntity.badRequest() .body(Map.of("error", "Missing user"));
            req.user.setMainRole(User.UserRole.PLAYER);
            User created = authService.register(req.user);
            Player p = playerRepository.findById(created.getId()) .orElseThrow();
            applyPlayerFields(p, req);
            playerRepository.save(p);
            playerService.assignRelations(created.getId(), req.divisionId, req.trainerId, req.parentId);
            return ResponseEntity.status(HttpStatus.CREATED) .body(playerService.getPlayerCombinedById(created.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/players/{id}")
    public ResponseEntity<?> updatePlayer(
    @PathVariable Long id,
    @RequestBody PlayerUpsert req) {
        try {
            Player p = playerService.getPlayerById(id);
            if (req != null && req.user != null) {
                User u = userRepository.findById(id) .orElseThrow();
                academyAccessService.assertCanAccessUser(u);
                u.setNom(req.user.getNom());
                u.setEmail(req.user.getEmail());
                u.setTel(req.user.getTel());
                u.setDateNaiss(req.user.getDateNaiss());
                userRepository.save(u);
            } applyPlayerFields(p, req);
            playerRepository.save(p);
            if (req != null &&(req.divisionId != null || req.trainerId != null || req.parentId != null)) {
                playerService.assignRelations(id, req.divisionId, req.trainerId, req.parentId);
            } return ResponseEntity.ok(playerService.getPlayerCombinedById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/players/{id}")
    public ResponseEntity<?> deletePlayer(
    @PathVariable Long id) {
        try {
            playerService.getPlayerById(id);
            // delete player row first
            playerRepository.deleteById(id);
            // then delete user
            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/players/{id}")
    public ResponseEntity<?> getPlayer(
    @PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerCombinedById(id));
    }
    private void applyPlayerFields(Player p, PlayerUpsert req) {
        if (req == null) return;
        p.setAge(req.age);
        p.setPosition(req.position);
        p.setNationality(req.nationality);
        p.setPhone(req.phone);
        p.setImageUrl(req.imageUrl);
        if (req.paid != null) p.setPaid(req.paid);
        p.setHeight(req.height);
        p.setWeight(req.weight);
        p.setCustomStats(req.customStats);
        if (req.divisionId != null) {
            Division division = divisionRepository.findById(req.divisionId) .orElse(null);
            if (division != null) {
                academyAccessService.assertCanAccessDivision(division);
            } p.setDivision(division);
        }
        if (req.sportId != null) {
            Sport sport = sportRepository.findById(req.sportId) .orElseThrow(() -> new RuntimeException("Sport not found"));
            academyAccessService.assertCanAccessSport(sport);
            p.setSport(sport);
        } else
        if (!academyAccessService.isSuperAdmin()) {
            p.setSport(academyAccessService.currentAcademyOrThrow() .getSport());
        }
        if (req.sportPositionId != null) {
            SportPosition position = sportPositionRepository.findById(req.sportPositionId) .orElseThrow(() -> new RuntimeException("Sport position not found"));
            if (p.getSport() != null && position.getSport() != null && !p.getSport() .getId() .equals(position.getSport() .getId())) {
                throw new RuntimeException("Sport position does not belong to selected sport");
            } p.setSportPosition(position);
            p.setPosition(position.getName());
        }
    }
    // -------- TRAINERS --------
    public static
    class TrainerUpsert {
        public User user;
        public String speciality;
        public String experience;
        public String license;
        public String notes;
        public Long divisionId;
    }
    @PostMapping("/trainers")
    public ResponseEntity<?> createTrainer(
    @RequestBody TrainerUpsert req) {
        try {
            if (req == null || req.user == null) return ResponseEntity.badRequest() .body(Map.of("error", "Missing user"));
            req.user.setMainRole(User.UserRole.TRAINER);
            User created = authService.register(req.user);
            Trainer t = trainerRepository.findById(created.getId()) .orElseThrow();
            t.setSpeciality(req.speciality);
            t.setExperience(req.experience);
            t.setLicense(req.license);
            t.setNotes(req.notes);
            if (req.divisionId != null) {
                Division division = divisionRepository.findById(req.divisionId) .orElse(null);
                if (division != null) {
                    academyAccessService.assertCanAccessDivision(division);
                } t.setDivision(division);
            } trainerRepository.save(t);
            return ResponseEntity.status(HttpStatus.CREATED) .body(trainerService.getTrainerCombinedById(created.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/trainers/{id}")
    public ResponseEntity<?> updateTrainer(
    @PathVariable Long id,
    @RequestBody TrainerUpsert req) {
        try {
            Trainer t = trainerRepository.findById(id) .orElseThrow(() -> new RuntimeException("Trainer not found"));
            academyAccessService.assertCanAccessAcademy(t.getAcademy());
            if (req != null && req.user != null) {
                User u = userRepository.findById(id) .orElseThrow();
                academyAccessService.assertCanAccessUser(u);
                u.setNom(req.user.getNom());
                u.setEmail(req.user.getEmail());
                u.setTel(req.user.getTel());
                u.setDateNaiss(req.user.getDateNaiss());
                userRepository.save(u);
            } t.setSpeciality(req.speciality);
            t.setExperience(req.experience);
            t.setLicense(req.license);
            t.setNotes(req.notes);
            if (req.divisionId != null) {
                Division division = divisionRepository.findById(req.divisionId) .orElse(null);
                if (division != null) {
                    academyAccessService.assertCanAccessDivision(division);
                } t.setDivision(division);
            } trainerRepository.save(t);
            return ResponseEntity.ok(trainerService.getTrainerCombinedById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/trainers/{id}")
    public ResponseEntity<?> deleteTrainer(
    @PathVariable Long id) {
        try {
            Trainer t = trainerRepository.findById(id) .orElseThrow(() -> new RuntimeException("Trainer not found"));
            academyAccessService.assertCanAccessAcademy(t.getAcademy());
            trainerRepository.deleteById(id);
            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/trainers/{id}")
    public ResponseEntity<?> getTrainer(
    @PathVariable Long id) {
        return ResponseEntity.ok(trainerService.getTrainerCombinedById(id));
    }
    // -------- PARENTS --------
    public static
    class ParentUpsert {
        public User user;
    }
    @PostMapping("/parents")
    public ResponseEntity<?> createParent(
    @RequestBody ParentUpsert req) {
        try {
            if (req == null || req.user == null) return ResponseEntity.badRequest() .body(Map.of("error", "Missing user"));
            req.user.setMainRole(User.UserRole.PARENT);
            User created = authService.register(req.user);
            Parent p = parentRepository.findById(created.getId()) .orElseThrow();
            return ResponseEntity.status(HttpStatus.CREATED) .body(p);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/parents/{id}")
    public ResponseEntity<?> updateParent(
    @PathVariable Long id,
    @RequestBody ParentUpsert req) {
        try {
            Parent p = parentRepository.findById(id) .orElseThrow(() -> new RuntimeException("Parent not found"));
            academyAccessService.assertCanAccessAcademy(p.getAcademy());
            if (req != null && req.user != null) {
                User u = userRepository.findById(id) .orElseThrow();
                academyAccessService.assertCanAccessUser(u);
                u.setNom(req.user.getNom());
                u.setEmail(req.user.getEmail());
                u.setTel(req.user.getTel());
                u.setDateNaiss(req.user.getDateNaiss());
                userRepository.save(u);
            } return ResponseEntity.ok(p);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/parents/{id}")
    public ResponseEntity<?> deleteParent(
    @PathVariable Long id) {
        try {
            Parent p = parentRepository.findById(id) .orElseThrow(() -> new RuntimeException("Parent not found"));
            academyAccessService.assertCanAccessAcademy(p.getAcademy());
            parentRepository.deleteById(id);
            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST) .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/parents/{id}")
    public ResponseEntity<?> getParent(
    @PathVariable Long id) {
        Parent parent = parentRepository.findById(id) .orElseThrow();
        academyAccessService.assertCanAccessAcademy(parent.getAcademy());
        return ResponseEntity.ok(parent);
    }
    @GetMapping("/parents")
    public ResponseEntity<?> listParents() {
        if (academyAccessService.isSuperAdmin()) {
            return ResponseEntity.ok(parentRepository.findAll());
        } return ResponseEntity.ok(parentRepository.findByAcademy_Id(academyAccessService.currentAcademyOrThrow() .getId()));
    }
}
