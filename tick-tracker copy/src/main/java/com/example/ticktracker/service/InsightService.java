package com.example.ticktracker.service;

import com.example.ticktracker.domain.SightingRepository;
import com.example.ticktracker.web.dto.RegionInsight;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class InsightService {

    private final SightingRepository repository;

    public InsightService(SightingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RegionInsight> regionTrends(LocalDate asOf, int days, int minSightings) {
        LocalDate endDay = asOf;
        if (endDay == null) {
            LocalDateTime latest = repository.findLatestSightingTime();
            if (latest == null) {
                return List.of();
            }
            endDay = latest.toLocalDate();
        }

        LocalDateTime windowEnd = endDay.plusDays(1).atStartOfDay();
        LocalDateTime currentStart = windowEnd.minusDays(days);
        LocalDateTime previousStart = currentStart.minusDays(days);

        Map<String, Long> current = totals(currentStart, windowEnd);
        Map<String, Long> previous = totals(previousStart, currentStart);

        Set<String> regions = new HashSet<>(current.keySet());
        regions.addAll(previous.keySet());

        return regions.stream()
                .map(region -> build(region,
                        current.getOrDefault(region, 0L),
                        previous.getOrDefault(region, 0L),
                        minSightings))
                .sorted(Comparator.comparingLong(RegionInsight::change).reversed()
                        .thenComparing(RegionInsight::region))
                .toList();
    }

    private Map<String, Long> totals(LocalDateTime from, LocalDateTime to) {
        Map<String, Long> result = new HashMap<>();
        repository.countByRegion(from, to).forEach(r -> {
            String name = (r.getRegion() == null || r.getRegion().isBlank()) ? "Unknown" : r.getRegion();
            result.merge(name, r.getTotal(), Long::sum);
        });
        return result;
    }

    private RegionInsight build(String region, long current, long previous, int minSightings) {
        long change = current - previous;
        Double percentChange = previous == 0
                ? null
                : Math.round(change * 1000.0 / previous) / 10.0;

        String status;
        if (current >= minSightings && (previous == 0 || current >= previous * 2)) {
            status = "SPIKE";
        } else if (current > previous) {
            status = "RISING";
        } else if (current < previous) {
            status = "FALLING";
        } else {
            status = "STABLE";
        }

        return new RegionInsight(region, current, previous, change, percentChange, status);
    }
}
