package com.footballacademy.controllers_rest.notification;

import com.footballacademy.DTO.notification.NotificationCampaignStatsDto;
import com.footballacademy.model.Academy;
import com.footballacademy.model.Admin;
import com.footballacademy.model.Division;
import com.footballacademy.model.Notification;
import com.footballacademy.model.Parent;
import com.footballacademy.model.Payment;
import com.footballacademy.model.Player;
import com.footballacademy.model.User;
import com.footballacademy.repository.AcademyRepository;
import com.footballacademy.repository.DivisionRepository;
import com.footballacademy.repository.ParentRepository;
import com.footballacademy.repository.PaymentRepository;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.repository.TrainerRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.NotificationService;
import com.footballacademy.services.academy.AcademyAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/admin/notifications")
public
class AdminNotificationController {
    private static final Set<User.UserRole> DIVISION_SCOPED_ROLES = EnumSet.of(User.UserRole.PLAYER, User.UserRole.PARENT, User.UserRole.TRAINER);
    private static final Set<User.UserRole> PLATFORM_ADDRESSABLE_ROLES = EnumSet.of(User.UserRole.ADMIN, User.UserRole.PLAYER, User.UserRole.PARENT, User.UserRole.TRAINER, User.UserRole.SCOUTER);
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final DivisionRepository divisionRepository;
    private final PlayerRepository playerRepository;
    private final TrainerRepository trainerRepository;
    private final ParentRepository parentRepository;
    private final PaymentRepository paymentRepository;
    private final AcademyRepository academyRepository;
    private final AcademyAccessService academyAccessService;
    public AdminNotificationController(NotificationService notificationService, UserRepository userRepository, DivisionRepository divisionRepository, PlayerRepository playerRepository, TrainerRepository trainerRepository, ParentRepository parentRepository, PaymentRepository paymentRepository, AcademyRepository academyRepository, AcademyAccessService academyAccessService) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.divisionRepository = divisionRepository;
        this.playerRepository = playerRepository;
        this.trainerRepository = trainerRepository;
        this.parentRepository = parentRepository;
        this.paymentRepository = paymentRepository;
        this.academyRepository = academyRepository;
        this.academyAccessService = academyAccessService;
    }
    @PostMapping("/targeted")
    public ResponseEntity<?> sendTargeted(
    @RequestBody Map<String, Object> payload) {
        try {
            String title = toString(payload.get("title"));
            String contentHtml = toString(payload.get("contentHtml"));
            if (title == null || title.isBlank()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "title is required"));
            }
            if (contentHtml == null || contentHtml.isBlank()) {
                return ResponseEntity.badRequest() .body(Map.of("error", "contentHtml is required"));
            } TargetingPlan plan = buildTargetingPlan(asMap(payload.get("targeting")));
            NotificationCampaignStatsDto stats = notificationService.sendTargetedCampaign(title, contentHtml, plan.targetingMode(), plan.audienceSummary(), plan.category(), plan.targetAcademyId(), plan.recipients(), resolveCurrentActorId());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("sentCount", stats.getTotalRecipients());
            out.put("mode", plan.targetingMode());
            out.put("targetUserCount", plan.recipients() .size());
            out.put("audienceSummary", plan.audienceSummary());
            out.put("roles", plan.targetedRoles() .stream() .map(Enum::name) .toList());
            out.put("academyIds", new ArrayList<>(plan.scope() .academyIds()));
            out.put("campaign", stats);
            return ResponseEntity.ok(out);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to send targeted notification: " + e.getMessage()));
        }
    }
    @PostMapping("/preview")
    public ResponseEntity<?> previewTargeted(
    @RequestBody Map<String, Object> payload) {
        try {
            TargetingPlan plan = buildTargetingPlan(asMap(payload.get("targeting")));
            List<Map<String, Object>> sampleRecipients = userRepository.findAllById(plan.recipients()) .stream() .sorted(Comparator.comparing(user -> displayName(user) .toLowerCase(Locale.ROOT))) .limit(8) .map(this::toUserOption) .toList();
            return ResponseEntity.ok(Map.of("recipientCount", plan.recipients() .size(), "audienceSummary", plan.audienceSummary(), "targetingMode", plan.targetingMode(), "roles", plan.targetedRoles() .stream() .map(Enum::name) .toList(), "academyIds", new ArrayList<>(plan.scope() .academyIds()), "sampleRecipients", sampleRecipients));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest() .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to preview targeted notification: " + e.getMessage()));
        }
    }
    @GetMapping("/targeting-options")
    public ResponseEntity<?> targetingOptions(
    @RequestParam(required = false) Long academyId,
    @RequestParam(required = false) List<Long> academyIds) {
        try {
            PermissionProfile profile = permissionProfile();
            AudienceScope scope = resolveRequestedAudienceScope(academyId, academyIds);
            List<Division> divisions = divisionsForScope(scope);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("scope", academyAccessService.isSuperAdmin() ? "SUPER_ADMIN" : "ACADEMY_ADMIN");
            response.put("responsibility", profile.label());
            response.put("canTargetAcrossAcademies", profile.canTargetAcrossAcademies());
            response.put("canUseFinancialFilters", profile.canUseFinancialFilters());
            response.put("canUseGlobalAudience", academyAccessService.isSuperAdmin());
            response.put("selectedAcademyIds", new ArrayList<>(scope.academyIds()));
            response.put("allowedAcademyRoleTargets", profile.allowedAcademyRoles() .stream() .map(Enum::name) .toList());
            response.put("allowedDivisionRoleTargets", profile.allowedDivisionRoles() .stream() .map(Enum::name) .toList());
            response.put("allowedSpecificUserRoles", profile.allowedRecipientRoles() .stream() .map(Enum::name) .toList());
            response.put("lifecycleStates", List.of("ANY", "ACTIVE", "INACTIVE", "NEW"));
            response.put("paymentFilters", profile.canUseFinancialFilters() ? List.of("NONE", "OVERDUE", "HIGH_CREDIT") : List.of("NONE"));
            response.put("targetingRecipes", List.of(Map.of("key", "academy_roles", "label", "Academy roles", "description", "Send to role packs like only players, only trainers, or players + parents."), Map.of("key", "division_roles", "label", "Divisions + roles", "description", "Pick one or many divisions, then choose players, parents, trainers, or all of them."), Map.of("key", "mixed_pack", "label", "Mixed packs", "description", "Combine academy-wide admins with selected division groups and manual recipients."), Map.of("key", "payment_lifecycle", "label", "Lifecycle and payment", "description", "Layer inactive, new, overdue, or high-credit filters on top of your audience.")));
            response.put("roleCatalog", roleCatalog(profile));
            response.put("divisions", divisions.stream() .map(this::toDivisionOption) .toList());
            response.put("academies", academiesForScope(scope));
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to load targeting options: " + e.getMessage()));
        }
    }
    @GetMapping("/user-search")
    public ResponseEntity<?> searchUsers(
    @RequestParam String q,
    @RequestParam(required = false) Long academyId,
    @RequestParam(required = false) List<Long> academyIds,
    @RequestParam(required = false) List<String> roles,
    @RequestParam(defaultValue = "20") int limit) {
        try {
            PermissionProfile profile = permissionProfile();
            AudienceScope scope = resolveRequestedAudienceScope(academyId, academyIds);
            Set<User.UserRole> requestedRoles = parseRoleSet(roles);
            if (requestedRoles.isEmpty()) {
                requestedRoles = profile.allowedRecipientRoles();
            } else {
                assertAllowedRoleTargets("roles", requestedRoles, profile.allowedRecipientRoles());
            } Set<User.UserRole> effectiveRoles = requestedRoles;
            int cappedLimit = Math.max(1, Math.min(limit, 20));
            List<Map<String, Object>> users = userRepository.searchByNomOrEmail(q) .stream() .filter(user -> isUserInScope(user, scope)) .filter(user -> effectiveRoles.contains(user.getMainRole())) .sorted(Comparator.comparing(user -> displayName(user) .toLowerCase(Locale.ROOT))) .limit(cappedLimit) .map(this::toUserOption) .toList();
            return ResponseEntity.ok(Map.of("results", users));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN) .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to search recipients: " + e.getMessage()));
        }
    }
    @GetMapping("/campaigns")
    public ResponseEntity<?> listCampaigns(
    @RequestParam(defaultValue = "false") boolean mineOnly) {
        try {
            return ResponseEntity.ok(notificationService.getCampaigns(mineOnly));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch notification campaigns: " + e.getMessage()));
        }
    }
    @GetMapping("/campaigns/{campaignId}")
    public ResponseEntity<?> getCampaign(
    @PathVariable Long campaignId) {
        try {
            return ResponseEntity.ok(notificationService.getCampaignStats(campaignId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR) .body(Map.of("error", "Failed to fetch notification campaign: " + e.getMessage()));
        }
    }
    private TargetingPlan buildTargetingPlan(Map<String, Object> targeting) {
        PermissionProfile profile = permissionProfile();
        assertCanSendNotifications(profile);
        AudienceScope scope = resolveAudienceScope(targeting);
        if (usesLegacyMode(targeting)) {
            String legacyMode = Optional.ofNullable(toString(targeting.get("mode"))) .orElse("USER") .toUpperCase(Locale.ROOT);
            Set<Long> recipients = resolveLegacyRecipients(legacyMode, targeting, scope, profile);
            if (recipients.isEmpty()) {
                throw new IllegalArgumentException("No recipients match the selected legacy targeting mode.");
            } return new TargetingPlan(scope, legacyMode, recipients, legacyAudienceSummary(legacyMode, targeting, recipients.size()), legacyRoles(legacyMode));
        } Set<User.UserRole> academyRoleTargets = parseRoleSet(targeting.get("academyRoleTargets"));
        Set<User.UserRole> divisionRoleTargets = parseRoleSet(targeting.get("divisionRoleTargets"));
        List<Long> divisionIds = parseLongList(targeting.get("divisionIds"));
        List<Long> specificUserIds = parseLongList(targeting.get("specificUserIds"));
        String userQuery = normalizeText(toString(targeting.get("userQuery")));
        boolean includeAllAcademyUsers = Boolean.TRUE.equals(toBoolean(targeting.get("includeAllAcademyUsers")));
        String paymentFilter = Optional.ofNullable(normalizeText(toString(targeting.get("paymentFilter")))) .orElse("NONE") .toUpperCase(Locale.ROOT);
        assertAllowedRoleTargets("academyRoleTargets", academyRoleTargets, profile.allowedAcademyRoles());
        assertAllowedRoleTargets("divisionRoleTargets", divisionRoleTargets, profile.allowedDivisionRoles());
        if (!divisionRoleTargets.isEmpty() && divisionIds.isEmpty()) {
            throw new IllegalArgumentException("Select at least one division before choosing division user types.");
        }
        if (! "NONE" .equals(paymentFilter) && !profile.canUseFinancialFilters()) {
            throw new AccessDeniedException("Your admin responsibility cannot use financial notification filters.");
        }
        if (!hasCombinationSelection(includeAllAcademyUsers, academyRoleTargets, divisionIds, specificUserIds, userQuery, paymentFilter)) {
            throw new IllegalArgumentException("Select at least one audience block: academy roles, divisions, specific users, search query, or financial filters.");
        } LinkedHashSet<Long> recipients = new LinkedHashSet<>();
        Set<User.UserRole> targetedRoles = EnumSet.noneOf(User.UserRole.
        class);
        if (includeAllAcademyUsers) {
            Set<User.UserRole> allAllowedRoles = profile.allowedRecipientRoles();
            recipients.addAll(resolveScopedRoleRecipients(allAllowedRoles, scope));
            targetedRoles.addAll(allAllowedRoles);
        }
        if (!academyRoleTargets.isEmpty()) {
            recipients.addAll(resolveScopedRoleRecipients(academyRoleTargets, scope));
            targetedRoles.addAll(academyRoleTargets);
        }
        if (!divisionIds.isEmpty()) {
            Set<User.UserRole> effectiveDivisionRoles = divisionRoleTargets.isEmpty() ? profile.defaultDivisionRoles() : divisionRoleTargets;
            recipients.addAll(resolveDivisionRecipients(divisionIds, effectiveDivisionRoles, scope));
            targetedRoles.addAll(effectiveDivisionRoles);
        }
        if (!specificUserIds.isEmpty()) {
            List<Long> allowedIds = filterVisibleUserIds(specificUserIds, scope, profile.allowedRecipientRoles());
            recipients.addAll(allowedIds);
            targetedRoles.addAll(userRepository.findAllById(allowedIds) .stream() .map(User::getMainRole) .filter(Objects::nonNull) .collect(Collectors.toSet()));
        }
        if (userQuery != null && !userQuery.isBlank()) {
            List<User> matchedUsers = userRepository.searchByNomOrEmail(userQuery) .stream() .filter(user -> isUserInScope(user, scope)) .filter(user -> profile.allowedRecipientRoles() .contains(user.getMainRole())) .toList();
            recipients.addAll(matchedUsers.stream() .map(User::getId) .toList());
            targetedRoles.addAll(matchedUsers.stream() .map(User::getMainRole) .filter(Objects::nonNull) .collect(Collectors.toSet()));
        }
        if (! "NONE" .equals(paymentFilter)) {
            recipients.addAll(resolvePaymentFilteredRecipients(paymentFilter, targeting, scope, profile));
            targetedRoles.add(User.UserRole.PARENT);
        } recipients = applyLifecycleFilter(recipients, targeting);
        if (recipients.isEmpty()) {
            throw new IllegalArgumentException("No recipients match the selected targeting combination.");
        }
        if (targetedRoles.isEmpty()) {
            targetedRoles.addAll(userRepository.findAllById(recipients) .stream() .map(User::getMainRole) .filter(Objects::nonNull) .collect(Collectors.toSet()));
        } return new TargetingPlan(scope, "COMBINATION", recipients, combinationAudienceSummary(scope, academyRoleTargets, divisionRoleTargets, divisionIds.size(), includeAllAcademyUsers, userQuery, specificUserIds.size(), targeting, paymentFilter, recipients.size()), targetedRoles);
    }
    private Set<Long> resolveLegacyRecipients(String mode, Map<String, Object> targeting, AudienceScope scope, PermissionProfile profile) {
        Set<Long> ids = new LinkedHashSet<>();
        switch (mode) {
            case "USER" -> {
                String q = toString(targeting.get("userQuery"));
                if (q != null && !q.isBlank()) {
                    userRepository.searchByNomOrEmail(q) .stream() .filter(user -> isUserInScope(user, scope)) .filter(user -> profile.allowedRecipientRoles() .contains(user.getMainRole())) .forEach(user -> ids.add(user.getId()));
                }
            } case "DIVISION" -> ids.addAll(resolveDivisionRecipients(resolveLegacyDivisionIds(targeting), EnumSet.of(User.UserRole.PLAYER, User.UserRole.PARENT, User.UserRole.TRAINER), scope));
            case "CREDIT" -> ids.addAll(resolvePaymentFilteredRecipients("HIGH_CREDIT", targeting, scope, profile));
            case "INACTIVE" -> ids.addAll(applyLifecycleFilter(new LinkedHashSet<>(resolveScopedRoleRecipients(profile.allowedRecipientRoles(), scope)), Map.of("activeState", "INACTIVE", "monthsWindow", toInt(targeting.get("monthsUnpaidMin"), 2))));
            case "ACTIVE" -> ids.addAll(applyLifecycleFilter(new LinkedHashSet<>(resolveScopedRoleRecipients(profile.allowedRecipientRoles(), scope)), Map.of("activeState", "ACTIVE", "monthsWindow", toInt(targeting.get("monthsUnpaidMin"), 2))));
            case "NEW" -> ids.addAll(applyLifecycleFilter(new LinkedHashSet<>(resolveScopedRoleRecipients(profile.allowedRecipientRoles(), scope)), Map.of("activeState", "NEW", "monthsWindow", toInt(targeting.get("monthsUnpaidMin"), 1))));
            default -> {
            }
        } return ids;
    }
    private List<Long> resolveLegacyDivisionIds(Map<String, Object> targeting) {
        List<Long> divisionIds = parseLongList(targeting.get("divisionIds"));
        if (!divisionIds.isEmpty()) {
            return divisionIds;
        } Long divisionId = toLong(targeting.get("divisionId"));
        if (divisionId != null && divisionId > 0) {
            return List.of(divisionId);
        } String divisionName = toString(targeting.get("divisionName"));
        if (divisionName != null && !divisionName.isBlank()) {
            return divisionRepository.findByNomIgnoreCase(divisionName) .map(division -> List.of(division.getId())) .orElse(List.of());
        } return List.of();
    }
    private LinkedHashSet<Long> applyLifecycleFilter(Set<Long> recipients, Map<String, Object> targeting) {
        if (recipients == null || recipients.isEmpty()) {
            return new LinkedHashSet<>();
        } String activeState = Optional.ofNullable(normalizeText(toString(targeting.get("activeState")))) .orElse("ANY") .toUpperCase(Locale.ROOT);
        if ("ANY" .equals(activeState)) {
            return new LinkedHashSet<>(recipients);
        } int monthsWindow = Math.max(1, toInt(targeting.get("monthsWindow"), toInt(targeting.get("monthsUnpaidMin"), 2)));
        LocalDateTime cutoff = LocalDateTime.now() .minus(monthsWindow, ChronoUnit.MONTHS);
        Map<Long, User> usersById = userRepository.findAllById(recipients) .stream() .collect(Collectors.toMap(User::getId, user -> user));
        return recipients.stream() .filter(userId -> {
            User user = usersById.get(userId);
            if (user == null) {
                return false;
            } return
            switch (activeState) {
                case "ACTIVE" -> Boolean.TRUE.equals(user.getActive()) && user.getLastLogin() != null && !user.getLastLogin() .isBefore(cutoff);
                case "INACTIVE" -> Boolean.FALSE.equals(user.getActive()) || user.getLastLogin() == null || user.getLastLogin() .isBefore(cutoff);
                case "NEW" -> user.getRegistrationDate() != null && !user.getRegistrationDate() .isBefore(cutoff);
                default -> true;
            };
        }) .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    private Set<Long> resolveScopedRoleRecipients(Set<User.UserRole> roles, AudienceScope scope) {
        return usersForScope(scope) .stream() .filter(user -> user.getMainRole() != null && roles.contains(user.getMainRole())) .map(User::getId) .filter(Objects::nonNull) .collect(Collectors.toCollection(LinkedHashSet::new));
    }
    private Set<Long> resolveDivisionRecipients(List<Long> divisionIds, Set<User.UserRole> roles, AudienceScope scope) {
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (Long divisionId : divisionIds) {
            if (divisionId == null || divisionId <= 0) {
                continue;
            } Division division = divisionRepository.findById(divisionId) .orElseThrow(() -> new IllegalArgumentException("Division not found: " + divisionId));
            academyAccessService.assertCanAccessDivision(division);
            Long divisionAcademyId = division.getAcademy() != null ? division.getAcademy() .getId() : null;
            if (!scope.isGlobal() && !scope.containsAcademy(divisionAcademyId)) {
                throw new AccessDeniedException("You cannot target divisions outside the selected academy scope.");
            } List<Player> divisionPlayers = playerRepository.findByDivisionIdWithUserAndRefs(divisionId);
            if (roles.contains(User.UserRole.PLAYER)) {
                divisionPlayers.stream() .map(Player::getUser) .filter(Objects::nonNull) .map(User::getId) .filter(Objects::nonNull) .forEach(ids::add);
            }
            if (roles.contains(User.UserRole.PARENT)) {
                divisionPlayers.stream() .map(Player::getParent) .filter(Objects::nonNull) .map(Parent::getUser) .filter(Objects::nonNull) .map(User::getId) .filter(Objects::nonNull) .forEach(ids::add);
            }
            if (roles.contains(User.UserRole.TRAINER)) {
                trainerRepository.findCoachingDivisionWithUser(divisionId) .stream() .map(trainer -> trainer.getUser()) .filter(Objects::nonNull) .map(User::getId) .filter(Objects::nonNull) .forEach(ids::add);
            }
        } return ids;
    }
    private Set<Long> resolvePaymentFilteredRecipients(String paymentFilter, Map<String, Object> targeting, AudienceScope scope, PermissionProfile profile) {
        if (!profile.canUseFinancialFilters()) {
            throw new AccessDeniedException("Your admin responsibility cannot use payment-based audience filters.");
        } LinkedHashSet<Long> ids = new LinkedHashSet<>();
        switch (paymentFilter) {
            case "HIGH_CREDIT", "CREDIT" -> {
                double minCredit = toDouble(targeting.get("creditMin"), 100.0);
                int minMonths = Math.max(1, toInt(targeting.get("monthsUnpaidMin"), 2));
                Map<Long, List<Payment>> byParent = paymentsForScope(scope) .stream() .filter(payment -> !payment.isPaid() && payment.getParentId() != null) .collect(Collectors.groupingBy(Payment::getParentId));
                byParent.forEach((parentId, payments) -> {
                    double unpaidTotal = payments.stream() .mapToDouble(Payment::getMontant) .sum();
                    long months = payments.stream() .map(Payment::getMois) .filter(Objects::nonNull) .map(date -> date.withDayOfMonth(1)) .distinct() .count();
                    if (unpaidTotal >= minCredit && months >= minMonths) {
                        parentUserId(parentId) .ifPresent(ids::add);
                    }
                });
            } case "OVERDUE" -> {
                LocalDate firstOfCurrentMonth = LocalDate.now() .withDayOfMonth(1);
                paymentsForScope(scope) .stream() .filter(payment -> !payment.isPaid() && payment.getMois() != null && payment.getMois() .isBefore(firstOfCurrentMonth) && payment.getParentId() != null) .map(Payment::getParentId) .distinct() .forEach(parentId -> parentUserId(parentId) .ifPresent(ids::add));
            } default -> {
            }
        } return ids;
    }
    private Optional<Long> parentUserId(Long parentId) {
        if (parentId == null) {
            return Optional.empty();
        } return parentRepository.findByIdWithUser(parentId) .flatMap(parent -> Optional.ofNullable(parent.getUser())) .map(User::getId);
    }
    private List<User> usersForScope(AudienceScope scope) {
        Stream<User> stream;
        if (scope.isGlobal()) {
            stream = userRepository.findAll() .stream();
        } else {
            stream = scope.academyIds() .stream() .flatMap(academyId -> userRepository.findByAcademy_Id(academyId) .stream());
        } return stream .filter(Objects::nonNull) .filter(user -> user.getMainRole() != null && PLATFORM_ADDRESSABLE_ROLES.contains(user.getMainRole())) .collect(Collectors.collectingAndThen(Collectors.toMap(User::getId, user -> user,(left, right) -> left, LinkedHashMap::new), map -> new ArrayList<>(map.values())));
    }
    private List<Payment> paymentsForScope(AudienceScope scope) {
        if (scope.isGlobal()) {
            return paymentRepository.findAll();
        }
        if (scope.academyIds() .size() == 1) {
            return paymentRepository.findByAcademy_Id(scope.academyIds() .iterator() .next());
        } return paymentRepository.findAll() .stream() .filter(payment -> payment.getAcademy() != null && scope.academyIds() .contains(payment.getAcademy() .getId())) .toList();
    }
    private List<Long> filterVisibleUserIds(Collection<Long> userIds, AudienceScope scope, Set<User.UserRole> allowedRoles) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        } return userIds.stream() .filter(Objects::nonNull) .distinct() .map(userRepository::findById) .flatMap(Optional::stream) .filter(user -> isUserInScope(user, scope)) .filter(user -> allowedRoles.contains(user.getMainRole())) .map(User::getId) .toList();
    }
    private boolean isUserInScope(User user, AudienceScope scope) {
        if (user == null || user.getMainRole() == null || !PLATFORM_ADDRESSABLE_ROLES.contains(user.getMainRole())) {
            return false;
        }
        if (scope.isGlobal()) {
            return true;
        } return user.getAcademy() != null && scope.containsAcademy(user.getAcademy() .getId());
    }
    private PermissionProfile permissionProfile() {
        if (academyAccessService.isSuperAdmin()) {
            return new PermissionProfile("SUPER_ADMIN", true, true, EnumSet.copyOf(PLATFORM_ADDRESSABLE_ROLES), EnumSet.copyOf(DIVISION_SCOPED_ROLES), EnumSet.copyOf(DIVISION_SCOPED_ROLES));
        } Admin.AdminResponsibility responsibility = academyAccessService.currentAdminResponsibility();
        if (responsibility == null) {
            throw new AccessDeniedException("Only configured academy admins can send targeted notifications.");
        } return
        switch (responsibility) {
            case ACADEMY_DIRECTOR, OPERATIONS_MANAGER, COMMUNICATIONS_MANAGER -> new PermissionProfile(responsibility.name(), false, responsibility == Admin.AdminResponsibility.OPERATIONS_MANAGER || responsibility == Admin.AdminResponsibility.ACADEMY_DIRECTOR, EnumSet.of(User.UserRole.ADMIN, User.UserRole.PLAYER, User.UserRole.PARENT, User.UserRole.TRAINER, User.UserRole.SCOUTER), EnumSet.copyOf(DIVISION_SCOPED_ROLES), EnumSet.copyOf(DIVISION_SCOPED_ROLES));
            case SPORTS_COORDINATOR -> new PermissionProfile(responsibility.name(), false, false, EnumSet.of(User.UserRole.PLAYER, User.UserRole.PARENT, User.UserRole.TRAINER), EnumSet.copyOf(DIVISION_SCOPED_ROLES), EnumSet.copyOf(DIVISION_SCOPED_ROLES));
            case PLAYER_REGISTRAR -> new PermissionProfile(responsibility.name(), false, false, EnumSet.of(User.UserRole.PLAYER, User.UserRole.PARENT), EnumSet.of(User.UserRole.PLAYER, User.UserRole.PARENT), EnumSet.of(User.UserRole.PLAYER, User.UserRole.PARENT));
            case FINANCE_MANAGER -> new PermissionProfile(responsibility.name(), false, true, EnumSet.of(User.UserRole.ADMIN, User.UserRole.PLAYER, User.UserRole.PARENT), EnumSet.of(User.UserRole.PLAYER, User.UserRole.PARENT), EnumSet.of(User.UserRole.PLAYER, User.UserRole.PARENT));
            case MEDICAL_WELFARE_MANAGER -> new PermissionProfile(responsibility.name(), false, false, EnumSet.of(User.UserRole.PLAYER, User.UserRole.PARENT, User.UserRole.TRAINER), EnumSet.copyOf(DIVISION_SCOPED_ROLES), EnumSet.copyOf(DIVISION_SCOPED_ROLES));
        };
    }
    private void assertCanSendNotifications(PermissionProfile profile) {
        if (profile == null || profile.allowedRecipientRoles() .isEmpty()) {
            throw new AccessDeniedException("Your admin responsibility cannot send targeted notifications.");
        }
    }
    private void assertAllowedRoleTargets(String fieldName, Set<User.UserRole> requested, Set<User.UserRole> allowed) {
        if (requested.isEmpty()) {
            return;
        } Set<User.UserRole> disallowed = EnumSet.copyOf(requested);
        disallowed.removeAll(allowed);
        if (!disallowed.isEmpty()) {
            throw new AccessDeniedException("You cannot target these roles with " + fieldName + ": " + disallowed);
        }
    }
    private AudienceScope resolveAudienceScope(Map<String, Object> targeting) {
        return resolveRequestedAudienceScope(toLong(targeting.get("academyId")), parseLongList(targeting.get("academyIds")));
    }
    private AudienceScope resolveRequestedAudienceScope(Long academyId, List<Long> academyIds) {
        LinkedHashSet<Long> selectedAcademyIds = new LinkedHashSet<>();
        if (academyIds != null) {
            selectedAcademyIds.addAll(academyIds.stream() .filter(Objects::nonNull) .filter(id -> id > 0) .toList());
        }
        if (academyId != null && academyId > 0) {
            selectedAcademyIds.add(academyId);
        }
        if (academyAccessService.isSuperAdmin()) {
            selectedAcademyIds.forEach(academyAccessService::assertCanAccessAcademy);
            Long campaignAcademyId = selectedAcademyIds.size() == 1 ? selectedAcademyIds.iterator() .next() : null;
            return new AudienceScope(selectedAcademyIds, campaignAcademyId);
        } Long currentAcademyId = academyAccessService.currentAcademyOrThrow() .getId();
        if (!selectedAcademyIds.isEmpty() &&(selectedAcademyIds.size() != 1 || !selectedAcademyIds.contains(currentAcademyId))) {
            throw new AccessDeniedException("You cannot target another academy.");
        } return new AudienceScope(new LinkedHashSet<>(Set.of(currentAcademyId)), currentAcademyId);
    }
    private boolean usesLegacyMode(Map<String, Object> targeting) {
        return targeting.containsKey("mode") && !targeting.containsKey("academyRoleTargets") && !targeting.containsKey("divisionRoleTargets") && !targeting.containsKey("divisionIds") && !targeting.containsKey("specificUserIds") && !targeting.containsKey("academyIds") && !targeting.containsKey("includeAllAcademyUsers") && !targeting.containsKey("activeState") && !targeting.containsKey("paymentFilter");
    }
    private boolean hasCombinationSelection(boolean includeAllAcademyUsers, Set<User.UserRole> academyRoleTargets, List<Long> divisionIds, List<Long> specificUserIds, String userQuery, String paymentFilter) {
        return includeAllAcademyUsers || !academyRoleTargets.isEmpty() || !divisionIds.isEmpty() || !specificUserIds.isEmpty() ||(userQuery != null && !userQuery.isBlank()) || ! "NONE" .equals(paymentFilter);
    }
    private Set<User.UserRole> parseRoleSet(Object value) {
        LinkedHashSet<User.UserRole> roles = new LinkedHashSet<>();
        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                parseRole(item) .ifPresent(roles::add);
            } return roles;
        } parseRole(value) .ifPresent(roles::add);
        return roles;
    }
    private Optional<User.UserRole> parseRole(Object value) {
        String raw = normalizeText(toString(value));
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(User.UserRole.valueOf(raw.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
    private List<Long> parseLongList(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream() .map(this::toLong) .filter(Objects::nonNull) .filter(id -> id > 0) .distinct() .toList();
        } Long single = toLong(value);
        if (single != null && single > 0) {
            return List.of(single);
        } return List.of();
    }
    private Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        } String raw = String.valueOf(value) .trim() .toLowerCase(Locale.ROOT);
        return "true" .equals(raw) || "1" .equals(raw) || "yes" .equals(raw);
    }
    private long resolveCurrentActorId() {
        Authentication auth = SecurityContextHolder.getContext() .getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName() .isBlank()) {
            return userRepository.findByEmail(auth.getName()) .map(User::getId) .orElse(1L);
        } return 1L;
    }
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return(Map<String, Object>) map;
        } return new HashMap<>();
    }
    private String toString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
    private String normalizeText(String value) {
        return value == null ? null : value.trim();
    }
    private int toInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }
    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
    private double toDouble(Object value, double fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return fallback;
        }
    }
    private String legacyAudienceSummary(String mode, Map<String, Object> targeting, int count) {
        return
        switch (mode) {
            case "USER" -> count + " recipients from search \"" + Optional.ofNullable(toString(targeting.get("userQuery"))) .orElse("") .trim() + "\"";
            case "DIVISION" -> count + " recipients inside selected division groups";
            case "CREDIT" -> count + " parent accounts above credit threshold";
            case "ACTIVE" -> count + " active users";
            case "INACTIVE" -> count + " inactive users";
            case "NEW" -> count + " newly registered users";
            default -> count + " recipients";
        };
    }
    private Set<User.UserRole> legacyRoles(String mode) {
        return
        switch (mode) {
            case "DIVISION" -> EnumSet.of(User.UserRole.PLAYER, User.UserRole.PARENT, User.UserRole.TRAINER);
            case "CREDIT" -> EnumSet.of(User.UserRole.PARENT);
            default -> EnumSet.noneOf(User.UserRole.
            class);
        };
    }
    private String combinationAudienceSummary(AudienceScope scope, Set<User.UserRole> academyRoleTargets, Set<User.UserRole> divisionRoleTargets, int divisionCount, boolean includeAllAcademyUsers, String userQuery, int explicitUserCount, Map<String, Object> targeting, String paymentFilter, int totalRecipients) {
        List<String> parts = new ArrayList<>();
        if (scope.isGlobal()) {
            parts.add("all academies");
        } else
        if (!scope.academyIds() .isEmpty()) {
            parts.add(scope.academyIds() .size() == 1 ? "1 academy" : scope.academyIds() .size() + " academies");
        }
        if (includeAllAcademyUsers) {
            parts.add("all allowed users");
        }
        if (!academyRoleTargets.isEmpty()) {
            parts.add("academy roles: " + academyRoleTargets.stream() .map(Enum::name) .collect(Collectors.joining(", ")));
        }
        if (divisionCount > 0) {
            String divisionRoles = divisionRoleTargets.isEmpty() ? "all division roles" : divisionRoleTargets.stream() .map(Enum::name) .collect(Collectors.joining(", "));
            parts.add(divisionCount + " division(s) - " + divisionRoles);
        }
        if (explicitUserCount > 0) {
            parts.add(explicitUserCount + " selected users");
        }
        if (userQuery != null && !userQuery.isBlank()) {
            parts.add("search: " + userQuery);
        }
        if (! "NONE" .equals(paymentFilter)) {
            parts.add("payment filter: " + paymentFilter);
        } String activeState = Optional.ofNullable(normalizeText(toString(targeting.get("activeState")))) .orElse("ANY") .toUpperCase(Locale.ROOT);
        if (! "ANY" .equals(activeState)) {
            parts.add("lifecycle: " + activeState);
        }
        if (parts.isEmpty()) {
            parts.add("custom audience");
        } return totalRecipients + " recipients - " + String.join(" - ", parts);
    }
    private List<Division> divisionsForScope(AudienceScope scope) {
        Stream<Division> stream;
        if (scope.isGlobal()) {
            stream = divisionRepository.findAll() .stream();
        } else {
            stream = scope.academyIds() .stream() .flatMap(academyId -> divisionRepository.findByAcademy_Id(academyId) .stream());
        } return stream .filter(Objects::nonNull) .filter(academyAccessService::canAccessDivision) .collect(Collectors.collectingAndThen(Collectors.toMap(Division::getId, division -> division,(left, right) -> left, LinkedHashMap::new), map -> map.values() .stream() .sorted(Comparator.comparing(division -> Optional.ofNullable(division.getNom()) .orElse(""), String.CASE_INSENSITIVE_ORDER)) .toList()));
    }
    private List<Map<String, Object>> academiesForScope(AudienceScope scope) {
        Stream<Academy> academyStream = academyAccessService.isSuperAdmin() ? academyRepository.findAll() .stream() : Stream.of(academyAccessService.currentAcademyOrThrow());
        return academyStream .filter(Objects::nonNull) .sorted(Comparator.comparing(academy -> Optional.ofNullable(academy.getName()) .orElse(""), String.CASE_INSENSITIVE_ORDER)) .map(academy -> {
            Map<String, Object> academyMap = new LinkedHashMap<>();
            academyMap.put("id", academy.getId());
            academyMap.put("name", academy.getName());
            academyMap.put("slug", Optional.ofNullable(academy.getSlug()) .orElse(""));
            academyMap.put("selected", scope.academyIds() .contains(academy.getId()));
            academyMap.put("status", academy.getStatus() != null ? academy.getStatus() .name() : "UNKNOWN");
            return academyMap;
        }) .toList();
    }
    private List<Map<String, Object>> roleCatalog(PermissionProfile profile) {
        return PLATFORM_ADDRESSABLE_ROLES.stream() .filter(profile.allowedRecipientRoles() ::contains) .map(role -> Map.<String, Object>of("key", role.name(), "label", prettyRole(role), "academyEligible", profile.allowedAcademyRoles() .contains(role), "divisionEligible", profile.allowedDivisionRoles() .contains(role))) .toList();
    }
    private Map<String, Object> toDivisionOption(Division division) {
        Map<String, Object> divisionMap = new LinkedHashMap<>();
        divisionMap.put("id", division.getId());
        divisionMap.put("name", Optional.ofNullable(division.getNom()).orElse("Division #" + division.getId()));
        divisionMap.put("category", Optional.ofNullable(division.getCategorie()).orElse(""));
        divisionMap.put("active", division.getActive() == null || Boolean.TRUE.equals(division.getActive()));
        divisionMap.put("academyId", division.getAcademy() != null ? division.getAcademy().getId() : null);
        divisionMap.put("academyName", division.getAcademy() != null ? Optional.ofNullable(division.getAcademy().getName()).orElse("") : "");
        return divisionMap;
    }
    private Map<String, Object> toUserOption(User user) {
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("id", user.getId());
        userMap.put("name", displayName(user));
        userMap.put("email", Optional.ofNullable(user.getEmail()) .orElse(""));
        userMap.put("role", user.getMainRole() != null ? user.getMainRole() .name() : "UNKNOWN");
        userMap.put("academyId", user.getAcademy() != null ? user.getAcademy() .getId() : null);
        userMap.put("academyName", user.getAcademy() != null ? Optional.ofNullable(user.getAcademy() .getName()) .orElse("") : "");
        userMap.put("active", Boolean.TRUE.equals(user.getActive()));
        return userMap;
    }
    private String displayName(User user) {
        return Optional.ofNullable(user.getNom()) .filter(name -> !name.isBlank()) .orElseGet(() -> Optional.ofNullable(user.getEmail()) .filter(email -> !email.isBlank()) .orElse("User #" + user.getId()));
    }
    private String prettyRole(User.UserRole role) {
        return
        switch (role) {
            case ADMIN -> "Admins";
            case PLAYER -> "Players";
            case PARENT -> "Parents";
            case TRAINER -> "Trainers";
            case SCOUTER -> "Scouters";
            case SUPER_ADMIN -> "Super Admins";
        };
    }
    private
    record PermissionProfile(String label, boolean canTargetAcrossAcademies, boolean canUseFinancialFilters, Set<User.UserRole> allowedAcademyRoles, Set<User.UserRole> allowedDivisionRoles, Set<User.UserRole> defaultDivisionRoles) {
        private Set<User.UserRole> allowedRecipientRoles() {
            LinkedHashSet<User.UserRole> roles = new LinkedHashSet<>();
            roles.addAll(allowedAcademyRoles);
            roles.addAll(allowedDivisionRoles);
            return roles;
        }
    }
    private
    record AudienceScope(Set<Long> academyIds, Long campaignAcademyId) {
        private boolean isGlobal() {
            return academyIds == null || academyIds.isEmpty();
        }
        private boolean containsAcademy(Long academyId) {
            return academyId != null && academyIds != null && academyIds.contains(academyId);
        }
    }
    private
    record TargetingPlan(AudienceScope scope, String targetingMode, Set<Long> recipients, String audienceSummary, Set<User.UserRole> targetedRoles) {
        private Long targetAcademyId() {
            return scope != null ? scope.campaignAcademyId() : null;
        }
        private Notification.Category category() {
            if (targetedRoles == null || targetedRoles.isEmpty()) {
                return Notification.Category.GENERAL;
            }
            if (targetedRoles.size() > 1) {
                return Notification.Category.GENERAL;
            } User.UserRole role = targetedRoles.iterator() .next();
            return
            switch (role) {
                case PARENT -> Notification.Category.PARENTS;
                case PLAYER -> Notification.Category.FOOTBALLERS;
                case TRAINER -> Notification.Category.TRAINERS;
                case ADMIN -> Notification.Category.ADMIN;
                default -> Notification.Category.GENERAL;
            };
        }
    }
}
