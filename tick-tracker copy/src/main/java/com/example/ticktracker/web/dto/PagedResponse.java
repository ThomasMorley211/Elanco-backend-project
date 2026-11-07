package com.example.tickertracker.web.dto; // TODO: change to your package

import org.springframework.data.domain.Page;

import java.util.List;

/** Stable paging envelope so the JSON shape doesn't depend on Spring's PageImpl. */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PagedResponse<T> from(Page<T> p) {
        return new PagedResponse<>(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages());
    }
}
