package com.footballacademy.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public
class PageMapper {
    /**      * Convert a page of entities to a page of DTOs      */
    public static <E, D> Page<D> toDtoPage(Page<E> entityPage, Function<E, D> mapper) {
        List<D> dtoList = entityPage.getContent() .stream() .map(mapper) .collect(Collectors.toList());
        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }
    /**      * Convert a list of entities to a page of DTOs      */
    public static <E, D> Page<D> toDtoPage(List<E> entities, Pageable pageable, Function<E, D> mapper) {
        int start =(int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), entities.size());
        if (start >= entities.size()) {
            return new PageImpl<>(List.of(), pageable, entities.size());
        } List<E> pageContent = entities.subList(start, end);
        List<D> dtoList = pageContent.stream() .map(mapper) .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, entities.size());
    }
    /**      * Convert a page of entities to a page of DTOs with custom total count      */
    public static <E, D> Page<D> toDtoPage(List<E> entities, Pageable pageable, long totalElements, Function<E, D> mapper) {
        int start =(int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), entities.size());
        if (start >= entities.size()) {
            return new PageImpl<>(List.of(), pageable, totalElements);
        } List<E> pageContent = entities.subList(start, end);
        List<D> dtoList = pageContent.stream() .map(mapper) .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, totalElements);
    }
    /**      * Create an empty page      */
    public static <T> Page<T> emptyPage(Pageable pageable) {
        return new PageImpl<>(List.of(), pageable, 0);
    }
    /**      * Create a single-item page      */
    public static <T> Page<T> singleItemPage(T item, Pageable pageable) {
        return new PageImpl<>(List.of(item), pageable, 1);
    }
}
