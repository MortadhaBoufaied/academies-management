package com.footballacademy.util;

import java.util.List;

/**  * Simple pagination result (0-based page).  */
public
record PagedResult<T>(List<T> items, int page, int size, long totalItems, int totalPages) {
    public boolean hasPrev() {
        return page > 0;
    }
    public boolean hasNext() {
        return page <(totalPages - 1);
    }
}
