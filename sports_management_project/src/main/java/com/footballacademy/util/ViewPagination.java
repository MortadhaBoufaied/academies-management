package com.footballacademy.util;

import java.util.List;

public
class ViewPagination<T> {
    private final List<T> items;
    private final int currentPage;
    private final int totalPages;
    private final long totalItems;
    private final int pageSize;
    private final int fromItem;
    private final int toItem;
    private final boolean hasPrevious;
    private final boolean hasNext;
    private final String previousHref;
    private final String nextHref;
    private final List<PageLink> pageLinks;
    public ViewPagination(List<T> items, int currentPage, int totalPages, long totalItems, int pageSize, int fromItem, int toItem, boolean hasPrevious, boolean hasNext, String previousHref, String nextHref, List<PageLink> pageLinks) {
        this.items = items;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
        this.fromItem = fromItem;
        this.toItem = toItem;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
        this.previousHref = previousHref;
        this.nextHref = nextHref;
        this.pageLinks = pageLinks;
    }
    public List<T> getItems() {
        return items;
    }
    public int getCurrentPage() {
        return currentPage;
    }
    public int getTotalPages() {
        return totalPages;
    }
    public long getTotalItems() {
        return totalItems;
    }
    public int getPageSize() {
        return pageSize;
    }
    public int getFromItem() {
        return fromItem;
    }
    public int getToItem() {
        return toItem;
    }
    public boolean isHasPrevious() {
        return hasPrevious;
    }
    public boolean isHasNext() {
        return hasNext;
    }
    public String getPreviousHref() {
        return previousHref;
    }
    public String getNextHref() {
        return nextHref;
    }
    public List<PageLink> getPageLinks() {
        return pageLinks;
    }
    public static
    class PageLink {
        private final String label;
        private final String href;
        private final boolean active;
        public PageLink(String label, String href, boolean active) {
            this.label = label;
            this.href = href;
            this.active = active;
        }
        public String getLabel() {
            return label;
        }
        public String getHref() {
            return href;
        }
        public boolean isActive() {
            return active;
        }
    }
}
