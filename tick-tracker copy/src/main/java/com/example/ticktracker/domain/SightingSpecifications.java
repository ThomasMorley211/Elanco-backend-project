package com.example.tickertracker.domain; // TODO: change to your package

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Each method returns null-safe "no-op" specifications when the filter is absent,
 * so they can be chained freely. Assumes Sighting has: sightedAt (LocalDateTime),
 * region (String) and location (String).
 */
public final class SightingSpecifications {

    private SightingSpecifications() {
    }

    public static Specification<Sighting> sightedOnOrAfter(LocalDate from) {
        return (root, query, cb) -> from == null ? null
                : cb.greaterThanOrEqualTo(root.get("sightedAt"), from.atStartOfDay());
    }

    /** Inclusive of the whole 'to' day. */
    public static Specification<Sighting> sightedOnOrBefore(LocalDate to) {
        return (root, query, cb) -> to == null ? null
                : cb.lessThan(root.get("sightedAt"), to.plusDays(1).atStartOfDay());
    }

    public static Specification<Sighting> regionEquals(String region) {
        return (root, query, cb) -> isBlank(region) ? null
                : cb.equal(cb.lower(root.get("region")), region.trim().toLowerCase());
    }

    public static Specification<Sighting> locationContains(String location) {
        return (root, query, cb) -> isBlank(location) ? null
                : cb.like(cb.lower(root.get("location")),
                        "%" + escapeLike(location.trim().toLowerCase()) + "%", '\\');
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
