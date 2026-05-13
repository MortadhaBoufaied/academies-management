package com.footballacademy.services.dashboard;

import com.footballacademy.DTO.dashboard.*;
import com.footballacademy.model.Activity;
import com.footballacademy.model.Payment;
import com.footballacademy.model.Player;
import com.footballacademy.model.Trainer;
import com.footballacademy.model.User;
import com.footballacademy.repository.*;
import com.footballacademy.services.activity.ActivityService;
import com.footballacademy.services.parent.ParentService;
import com.footballacademy.services.PaymentService;
import com.footballacademy.services.trainer.TrainerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public
class DashboardService {
    private final ParentService parentService;
    private final TrainerService trainerService;
    private final ActivityService activityService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final TrainerRepository trainerRepository;
    private final ParentRepository parentRepository;
    private final DivisionRepository divisionRepository;
    public DashboardService(ParentService parentService, TrainerService trainerService, ActivityService activityService, PaymentService paymentService, UserRepository userRepository, PlayerRepository playerRepository, TrainerRepository trainerRepository, ParentRepository parentRepository, DivisionRepository divisionRepository) {
        this.parentService = parentService;
        this.trainerService = trainerService;
        this.activityService = activityService;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.trainerRepository = trainerRepository;
        this.parentRepository = parentRepository;
        this.divisionRepository = divisionRepository;
    }
    public ParentDashboardDTO parentDashboard(Long parentId) {
        List<Player> children = parentService.getChildren(parentId);
        if (children == null) children = List.of();
        List<ChildStatsDTO> childDtos = new ArrayList<>();
        for (Player p : children) {
            if (p == null) continue;
            // ÃƒÆ’Ã‚Â¢Ãƒâ€¦Ã¢â‚¬Å“ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ Correct way: player name comes from linked User
            String name = resolvePlayerName(p);
            Integer goals = safeInt(p.getGoals());
            Integer assists = safeInt(p.getAssists());
            Integer matches = safeInt(p.getMatches());
            Double avg = safeDouble(p.getAverageRating());
            Long divId = p.getDivision() != null ? p.getDivision() .getId() : null;
            childDtos.add(new ChildStatsDTO(p.getId(), name, divId, goals, assists, matches, avg));
        } List<Payment> pending = parentService.getPendingPayments(parentId);
        if (pending == null) pending = List.of();
        double total = 0;
        for (Payment p : pending) {
            if (p == null) continue;
            try {
                total += p.getMontant();
            } catch (Exception ignored) {
            }
        } return new ParentDashboardDTO(parentId, childDtos.size(), total, pending.size(), childDtos);
    }
    public TrainerDashboardDTO trainerDashboard(Long trainerId) {
        Trainer t = trainerService.getTrainerById(trainerId);
        Long divId = t.getDivision() != null ? t.getDivision() .getId() : null;
        String divName = divId != null ? divisionRepository.findById(divId) .map(d -> d.getNom()) .orElse(null) : null;
        int playersCount = 0;
        if (divId != null) {
            try {
                playersCount = playerRepository.findByDivisionId(divId) .size();
            } catch (Exception ignored) {
            }
        }
        // Activities for trainer filtered to this month
        List<Activity> all = trainerService.getTrainerActivities(trainerId);
        if (all == null) all = List.of();
        LocalDate now = LocalDate.now();
        int ym = now.getYear() * 100 + now.getMonthValue();
        List<ActivitySummaryDTO> list = new ArrayList<>();
        for (Activity a : all) {
            if (a == null || a.getDate() == null) continue;
            int aym = a.getDate() .getYear() * 100 + a.getDate() .getMonthValue();
            if (aym != ym) continue;
            list.add(new ActivitySummaryDTO(a.getId(), a.getTitre(), a.getDate() .toString(), a.getLieu(), a.getType()));
        } return new TrainerDashboardDTO(trainerId, divId, divName, playersCount, list.size(), list);
    }
    public AdminDashboardDTO adminDashboard() {
        long users = userRepository.count();
        long players = playerRepository.count();
        long trainers = trainerRepository.count();
        long parents = parentRepository.count();
        long divisions = divisionRepository.count();
        long activities = 0;
        try {
            var all = activityService.getAllActivities();
            activities = all != null ? all.size() : 0;
        } catch (Exception ignored) {
        } double monthlyRevenue = 0;
        try {
            monthlyRevenue = paymentService.getMonthlyRevenue();
        } catch (Exception ignored) {
        } long pendingPayments = 0;
        long overduePayments = 0;
        try {
            pendingPayments = paymentService.getPendingPayments() .size();
        } catch (Exception ignored) {
        }
        try {
            overduePayments = paymentService.getOverduePayments() .size();
        } catch (Exception ignored) {
        } return new AdminDashboardDTO(users, players, trainers, parents, divisions, activities, monthlyRevenue, pendingPayments, overduePayments);
    }
    // ------------------------------------------------------------     // Helpers     // ------------------------------------------------------------
    private String resolvePlayerName(Player p) {
        try {
            User u = p.getUser();
            if (u != null) {
                if (u.getNom() != null && !u.getNom() .isBlank()) return u.getNom();
                if (u.getEmail() != null && !u.getEmail() .isBlank()) return u.getEmail();
            }
        } catch (Exception ignored) {
        } return "Player #" +(p.getId() != null ? p.getId() : "?");
    }
    private Integer safeInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return l.intValue();
        if (v instanceof Double d) return d.intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (Exception e) {
            return 0;
        }
    }
    private Double safeDouble(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Double d) return d;
        if (v instanceof Integer i) return i.doubleValue();
        if (v instanceof Long l) return l.doubleValue();
        try {
            return Double.parseDouble(v.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
