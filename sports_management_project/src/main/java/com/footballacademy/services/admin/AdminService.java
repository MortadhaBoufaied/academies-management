package com.footballacademy.services.admin;

import com.footballacademy.DTO.UserWithRoleDTO;
import com.footballacademy.model.AcademyInfo;
import com.footballacademy.model.ChatbotData;
import com.footballacademy.model.User;
import com.footballacademy.repository.AcademyInfoRepository;
import com.footballacademy.repository.ChatbotDataRepository;
import com.footballacademy.repository.UserRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import com.footballacademy.services.roles.RoleService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public
class AdminService {
    private final UserRepository userRepository;
    private final AcademyInfoRepository academyInfoRepository;
    private final ChatbotDataRepository chatbotDataRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AcademyAccessService academyAccessService;
    public AdminService(UserRepository userRepository, AcademyInfoRepository academyInfoRepository, ChatbotDataRepository chatbotDataRepository, RoleService roleService, PasswordEncoder passwordEncoder, AcademyAccessService academyAccessService) {
        this.userRepository = userRepository;
        this.academyInfoRepository = academyInfoRepository;
        this.chatbotDataRepository = chatbotDataRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.academyAccessService = academyAccessService;
    }
    public List<User> getAllUsers() {
        List<User> users = academyAccessService.isSuperAdmin() ? userRepository.findAll() : userRepository.findByAcademy_Id(academyAccessService.currentAcademyOrThrow() .getId());
        return users != null ? users : Collections.emptyList();
    }
    public User updateUser(Long userId, User userDetails) {
        User user = userRepository.findById(userId) .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        academyAccessService.assertCanAccessUser(user);
        user.setNom(userDetails.getNom());
        user.setDateNaiss(userDetails.getDateNaiss());
        user.setTel(userDetails.getTel());
        user.setEmail(userDetails.getEmail());
        return userRepository.save(user);
    }
    public User resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId) .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        academyAccessService.assertCanAccessUser(user);
        user.setMdp(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
    public User updateUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId) .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        academyAccessService.assertCanAccessUser(user);
        user.setActive(active);
        return userRepository.save(user);
    }
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId) .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        academyAccessService.assertCanAccessUser(user);
        userRepository.delete(user);
    }
    public AcademyInfo updateAcademyInfo(AcademyInfo academyInfo) {
        List<AcademyInfo> existingInfo = academyInfoRepository.findAll();
        AcademyInfo infoToUpdate;
        if (existingInfo.isEmpty()) {
            infoToUpdate = academyInfo;
        } else {
            infoToUpdate = existingInfo.get(0);
            infoToUpdate.setNom(academyInfo.getNom());
            infoToUpdate.setDescription(academyInfo.getDescription());
            infoToUpdate.setTotalPlayers(academyInfo.getTotalPlayers());
            infoToUpdate.setTotalCoaches(academyInfo.getTotalCoaches());
            infoToUpdate.setTopPlayers(academyInfo.getTopPlayers());
            infoToUpdate.setAchievements(academyInfo.getAchievements());
            infoToUpdate.setImageUrl(academyInfo.getImageUrl());
        }
        if (!academyAccessService.isSuperAdmin()) {
            infoToUpdate.setAcademy(academyAccessService.currentAcademyOrThrow());
        } return academyInfoRepository.save(infoToUpdate);
    }
    public ChatbotData uploadChatbotData(ChatbotData chatbotData) {
        if (!academyAccessService.isSuperAdmin()) {
            chatbotData.setScope(ChatbotData.Scope.ACADEMY);
            chatbotData.setAcademy(academyAccessService.currentAcademyOrThrow());
        } return chatbotDataRepository.save(chatbotData);
    }
    public List<ChatbotData> getAllChatbotData() {
        List<ChatbotData> data = academyAccessService.isSuperAdmin() ? chatbotDataRepository.findAll() : chatbotDataRepository.findByScopeAndAcademy_IdOrderByUploadedAtDesc(ChatbotData.Scope.ACADEMY, academyAccessService.currentAcademyOrThrow() .getId());
        return data != null ? data : Collections.emptyList();
    }
    public void deleteChatbotData(Long dataId) {
        ChatbotData data = chatbotDataRepository.findById(dataId) .orElseThrow(() -> new RuntimeException("Chatbot data not found with id: " + dataId));
        if (!academyAccessService.isSuperAdmin()) {
            if (data.getScope() != ChatbotData.Scope.ACADEMY || !academyAccessService.canAccessAcademy(data.getAcademy())) {
                throw new AccessDeniedException("You cannot delete chatbot data from another scope");
            }
        } chatbotDataRepository.delete(data);
    }
    public AcademyInfo getAcademyInfo() {
        List<AcademyInfo> info = academyAccessService.isSuperAdmin() ? academyInfoRepository.findAll() : academyInfoRepository.findAll() .stream() .filter(item -> item.getAcademy() != null && item.getAcademy() .getId() .equals(academyAccessService.currentAcademyOrThrow() .getId())) .toList();
        if (info.isEmpty()) {
            throw new RuntimeException("Academy info not found");
        } return info.get(0);
    }
    public UserWithRoleDTO getUserWithRoleEntity(Long userId) {
        User user = userRepository.findById(userId) .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        academyAccessService.assertCanAccessUser(user);
        Object roleEntity = roleService.getRoleSpecificEntity(user);
        return new UserWithRoleDTO(user, user.getMainRole() .name(), roleEntity);
    }
}
