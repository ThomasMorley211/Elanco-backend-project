package com.example.ticktracker.web;

import com.example.ticktracker.service.InsightService;
import com.example.ticktracker.web.dto.RegionInsight;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/insights")
@Validated
public class InsightController {

    private final InsightService insightService;

    public InsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping("/region-trends")
    public List<RegionInsight> regionTrends(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf,
            @RequestParam(defaultValue = "7") @Min(1) @Max(90) int days,
            @RequestParam(defaultValue = "5") @Min(1) int minSightings) {
        return insightService.regionTrends(asOf, days, minSightings);
    }
}
