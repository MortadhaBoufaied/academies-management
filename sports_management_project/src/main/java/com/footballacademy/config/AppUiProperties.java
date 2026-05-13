package com.footballacademy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public
class AppUiProperties {
    private final Pagination pagination = new Pagination();
    private final Dashboard dashboard = new Dashboard();
    private final Webhooks webhooks = new Webhooks();
    private final Payments payments = new Payments();
    public Pagination getPagination() {
        return pagination;
    }
    public Dashboard getDashboard() {
        return dashboard;
    }
    public Webhooks getWebhooks() {
        return webhooks;
    }
    public Payments getPayments() {
        return payments;
    }
    public static
    class Pagination {
        private int itemsPerPage = AppDefaults.ITEMS_PER_PAGE;
        private int maxItemsPerPage = AppDefaults.MAX_ITEMS_PER_PAGE;
        private int maxPageLinks = AppDefaults.MAX_PAGE_LINKS;
        public int getItemsPerPage() {
            return itemsPerPage;
        }
        public void setItemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
        }
        public int getMaxItemsPerPage() {
            return maxItemsPerPage;
        }
        public void setMaxItemsPerPage(int maxItemsPerPage) {
            this.maxItemsPerPage = maxItemsPerPage;
        }
        public int getMaxPageLinks() {
            return maxPageLinks;
        }
        public void setMaxPageLinks(int maxPageLinks) {
            this.maxPageLinks = maxPageLinks;
        }
    }
    public static
    class Dashboard {
        private int recentItemsLimit = AppDefaults.DASHBOARD_RECENT_ITEMS;
        private int chartMonths = AppDefaults.DASHBOARD_CHART_MONTHS;
        private int recentWebhookLogsLimit = AppDefaults.DASHBOARD_RECENT_WEBHOOK_LOGS;
        public int getRecentItemsLimit() {
            return recentItemsLimit;
        }
        public void setRecentItemsLimit(int recentItemsLimit) {
            this.recentItemsLimit = recentItemsLimit;
        }
        public int getChartMonths() {
            return chartMonths;
        }
        public void setChartMonths(int chartMonths) {
            this.chartMonths = chartMonths;
        }
        public int getRecentWebhookLogsLimit() {
            return recentWebhookLogsLimit;
        }
        public void setRecentWebhookLogsLimit(int recentWebhookLogsLimit) {
            this.recentWebhookLogsLimit = recentWebhookLogsLimit;
        }
    }
    public static
    class Webhooks {
        private int failurePreviewLimit = AppDefaults.WEBHOOK_FAILURE_PREVIEW;
        private int detailLogsLimit = AppDefaults.WEBHOOK_DETAIL_LOGS;
        public int getFailurePreviewLimit() {
            return failurePreviewLimit;
        }
        public void setFailurePreviewLimit(int failurePreviewLimit) {
            this.failurePreviewLimit = failurePreviewLimit;
        }
        public int getDetailLogsLimit() {
            return detailLogsLimit;
        }
        public void setDetailLogsLimit(int detailLogsLimit) {
            this.detailLogsLimit = detailLogsLimit;
        }
    }
    public static
    class Payments {
        private int minAllowedYear = AppDefaults.MIN_ALLOWED_YEAR;
        private int maxAllowedYear = AppDefaults.MAX_ALLOWED_YEAR;
        public int getMinAllowedYear() {
            return minAllowedYear;
        }
        public void setMinAllowedYear(int minAllowedYear) {
            this.minAllowedYear = minAllowedYear;
        }
        public int getMaxAllowedYear() {
            return maxAllowedYear;
        }
        public void setMaxAllowedYear(int maxAllowedYear) {
            this.maxAllowedYear = maxAllowedYear;
        }
    }
}
