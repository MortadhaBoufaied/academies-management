package com.footballacademy.services.admin;

import com.footballacademy.model.User;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public
class AdminMenuService {
    public static
    class NavItem {
        private final String key;
        private final String label;
        private final String path;
        private final String icon;
        public NavItem(String key, String label, String path, String icon) {
            this.key = key;
            this.label = label;
            this.path = path;
            this.icon = icon;
        }
        public String getKey() {
            return key;
        }
        public String getLabel() {
            return label;
        }
        public String getPath() {
            return path;
        }
        public String getIcon() {
            return icon;
        }
    }
    public List<NavItem> menuFor(User user) {
        if (user == null) return List.of();
        boolean isSuperAdmin = user.hasRole("SUPER_ADMIN");
        boolean isAdmin = user.hasRole("ADMIN");
        boolean isTrainer = user.hasRole("TRAINER");
        boolean isParent = user.hasRole("PARENT");
        List<NavItem> items = new ArrayList<>();
        if (isSuperAdmin) {
            items.add(new NavItem("dashboard", "Dashboard", "/super-admin/dashboard", "bi-speedometer"));
            items.add(new NavItem("academies", "Academies", "/super-admin/academies/list", "bi-building"));
            items.add(new NavItem("sports", "Sports", "/super-admin/sports/list", "bi-trophy"));
            items.add(new NavItem("contact-admins", "Contact Admins", "/super-admin/contact-admins", "bi-envelope"));
            items.add(new NavItem("chatbot-global", "Global Chatbot", "/super-admin/chatbot-global", "bi-robot"));
            items.add(new NavItem("webhooks", "Webhooks", "/super-admin/webhooks", "bi-diagram-2"));
            items.add(new NavItem("academy-payments", "Academy Payments", "/super-admin/academy-payments", "bi-credit-card"));
            items.add(new NavItem("settings", "Settings", "/super-admin/settings", "bi-gear"));
            return items;
        } items.add(new NavItem("dashboard", "Dashboard", "/admin/view/dashboard", "bi-speedometer"));
        if (isAdmin || isTrainer) {
            items.add(new NavItem("players", "Players", "/admin/view/players", "bi-people"));
            items.add(new NavItem("trainers", "Trainers", "/admin/view/trainers", "bi-person-badge"));
            items.add(new NavItem("parents", "Parents", "/admin/view/parents", "bi-people-fill"));
            items.add(new NavItem("divisions", "Divisions", "/admin/view/divisions", "bi-diagram-3"));
            items.add(new NavItem("activities", "Activities", "/admin/view/activities", "bi-calendar-event"));
            items.add(new NavItem("calendar", "Calendar", "/admin/view/calendar", "bi-calendar3"));
        }
        if (isAdmin) {
            items.add(new NavItem("users", "Users", "/admin/view/users", "bi-person-lines-fill"));
            items.add(new NavItem("payments", "Payments", "/admin/view/payments", "bi-cash"));
            items.add(new NavItem("notifications", "Notifications", "/admin/view/notifications", "bi-bell"));
            items.add(new NavItem("academy-info", "Academy Info", "/admin/view/academy-info", "bi-gear"));
            items.add(new NavItem("subscription", "Subscription", "/admin/view/subscription", "bi-stars"));
            items.add(new NavItem("chatbot-data", "Chatbot Data", "/admin/view/chatbot-data", "bi-robot"));
        }
        if (isParent) {
            items.add(new NavItem("payments", "Payments Overview", "/admin/view/payments/overview", "bi-wallet"));
        } return items;
    }
}
