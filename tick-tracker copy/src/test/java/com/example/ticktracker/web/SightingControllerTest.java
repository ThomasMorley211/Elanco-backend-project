package com.example.ticktracker.web;

import com.example.ticktracker.domain.TickSighting;
import com.example.ticktracker.service.SightingSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SightingControllerTest {

    private SightingSearchService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(SightingSearchService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new SightingController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsPagedSightings() throws Exception {
        TickSighting sighting = new TickSighting("abc-1", LocalDateTime.of(2025, 6, 2, 10, 30),
                "Reading", "Sheep tick", "Ixodes ricinus");
        when(service.search(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(sighting), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/sightings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("abc-1"))
                .andExpect(jsonPath("$.content[0].location").value("Reading"))
                .andExpect(jsonPath("$.content[0].species").value("Sheep tick"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void passesFiltersToService() throws Exception {
        when(service.search(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<TickSighting>(List.of(), PageRequest.of(1, 10), 0));

        mockMvc.perform(get("/api/sightings")
                        .param("from", "2025-01-01")
                        .param("to", "2025-06-30")
                        .param("location", "reading")
                        .param("species", "sheep")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(service).search(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30),
                "reading", "sheep", 1, 10);
    }

    @Test
    void rejectsUnparseableDate() throws Exception {
        mockMvc.perform(get("/api/sightings").param("from", "banana"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid parameter"));

        verifyNoInteractions(service);
    }

    @Test
    void returnsBadRequestWhenRangeIsInvalid() throws Exception {
        when(service.search(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("'from' must not be after 'to'."));

        mockMvc.perform(get("/api/sightings")
                        .param("from", "2025-06-30")
                        .param("to", "2025-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad request"))
                .andExpect(jsonPath("$.detail").value("'from' must not be after 'to'."));
    }
}
