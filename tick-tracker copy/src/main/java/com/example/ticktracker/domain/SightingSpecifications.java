package com.example.ticktracker.domain;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class SightingSpecifications {

    private SightingSpecifications() {
    }

    public static Specification<TickSighting> sightedOnOrAfter(LocalDate from) {
        return (root, query, cb) -> from == null ? null
                : cb.greaterThanOrEqualTo(root.get("date"), from.atStartOfDay());
    }

    public static Specification<TickSighting> sightedOnOrBefore(LocalDate to) {
        return (root, query, cb) -> to == null ? null
                : cb.lessThan(root.get("date"), to.plusDays(1).atStartOfDay());
    }

    public static Specification<TickSighting> locationContains(String location) {
        return (root, query, cb) -> isBlank(location) ? null
                : cb.like(cb.lower(root.get("location")),
                        "%" + escapeLike(location.trim().toLowerCase()) + "%", '\\');
    }

    public static Specification<TickSighting> speciesContains(String species) {
        return (root, query, cb) -> isBlank(species) ? null
                : cb.like(cb.lower(root.get("species")),
                        "%" + escapeLike(species.trim().toLowerCase()) + "%", '\\');
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
