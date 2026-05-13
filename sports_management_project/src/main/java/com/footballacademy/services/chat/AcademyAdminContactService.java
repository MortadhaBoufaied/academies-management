package com.footballacademy.services.chat;

import com.footballacademy.model.*;
import com.footballacademy.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AcademyAdminContactService {
    private final AcademyRepository academyRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final ScoutingAssignmentRepository scoutingAssignmentRepository;
    private final ChatAccessService chatAccessService;
    private final ChatRoomService chatRoomService;

    public AcademyAdminContactService(
            AcademyRepository academyRepository,
            AdminRepository adminRepository,
            UserRepository userRepository,
            ScoutingAssignmentRepository scoutingAssignmentRepository,
            ChatAccessService chatAccessService,
            ChatRoomService chatRoomService
    ) {
        this.academyRepository = academyRepository;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.scoutingAssignmentRepository = scoutingAssignmentRepository;
        this.chatAccessService = chatAccessService;
        this.chatRoomService = chatRoomService;
    }

    @Transactional
    public Map<String, Object> contactAdmin(User requester, Long requestedAcademyId) {
        if (requester == null || requester.getId() == null || requester.getMainRole() == null) {
            throw new AccessDeniedException("Authenticated user required");
        }

        Academy academy = resolveAcademy(requester, requestedAcademyId);
        User admin = responsibleAdmin(academy);

        if (admin == null) {
            throw new IllegalStateException("No academy admin or platform fallback is available");
        }

        if (!chatAccessService.canContact(requester, admin.getId())) {
            throw new AccessDeniedException("You cannot contact this academy admin");
        }

        Long conversationId = chatRoomService.ensureAcademyAdminConversation(requester.getId(), admin.getId(), academy.getId());

        return Map.of(
                "conversationId", conversationId,
                "academyId", academy.getId(),
                "adminId", admin.getId(),
                "message", "Conversation ready"
        );
    }

    public User responsibleAdmin(Academy academy) {
        if (academy == null) return platformAdminFallback();

        User owner = academy.getOwnerUser();
        if (isActiveAdmin(owner) && Objects.equals(owner.getAcademyId(), academy.getId())) {
            return owner;
        }

        List<Admin> admins = adminRepository.findByAcademy_Id(academy.getId()).stream()
                .filter(admin -> admin.getUser() != null)
                .filter(admin -> Boolean.TRUE.equals(admin.getUser().getActive()))
                .sorted(Comparator.comparingInt(this::adminPriority))
                .toList();

        if (!admins.isEmpty()) {
            return admins.get(0).getUser();
        }

        return platformAdminFallback();
    }

    private Academy resolveAcademy(User requester, Long requestedAcademyId) {
        if (requester.getMainRole() == User.UserRole.SCOUTER) {
            if (requestedAcademyId == null) {
                throw new IllegalArgumentException("academyId is required for scouters");
            }
            Academy academy = academyRepository.findById(requestedAcademyId)
                    .orElseThrow(() -> new IllegalArgumentException("Academy not found"));
            if (!canScouterContactAcademy(requester, academy)) {
                throw new AccessDeniedException("Scouter contact is not allowed for this academy");
            }
            return academy;
        }

        Academy academy = requester.getAcademy();
        if (academy == null) {
            throw new IllegalArgumentException("User is not linked to an academy");
        }
        if (requestedAcademyId != null && !Objects.equals(academy.getId(), requestedAcademyId)) {
            throw new AccessDeniedException("Users can only contact their own academy admin");
        }
        return academy;
    }

    private boolean canScouterContactAcademy(User scouterUser, Academy academy) {
        if (scouterUser == null || academy == null || scouterUser.getMainRole() != User.UserRole.SCOUTER) return false;
        if (academy.getStatus() != null && academy.getStatus() != Academy.AcademyStatus.ACTIVE) return false;
        boolean openToContact = academy.getScouterContactEnabled() == null || Boolean.TRUE.equals(academy.getScouterContactEnabled());
        if (openToContact) return true;
        return scoutingAssignmentRepository.findByScouter_IdAndAcademy_Id(scouterUser.getId(), academy.getId()).stream()
                .anyMatch(ScoutingAssignment::isActive);
    }

    private boolean isActiveAdmin(User user) {
        return user != null
                && user.getMainRole() == User.UserRole.ADMIN
                && (user.getActive() == null || Boolean.TRUE.equals(user.getActive()));
    }

    private int adminPriority(Admin admin) {
        if (admin == null || admin.getResponsibility() == null) return 99;
        return switch (admin.getResponsibility()) {
            case ACADEMY_DIRECTOR -> 1;
            case COMMUNICATIONS_MANAGER -> 2;
            case OPERATIONS_MANAGER -> 3;
            case SPORTS_COORDINATOR -> 4;
            case PLAYER_REGISTRAR -> 5;
            case FINANCE_MANAGER -> 6;
            case MEDICAL_WELFARE_MANAGER -> 7;
        };
    }

    private User platformAdminFallback() {
        return userRepository.findByMainRole(User.UserRole.SUPER_ADMIN).stream()
                .filter(user -> user.getActive() == null || Boolean.TRUE.equals(user.getActive()))
                .findFirst()
                .orElse(null);
    }
}
