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
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MainRoleSchemaFix implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MainRoleSchemaFix.class);
    private final JdbcTemplate jdbcTemplate;
    public MainRoleSchemaFix(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public void run(String...args) {
        try {
            if (jdbcTemplate.getDataSource() == null) {
                return;
            }
            String databaseProductName;
            try(Connection connection = jdbcTemplate.getDataSource().getConnection()) {
                databaseProductName = connection.getMetaData().getDatabaseProductName();
            }
            String product = databaseProductName == null ? "" : databaseProductName.toLowerCase(Locale.ROOT);
            if (!product.contains("mysql") && !product.contains("mariadb")) {
                return;
            }
            String currentDatabase = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
            if (currentDatabase == null || currentDatabase.isBlank()) {
                return;
            }
            Integer columnCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'users' AND COLUMN_NAME = 'main_role'", Integer.class, currentDatabase);
            if (columnCount == null || columnCount == 0) {
                log.info("Skipping users.main_role schema fix because the column does not exist yet");
                return;
            }
            String columnType = jdbcTemplate.queryForObject("SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'users' AND COLUMN_NAME = 'main_role'", String.class, currentDatabase);
            if (columnType == null || columnType.isBlank()) {
                return;
            }
            String normalized = columnType.toLowerCase(Locale.ROOT);
            if (normalized.startsWith("enum(") && (!normalized.contains("'scouter'") || !normalized.contains("'super_admin'"))) {
                jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN main_role " + "ENUM('SUPER_ADMIN','ADMIN','PLAYER','PARENT','TRAINER','SCOUTER') NOT NULL");
                log.info("Schema fix applied: users.main_role enum now includes SUPER_ADMIN and SCOUTER");
                return;
            }
            Integer varcharLength = parseVarcharLength(normalized);
            if (varcharLength != null && varcharLength < 7) {
                jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN main_role VARCHAR(20) NOT NULL");
                log.info("Schema fix applied: users.main_role varchar length increased to 20");
            }
        } catch (Exception ex) {
            log.warn("Could not auto-fix users.main_role schema: {}", ex.getMessage());
        }
    }
    private Integer parseVarcharLength(String columnType) {
        if (!columnType.startsWith("varchar(")) {
            return null;
        }
        int open = columnType.indexOf('(');
        int close = columnType.indexOf(')', open + 1);
        if (open < 0 || close < 0) {
            return null;
        }
        try {
            return Integer.parseInt(columnType.substring(open + 1, close));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
