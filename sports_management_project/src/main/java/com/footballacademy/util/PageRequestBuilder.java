package com.footballacademy.util;

import com.footballacademy.config.AppDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public
class PageRequestBuilder {
    public static Pageable createPageRequest(int page, int size) {
        return createPageRequest(page, size, null, null);
    }
    public static Pageable createPageRequest(int page, int size, String sortBy, String sortDirection) {
        // Validate and normalize page and size         page =
        Math.max(0, page);
        size = Math.min(Math.max(1, size), AppDefaults.MAX_ITEMS_PER_PAGE);
        // Handle sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = parseSortDirection(sortDirection);
            return PageRequest.of(page, size, Sort.by(direction, sortBy));
        } return PageRequest.of(page, size);
    }
    public static Pageable createPageRequest(int page, int size, Sort sort) {
        page = Math.max(0, page);
        size = Math.min(Math.max(1, size), AppDefaults.MAX_ITEMS_PER_PAGE);
        return PageRequest.of(page, size, sort);
    }
    public static Pageable createUnsortedPageRequest(int page, int size) {
        page = Math.max(0, page);
        size = Math.min(Math.max(1, size), AppDefaults.MAX_ITEMS_PER_PAGE);
        return PageRequest.of(page, size);
    }
    private static Sort.Direction parseSortDirection(String direction) {
        if (direction == null || direction.isEmpty()) {
            return Sort.Direction.ASC;
        }
        switch (direction.toLowerCase()) {
            case "desc" : case "descending" : return Sort.Direction.DESC;
            case "asc" : case "ascending" : default: return Sort.Direction.ASC;
        }
    }
    public static
    class PageMetadata {
        private final int currentPage;
        private final int pageSize;
        private final long totalElements;
        private final int totalPages;
        private final boolean isFirst;
        private final boolean isLast;
        private final boolean hasNext;
        private final boolean hasPrevious;
        public PageMetadata(Page<?> page) {
            this.currentPage = page.getNumber();
            this.pageSize = page.getSize();
            this.totalElements = page.getTotalElements();
            this.totalPages = page.getTotalPages();
            this.isFirst = page.isFirst();
            this.isLast = page.isLast();
            this.hasNext = page.hasNext();
            this.hasPrevious = page.hasPrevious();
        }
        // Getters
        public int getCurrentPage() {
            return currentPage;
        }
        public int getPageSize() {
            return pageSize;
        }
        public long getTotalElements() {
            return totalElements;
        }
        public int getTotalPages() {
            return totalPages;
        }
        public boolean isFirst() {
            return isFirst;
        }
        public boolean isLast() {
            return isLast;
        }
        public boolean hasNext() {
            return hasNext;
        }
        public boolean hasPrevious() {
            return hasPrevious;
        }
    }
}
