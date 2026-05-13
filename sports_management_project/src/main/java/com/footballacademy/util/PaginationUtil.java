package com.footballacademy.util;

import com.footballacademy.config.AppDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.List;

public final
class PaginationUtil {
    private static final int DEFAULT_PAGE_SIZE = AppDefaults.ITEMS_PER_PAGE;
    private static final int MAX_PAGE_SIZE = AppDefaults.MAX_ITEMS_PER_PAGE;
    private PaginationUtil() {
    }
    public static <T> PagedResult<T> paginate(List<T> all, int page, int size) {
        size = validateSize(size);
        page = validatePage(page);
        if (all == null) {
            return new PagedResult<>(Collections.emptyList(), page, size, 0, 0);
        } long totalItems = all.size();
        int totalPages = calculateTotalPages((int) totalItems, size);
        if (totalPages == 0) {
            return new PagedResult<>(Collections.emptyList(), page, size, 0, 0);
        }
        if (page >= totalPages) {
            page = totalPages - 1;
        } int from = page * size;
        int to = Math.min(from + size, all.size());
        List<T> slice =(from >= 0 && from < to) ? all.subList(from, to) : Collections.emptyList();
        return new PagedResult<>(slice, page, size, totalItems, totalPages);
    }
    public static <T> Page<T> createPage(List<T> items, Pageable pageable) {
        int start =(int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), items.size());
        if (start >= items.size()) {
            return new PageImpl<>(List.of(), pageable, items.size());
        } List<T> pageContent = items.subList(start, end);
        return new PageImpl<>(pageContent, pageable, items.size());
    }
    public static <T> Page<T> createPage(List<T> items, int page, int size) {
        size = validateSize(size);
        page = validatePage(page);
        int start = page * size;
        int end = Math.min(start + size, items.size());
        if (start >= items.size()) {
            return new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(page, size), items.size());
        } List<T> pageContent = items.subList(start, end);
        return new PageImpl<>(pageContent, org.springframework.data.domain.PageRequest.of(page, size), items.size());
    }
    public static int calculateTotalPages(int totalItems, int pageSize) {
        return(int) Math.ceil((double) totalItems / pageSize);
    }
    public static int validatePage(int page) {
        return Math.max(0, page);
    }
    public static int validateSize(int size) {
        return Math.min(Math.max(1, size), MAX_PAGE_SIZE);
    }
    public static int validateSize(int size, int maxSize) {
        return Math.min(Math.max(1, size), maxSize);
    }
    public static int getDefaultPageSize() {
        return DEFAULT_PAGE_SIZE;
    }
    public static int getMaxPageSize() {
        return MAX_PAGE_SIZE;
    }
}
