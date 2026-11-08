package com.example.ticktracker.web;

import com.example.ticktracker.service.ReportService;
import com.example.ticktracker.web.dto.RegionCount;
import com.example.ticktracker.web.dto.TrendPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportControllerTest {

    private ReportService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(ReportService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ReportController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsSightingsByRegion() throws Exception {
        when(service.sightingsByRegion(any(), any()))
                .thenReturn(List.of(new RegionCount("Berkshire", 12), new RegionCount("Surrey", 4)));

        mockMvc.perform(get("/api/reports/sightings-by-region"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].region").value("Berkshire"))
                .andExpect(jsonPath("$[0].sightings").value(12));
    }

    @Test
    void trendsDefaultsToWeekly() throws Exception {
        when(service.trends(eq("weekly"), any(), any(), any()))
                .thenReturn(List.of(new TrendPoint(LocalDate.of(2025, 6, 2), 3)));

        mockMvc.perform(get("/api/reports/trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sightings").value(3));

        verify(service).trends("weekly", null, null, null);
    }

    @Test
    void trendsPassesAllParameters() throws Exception {
        when(service.trends(any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/trends")
                        .param("granularity", "monthly")
                        .param("from", "2025-01-01")
                        .param("to", "2025-06-30")
                        .param("region", "Berkshire"))
                .andExpect(status().isOk());

        verify(service).trends("monthly", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30), "Berkshire");
    }

    @Test
    void invalidGranularityReturnsBadRequest() throws Exception {
        when(service.trends(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("granularity must be 'weekly' or 'monthly'."));

        mockMvc.perform(get("/api/reports/trends").param("granularity", "daily"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("granularity must be 'weekly' or 'monthly'."));
    }

    @Test
    void invalidDateReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/reports/sightings-by-region").param("to", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid parameter"));

        verifyNoInteractions(service);
    }
}
