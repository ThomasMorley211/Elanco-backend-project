package com.example.ticktracker.service;

import com.example.ticktracker.domain.SightingRepository;
import com.example.ticktracker.web.dto.RegionCount;
import com.example.ticktracker.web.dto.TrendPoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

@Service
public class ReportService {

    private static final LocalDateTime MIN = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final LocalDateTime MAX = LocalDateTime.of(2999, 12, 31, 0, 0);

    private final SightingRepository repository;

    public ReportService(SightingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RegionCount> sightingsByRegion(LocalDate from, LocalDate to) {
        validateRange(from, to);

        Map<String, Long> totals = new TreeMap<>();
        repository.countByRegion(start(from), endExclusive(to)).forEach(r -> {
            String name = (r.getRegion() == null || r.getRegion().isBlank()) ? "Unknown" : r.getRegion();
            totals.merge(name, r.getTotal(), Long::sum);
        });

        return totals.entrySet().stream()
                .map(e -> new RegionCount(e.getKey(), e.getValue()))
                .sorted((a, b) -> Long.compare(b.sightings(), a.sightings()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrendPoint> trends(String granularity, LocalDate from, LocalDate to, String region) {
        validateRange(from, to);

        UnaryOperator<LocalDate> bucket;
        boolean weekly;
        switch (granularity == null ? "" : granularity.trim().toLowerCase(Locale.ROOT)) {
            case "weekly" -> {
                weekly = true;
                bucket = d -> d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            }
            case "monthly" -> {
                weekly = false;
                bucket = d -> d.withDayOfMonth(1);
            }
            default -> throw new IllegalArgumentException("granularity must be 'weekly' or 'monthly'.");
        }

        String regionFilter = region == null ? "" : region.trim().toLowerCase(Locale.ROOT);

        TreeMap<LocalDate, Long> counts = new TreeMap<>();
        repository.findSightingTimes(start(from), endExclusive(to), regionFilter)
                .forEach(t -> counts.merge(bucket.apply(t.toLocalDate()), 1L, Long::sum));

        if (counts.isEmpty()) {
            return List.of();
        }

        List<TrendPoint> points = new ArrayList<>();
        LocalDate last = counts.lastKey();
        for (LocalDate p = counts.firstKey(); !p.isAfter(last); p = weekly ? p.plusWeeks(1) : p.plusMonths(1)) {
            points.add(new TrendPoint(p, counts.getOrDefault(p, 0L)));
        }
        return points;
    }

    private static void validateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'.");
        }
    }

    private static LocalDateTime start(LocalDate from) {
        return from == null ? MIN : from.atStartOfDay();
    }

    private static LocalDateTime endExclusive(LocalDate to) {
        return to == null ? MAX : to.plusDays(1).atStartOfDay();
    }
}
