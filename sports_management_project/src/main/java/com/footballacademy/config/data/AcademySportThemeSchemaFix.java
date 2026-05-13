package com.footballacademy.config.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public
class AcademySportThemeSchemaFix implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AcademySportThemeSchemaFix.
    class);
    private final JdbcTemplate jdbcTemplate;
    public AcademySportThemeSchemaFix(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public void run(String...args) {
        try {
            if (!isMySql()) {
                return;
            } String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.
            class);
            if (database == null || database.isBlank()) {
                return;
            } createSportThemesTableIfMissing();
            createSportsTableIfMissing();
            createSportCategoriesTableIfMissing();
            createAcademiesTableIfMissing();
            createDivisionsTableIfMissing();
            createActivitiesTableIfMissing();
            createPaymentsTableIfMissing();
            createAcademyPaymentsTableIfMissing();
            createWebhooksTableIfMissing();
            createWebhookLogsTableIfMissing();
            addColumnIfMissing(database, "users", "academy_id", "BIGINT NULL");
            addColumnIfMissing(database, "admins", "academy_id", "BIGINT NULL");
            addColumnIfMissing(database, "admins", "responsibility", "VARCHAR(60) NULL");
            addColumnIfMissing(database, "academy_info", "academy_id", "BIGINT NULL");
            addColumnIfMissing(database, "academies", "sport_id", "BIGINT NULL");
            addColumnIfMissing(database, "academies", "subscription_offer", "VARCHAR(40) NOT NULL DEFAULT 'REGULAR'");
            addColumnIfMissing(database, "academies", "subscription_payment_status", "VARCHAR(40) NOT NULL DEFAULT 'PENDING'");
            addColumnIfMissing(database, "academies", "subscription_activated_at", "DATETIME(6) NULL");
            addColumnIfMissing(database, "academies", "subscription_updated_at", "DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6)");
            addColumnIfMissing(database, "academies", "owner_user_id", "BIGINT NULL");
            addColumnIfMissing(database, "sports", "theme_id", "BIGINT NULL");
            addColumnIfMissing(database, "activities", "academy_id", "BIGINT NULL");
            addPaymentColumns(database);
            addRoleEntityColumns(database);
            addNotificationColumns(database);
            addConversationColumns(database);
            addChatbotColumns(database);
            addDivisionColumns(database);
            seedDefaultHierarchy();
            backfillAcademySport(database);
            backfillSportTheme(database);
            backfillDivisionSport();
            assignDefaultAcademyToUsers();
            backfillAdmins();
            backfillAcademyInfo();
            backfillActivitiesAcademy();
            backfillPaymentsAcademyAndDefaults();
            backfillAcademySubscriptions();
            backfillRoleEntityAcademies();
            backfillNotificationAndConversationScopes();
            backfillChatbotScopes();
            repairBrokenReferences();
            mergeDuplicateAcademyInfoRows();
            addIndexIfMissing(database, "users", "idx_user_academy", "academy_id");
            addIndexIfMissing(database, "admins", "idx_admin_academy", "academy_id");
            addIndexIfMissing(database, "academy_info", "idx_academy_info_academy", "academy_id");
            addUniqueIndexIfMissing(database, "academy_info", "uk_academy_info_academy", "academy_id");
            addIndexIfMissing(database, "academies", "idx_academy_sport", "sport_id");
            addIndexIfMissing(database, "academies", "idx_academy_subscription_offer", "subscription_offer");
            addIndexIfMissing(database, "academies", "idx_academy_owner_user", "owner_user_id");
            addIndexIfMissing(database, "sports", "idx_sport_theme", "theme_id");
            addIndexIfMissing(database, "divisions", "idx_division_sport_id", "sport_id");
            addIndexIfMissing(database, "divisions", "idx_division_academy", "academy_id");
            addIndexIfMissing(database, "divisions", "idx_division_category_id", "category_id");
            addIndexIfMissing(database, "activities", "idx_activity_academy", "academy_id");
            addIndexIfMissing(database, "payments", "idx_payment_academy", "academy_id");
            addIndexIfMissing(database, "academy_payments", "idx_academy_payment_academy", "academy_id");
            addIndexIfMissing(database, "academy_payments", "idx_academy_payment_status", "status");
            addIndexIfMissing(database, "academy_payments", "idx_academy_payment_created", "created_at");
            addIndexIfMissing(database, "webhooks", "idx_webhook_event_type", "event_type");
            addIndexIfMissing(database, "webhooks", "idx_webhook_active", "is_active");
            addIndexIfMissing(database, "webhook_logs", "idx_webhook_log_webhook", "webhook_id");
            addIndexIfMissing(database, "webhook_logs", "idx_webhook_log_event", "event_type");
            addIndexIfMissing(database, "webhook_logs", "idx_webhook_log_executed", "executed_at");
            addIndexIfMissing(database, "players", "idx_player_academy", "academy_id");
            addIndexIfMissing(database, "parents", "idx_parent_academy", "academy_id");
            addIndexIfMissing(database, "trainers", "idx_trainer_academy", "academy_id");
            addIndexIfMissing(database, "notifications", "idx_notification_academy", "academy_id");
            addIndexIfMissing(database, "conversations", "idx_conversation_academy", "academy_id");
            addIndexIfMissing(database, "chatbot_data", "idx_chatbot_academy", "academy_id");
            addForeignKeyIfMissing(database, "users", "fk_user_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "admins", "fk_admin_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "academy_info", "fk_academy_info_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "academies", "fk_academy_sport", "sport_id", "sports", "id");
            addForeignKeyIfMissing(database, "academies", "fk_academy_owner_user", "owner_user_id", "users", "id");
            addForeignKeyIfMissing(database, "sports", "fk_sport_theme", "theme_id", "sport_themes", "id");
            addForeignKeyIfMissing(database, "sport_themes", "fk_sport_theme_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "sport_themes", "fk_sport_theme_sport", "sport_id", "sports", "id");
            addForeignKeyIfMissing(database, "sport_categories", "fk_sport_category_sport", "sport_id", "sports", "id");
            addForeignKeyIfMissing(database, "divisions", "fk_division_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "divisions", "fk_division_sport", "sport_id", "sports", "id");
            addForeignKeyIfMissing(database, "divisions", "fk_division_sport_category", "category_id", "sport_categories", "id");
            addForeignKeyIfMissing(database, "activities", "fk_activity_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "payments", "fk_payment_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "academy_payments", "fk_academy_payment_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "webhook_logs", "fk_log_webhook", "webhook_id", "webhooks", "id");
            addForeignKeyIfMissing(database, "players", "fk_player_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "parents", "fk_parent_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "trainers", "fk_trainer_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "notifications", "fk_notification_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "conversations", "fk_conversation_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "chatbot_data", "fk_chatbot_data_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "chatbot_data", "fk_chatbot_data_sport", "sport_id", "sports", "id");
            addForeignKeyIfMissing(database, "chatbot_data", "fk_chatbot_data_uploaded_by", "uploaded_by", "users", "id");
        } catch (Exception ex) {
            log.warn("Could not auto-fix academy/sport/theme schema: {}", ex.getMessage());
        }
    }
    private boolean isMySql() throws Exception {
        if (jdbcTemplate.getDataSource() == null) {
            return false;
        }
        try(Connection connection = jdbcTemplate.getDataSource() .getConnection()) {
            String product = connection.getMetaData() .getDatabaseProductName();
            String normalized = product == null ? "" : product.toLowerCase(Locale.ROOT);
            return normalized.contains("mysql") || normalized.contains("mariadb");
        }
    }
    private void createSportThemesTableIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sport_themes (" + "id BIGINT NOT NULL AUTO_INCREMENT, " + "scope VARCHAR(40) NOT NULL, " + "academy_id BIGINT NULL, " + "sport_id BIGINT NULL, " + "primary_color VARCHAR(255) NULL, " + "secondary_color VARCHAR(255) NULL, " + "background_color VARCHAR(255) NULL, " + "accent_color VARCHAR(255) NULL, " + "text_color VARCHAR(255) NULL, " + "logo_url VARCHAR(1000) NULL, " + "default_player_image_url VARCHAR(1000) NULL, " + "default_trainer_image_url VARCHAR(1000) NULL, " + "default_parent_image_url VARCHAR(1000) NULL, " + "default_admin_image_url VARCHAR(1000) NULL, " + "home_banner_url VARCHAR(1000) NULL, " + "splash_image_url VARCHAR(1000) NULL, " + "card_style VARCHAR(255) NULL, " + "font_family VARCHAR(255) NULL, " + "button_style VARCHAR(255) NULL, " + "icon_style VARCHAR(255) NULL, " + "version INT NOT NULL DEFAULT 1, " + "created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), " + "updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), " + "PRIMARY KEY (id), " + "INDEX idx_sport_theme_scope (scope), " + "INDEX idx_sport_theme_academy (academy_id), " + "INDEX idx_sport_theme_sport (sport_id)" + ")");
    }
    private void createSportsTableIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sports (" + "id BIGINT NOT NULL AUTO_INCREMENT, " + "code VARCHAR(255) NOT NULL, " + "name VARCHAR(255) NOT NULL, " + "description VARCHAR(255) NULL, " + "is_active BIT(1) NOT NULL DEFAULT b'1', " + "display_order INT NOT NULL DEFAULT 0, " + "theme_id BIGINT NULL, " + "PRIMARY KEY (id), " + "UNIQUE KEY uk_sports_code (code), " + "INDEX idx_sport_theme (theme_id)" + ")");
    }
    private void createSportCategoriesTableIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sport_categories (" + "id BIGINT NOT NULL AUTO_INCREMENT, " + "code VARCHAR(255) NOT NULL, " + "name VARCHAR(255) NOT NULL, " + "description VARCHAR(255) NULL, " + "sport_id BIGINT NULL, " + "is_active BIT(1) NOT NULL DEFAULT b'1', " + "display_order INT NOT NULL DEFAULT 0, " + "PRIMARY KEY (id), " + "INDEX idx_sport_category_code (code), " + "INDEX idx_sport_category_sport (sport_id), " + "INDEX idx_sport_category_active (is_active)" + ")");
    }
    private void createAcademiesTableIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS academies (" + "id BIGINT NOT NULL AUTO_INCREMENT, " + "name VARCHAR(255) NOT NULL, " + "slug VARCHAR(255) NOT NULL, " + "email VARCHAR(255) NULL, " + "phone VARCHAR(255) NULL, " + "address VARCHAR(255) NULL, " + "city VARCHAR(255) NULL, " + "country VARCHAR(255) NULL, " + "status VARCHAR(40) NOT NULL DEFAULT 'ACTIVE', " + "logo_url VARCHAR(1000) NULL, " + "sport_id BIGINT NULL, " + "created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), " + "updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), " + "PRIMARY KEY (id), " + "UNIQUE KEY uk_academies_slug (slug), " + "INDEX idx_academy_slug (slug), " + "INDEX idx_academy_status (status), " + "INDEX idx_academy_city (city), " + "INDEX idx_academy_sport (sport_id)" + ")");
    }
    private void createDivisionsTableIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS divisions (" + "id BIGINT NOT NULL AUTO_INCREMENT, " + "nom VARCHAR(255) NOT NULL, " + "categorie VARCHAR(255) NULL, " + "academy_id BIGINT NULL, " + "sport_id BIGINT NULL, " + "category_id BIGINT NULL, " + "min_age INT NULL, " + "max_age INT NULL, " + "gender VARCHAR(255) NULL, " + "level VARCHAR(255) NULL, " + "min_weight DECIMAL(6,2) NULL, " + "max_weight DECIMAL(6,2) NULL, " + "competition_scope VARCHAR(255) NULL, " + "display_order INT NULL DEFAULT 0, " + "is_active BIT(1) NULL DEFAULT b'1', " + "PRIMARY KEY (id), " + "INDEX idx_division_academy (academy_id), " + "INDEX idx_division_sport_id (sport_id), " + "INDEX idx_division_category_id (category_id), " + "INDEX idx_division_categorie (categorie), " + "INDEX idx_division_nom (nom)" + ")");
    }
    private void createActivitiesTableIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS activities (" + "id BIGINT NOT NULL AUTO_INCREMENT, " + "trainer_id BIGINT NULL, " + "academy_id BIGINT NULL, " + "titre VARCHAR(255) NOT NULL, " + "description VARCHAR(255) NULL, " + "date DATE NOT NULL, " + "lieu VARCHAR(255) NULL, " + "PRIMARY KEY (id), " + "INDEX idx_activity_trainer_id (trainer_id), " + "INDEX idx_activity_academy (academy_id), " + "INDEX idx_activity_date (date), " + "INDEX idx_activity_titre (titre), " + "INDEX idx_activity_lieu (lieu)" + ")");
    }
    private void createPaymentsTableIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS payments (" + "id BIGINT NOT NULL AUTO_INCREMENT, " + "montant DOUBLE NULL, " + "mois DATE NOT NULL, " + "is_paid BIT(1) NULL DEFAULT b'0', " + "player_id BIGINT NULL, " + "parent_id BIGINT NULL, " + "academy_id BIGINT NULL, " + "currency VARCHAR(3) NULL DEFAULT 'USD', " + "payment_type VARCHAR(50) NULL DEFAULT 'MONTHLY_FEE', " + "due_date DATE NULL, " + "status VARCHAR(20) NULL DEFAULT 'PENDING', " + "description VARCHAR(500) NULL, " + "created_at DATETIME(6) NULL, " + "updated_at DATETIME(6) NULL, " + "completed_at DATETIME(6) NULL, " + "failed_reason VARCHAR(500) NULL, " + "version BIGINT NULL DEFAULT 0, " + "PRIMARY KEY (id), " + "INDEX idx_payment_academy (academy_id), " + "INDEX idx_payment_player (player_id), " + "INDEX idx_payment_parent (parent_id), " + "INDEX idx_payment_status (status), " + "INDEX idx_payment_due_date (due_date)" + ")");
    }
    private void createAcademyPaymentsTableIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS academy_payments (" + "id BIGINT NOT NULL AUTO_INCREMENT, " + "academy_id BIGINT NOT NULL, " + "offer_code VARCHAR(40) NOT NULL DEFAULT 'REGULAR', " + "amount DECIMAL(10,2) NOT NULL DEFAULT 0.00, " + "currency VARCHAR(3) NOT NULL DEFAULT 'TND', " + "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', " + "payment_method VARCHAR(30) NOT NULL DEFAULT 'MANUAL', " + "reference_code VARCHAR(100) NULL, " + "notes VARCHAR(500) NULL, " + "due_date DATE NULL, " + "paid_at DATETIME(6) NULL, " + "created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), " + "updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), " + "PRIMARY KEY (id), " + "INDEX idx_academy_payment_academy (academy_id), " + "INDEX idx_academy_payment_status (status), " + "INDEX idx_academy_payment_created (created_at)" + ")");
    }
    private void createWebhooksTableIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS webhooks (" + "id BIGINT NOT NULL AUTO_INCREMENT, " + "name VARCHAR(255) NOT NULL, " + "url VARCHAR(255) NOT NULL, " + "event_type VARCHAR(255) NOT NULL, " + "is_active BIT(1) NOT NULL DEFAULT b'1', " + "http_method VARCHAR(255) NOT NULL, " + "headers VARCHAR(255) NULL, " + "authentication VARCHAR(255) NULL, " + "created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), " + "updated_at DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6), " + "last_triggered_at DATETIME(6) NULL, " + "trigger_count INT NULL DEFAULT 0, " + "PRIMARY KEY (id), " + "UNIQUE KEY uk_webhook_name (name)" + ")");
    }
    private void createWebhookLogsTableIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS webhook_logs (" + "id BIGINT NOT NULL AUTO_INCREMENT, " + "webhook_id BIGINT NULL, " + "event_type VARCHAR(255) NOT NULL, " + "payload TEXT NOT NULL, " + "status_code INT NOT NULL, " + "response_body TEXT NULL, " + "success BIT(1) NOT NULL DEFAULT b'0', " + "error_message VARCHAR(255) NULL, " + "executed_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), " + "response_time_ms BIGINT NULL, " + "PRIMARY KEY (id)" + ")");
    }
    private void addPaymentColumns(String database) {
        addColumnIfMissing(database, "payments", "academy_id", "BIGINT NULL");
        addColumnIfMissing(database, "payments", "currency", "VARCHAR(3) NULL DEFAULT 'USD'");
        addColumnIfMissing(database, "payments", "payment_type", "VARCHAR(50) NULL DEFAULT 'MONTHLY_FEE'");
        addColumnIfMissing(database, "payments", "due_date", "DATE NULL");
        addColumnIfMissing(database, "payments", "status", "VARCHAR(20) NULL DEFAULT 'PENDING'");
        addColumnIfMissing(database, "payments", "description", "VARCHAR(500) NULL");
        addColumnIfMissing(database, "payments", "created_at", "DATETIME(6) NULL");
        addColumnIfMissing(database, "payments", "updated_at", "DATETIME(6) NULL");
        addColumnIfMissing(database, "payments", "completed_at", "DATETIME(6) NULL");
        addColumnIfMissing(database, "payments", "failed_reason", "VARCHAR(500) NULL");
        addColumnIfMissing(database, "payments", "version", "BIGINT NULL DEFAULT 0");
    }
    private void addRoleEntityColumns(String database) {
        addColumnIfMissing(database, "players", "academy_id", "BIGINT NULL");
        addColumnIfMissing(database, "players", "sport_id", "BIGINT NULL");
        addColumnIfMissing(database, "players", "sport_position_id", "BIGINT NULL");
        addColumnIfMissing(database, "players", "custom_stats", "TEXT NULL");
        addColumnIfMissing(database, "parents", "academy_id", "BIGINT NULL");
        addColumnIfMissing(database, "trainers", "academy_id", "BIGINT NULL");
    }
    private void addNotificationColumns(String database) {
        addColumnIfMissing(database, "notifications", "academy_id", "BIGINT NULL");
        addColumnIfMissing(database, "notifications", "category", "VARCHAR(40) NULL");
        addColumnIfMissing(database, "notifications", "created_by", "BIGINT NULL");
        addColumnIfMissing(database, "notifications", "read_at", "DATETIME(6) NULL");
        addColumnIfMissing(database, "notifications", "is_scheduled", "BIT(1) NULL DEFAULT b'0'");
        addColumnIfMissing(database, "notifications", "scheduled_for", "DATETIME(6) NULL");
        addColumnIfMissing(database, "notifications", "conversation_id", "BIGINT NULL");
    }
    private void addConversationColumns(String database) {
        addColumnIfMissing(database, "conversations", "academy_id", "BIGINT NULL");
    }
    private void addChatbotColumns(String database) {
        addColumnIfMissing(database, "chatbot_data", "scope", "VARCHAR(40) NULL");
        addColumnIfMissing(database, "chatbot_data", "academy_id", "BIGINT NULL");
        addColumnIfMissing(database, "chatbot_data", "sport_id", "BIGINT NULL");
        addColumnIfMissing(database, "chatbot_data", "source_type", "VARCHAR(40) NULL");
        addColumnIfMissing(database, "chatbot_data", "uploaded_by", "BIGINT NULL");
    }
    private void addDivisionColumns(String database) {
        addColumnIfMissing(database, "divisions", "academy_id", "BIGINT NULL");
        addColumnIfMissing(database, "divisions", "sport_id", "BIGINT NULL");
        addColumnIfMissing(database, "divisions", "category_id", "BIGINT NULL");
        addColumnIfMissing(database, "divisions", "min_age", "INT NULL");
        addColumnIfMissing(database, "divisions", "max_age", "INT NULL");
        addColumnIfMissing(database, "divisions", "gender", "VARCHAR(255) NULL");
        addColumnIfMissing(database, "divisions", "level", "VARCHAR(255) NULL");
        addColumnIfMissing(database, "divisions", "min_weight", "DECIMAL(6,2) NULL");
        addColumnIfMissing(database, "divisions", "max_weight", "DECIMAL(6,2) NULL");
        addColumnIfMissing(database, "divisions", "competition_scope", "VARCHAR(255) NULL");
        addColumnIfMissing(database, "divisions", "display_order", "INT NULL DEFAULT 0");
        addColumnIfMissing(database, "divisions", "is_active", "BIT(1) NULL DEFAULT b'1'");
    }
    private void seedDefaultHierarchy() {
        jdbcTemplate.update("INSERT INTO sport_themes " + "(scope, primary_color, secondary_color, background_color, accent_color, text_color, " + "logo_url, default_player_image_url, default_trainer_image_url, default_parent_image_url, default_admin_image_url, " + "card_style, font_family, button_style, icon_style, version, created_at, updated_at) " + "SELECT 'PLATFORM_DEFAULT', '#0f766e', '#065f46', '#f8fafc', '#f59e0b', '#0f172a', " + "'/uploads/defaults/player.jpg', '/uploads/defaults/player.jpg', '/uploads/defaults/player.jpg', " + "'/uploads/defaults/player.jpg', '/uploads/defaults/player.jpg', 'compact', 'Public Sans', 'solid', 'rounded', 1, NOW(6), NOW(6) " + "WHERE NOT EXISTS (SELECT 1 FROM sport_themes WHERE scope = 'PLATFORM_DEFAULT')");
        jdbcTemplate.update("INSERT INTO sports (code, name, description, is_active, display_order, theme_id) " + "SELECT 'FOOTBALL', 'Football', 'Association football', b'1', 1, " + "(SELECT id FROM sport_themes WHERE scope = 'PLATFORM_DEFAULT' ORDER BY id LIMIT 1) " + "WHERE NOT EXISTS (SELECT 1 FROM sports WHERE code = 'FOOTBALL')");
        jdbcTemplate.update("UPDATE sports SET theme_id = COALESCE(theme_id, " + "(SELECT id FROM sport_themes WHERE scope = 'PLATFORM_DEFAULT' ORDER BY id LIMIT 1)) " + "WHERE code = 'FOOTBALL'");
        jdbcTemplate.update("UPDATE sport_themes SET sport_id = COALESCE(sport_id, " + "(SELECT id FROM sports WHERE code = 'FOOTBALL' ORDER BY id LIMIT 1)) " + "WHERE scope = 'PLATFORM_DEFAULT'");
        jdbcTemplate.update("INSERT INTO academies (name, slug, email, phone, address, city, country, status, logo_url, sport_id, created_at, updated_at) " + "SELECT 'Default Sport Academy', 'default-academy', 'academy@sportacademy.local', '+21600000000', " + "'Main Campus', 'Tunis', 'Tunisia', 'ACTIVE', '/uploads/defaults/player.jpg', " + "(SELECT id FROM sports WHERE code = 'FOOTBALL' ORDER BY id LIMIT 1), NOW(6), NOW(6) " + "WHERE NOT EXISTS (SELECT 1 FROM academies WHERE slug = 'default-academy')");
        jdbcTemplate.update("UPDATE academies SET sport_id = COALESCE(sport_id, " + "(SELECT id FROM sports WHERE code = 'FOOTBALL' ORDER BY id LIMIT 1)) " + "WHERE slug = 'default-academy'");
    }
    private void assignDefaultAcademyToUsers() {
        jdbcTemplate.update("UPDATE users SET academy_id = " + "(SELECT id FROM academies WHERE slug = 'default-academy' ORDER BY id LIMIT 1) " + "WHERE academy_id IS NULL AND (main_role IS NULL OR main_role <> 'SUPER_ADMIN')");
    }
    private void backfillAdmins() {
        jdbcTemplate.update("UPDATE admins a JOIN users u ON u.id = a.user_id " + "SET a.academy_id = COALESCE(a.academy_id, u.academy_id)");
        jdbcTemplate.update("UPDATE admins SET responsibility = 'OPERATIONS_MANAGER' " + "WHERE responsibility IS NULL OR responsibility = '' OR responsibility NOT IN (" + "'ACADEMY_DIRECTOR', 'OPERATIONS_MANAGER', 'SPORTS_COORDINATOR', 'PLAYER_REGISTRAR', " + "'FINANCE_MANAGER', 'COMMUNICATIONS_MANAGER', 'MEDICAL_WELFARE_MANAGER')");
    }
    private void backfillAcademyInfo() {
        jdbcTemplate.update("UPDATE academy_info SET academy_id = " + "(SELECT id FROM academies WHERE slug = 'default-academy' ORDER BY id LIMIT 1) " + "WHERE academy_id IS NULL");
    }
    private void backfillActivitiesAcademy() {
        jdbcTemplate.update("UPDATE activities a " + "JOIN trainers t ON t.user_id = a.trainer_id " + "SET a.academy_id = t.academy_id " + "WHERE a.academy_id IS NULL AND t.academy_id IS NOT NULL");
        jdbcTemplate.update("UPDATE activities SET academy_id = " + "(SELECT id FROM academies WHERE slug = 'default-academy' ORDER BY id LIMIT 1) " + "WHERE academy_id IS NULL");
    }
    private void backfillPaymentsAcademyAndDefaults() {
        jdbcTemplate.update("UPDATE payments p " + "JOIN players pl ON pl.user_id = p.player_id " + "SET p.academy_id = pl.academy_id " + "WHERE p.academy_id IS NULL AND pl.academy_id IS NOT NULL");
        jdbcTemplate.update("UPDATE payments p " + "JOIN parents pa ON pa.user_id = p.parent_id " + "SET p.academy_id = pa.academy_id " + "WHERE p.academy_id IS NULL AND pa.academy_id IS NOT NULL");
        jdbcTemplate.update("UPDATE payments SET academy_id = " + "(SELECT id FROM academies WHERE slug = 'default-academy' ORDER BY id LIMIT 1) " + "WHERE academy_id IS NULL");
        jdbcTemplate.update("UPDATE payments SET currency = 'USD' WHERE currency IS NULL OR currency = ''");
        jdbcTemplate.update("UPDATE payments SET payment_type = 'MONTHLY_FEE' WHERE payment_type IS NULL OR payment_type = ''");
        jdbcTemplate.update("UPDATE payments SET due_date = mois WHERE due_date IS NULL AND mois IS NOT NULL");
        jdbcTemplate.update("UPDATE payments SET status = CASE WHEN is_paid = b'1' THEN 'PAID' ELSE 'PENDING' END " + "WHERE status IS NULL OR status = ''");
        jdbcTemplate.update("UPDATE payments SET created_at = NOW(6) WHERE created_at IS NULL");
        jdbcTemplate.update("UPDATE payments SET updated_at = NOW(6) WHERE updated_at IS NULL");
        jdbcTemplate.update("UPDATE payments SET version = 0 WHERE version IS NULL");
        jdbcTemplate.update("UPDATE payments SET completed_at = updated_at " + "WHERE completed_at IS NULL AND status = 'PAID'");
    }
    private void backfillAcademySubscriptions() {
        jdbcTemplate.update("UPDATE academies SET subscription_offer = 'PRO' " + "WHERE subscription_offer IS NULL OR subscription_offer = ''");
        jdbcTemplate.update("UPDATE academies SET subscription_payment_status = 'PAID' " + "WHERE subscription_payment_status IS NULL OR subscription_payment_status = ''");
        jdbcTemplate.update("UPDATE academies SET subscription_offer = 'PRO', subscription_payment_status = 'PAID' " + "WHERE owner_user_id IS NULL AND subscription_payment_status = 'PENDING'");
        jdbcTemplate.update("UPDATE academies SET subscription_updated_at = NOW(6) " + "WHERE subscription_updated_at IS NULL");
        jdbcTemplate.update("UPDATE academies SET subscription_activated_at = NOW(6) " + "WHERE subscription_payment_status = 'PAID' AND subscription_activated_at IS NULL");
        jdbcTemplate.update("UPDATE academies a " + "JOIN users u ON LOWER(u.email) = LOWER(a.email) AND u.academy_id = a.id AND u.main_role = 'ADMIN' " + "SET a.owner_user_id = COALESCE(a.owner_user_id, u.id) " + "WHERE a.owner_user_id IS NULL");
        jdbcTemplate.update("UPDATE academies a " + "JOIN (" + "  SELECT u.academy_id, MIN(u.id) AS user_id " + "  FROM users u " + "  WHERE u.main_role = 'ADMIN' AND u.academy_id IS NOT NULL " + "  GROUP BY u.academy_id" + ") owners ON owners.academy_id = a.id " + "SET a.owner_user_id = COALESCE(a.owner_user_id, owners.user_id) " + "WHERE a.owner_user_id IS NULL");
        jdbcTemplate.update("INSERT INTO academy_payments (academy_id, offer_code, amount, currency, status, payment_method, reference_code, notes, due_date, paid_at, created_at, updated_at) " + "SELECT a.id, a.subscription_offer, " + "CASE WHEN a.subscription_offer = 'PRO' THEN 349.00 ELSE 149.00 END, " + "'TND', " + "CASE WHEN a.subscription_payment_status = 'PAID' THEN 'PAID' ELSE 'PENDING' END, " + "'MANUAL', " + "CONCAT(UPPER(REPLACE(a.slug, '-', '_')), '-LEGACY'), " + "'Backfilled academy subscription', " + "CURDATE(), " + "CASE WHEN a.subscription_payment_status = 'PAID' THEN NOW(6) ELSE NULL END, " + "NOW(6), NOW(6) " + "FROM academies a " + "WHERE NOT EXISTS (SELECT 1 FROM academy_payments ap WHERE ap.academy_id = a.id)");
    }
    private void backfillRoleEntityAcademies() {
        jdbcTemplate.update("UPDATE players p " + "JOIN users u ON u.id = p.user_id " + "SET p.academy_id = u.academy_id " + "WHERE p.academy_id IS NULL AND u.academy_id IS NOT NULL");
        jdbcTemplate.update("UPDATE parents p " + "JOIN users u ON u.id = p.user_id " + "SET p.academy_id = u.academy_id " + "WHERE p.academy_id IS NULL AND u.academy_id IS NOT NULL");
        jdbcTemplate.update("UPDATE trainers t " + "JOIN users u ON u.id = t.user_id " + "SET t.academy_id = u.academy_id " + "WHERE t.academy_id IS NULL AND u.academy_id IS NOT NULL");
    }
    private void backfillNotificationAndConversationScopes() {
        jdbcTemplate.update("UPDATE notifications n " + "JOIN users u ON u.id = n.user_id " + "SET n.academy_id = u.academy_id " + "WHERE n.academy_id IS NULL AND u.academy_id IS NOT NULL");
        jdbcTemplate.update("UPDATE notifications SET category = 'GENERAL' " + "WHERE category IS NULL OR category = ''");
        jdbcTemplate.update("UPDATE notifications SET created_by = 1 " + "WHERE created_by IS NULL");
        jdbcTemplate.update("UPDATE conversations c " + "JOIN divisions d ON d.id = c.division_id " + "SET c.academy_id = d.academy_id " + "WHERE c.academy_id IS NULL AND d.academy_id IS NOT NULL");
    }
    private void backfillChatbotScopes() {
        jdbcTemplate.update("UPDATE chatbot_data SET scope = 'GLOBAL' WHERE scope IS NULL OR scope = ''");
        jdbcTemplate.update("UPDATE chatbot_data SET source_type = 'MANUAL' WHERE source_type IS NULL OR source_type = ''");
        jdbcTemplate.update("UPDATE chatbot_data cd " + "JOIN users u ON u.id = cd.uploaded_by " + "SET cd.academy_id = u.academy_id " + "WHERE cd.academy_id IS NULL AND u.academy_id IS NOT NULL");
    }
    private void repairBrokenReferences() {
        jdbcTemplate.update("UPDATE users u " + "LEFT JOIN academies a ON a.id = u.academy_id " + "SET u.academy_id = (SELECT id FROM academies WHERE slug = 'default-academy' ORDER BY id LIMIT 1) " + "WHERE u.academy_id IS NOT NULL AND a.id IS NULL");
        jdbcTemplate.update("UPDATE academies a " + "LEFT JOIN sports s ON s.id = a.sport_id " + "SET a.sport_id = (SELECT id FROM sports WHERE code = 'FOOTBALL' ORDER BY id LIMIT 1) " + "WHERE a.sport_id IS NULL OR s.id IS NULL");
        jdbcTemplate.update("UPDATE sports s " + "LEFT JOIN sport_themes t ON t.id = s.theme_id " + "SET s.theme_id = (SELECT id FROM sport_themes WHERE scope = 'PLATFORM_DEFAULT' ORDER BY id LIMIT 1) " + "WHERE s.theme_id IS NULL OR t.id IS NULL");
        jdbcTemplate.update("UPDATE sport_themes t " + "LEFT JOIN sports s ON s.id = t.sport_id " + "SET t.sport_id = (SELECT id FROM sports WHERE code = 'FOOTBALL' ORDER BY id LIMIT 1) " + "WHERE t.sport_id IS NOT NULL AND s.id IS NULL");
        jdbcTemplate.update("UPDATE sport_themes t " + "LEFT JOIN academies a ON a.id = t.academy_id " + "SET t.academy_id = NULL " + "WHERE t.academy_id IS NOT NULL AND a.id IS NULL");
        jdbcTemplate.update("UPDATE divisions d " + "LEFT JOIN sports s ON s.id = d.sport_id " + "SET d.sport_id = (SELECT id FROM sports WHERE code = 'FOOTBALL' ORDER BY id LIMIT 1) " + "WHERE d.sport_id IS NULL OR s.id IS NULL");
        jdbcTemplate.update("UPDATE divisions d " + "LEFT JOIN academies a ON a.id = d.academy_id " + "SET d.academy_id = NULL " + "WHERE d.academy_id IS NOT NULL AND a.id IS NULL");
        jdbcTemplate.update("UPDATE divisions d " + "LEFT JOIN sport_categories c ON c.id = d.category_id " + "SET d.category_id = NULL " + "WHERE d.category_id IS NOT NULL AND c.id IS NULL");
        jdbcTemplate.update("UPDATE admins a " + "LEFT JOIN academies ac ON ac.id = a.academy_id " + "SET a.academy_id = NULL " + "WHERE a.academy_id IS NOT NULL AND ac.id IS NULL");
        jdbcTemplate.update("UPDATE academies a " + "LEFT JOIN users u ON u.id = a.owner_user_id " + "SET a.owner_user_id = NULL " + "WHERE a.owner_user_id IS NOT NULL AND u.id IS NULL");
        jdbcTemplate.update("UPDATE academy_info ai " + "LEFT JOIN academies ac ON ac.id = ai.academy_id " + "SET ai.academy_id = NULL " + "WHERE ai.academy_id IS NOT NULL AND ac.id IS NULL");
    }
    private void mergeDuplicateAcademyInfoRows() {
        List<Map<String, Object>> duplicateGroups = jdbcTemplate.queryForList("SELECT academy_id, MIN(id) AS keep_id " + "FROM academy_info " + "WHERE academy_id IS NOT NULL " + "GROUP BY academy_id " + "HAVING COUNT(*) > 1");
        for (Map<String, Object> group : duplicateGroups) {
            Long academyId =((Number) group.get("academy_id")) .longValue();
            Long keepId =((Number) group.get("keep_id")) .longValue();
            List<Long> duplicateIds = jdbcTemplate.queryForList("SELECT id FROM academy_info WHERE academy_id = ? AND id <> ? ORDER BY id", Long.
            class, academyId, keepId);
            for (Long duplicateId : duplicateIds) {
                jdbcTemplate.update("INSERT INTO academy_info_divisions (academy_info_id, division_ids) " + "SELECT ?, d.division_ids " + "FROM academy_info_divisions d " + "WHERE d.academy_info_id = ? " + "AND NOT EXISTS (" + "  SELECT 1 FROM academy_info_divisions existing " + "  WHERE existing.academy_info_id = ? AND existing.division_ids = d.division_ids" + ")", keepId, duplicateId, keepId);
                jdbcTemplate.update("DELETE FROM academy_info_divisions WHERE academy_info_id = ?", duplicateId);
                jdbcTemplate.update("DELETE FROM academy_info WHERE id = ?", duplicateId);
                log.info("Schema fix applied: merged duplicate academy_info {} into {} for academy {}", duplicateId, keepId, academyId);
            }
        }
    }
    private void addColumnIfMissing(String database, String table, String column, String definition) {
        if (!tableExists(database, table)) {
            return;
        } Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?", Integer.
        class, database, table, column);
        if (count == null || count > 0) {
            return;
        } jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        log.info("Schema fix applied: added {}.{}", table, column);
    }
    private void backfillAcademySport(String database) {
        if (!tableExists(database, "academy_sports")) {
            return;
        } jdbcTemplate.execute("UPDATE academies a " + "JOIN (" + "  SELECT academy_id, MIN(sport_id) AS sport_id " + "  FROM academy_sports " + "  GROUP BY academy_id" + ") s ON s.academy_id = a.id " + "SET a.sport_id = COALESCE(a.sport_id, s.sport_id)");
    }
    private void backfillSportTheme(String database) {
        if (!tableExists(database, "sport_themes")) {
            return;
        } jdbcTemplate.execute("UPDATE sports s " + "JOIN (" + "  SELECT sport_id, MAX(id) AS theme_id " + "  FROM sport_themes " + "  WHERE sport_id IS NOT NULL " + "  GROUP BY sport_id" + ") t ON t.sport_id = s.id " + "SET s.theme_id = COALESCE(s.theme_id, t.theme_id)");
    }
    private void backfillDivisionSport() {
        jdbcTemplate.update("UPDATE divisions SET sport_id = " + "(SELECT id FROM sports WHERE code = 'FOOTBALL' ORDER BY id LIMIT 1) " + "WHERE sport_id IS NULL");
        jdbcTemplate.update("UPDATE divisions SET display_order = 0 WHERE display_order IS NULL");
        jdbcTemplate.update("UPDATE divisions SET is_active = b'1' WHERE is_active IS NULL");
    }
    private void addIndexIfMissing(String database, String table, String indexName, String column) {
        if (!tableExists(database, table)) {
            return;
        } Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS " + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND INDEX_NAME = ?", Integer.
        class, database, table, indexName);
        if (count == null || count > 0) {
            return;
        } jdbcTemplate.execute("CREATE INDEX " + indexName + " ON " + table + " (" + column + ")");
        log.info("Schema fix applied: added index {} on {}", indexName, table);
    }
    private void addUniqueIndexIfMissing(String database, String table, String indexName, String column) {
        if (!tableExists(database, table)) {
            return;
        } Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS " + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND INDEX_NAME = ?", Integer.
        class, database, table, indexName);
        if (count == null || count > 0) {
            return;
        } jdbcTemplate.execute("CREATE UNIQUE INDEX " + indexName + " ON " + table + " (" + column + ")");
        log.info("Schema fix applied: added unique index {} on {}", indexName, table);
    }
    private void addForeignKeyIfMissing(String database, String table, String constraintName, String column, String referencedTable, String referencedColumn) {
        if (!tableExists(database, table) || !tableExists(database, referencedTable)) {
            return;
        } Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " + "WHERE CONSTRAINT_SCHEMA = ? AND TABLE_NAME = ? AND CONSTRAINT_NAME = ?", Integer.
        class, database, table, constraintName);
        if (count == null || count > 0) {
            return;
        } jdbcTemplate.execute("ALTER TABLE " + table + " ADD CONSTRAINT " + constraintName + " FOREIGN KEY (" + column + ") REFERENCES " + referencedTable + "(" + referencedColumn + ")");
        log.info("Schema fix applied: added foreign key {} on {}", constraintName, table);
    }
    private boolean tableExists(String database, String table) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?", Integer.
        class, database, table);
        return count != null && count > 0;
    }
}
