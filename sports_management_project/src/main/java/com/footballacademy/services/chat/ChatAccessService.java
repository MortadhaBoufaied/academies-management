package com.footballacademy.services.chat;

import com.footballacademy.DTO.chat.ContactDTO;
import com.footballacademy.model.*;
import com.footballacademy.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatAccessService {

    private static final EnumSet<Admin.AdminResponsibility> FULL_CHAT_ADMIN_RESPONSIBILITIES =
            EnumSet.of(
                    Admin.AdminResponsibility.ACADEMY_DIRECTOR,
                    Admin.AdminResponsibility.OPERATIONS_MANAGER,
                    Admin.AdminResponsibility.SPORTS_COORDINATOR,
                    Admin.AdminResponsibility.COMMUNICATIONS_MANAGER
            );

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final ParentRepository parentRepository;
    private final TrainerRepository trainerRepository;
    private final AdminRepository adminRepository;
    private final DivisionRepository divisionRepository;
    private final ScoutingAssignmentRepository scoutingAssignmentRepository;
    private final ChatRoomService chatRoomService;

    public ChatAccessService(
            UserRepository userRepository,
            PlayerRepository playerRepository,
            ParentRepository parentRepository,
            TrainerRepository trainerRepository,
            AdminRepository adminRepository,
            DivisionRepository divisionRepository,
            ScoutingAssignmentRepository scoutingAssignmentRepository,
            ChatRoomService chatRoomService
    ) {
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.parentRepository = parentRepository;
        this.trainerRepository = trainerRepository;
        this.adminRepository = adminRepository;
        this.divisionRepository = divisionRepository;
        this.scoutingAssignmentRepository = scoutingAssignmentRepository;
        this.chatRoomService = chatRoomService;
    }

    // =====================================================
    // === PERMISSION CHECKS
    // =====================================================

    public boolean canContact(User me, Long otherUserId) {
        if (me == null || otherUserId == null) return false;
        if (me.getId() == null || me.getId().equals(otherUserId)) return false;
        if (userRepository.findById(otherUserId).isEmpty()) return false;
        return allowedDirectUserIds(me).contains(otherUserId);
    }

    public boolean canJoinDivisionGroup(User me, Long divisionId) {
        if (me == null || divisionId == null || divisionId <= 0) return false;

        Division division = divisionRepository.findById(divisionId).orElse(null);
        if (division == null) return false;

        User.UserRole role = me.getMainRole();
        if (role == null) return false;

        if (role == User.UserRole.SUPER_ADMIN) {
            // Super admins only use direct chats
            return false;
        }

        if (!sameAcademy(me, division.getAcademy())) return false;

        return switch (role) {
            case ADMIN -> canAdminUseFullChat(me);
            case TRAINER -> trainerDivisionIds(
                    trainerRepository.findById(me.getId()).orElse(null)
            ).contains(divisionId);
            case PLAYER -> {
                Player p = playerRepository.findById(me.getId()).orElse(null);
                Long playerDivId = p != null && p.getDivision() != null
                        ? p.getDivision().getId()
                        : null;
                yield Objects.equals(playerDivId, divisionId);
            }
            default -> false;
        };
    }

    // =====================================================
    // === CONTACT LIST ENTRY POINT
    // =====================================================

    public List<ContactDTO> listContacts(User me, String category, String q) {
        if (me == null || me.getMainRole() == null) return List.of();

        String query = normalizeQuery(q);

        return switch (me.getMainRole()) {
            case SUPER_ADMIN -> superAdminContacts(query);
            case ADMIN -> canAdminUseFullChat(me)
                    ? adminContactsFull(me, query)
                    : adminContactsLimited(me, query);
            case TRAINER -> trainerContacts(me, query);
            case PARENT -> parentContacts(me, query);
            case PLAYER -> playerContacts(me, query);
            default -> List.of();
        };
    }

    // =====================================================
    // === CONTACT PROVIDERS BY ROLE
    // =====================================================

    private List<ContactDTO> superAdminContacts(String query) {
        List<ContactDTO> out = new ArrayList<>();

        for (User u : userRepository.findByMainRole(User.UserRole.ADMIN)) {
            if (isRegularAdmin(u) && matchesUser(u, query)) {
                out.add(ContactDTO.user(
                        u.getId(),
                        safeName(u),
                        u.getEmail(),
                        "ADMIN",
                        null
                ));
            }
        }
        return dedup(out);
    }

    private List<ContactDTO> adminContactsFull(User me, String query) {
        List<ContactDTO> out = new ArrayList<>();
        Long academyId = academyId(me);
        if (academyId == null) return out;

        for (User u : userRepository.findByAcademy_Id(academyId)) {
            if (u == null || u.getId().equals(me.getId())) continue;
            if (u.getMainRole() == User.UserRole.SUPER_ADMIN) continue;
            if (!matchesUser(u, query)) continue;

            out.add(ContactDTO.user(
                    u.getId(),
                    safeName(u),
                    u.getEmail(),
                    u.getMainRole().name(),
                    null
            ));
        }

        for (Division d : divisionRepository.findByAcademy_Id(academyId)) {
            String title = divisionGroupTitle(d.getNom(), d.getId());
            if (!matchesText(title, query)) continue;

            Long convId = chatRoomService.ensureDivisionGroup(d.getId(), me.getId());
            if (convId != null) {
                out.add(ContactDTO.group(-d.getId(), title, d.getId(), convId));
            }
        }
        return dedup(out);
    }

    private List<ContactDTO> adminContactsLimited(User me, String query) {
        List<ContactDTO> out = new ArrayList<>();
        Long academyId = academyId(me);
        if (academyId == null) return out;

        for (User u : userRepository.findByAcademy_IdAndMainRole(
                academyId, User.UserRole.ADMIN
        )) {
            if (!u.getId().equals(me.getId()) && matchesUser(u, query)) {
                out.add(ContactDTO.user(
                        u.getId(),
                        safeName(u),
                        u.getEmail(),
                        "ADMIN",
                        null
                ));
            }
        }
        return dedup(out);
    }

    // =====================================================
    // === COMMON UTILITIES
    // =====================================================

    private boolean canAdminUseFullChat(User me) {
        if (me == null || me.getMainRole() != User.UserRole.ADMIN) return false;

        Admin.AdminResponsibility r =
                adminRepository.findByUser_Id(me.getId())
                        .map(Admin::getResponsibility)
                        .orElse(null);

        return r != null && FULL_CHAT_ADMIN_RESPONSIBILITIES.contains(r);
    }

    private Long academyId(User user) {
        return user != null && user.getAcademy() != null
                ? user.getAcademy().getId()
                : null;
    }

    private boolean sameAcademy(User user, Academy academy) {
        return academy != null && Objects.equals(academyId(user), academy.getId());
    }

    private String normalizeQuery(String q) {
        if (q == null) return null;
        String s = q.trim();
        return s.isEmpty() ? null : s.toLowerCase();
    }

    private boolean matchesText(String text, String q) {
        return q == null || (text != null && text.toLowerCase().contains(q));
    }

    private boolean matchesUser(User user, String q) {
        if (q == null || user == null) return true;
        return Optional.ofNullable(user.getNom()).orElse("")
                .toLowerCase().contains(q)
                || Optional.ofNullable(user.getEmail()).orElse("")
                .toLowerCase().contains(q);
    }

    private String safeName(User u) {
        if (u == null) return "User";
        if (u.getNom() != null && !u.getNom().isBlank()) return u.getNom();
        if (u.getEmail() != null && !u.getEmail().isBlank()) return u.getEmail();
        return "User #" + u.getId();
    }

    private String divisionGroupTitle(String name, Long id) {
        return name != null && !name.isBlank()
                ? "Division Group - " + name
                : "Division Group - #" + id;
    }

    private Set<Long> trainerDivisionIds(Trainer trainer) {
        Set<Long> ids = new LinkedHashSet<>();
        if (trainer == null) return ids;

        if (trainer.getDivision() != null) {
            ids.add(trainer.getDivision().getId());
        }
        if (trainer.getDivisions() != null) {
            trainer.getDivisions().stream()
                    .filter(d -> d.getId() != null)
                    .forEach(d -> ids.add(d.getId()));
        }
        return ids;
    }

    private boolean intersects(Set<Long> a, Set<Long> b) {
        return a != null && b != null && !Collections.disjoint(a, b);
    }

    private List<ContactDTO> dedup(List<ContactDTO> list) {
        Map<String, ContactDTO> map = new LinkedHashMap<>();
        for (ContactDTO c : list) {
            map.putIfAbsent(c.kind() + ":" + c.id(), c);
        }
        return new ArrayList<>(map.values());
    }

    private boolean isRegularAdmin(User u) {
        return u != null
                && u.getMainRole() == User.UserRole.ADMIN
                && !u.hasRole("SUPER_ADMIN");
    }

    // ==================== MISSING METHODS ====================
    public List<Long> allowedDirectUserIds(User user) {
        List<Long> ids = new ArrayList<>();
        if (user == null) return ids;

        User.UserRole role = user.getMainRole();
        if (role == null) return ids;

        if (role == User.UserRole.SUPER_ADMIN) {
            userRepository.findAll().stream()
                    .filter(u -> !Objects.equals(u.getId(), user.getId()))
                    .map(User::getId)
                    .forEach(ids::add);
            return ids;
        }

        Long academyId = academyId(user);

        if (role == User.UserRole.SCOUTER) {
            Set<Long> allowedAcademyIds = new LinkedHashSet<>();
            scoutingAssignmentRepository.findByScouter_Id(user.getId()).stream()
                    .filter(ScoutingAssignment::isActive)
                    .map(ScoutingAssignment::getAcademy)
                    .filter(Objects::nonNull)
                    .map(Academy::getId)
                    .forEach(allowedAcademyIds::add);

            for (Admin admin : adminRepository.findAll()) {
                User adminUser = admin.getUser();
                Academy academy = admin.getAcademy();
                if (adminUser == null || academy == null || !Boolean.TRUE.equals(adminUser.getActive())) continue;
                boolean openToContact = academy.getScouterContactEnabled() == null || Boolean.TRUE.equals(academy.getScouterContactEnabled());
                if (openToContact || allowedAcademyIds.contains(academy.getId())) {
                    ids.add(adminUser.getId());
                }
            }
            userRepository.findByMainRole(User.UserRole.SUPER_ADMIN).stream()
                    .filter(u -> u.getActive() == null || Boolean.TRUE.equals(u.getActive()))
                    .map(User::getId)
                    .forEach(ids::add);
            return ids;
        }

        if (academyId == null) return ids;

        if (role == User.UserRole.ADMIN && canAdminUseFullChat(user)) {
            userRepository.findByAcademy_Id(academyId).stream()
                    .filter(u -> !Objects.equals(u.getId(), user.getId()))
                    .map(User::getId)
                    .forEach(ids::add);
            return ids;
        }

        userRepository.findByAcademy_IdAndMainRole(academyId, User.UserRole.ADMIN).stream()
                .filter(u -> !Objects.equals(u.getId(), user.getId()))
                .filter(u -> u.getActive() == null || Boolean.TRUE.equals(u.getActive()))
                .map(User::getId)
                .forEach(ids::add);
        userRepository.findByMainRole(User.UserRole.SUPER_ADMIN).stream()
                .filter(u -> u.getActive() == null || Boolean.TRUE.equals(u.getActive()))
                .map(User::getId)
                .forEach(ids::add);
        return dedupIds(ids);
    }

    private List<Long> dedupIds(List<Long> ids) {
        return new ArrayList<>(new LinkedHashSet<>(ids));
    }

    public List<ContactDTO> trainerContacts(User user, String filter) {
        List<ContactDTO> contacts = new ArrayList<>();
        trainerRepository.findAll().forEach(trainer -> {
            if (trainer.getUser() != null) {
                String name = trainer.getUser().getNom();
                if (filter == null || name.contains(filter)) {
                    contacts.add(new ContactDTO(
                        trainer.getId(),
                        name,
                        trainer.getUser().getEmail(),
                        "TRAINER",
                        null,
                        "trainer",
                        null
                    ));
                }
            }
        });
        return contacts;
    }

    public List<ContactDTO> parentContacts(User user, String filter) {
        List<ContactDTO> contacts = new ArrayList<>();
        parentRepository.findAll().forEach(parent -> {
            if (parent.getUser() != null) {
                String name = parent.getUser().getNom();
                if (filter == null || name.contains(filter)) {
                    contacts.add(new ContactDTO(
                        parent.getId(),
                        name,
                        parent.getUser().getEmail(),
                        "PARENT",
                        null,
                        "parent",
                        null
                    ));
                }
            }
        });
        return contacts;
    }

    public List<ContactDTO> playerContacts(User user, String filter) {
        List<ContactDTO> contacts = new ArrayList<>();
        playerRepository.findAll().forEach(player -> {
            if (player.getUser() != null) {
                String name = player.getUser().getNom();
                if (filter == null || name.contains(filter)) {
                    contacts.add(new ContactDTO(
                        player.getId(),
                        name,
                        player.getUser().getEmail(),
                        "PLAYER",
                        player.getDivision() != null ? player.getDivision().getId() : null,
                        "player",
                        null
                    ));
                }
            }
        });
        return contacts;
    }
}
