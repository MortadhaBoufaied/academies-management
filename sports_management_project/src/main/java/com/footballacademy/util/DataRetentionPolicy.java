package com.footballacademy.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public
class DataRetentionPolicy {
    public
    enum RetentionPeriod {
        ONE_DAY(1), ONE_WEEK(7), ONE_MONTH(30), THREE_MONTHS(90), SIX_MONTHS(180), ONE_YEAR(365), TWO_YEARS(730), FIVE_YEARS(1825), PERMANENT(Integer.MAX_VALUE);
        private final int days;
        RetentionPeriod(int days) {
            this.days = days;
        }
        public int getDays() {
            return days;
        }
    }
    public static
    class DataCleanupPolicy {
        private final String dataType;
        private final RetentionPeriod retentionPeriod;
        private final boolean anonymizeBeforeDelete;
        private final int batchSize;
        public DataCleanupPolicy(String dataType, RetentionPeriod retentionPeriod, boolean anonymizeBeforeDelete, int batchSize) {
            this.dataType = dataType;
            this.retentionPeriod = retentionPeriod;
            this.anonymizeBeforeDelete = anonymizeBeforeDelete;
            this.batchSize = batchSize;
        }
        public String getDataType() {
            return dataType;
        }
        public RetentionPeriod getRetentionPeriod() {
            return retentionPeriod;
        }
        public boolean shouldAnonymizeBeforeDelete() {
            return anonymizeBeforeDelete;
        }
        public boolean isAnonymizeBeforeDelete() {
            return anonymizeBeforeDelete;
        }
        public int getBatchSize() {
            return batchSize;
        }
    }
    private static final Map<String, DataCleanupPolicy> DEFAULT_POLICIES = Map.of("audit_logs", new DataCleanupPolicy("audit_logs", RetentionPeriod.ONE_YEAR, true, 1000), "notifications", new DataCleanupPolicy("notifications", RetentionPeriod.SIX_MONTHS, false, 500), "payment_transactions", new DataCleanupPolicy("payment_transactions", RetentionPeriod.FIVE_YEARS, true, 100), "webhook_logs", new DataCleanupPolicy("webhook_logs", RetentionPeriod.ONE_MONTH, false, 200), "temp_files", new DataCleanupPolicy("temp_files", RetentionPeriod.ONE_DAY, false, 50), "user_sessions", new DataCleanupPolicy("user_sessions", RetentionPeriod.ONE_MONTH, false, 200));
    public static DataCleanupPolicy getPolicy(String dataType) {
        return DEFAULT_POLICIES.getOrDefault(dataType, new DataCleanupPolicy(dataType, RetentionPeriod.ONE_YEAR, false, 100));
    }
    public static void addPolicy(String dataType, DataCleanupPolicy policy) {
        // In a real implementation, this would be stored in a database
        DEFAULT_POLICIES.put(dataType, policy);
    }
    public static boolean shouldRetain(String dataType, java.time.LocalDateTime createdAt) {
        DataCleanupPolicy policy = getPolicy(dataType);
        java.time.LocalDateTime cutoffDate = java.time.LocalDateTime.now() .minusDays(policy.getRetentionPeriod() .getDays());
        return createdAt.isAfter(cutoffDate);
    }
    public static java.time.LocalDateTime getCutoffDate(String dataType) {
        DataCleanupPolicy policy = getPolicy(dataType);
        return java.time.LocalDateTime.now() .minusDays(policy.getRetentionPeriod() .getDays());
    }
    public static List<String> getDataTypesWithPolicies() {
        return DEFAULT_POLICIES.keySet() .stream() .collect(Collectors.toList());
    }
    public static Map<String, DataCleanupPolicy> getAllPolicies() {
        return Map.copyOf(DEFAULT_POLICIES);
    }
}
