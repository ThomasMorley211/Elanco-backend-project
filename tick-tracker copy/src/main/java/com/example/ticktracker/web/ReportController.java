package com.example.tickertracker.web; // TODO: change to your package

import com.example.tickertracker.service.ReportService;
import com.example.tickertracker.web.dto.RegionCount;
import com.example.tickertracker.web.dto.TrendPoint;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * GET /api/reports/sightings-by-region?from=2025-01-01&to=2025-06-30
 * GET /api/reports/trends?granularity=weekly|monthly&from=...&to=...&region=Berkshire
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sightings-by-region")
    public List<RegionCount> sightingsByRegion(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return reportService.sightingsByRegion(from, to);
    }

    @GetMapping("/trends")
    public List<TrendPoint> trends(
            @RequestParam(defaultValue = "weekly") String granularity,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String region) {
        return reportService.trends(granularity, from, to, region);
    }
}
