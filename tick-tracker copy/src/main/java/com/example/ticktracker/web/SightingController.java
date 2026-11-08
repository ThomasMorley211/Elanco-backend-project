package com.example.ticktracker.web;

import com.example.ticktracker.service.SightingSearchService;
import com.example.ticktracker.web.dto.PagedResponse;
import com.example.ticktracker.web.dto.SightingResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/sightings")
@Validated
public class SightingController {

    private final SightingSearchService searchService;

    public SightingController(SightingSearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public PagedResponse<SightingResponse> search(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(required = false) String location,
            @RequestParam(required = false) String species,

            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return PagedResponse.from(
                searchService.search(from, to, location, species, page, size)
                        .map(SightingResponse::from));
    }
}
