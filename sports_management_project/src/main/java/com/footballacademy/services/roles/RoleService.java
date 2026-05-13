package com.footballacademy.services.roles;

import com.footballacademy.model.Admin;
import com.footballacademy.model.Parent;
import com.footballacademy.model.Player;
import com.footballacademy.model.Trainer;
import com.footballacademy.model.User;
import com.footballacademy.repository.AdminRepository;
import com.footballacademy.repository.ParentRepository;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.repository.TrainerRepository;
import com.footballacademy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public
class RoleService {
    private final PlayerRepository playerRepository;
    private final ParentRepository parentRepository;
    private final TrainerRepository trainerRepository;
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    public RoleService(PlayerRepository playerRepository, ParentRepository parentRepository, TrainerRepository trainerRepository, AdminRepository adminRepository, UserRepository userRepository) {
        this.playerRepository = playerRepository;
        this.parentRepository = parentRepository;
        this.trainerRepository = trainerRepository;
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public void createRoleSpecificEntity(User user) {
        if (user == null || user.getId() == null || user.getMainRole() == null) {
            throw new IllegalArgumentException("User or role is null");
        }
        // ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ IMPORTANT:         // Ensure the User is MANAGED in the current persistence context
        User managedUser = userRepository.findById(user.getId()) .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));
        switch (managedUser.getMainRole()) {
            case PLAYER -> {
                if (!playerRepository.existsById(managedUser.getId())) {
                    Player player = new Player();
                    player.setUser(managedUser);
                    player.setAcademy(managedUser.getAcademy());
                    playerRepository.save(player);
                }
            } case PARENT -> {
                if (!parentRepository.existsById(managedUser.getId())) {
                    Parent parent = new Parent();
                    parent.setUser(managedUser);
                    parent.setAcademy(managedUser.getAcademy());
                    parentRepository.save(parent);
                }
            } case TRAINER -> {
                if (!trainerRepository.existsById(managedUser.getId())) {
                    Trainer trainer = new Trainer();
                    trainer.setUser(managedUser);
                    trainer.setAcademy(managedUser.getAcademy());
                    trainerRepository.save(trainer);
                }
            } case ADMIN -> {
                if (!adminRepository.existsById(managedUser.getId())) {
                    Admin admin = new Admin();
                    admin.setUser(managedUser);
                    admin.setAcademy(managedUser.getAcademy());
                    admin.setResponsibility(Admin.AdminResponsibility.OPERATIONS_MANAGER);
                    adminRepository.save(admin);
                }
            } case SUPER_ADMIN -> {
                // SUPER_ADMIN is platform-scoped and does not need an academy table row.
            } case SCOUTER -> {
                // SCOUTER is authentication/authorization-only for now.                 // No dedicated role-specific entity table is required.
            }
        }
    }
    public Object getRoleSpecificEntity(User user) {
        if (user == null || user.getId() == null || user.getMainRole() == null) {
            return null;
        }
        switch (user.getMainRole()) {
            case PLAYER: return playerRepository.findById(user.getId()) .orElse(null);
            case PARENT: return parentRepository.findById(user.getId()) .orElse(null);
            case TRAINER: return trainerRepository.findById(user.getId()) .orElse(null);
            case ADMIN: return adminRepository.findById(user.getId()) .orElse(null);
            case SUPER_ADMIN: case SCOUTER: return null;
            default: return null;
        }
    }
}
