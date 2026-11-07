package com.example.tickertracker.web; // TODO: change to your package

import com.example.tickertracker.service.SightingSearchService;
import com.example.tickertracker.web.dto.PagedResponse;
import com.example.tickertracker.web.dto.SightingResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * GET /api/sightings?from=2025-01-01&to=2025-06-30&region=Berkshire&location=reading&page=0&size=20
 * All filters are optional and can be combined.
 */
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

            @RequestParam(required = false) String region,
            @RequestParam(required = false) String location,

            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return PagedResponse.from(
                searchService.search(from, to, region, location, page, size)
                        .map(SightingResponse::from));
    }
}
