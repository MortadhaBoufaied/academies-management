package com.footballacademy.config.data;

import com.footballacademy.model.*;
import com.footballacademy.repository.*;
import com.footballacademy.services.roles.RoleService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
public
class DataInitializer implements CommandLineRunner {
    private final ObjectMapper mapper = new ObjectMapper();
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final DivisionRepository divisionRepository;
    private final TrainerRepository trainerRepository;
    private final ParentRepository parentRepository;
    private final PlayerRepository playerRepository;
    private final PlayerRankingRepository playerRankingRepository;
    private final ActivityRepository activityRepository;
    private final TrainingRepository trainingRepository;
    private final MatchRepository matchRepository;
    private final PaymentRepository paymentRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MessageReadRepository messageReadRepository;
    private final NotificationRepository notificationRepository;
    private final AcademyInfoRepository academyInfoRepository;
    private final AcademyRepository academyRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, DivisionRepository divisionRepository, TrainerRepository trainerRepository, ParentRepository parentRepository, PlayerRepository playerRepository, PlayerRankingRepository playerRankingRepository, ActivityRepository activityRepository, TrainingRepository trainingRepository, MatchRepository matchRepository, PaymentRepository paymentRepository, ConversationRepository conversationRepository, MessageRepository messageRepository, MessageReadRepository messageReadRepository, NotificationRepository notificationRepository, AcademyInfoRepository academyInfoRepository, AcademyRepository academyRepository, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.divisionRepository = divisionRepository;
        this.trainerRepository = trainerRepository;
        this.parentRepository = parentRepository;
        this.playerRepository = playerRepository;
        this.playerRankingRepository = playerRankingRepository;
        this.activityRepository = activityRepository;
        this.trainingRepository = trainingRepository;
        this.matchRepository = matchRepository;
        this.paymentRepository = paymentRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.messageReadRepository = messageReadRepository;
        this.notificationRepository = notificationRepository;
        this.academyInfoRepository = academyInfoRepository;
        this.academyRepository = academyRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }
    @Override
    public void run(String...args) {
        System.out.println("Application seed is disabled. Uncomment DataInitializer.run(...) seed block to run it once.");
        /*         // Manual one-time seed:         // 1. Uncomment this block.         // 2. Start the backend once.         // 3. Confirm the data in MySQL.         // 4. Comment this block again before the next normal startup.         ensureBaseRoles();         Academy defaultAcademy = ensureDefaultAcademy();         ensureDefaultSuperAdmin();          Optional<JsonNode> seedOpt = loadSeedJson();         if (seedOpt.isEmpty()) {             System.out.println("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã‚Â¡Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â¯Ãƒâ€šÃ‚Â¸Ãƒâ€šÃ‚Â seed.prepared.json not found ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â skipping JSON seeding.");             return;         }          JsonNode root = seedOpt.get();          // 1) Divisions         Map<String, Division> divisionsByName = upsertDivisions(root.path("divisions"), defaultAcademy);          // 2) Users (+ roles + role-specific entity creation)         Map<String, User> usersByEmail = upsertUsers(root.path("users"), defaultAcademy);          // 3) Role-specific data         upsertTrainers(root.path("trainers"), usersByEmail, divisionsByName, defaultAcademy);         upsertParents(root.path("parents"), usersByEmail, defaultAcademy);         upsertPlayers(root.path("players"), usersByEmail, divisionsByName, defaultAcademy);          // 4) Academy info         upsertAcademy(root.path("academy"), divisionsByName, defaultAcademy);          // 5) Activities (Training/Match/Activity) ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â avoid duplicating on restart         if (activityRepository.count() == 0 && trainingRepository.count() == 0 && matchRepository.count() == 0) {             upsertActivities(root.path("activities"), usersByEmail, defaultAcademy);         }          // 6) Payments ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â idempotent check by (month,parentId,playerId)         upsertPayments(root.path("payments"), usersByEmail, defaultAcademy);          // 7) Chat (skip if already seeded)         if (conversationRepository.count() == 0 && messageRepository.count() == 0) {             Map<String, Long> convKeyToId = upsertConversations(root.path("conversations"), usersByEmail, divisionsByName, defaultAcademy);             upsertMessages(root.path("messages"), convKeyToId, usersByEmail);         }         if (messageReadRepository.count() == 0) {             upsertMessageReads(root.path("messageReads"), usersByEmail);         }          // 8) Notifications (skip if already present)         if (notificationRepository.count() == 0) {             upsertNotifications(root.path("notifications"), usersByEmail, defaultAcademy);         }          System.out.println("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ JSON seed applied successfully.");     }          */
    }
    // ---------------- Seed loading ----------------
    private Optional<JsonNode> loadSeedJson() {
        Path p1 = Paths.get("src/main/java/com/footballacademy/config/data/seed.prepared.json");
        Path p2 = Paths.get("src/main/java/com/footballacademy/config/data/seed.prepared.son");
        try {
            if (Files.exists(p1)) return Optional.of(mapper.readTree(p1.toFile()));
            if (Files.exists(p2)) return Optional.of(mapper.readTree(p2.toFile()));
        } catch (Exception e) {
            System.out.println("ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã‚Â¡Ãƒâ€šÃ‚Â ÃƒÆ’Ã‚Â¯Ãƒâ€šÃ‚Â¸Ãƒâ€šÃ‚Â Failed to read seed file from filesystem: " + e.getMessage());
        }
        try {
            ClassPathResource r = new ClassPathResource("config/data/seed.prepared.json");
            if (r.exists()) {
                try(InputStream in = r.getInputStream()) {
                    return Optional.of(mapper.readTree(in));
                }
            }
        } catch (Exception ignored) {
        } return Optional.empty();
    }
    // ---------------- Roles ----------------
    private void ensureBaseRoles() {
        String[] roleNames = {
            "SUPER_ADMIN", "ADMIN", "PLAYER", "PARENT", "TRAINER", "SCOUTER"
        };
        for (String roleName : roleNames) {
            roleRepository.findByName(roleName) .orElseGet(() -> roleRepository.save(new Role(roleName)));
        }
    }
    private Academy ensureDefaultAcademy() {
        return academyRepository.findBySlugIgnoreCase("default-academy") .orElseGet(() -> {
            Academy academy = new Academy("Default Sport Academy", "default-academy");
            academy.setEmail("academy@sportacademy.local");
            academy.setPhone("+21600000000");
            academy.setAddress("Main Campus");
            academy.setCity("Tunis");
            academy.setCountry("Tunisia");
            academy.setStatus(Academy.AcademyStatus.ACTIVE);
            return academyRepository.save(academy);
        });
    }
    private void ensureDefaultSuperAdmin() {
        if (!userRepository.findByMainRole(User.UserRole.SUPER_ADMIN) .isEmpty()) {
            return;
        }
        User user = new User();
        user.setNom("Platform Super Admin");
        user.setEmail("superadmin@sportacademy.local");
        user.setMdp(passwordEncoder.encode("SuperAdmin123!"));
        user.setMainRole(User.UserRole.SUPER_ADMIN);
        user.setActive(true);
        user.setRegistrationDate(LocalDateTime.now());
        user.setLoginCount(0L);
        Role role = roleRepository.findByName("SUPER_ADMIN") .orElseGet(() -> roleRepository.save(new Role("SUPER_ADMIN")));
        user.addRole(role);
        userRepository.save(user);
    }
    // ---------------- Divisions ----------------
    private Map<String, Division> upsertDivisions(JsonNode divisionsNode, Academy defaultAcademy) {
        Map<String, Division> map = new HashMap<>();
        if (divisionsNode != null && divisionsNode.isArray()) {
            for (JsonNode d : divisionsNode) {
                String nom = text(d, "nom");
                String cat = text(d, "categorie");
                if (nom == null) continue;
                Division div = divisionRepository .findByNomIgnoreCaseAndCategorieIgnoreCase(nom, cat == null ? "" : cat) .orElseGet(() -> divisionRepository.save(new Division(nom, cat)));
                if (div.getAcademy() == null) {
                    div.setAcademy(defaultAcademy);
                    div = divisionRepository.save(div);
                } map.put(nom.toLowerCase(), div);
            }
        } return map;
    }
    // ---------------- Users (+ roles) ----------------
    private Map<String, User> upsertUsers(JsonNode usersNode, Academy defaultAcademy) {
        Map<String, User> map = new HashMap<>();
        if (usersNode == null || !usersNode.isArray()) return map;
        for (JsonNode u : usersNode) {
            String email = text(u, "email");
            if (email == null) continue;
            User user = userRepository.findByEmail(email) .orElseGet(User::new);
            user.setEmail(email);
            String nom = text(u, "nom");
            if (nom != null) user.setNom(nom);
            String tel = text(u, "tel");
            if (tel != null) user.setTel(tel);
            String dateNaiss = text(u, "dateNaiss");
            if (dateNaiss != null) {
                try {
                    user.setDateNaiss(LocalDate.parse(dateNaiss));
                } catch (Exception ignored) {
                }
            } String mainRole = text(u, "mainRole");
            if (mainRole != null) {
                try {
                    user.setMainRole(User.UserRole.valueOf(mainRole.toUpperCase()));
                } catch (Exception ignored) {
                }
            }
            if (user.getMainRole() != User.UserRole.SUPER_ADMIN && user.getAcademy() == null) {
                user.setAcademy(defaultAcademy);
            } String mdp = text(u, "mdp");
            boolean encoded = bool(u, "passwordEncoded", false);
            if (mdp != null && !mdp.isBlank()) {
                user.setMdp(encoded ? mdp : passwordEncoder.encode(mdp));
            } User saved = userRepository.save(user);
            // roles array
            JsonNode roles = u.get("roles");
            if (roles != null && roles.isArray()) {
                for (JsonNode r : roles) {
                    String rn = r.asText(null);
                    if (rn == null) continue;
                    Role role = roleRepository.findByName(rn.toUpperCase()) .orElseGet(() -> roleRepository.save(new Role(rn.toUpperCase())));
                    boolean has = saved.getRoles() .stream() .anyMatch(x -> x.getName() != null && x.getName() .equalsIgnoreCase(role.getName()));
                    if (!has) saved.getRoles() .add(role);
                } saved = userRepository.save(saved);
            }
            // ensure role-specific entity exists (Player/Trainer/Parent/Admin)
            try {
                roleService.createRoleSpecificEntity(saved);
            } catch (Exception ignored) {
            } map.put(email.toLowerCase(), saved);
        } return map;
    }
    // ---------------- Trainers / Parents / Players ----------------
    private void upsertTrainers(JsonNode trainersNode, Map<String, User> usersByEmail, Map<String, Division> divisionsByName, Academy defaultAcademy) {
        if (trainersNode == null || !trainersNode.isArray()) return;
        for (JsonNode t : trainersNode) {
            String email = text(t, "email");
            if (email == null) continue;
            User u = usersByEmail.get(email.toLowerCase());
            if (u == null) continue;
            Trainer tr = trainerRepository.findById(u.getId()) .orElse(null);
            if (tr == null) continue;
            if (tr.getAcademy() == null) tr.setAcademy(defaultAcademy);
            tr.setSpeciality(text(t, "speciality"));
            tr.setExperience(text(t, "experience"));
            tr.setLicense(text(t, "license"));
            tr.setNotes(text(t, "notes"));
            String divNom = text(t, "divisionNom");
            if (divNom != null) tr.setDivision(divisionsByName.get(divNom.toLowerCase()));
            trainerRepository.save(tr);
        }
    }
    private void upsertParents(JsonNode parentsNode, Map<String, User> usersByEmail, Academy defaultAcademy) {
        if (parentsNode == null || !parentsNode.isArray()) return;
        for (JsonNode p : parentsNode) {
            String email = text(p, "email");
            if (email == null) continue;
            User u = usersByEmail.get(email.toLowerCase());
            if (u == null) continue;
            // Parent entity already created by RoleService
            Parent parent = parentRepository.findById(u.getId()) .orElse(null);
            if (parent != null && parent.getAcademy() == null) {
                parent.setAcademy(defaultAcademy);
                parentRepository.save(parent);
            }
        }
    }
    private void upsertPlayers(JsonNode playersNode, Map<String, User> usersByEmail, Map<String, Division> divisionsByName, Academy defaultAcademy) {
        if (playersNode == null || !playersNode.isArray()) return;
        for (JsonNode p : playersNode) {
            String email = text(p, "email");
            if (email == null) continue;
            User u = usersByEmail.get(email.toLowerCase());
            if (u == null) continue;
            Player pl = playerRepository.findById(u.getId()) .orElse(null);
            if (pl == null) continue;
            if (pl.getAcademy() == null) pl.setAcademy(defaultAcademy);
            pl.setPosition(text(p, "position"));
            pl.setNationality(text(p, "nationality"));
            pl.setPhone(text(p, "phone"));
            if (p.has("age") && !p.get("age") .isNull()) pl.setAge(p.get("age") .asInt());
            if (p.has("isPaid") && !p.get("isPaid") .isNull()) pl.setPaid(p.get("isPaid") .asBoolean());
            if (p.has("height") && !p.get("height") .isNull()) pl.setHeight(p.get("height") .asDouble());
            if (p.has("weight") && !p.get("weight") .isNull()) pl.setWeight(p.get("weight") .asDouble());
            if (p.has("goals") && !p.get("goals") .isNull()) pl.setGoals(p.get("goals") .asInt());
            if (p.has("assists") && !p.get("assists") .isNull()) pl.setAssists(p.get("assists") .asInt());
            if (p.has("matches") && !p.get("matches") .isNull()) pl.setMatches(p.get("matches") .asInt());
            if (p.has("averageRating") && !p.get("averageRating") .isNull()) pl.setAverageRating(p.get("averageRating") .asDouble());
            String divNom = text(p, "divisionNom");
            if (divNom != null) pl.setDivision(divisionsByName.get(divNom.toLowerCase()));
            String parentEmail = text(p, "parentEmail");
            if (parentEmail != null) {
                User pu = usersByEmail.get(parentEmail.toLowerCase());
                if (pu != null) parentRepository.findById(pu.getId()) .ifPresent(pl::setParent);
            } String trainerEmail = text(p, "trainerEmail");
            if (trainerEmail != null) {
                User tu = usersByEmail.get(trainerEmail.toLowerCase());
                if (tu != null) trainerRepository.findById(tu.getId()) .ifPresent(pl::setTrainer);
            } playerRepository.save(pl);
            // Ranking
            JsonNode ranking = p.get("ranking");
            if (ranking != null && ranking.isObject()) {
                PlayerRanking pr = playerRankingRepository.findByPlayerId(pl.getId()) .orElse(new PlayerRanking());
                pr.setPlayer(pl);
                pr.setPlayerId(pl.getId());
                if (ranking.has("score") && !ranking.get("score") .isNull()) pr.setScore(ranking.get("score") .asDouble());
                String tier = text(ranking, "tier");
                if (tier != null) {
                    try {
                        pr.setTier(PlayerRanking.Tier.valueOf(tier));
                    } catch (Exception ignored) {
                    }
                } pr.setLastUpdated(LocalDateTime.now());
                playerRankingRepository.save(pr);
            }
        }
    }
    // ---------------- Academy ----------------
    private void upsertAcademy(JsonNode academyNode, Map<String, Division> divisionsByName, Academy defaultAcademy) {
        if (academyNode == null || !academyNode.isObject()) return;
        JsonNode info = academyNode.get("info");
        if (info == null || !info.isObject()) return;
        // Single row pattern: if exists, update first; else create
        AcademyInfo ai = academyInfoRepository.findAll() .stream() .findFirst() .orElse(new AcademyInfo());
        if (ai.getAcademy() == null) {
            ai.setAcademy(defaultAcademy);
        }
        String nom = text(info, "nom");
        ai.setNom(nom == null ? "Football Academy" : nom);
        if (info.has("foundedYear") && !info.get("foundedYear") .isNull()) ai.setFoundedYear(info.get("foundedYear") .asInt());
        if (info.has("totalPlayers") && !info.get("totalPlayers") .isNull()) ai.setTotalPlayers(info.get("totalPlayers") .asInt());
        if (info.has("totalCoaches") && !info.get("totalCoaches") .isNull()) ai.setTotalCoaches(info.get("totalCoaches") .asInt());
        ai.setDescription(text(info, "description"));
        ai.setAddress(text(info, "address"));
        ai.setCity(text(info, "city"));
        ai.setCountry(text(info, "country"));
        ai.setPostalCode(text(info, "postalCode"));
        ai.setEmail(text(info, "email"));
        ai.setEmailSupport(text(info, "emailSupport"));
        ai.setPhone(text(info, "phone"));
        ai.setPhoneSupport(text(info, "phoneSupport"));
        ai.setWebsite(text(info, "website"));
        ai.setSlogan(text(info, "slogan"));
        ai.setMission(text(info, "mission"));
        ai.setVision(text(info, "vision"));
        ai.setGoogleMapsUrl(text(info, "googleMapsUrl"));
        // IMPORTANT: do not set imageUrl (no images)          // divisions list by names
        List<Long> divisionIds = new ArrayList<>();
        JsonNode divs = academyNode.get("divisions");
        if (divs != null && divs.isArray()) {
            for (JsonNode dn : divs) {
                String dname = dn.asText(null);
                if (dname == null) continue;
                Division d = divisionsByName.get(dname.toLowerCase());
                if (d != null) divisionIds.add(d.getId());
            }
        }
        if (!divisionIds.isEmpty()) {
            ai.setDivisionsList(divisionIds);
        } academyInfoRepository.save(ai);
    }
    // ---------------- Activities ----------------
    private void upsertActivities(JsonNode activitiesNode, Map<String, User> usersByEmail, Academy defaultAcademy) {
        if (activitiesNode == null || !activitiesNode.isArray()) return;
        for (JsonNode a : activitiesNode) {
            String type = text(a, "type");
            String dateStr = text(a, "date");
            String titre = text(a, "titre");
            String desc = text(a, "description");
            String lieu = text(a, "lieu");
            if (dateStr == null || titre == null) continue;
            LocalDate date;
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception ex) {
                continue;
            } Long trainerId;
            String trainerEmail = text(a, "trainerEmail");
            if (trainerEmail != null) {
                User tu = usersByEmail.get(trainerEmail.toLowerCase());
                if (tu != null) trainerId = tu.getId();
                else {
                    trainerId = null;
                }
            } else {
                trainerId = null;
            }
            // We avoid duplicates by (date, titre, trainerId) heuristic
            boolean dup = activityRepository.findAll() .stream() .anyMatch(x -> x.getDate() != null && x.getDate() .equals(date) && x.getTitre() != null && x.getTitre() .equalsIgnoreCase(titre) && Objects.equals(x.getTrainerId(), trainerId));
            if (dup) continue;
            if ("TRAINING" .equalsIgnoreCase(type)) {
                Training tr = new Training();
                tr.setTrainerId(trainerId);
                tr.setDate(date);
                tr.setTitre(titre);
                tr.setDescription(desc);
                tr.setLieu(lieu);
                tr.setAcademy(defaultAcademy);
                tr.setSessionType(text(a, "sessionType"));
                tr.setObjectives(text(a, "objectives"));
                trainingRepository.save(tr);
            } else
            if ("MATCH" .equalsIgnoreCase(type)) {
                Match m = new Match();
                m.setTrainerId(trainerId);
                m.setDate(date);
                m.setTitre(titre);
                m.setDescription(desc);
                m.setLieu(lieu);
                m.setAcademy(defaultAcademy);
                m.setOpponent(text(a, "opponent"));
                m.setResult(text(a, "result"));
                matchRepository.save(m);
            } else {
                Activity act = new Activity();
                act.setTrainerId(trainerId);
                act.setDate(date);
                act.setTitre(titre);
                act.setDescription(desc);
                act.setLieu(lieu);
                act.setAcademy(defaultAcademy);
                activityRepository.save(act);
            }
        }
    }
    // ---------------- Payments ----------------
    private void upsertPayments(JsonNode paymentsNode, Map<String, User> usersByEmail, Academy defaultAcademy) {
        if (paymentsNode == null || !paymentsNode.isArray()) return;
        for (JsonNode p : paymentsNode) {
            String month = text(p, "month");
            String parentEmail = text(p, "parentEmail");
            String playerEmail = text(p, "playerEmail");
            if (month == null || parentEmail == null || playerEmail == null) continue;
            User parentU = usersByEmail.get(parentEmail.toLowerCase());
            User playerU = usersByEmail.get(playerEmail.toLowerCase());
            if (parentU == null || playerU == null) continue;
            LocalDate mois;
            try {
                mois = LocalDate.parse(month);
            } catch (Exception ex) {
                continue;
            }
            // idempotent: check existing payments for that parent
            boolean exists = paymentRepository.findByParentId(parentU.getId()) .stream() .anyMatch(x -> x.getPlayerId() != null && x.getPlayerId() .equals(playerU.getId()) && x.getMois() != null && x.getMois() .equals(mois));
            if (exists) continue;
            Payment pay = new Payment();
            if (p.has("amount") && !p.get("amount") .isNull()) pay.setMontant(p.get("amount") .asDouble());
            pay.setMois(mois);
            pay.setParentId(parentU.getId());
            pay.setPlayerId(playerU.getId());
            pay.setAcademy(defaultAcademy);
            pay.setPaid(bool(p, "isPaid", false));
            paymentRepository.save(pay);
        }
    }
    // ---------------- Chat ----------------
    private Map<String, Long> upsertConversations(JsonNode convNode, Map<String, User> usersByEmail, Map<String, Division> divisionsByName, Academy defaultAcademy) {
        Map<String, Long> map = new HashMap<>();
        if (convNode == null || !convNode.isArray()) return map;
        for (JsonNode c : convNode) {
            String key = text(c, "key");
            String type = text(c, "type");
            String title = text(c, "title");
            String divisionNom = text(c, "divisionNom");
            Conversation conv = new Conversation();
            conv.setTitle(title);
            conv.setAcademy(defaultAcademy);
            if (type != null) {
                try {
                    conv.setType(Conversation.ConversationType.valueOf(type));
                } catch (Exception ignored) {
                }
            }
            if (divisionNom != null) {
                Division d = divisionsByName.get(divisionNom.toLowerCase());
                if (d != null) conv.setDivisionId(d.getId());
            } List<Long> pids = new ArrayList<>();
            JsonNode pe = c.get("participantsEmails");
            if (pe != null && pe.isArray()) {
                for (JsonNode e : pe) {
                    User u = usersByEmail.get(e.asText("") .toLowerCase());
                    if (u != null) pids.add(u.getId());
                }
            } conv.setParticipantIds(pids);
            Conversation saved = conversationRepository.save(conv);
            if (key != null) map.put(key, saved.getId());
        } return map;
    }
    private void upsertMessages(JsonNode messagesNode, Map<String, Long> convKeyToId, Map<String, User> usersByEmail) {
        if (messagesNode == null || !messagesNode.isArray()) return;
        for (JsonNode m : messagesNode) {
            String convKey = text(m, "conversationKey");
            Long convId = convKeyToId.get(convKey);
            if (convId == null) continue;
            String senderEmail = text(m, "senderEmail");
            if (senderEmail == null) continue;
            User sender = usersByEmail.get(senderEmail.toLowerCase());
            if (sender == null) continue;
            String receiverEmail = text(m, "receiverEmail");
            Long receiverId = null;
            if (receiverEmail != null) {
                User r = usersByEmail.get(receiverEmail.toLowerCase());
                if (r != null) receiverId = r.getId();
            } String content = text(m, "content");
            if (content == null) content = "";
            String clientTempId = text(m, "clientTempId");
            Message msg = new Message(convId, sender.getId(), receiverId, content, clientTempId);
            String ts = text(m, "timestamp");
            if (ts != null) {
                try {
                    msg.setTimestamp(LocalDateTime.parse(ts.replace(' ', 'T')));
                } catch (Exception ignored) {
                }
            } msg.setRead(bool(m, "read", false));
            messageRepository.save(msg);
        }
    }
    private void upsertMessageReads(JsonNode readsNode, Map<String, User> usersByEmail) {
        if (readsNode == null || !readsNode.isArray()) return;
        for (JsonNode r : readsNode) {
            String userEmail = text(r, "userEmail");
            if (userEmail == null) continue;
            User u = usersByEmail.get(userEmail.toLowerCase());
            if (u == null) continue;
            if (!r.has("messageId") || r.get("messageId") .isNull()) continue;
            long messageId = r.get("messageId") .asLong();
            boolean exists = messageReadRepository.existsByMessageIdAndUserId(messageId, u.getId());
            if (exists) continue;
            String readAt = text(r, "readAt");
            LocalDateTime t = LocalDateTime.now();
            if (readAt != null) {
                try {
                    t = LocalDateTime.parse(readAt.replace(' ', 'T'));
                } catch (Exception ignored) {
                }
            } MessageRead mr = new MessageRead();
            mr.setMessageId(messageId);
            mr.setUserId(u.getId());
            mr.setReadAt(t);
            messageReadRepository.save(mr);
        }
    }
    // ---------------- Notifications ----------------
    private void upsertNotifications(JsonNode notifsNode, Map<String, User> usersByEmail, Academy defaultAcademy) {
        if (notifsNode == null || !notifsNode.isArray()) return;
        for (JsonNode n : notifsNode) {
            String userEmail = text(n, "userEmail");
            if (userEmail == null) continue;
            User u = usersByEmail.get(userEmail.toLowerCase());
            if (u == null) continue;
            Notification notif = new Notification();
            notif.setUserId(u.getId());
            notif.setTitle(text(n, "title") == null ? "Notification" : text(n, "title"));
            notif.setContent(text(n, "content") == null ? "" : text(n, "content"));
            notif.setRead(bool(n, "isRead", false));
            notif.setCreatedBy(1L);
            // Assuming admin ID 1
            notif.setCategory(Notification.Category.GENERAL);
            notif.setAcademy(defaultAcademy);
            String createdAt = text(n, "createdAt");
            if (createdAt != null) {
                try {
                    notif.setCreatedAt(LocalDateTime.parse(createdAt.replace(' ', 'T')));
                } catch (Exception ex) {
                    notif.setCreatedAt(LocalDateTime.now());
                }
            } else {
                notif.setCreatedAt(LocalDateTime.now());
            } notificationRepository.save(notif);
        }
    }
    // ---------------- Helpers ----------------
    private String text(JsonNode node, String key) {
        if (node == null || key == null) return null;
        JsonNode v = node.get(key);
        if (v == null || v.isNull()) return null;
        String s = v.asText();
        return(s == null || s.isBlank()) ? null : s;
    }
    private boolean bool(JsonNode node, String key, boolean def) {
        if (node == null || key == null) return def;
        JsonNode v = node.get(key);
        if (v == null || v.isNull()) return def;
        return v.asBoolean(def);
    }
}
