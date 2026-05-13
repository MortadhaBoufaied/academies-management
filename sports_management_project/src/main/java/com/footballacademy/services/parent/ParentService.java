package com.footballacademy.services.parent;

import com.footballacademy.DTO.ParentCombinedDTO;
import com.footballacademy.model.Parent;
import com.footballacademy.model.Payment;
import com.footballacademy.model.Player;
import com.footballacademy.model.User;
import com.footballacademy.repository.ParentRepository;
import com.footballacademy.repository.PaymentRepository;
import com.footballacademy.repository.PlayerRepository;
import com.footballacademy.services.academy.AcademyAccessService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.footballacademy.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public
class ParentService {
    private final ParentRepository parentRepository;
    private final PlayerRepository playerRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final AcademyAccessService academyAccessService;
    public ParentService(ParentRepository parentRepository, PlayerRepository playerRepository, PaymentRepository paymentRepository, UserRepository userRepository, AcademyAccessService academyAccessService) {
        this.parentRepository = parentRepository;
        this.playerRepository = playerRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.academyAccessService = academyAccessService;
    }
    public List<Parent> getAllParents() {
        List<Parent> parents = academyAccessService.isSuperAdmin() ? parentRepository.findAll() : parentRepository.findByAcademy_Id(academyAccessService.currentAcademyOrThrow() .getId());
        return parents != null ? parents : Collections.emptyList();
    }
    @Transactional(readOnly = true)
    public List<ParentCombinedDTO> getAllParentsCombined() {
        List<Parent> parents = academyAccessService.isSuperAdmin() ? parentRepository.findAllWithUser() : parentRepository.findByAcademyIdWithUser(academyAccessService.currentAcademyOrThrow() .getId());
        if (parents == null || parents.isEmpty()) return Collections.emptyList();
        return parents.stream() .map(p -> {
            User u = p.getUser();
            Long userId =(u != null ? u.getId() : p.getId());
            int childrenCount =(p.getChildren() != null ? p.getChildren() .size() : 0);
            return new ParentCombinedDTO(p.getId(), userId,(u != null ? u.getNom() : null),(u != null ? u.getEmail() : null),(u != null ? u.getTel() : null), childrenCount);
        }) .toList();
    }
    public Parent getParentById(Long id) {
        Parent parent = parentRepository.findByIdWithUser(id) .orElseThrow(() -> new RuntimeException("Parent not found with id: " + id));
        assertVisible(parent);
        return parent;
    }
    public Parent createParent(Parent parent) {
        if (!academyAccessService.isSuperAdmin() || parent.getAcademy() == null) {
            parent.setAcademy(academyAccessService.academyForWrite(parent.getAcademy()));
        } return parentRepository.save(parent);
    }
    public Parent updateParent(Long id, Parent parentDetails) {
        Parent parent = getParentById(id);
        if (parentDetails.getUser() != null) {
            parent.setUser(parentDetails.getUser());
        }
        if (parentDetails.getChildren() != null) {
            parent.setChildren(parentDetails.getChildren());
        } return parentRepository.save(parent);
    }
    public void deleteParent(Long id) {
        Parent parent = getParentById(id);
        parentRepository.delete(parent);
    }
    public List<Player> getChildren(Long id) {
        Parent parent = getParentById(id);
        List<Player> children = parent.getChildren();
        return children != null ? children : Collections.emptyList();
    }
    public void addChild(Long parentId, Long playerId) {
        Parent parent = getParentById(parentId);
        Player player = playerRepository.findById(playerId) .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        assertSameAcademy(parent, player);
        parent.addChild(player);
        parentRepository.save(parent);
    }
    public void removeChild(Long parentId, Long playerId) {
        Parent parent = getParentById(parentId);
        Player player = playerRepository.findById(playerId) .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        assertSameAcademy(parent, player);
        parent.removeChild(player);
        parentRepository.save(parent);
    }
    public Payment payForPlayer(Long parentId, Long playerId, Payment payment) {
        Parent parent = getParentById(parentId);
        Player player = playerRepository.findById(playerId) .orElseThrow(() -> new RuntimeException("Player not found with id: " + playerId));
        assertSameAcademy(parent, player);
        if (player.getParent() == null || !player.getParent() .getId() .equals(parent.getId())) {
            throw new RuntimeException("Player does not belong to this parent");
        } payment.setParentId(parent.getId());
        payment.setPlayerId(player.getId());
        payment.setParent(parent);
        payment.setPlayer(player);
        payment.setAcademy(parent.getAcademy());
        payment.setPaid(true);
        Payment savedPayment = paymentRepository.save(payment);
        player.setPaid(true);
        playerRepository.save(player);
        return savedPayment;
    }
    public List<Payment> getPayments(Long parentId) {
        Parent parent = getParentById(parentId);
        List<Payment> payments = paymentRepository.findByParentId(parent.getId()) .stream() .filter(this::isPaymentVisible) .toList();
        return payments != null ? payments : Collections.emptyList();
    }
    public List<Payment> getPendingPayments(Long parentId) {
        Parent parent = getParentById(parentId);
        List<Payment> payments = paymentRepository.findByIsPaidFalseAndParentId(parent.getId()) .stream() .filter(this::isPaymentVisible) .toList();
        return payments != null ? payments : Collections.emptyList();
    }
    public Long findUserIdByEmail(String email) {
        return userRepository.findByEmail(email) .map(u -> u.getId()) .orElse(null);
    }
    private void assertVisible(Parent parent) {
        if (academyAccessService.isSuperAdmin()) {
            return;
        }
        if (parent == null || !academyAccessService.canAccessAcademy(parent.getAcademy())) {
            throw new AccessDeniedException("You cannot access another academy's parent");
        }
    }
    private void assertSameAcademy(Parent parent, Player player) {
        assertVisible(parent);
        if (academyAccessService.isSuperAdmin()) {
            return;
        }
        if (player == null || !academyAccessService.canAccessAcademy(player.getAcademy())) {
            throw new AccessDeniedException("Cannot use a player from another academy");
        }
    }
    private boolean isPaymentVisible(Payment payment) {
        return payment == null || academyAccessService.isSuperAdmin() || academyAccessService.canAccessAcademy(payment.getAcademy());
    }
}
