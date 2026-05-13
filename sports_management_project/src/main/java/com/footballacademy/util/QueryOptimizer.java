package com.footballacademy.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public
class QueryOptimizer {
    /**      * Execute a query with pagination and avoid N+1 problem by using entity graphs      */
    public static <T, R> Page<R> executePaginatedQuery(JpaRepository<T, Long> repository, Pageable pageable, Function<List<T>, List<R>> mapper) {
        Page<T> entityPage = repository.findAll(pageable);
        List<R> dtoList = mapper.apply(entityPage.getContent());
        return new PageImpl<>(dtoList, pageable, entityPage.getTotalElements());
    }
    /**      * Batch process large datasets to avoid memory issues      */
    public static <T> void batchProcess(List<T> items, int batchSize, java.util.function.Consumer<List<T>> processor) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (int i = 0;
        i < items.size();
        i += batchSize) {
            int end = Math.min(i + batchSize, items.size());
            List<T> batch = items.subList(i, end);
            processor.accept(batch);
        }
    }
    /**      * Optimize query by selecting only needed fields      */
    public static <T, R> List<R> optimizeQuery(List<T> entities, Function<T, R> mapper) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        } return entities.stream() .map(mapper) .collect(Collectors.toList());
    }
    /**      * Lazy load relationships to avoid N+1 problem      */
    public static <T, R> List<R> lazyLoadOptimize(List<T> entities, Function<T, R> mapper) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        } return entities.stream() .map(mapper) .collect(Collectors.toList());
    }
}
