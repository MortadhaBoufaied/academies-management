package com.footballacademy.config.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.util.Locale;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public
class NotificationCampaignSchemaFix implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(NotificationCampaignSchemaFix.
    class);
    private final JdbcTemplate jdbcTemplate;
    public NotificationCampaignSchemaFix(JdbcTemplate jdbcTemplate) {
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
            } createNotificationCampaignsIfMissing();
            addColumnIfMissing(database, "notifications", "campaign_id", "BIGINT NULL");
            addColumnIfMissing(database, "notifications", "content_html", "TEXT NULL");
            addIndexIfMissing(database, "notifications", "idx_notification_campaign_id", "campaign_id");
            addIndexIfMissing(database, "notification_campaigns", "idx_notification_campaign_academy", "academy_id");
            addIndexIfMissing(database, "notification_campaigns", "idx_notification_campaign_created_by", "created_by");
            addIndexIfMissing(database, "notification_campaigns", "idx_notification_campaign_created_at", "created_at");
            addForeignKeyIfMissing(database, "notification_campaigns", "fk_notification_campaign_academy", "academy_id", "academies", "id");
            addForeignKeyIfMissing(database, "notifications", "fk_notification_campaign", "campaign_id", "notification_campaigns", "id");
            backfillContentHtml();
        } catch (Exception ex) {
            log.warn("Could not auto-fix notification campaign schema: {}", ex.getMessage());
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
    private void createNotificationCampaignsIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS notification_campaigns (" + "id BIGINT NOT NULL AUTO_INCREMENT, " + "academy_id BIGINT NULL, " + "created_by BIGINT NOT NULL, " + "title VARCHAR(255) NOT NULL, " + "content TEXT NOT NULL, " + "content_html TEXT NULL, " + "targeting_mode VARCHAR(40) NOT NULL DEFAULT 'GENERAL', " + "audience_summary VARCHAR(255) NULL, " + "category VARCHAR(40) NOT NULL DEFAULT 'GENERAL', " + "created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), " + "PRIMARY KEY (id), " + "INDEX idx_notification_campaign_academy (academy_id), " + "INDEX idx_notification_campaign_created_by (created_by), " + "INDEX idx_notification_campaign_created_at (created_at)" + ")");
    }
    private void backfillContentHtml() {
        jdbcTemplate.update("UPDATE notifications SET content_html = content " + "WHERE content_html IS NULL AND content IS NOT NULL");
    }
    private void addColumnIfMissing(String database, String table, String column, String definition) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?", Integer.
        class, database, table, column);
        if (count == null || count > 0) {
            return;
        } jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        log.info("Schema fix applied: added {}.{}", table, column);
    }
    private void addIndexIfMissing(String database, String table, String indexName, String column) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS " + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND INDEX_NAME = ?", Integer.
        class, database, table, indexName);
        if (count == null || count > 0) {
            return;
        } jdbcTemplate.execute("CREATE INDEX " + indexName + " ON " + table + " (" + column + ")");
        log.info("Schema fix applied: added index {} on {}", indexName, table);
    }
    private void addForeignKeyIfMissing(String database, String table, String constraintName, String column, String referencedTable, String referencedColumn) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " + "WHERE CONSTRAINT_SCHEMA = ? AND TABLE_NAME = ? AND CONSTRAINT_NAME = ?", Integer.
        class, database, table, constraintName);
        if (count == null || count > 0) {
            return;
        } jdbcTemplate.execute("ALTER TABLE " + table + " ADD CONSTRAINT " + constraintName + " FOREIGN KEY (" + column + ") REFERENCES " + referencedTable + "(" + referencedColumn + ")");
        log.info("Schema fix applied: added foreign key {} on {}", constraintName, table);
    }
}
