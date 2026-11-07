package com.example.ticktracker.web.dto;

public record RegionInsight(
        String region,
        long currentPeriod,
        long previousPeriod,
        long change,
        Double percentChange,
        String status
) {
}
