package com.example.tickertracker.web.dto; // TODO: change to your package

import com.example.tickertracker.domain.Sighting;

import java.time.LocalDateTime;

/** What the API returns for a sighting (keeps the JPA entity out of the response). */
public record SightingResponse(
        Long id,
        String region,
        String location,
        LocalDateTime sightedAt
) {
    public static SightingResponse from(Sighting s) {
        return new SightingResponse(s.getId(), s.getRegion(), s.getLocation(), s.getSightedAt());
    }
}
