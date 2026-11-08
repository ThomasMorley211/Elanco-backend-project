package com.example.ticktracker.web.dto;

import com.example.ticktracker.domain.TickSighting;

import java.time.LocalDateTime;

public record SightingResponse(
        String id,
        LocalDateTime date,
        String location,
        String species,
        String latinName
) {
    public static SightingResponse from(TickSighting s) {
        return new SightingResponse(s.getId(), s.getDate(), s.getLocation(),
                s.getSpecies(), s.getLatinName());
    }
}
