package com.example.ticktracker.service;

import com.example.ticktracker.domain.SightingRepository;
import com.example.ticktracker.web.dto.RegionCount;
import com.example.ticktracker.web.dto.TrendPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceTest {

    private SightingRepository repository;
    private ReportService service;

    @BeforeEach
    void setUp() {
        repository = mock(SightingRepository.class);
        service = new ReportService(repository);
    }

    @Test
    void groupsMissingRegionsAsUnknownAndSortsByCount() {
        when(repository.countByRegion(any(), any())).thenReturn(List.of(
                total("Berkshire", 5),
                total(null, 2),
                total("", 1),
                total("Surrey", 7)));

        List<RegionCount> result = service.sightingsByRegion(null, null);

        assertThat(result).extracting(RegionCount::region)
                .containsExactly("Surrey", "Berkshire", "Unknown");
        assertThat(result).extracting(RegionCount::sightings)
                .containsExactly(7L, 5L, 3L);
    }

    @Test
    void weeklyTrendsStartOnMondayAndFillGaps() {
        when(repository.findSightingTimes(any(), any(), eq(""))).thenReturn(List.of(
                LocalDateTime.of(2025, 6, 2, 10, 0),
                LocalDateTime.of(2025, 6, 3, 15, 30),
                LocalDateTime.of(2025, 6, 16, 9, 0)));

        List<TrendPoint> result = service.trends("weekly", null, null, null);

        assertThat(result).extracting(TrendPoint::periodStart).containsExactly(
                LocalDate.of(2025, 6, 2),
                LocalDate.of(2025, 6, 9),
                LocalDate.of(2025, 6, 16));
        assertThat(result).extracting(TrendPoint::sightings).containsExactly(2L, 0L, 1L);
    }

    @Test
    void monthlyTrendsStartOnFirstAndFillGaps() {
        when(repository.findSightingTimes(any(), any(), eq(""))).thenReturn(List.of(
                LocalDateTime.of(2025, 1, 15, 12, 0),
                LocalDateTime.of(2025, 3, 10, 8, 0)));

        List<TrendPoint> result = service.trends("MONTHLY", null, null, null);

        assertThat(result).extracting(TrendPoint::periodStart).containsExactly(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 3, 1));
        assertThat(result).extracting(TrendPoint::sightings).containsExactly(1L, 0L, 1L);
    }

    @Test
    void trendsReturnsEmptyListWhenNoData() {
        when(repository.findSightingTimes(any(), any(), any())).thenReturn(List.of());

        assertThat(service.trends("weekly", null, null, null)).isEmpty();
    }

    @Test
    void trendsNormalisesRegionFilter() {
        when(repository.findSightingTimes(any(), any(), eq("berkshire"))).thenReturn(List.of());

        service.trends("weekly", null, null, " Berkshire ");

        verify(repository).findSightingTimes(any(), any(), eq("berkshire"));
    }

    @Test
    void rejectsUnknownGranularity() {
        assertThatThrownBy(() -> service.trends("daily", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("weekly");
    }

    @Test
    void rejectsFromAfterTo() {
        assertThatThrownBy(() -> service.sightingsByRegion(LocalDate.of(2025, 6, 30), LocalDate.of(2025, 1, 1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.trends("weekly", LocalDate.of(2025, 6, 30), LocalDate.of(2025, 1, 1), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static SightingRepository.RegionTotal total(String region, long total) {
        return new SightingRepository.RegionTotal() {
            @Override
            public String getRegion() {
                return region;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
    }
}
