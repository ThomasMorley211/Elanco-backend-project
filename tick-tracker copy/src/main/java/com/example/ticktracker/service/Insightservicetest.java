package main.java.com.example.ticktracker.service;

import com.example.ticktracker.domain.SightingRepository;
import com.example.ticktracker.web.dto.RegionInsight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsightServiceTest {

    private static final LocalDateTime PREVIOUS_START = LocalDateTime.of(2025, 6, 17, 0, 0);
    private static final LocalDateTime CURRENT_START = LocalDateTime.of(2025, 6, 24, 0, 0);
    private static final LocalDateTime WINDOW_END = LocalDateTime.of(2025, 7, 1, 0, 0);
    private static final LocalDate AS_OF = LocalDate.of(2025, 6, 30);

    private SightingRepository repository;
    private InsightService service;

    @BeforeEach
    void setUp() {
        repository = mock(SightingRepository.class);
        service = new InsightService(repository);
    }

    @Test
    void classifiesRegionsAndSortsByBiggestIncrease() {
        when(repository.countByRegion(CURRENT_START, WINDOW_END)).thenReturn(List.of(
                total("Berkshire", 12), total("Surrey", 4), total("Kent", 3)));
        when(repository.countByRegion(PREVIOUS_START, CURRENT_START)).thenReturn(List.of(
                total("Berkshire", 5), total("Surrey", 4), total("Kent", 6), total("Essex", 2)));

        List<RegionInsight> result = service.regionTrends(AS_OF, 7, 5);

        assertThat(result).extracting(RegionInsight::region)
                .containsExactly("Berkshire", "Surrey", "Essex", "Kent");
        assertThat(result).extracting(RegionInsight::status)
                .containsExactly("SPIKE", "STABLE", "FALLING", "FALLING");
        assertThat(result).extracting(RegionInsight::change)
                .containsExactly(7L, 0L, -2L, -3L);
        assertThat(result).extracting(RegionInsight::percentChange)
                .containsExactly(140.0, 0.0, -100.0, -50.0);
    }

    @Test
    void newActivityFromZeroAboveThresholdIsSpike() {
        when(repository.countByRegion(CURRENT_START, WINDOW_END)).thenReturn(List.of(total("Dorset", 6)));
        when(repository.countByRegion(PREVIOUS_START, CURRENT_START)).thenReturn(List.of());

        List<RegionInsight> result = service.regionTrends(AS_OF, 7, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("SPIKE");
        assertThat(result.get(0).percentChange()).isNull();
    }

    @Test
    void increaseBelowThresholdIsRisingNotSpike() {
        when(repository.countByRegion(CURRENT_START, WINDOW_END)).thenReturn(List.of(total("Dorset", 3)));
        when(repository.countByRegion(PREVIOUS_START, CURRENT_START)).thenReturn(List.of());

        List<RegionInsight> result = service.regionTrends(AS_OF, 7, 5);

        assertThat(result.get(0).status()).isEqualTo("RISING");
    }

    @Test
    void usesLatestSightingDateWhenAsOfMissing() {
        when(repository.findLatestSightingTime()).thenReturn(LocalDateTime.of(2025, 6, 30, 14, 20));
        when(repository.countByRegion(CURRENT_START, WINDOW_END)).thenReturn(List.of(total("Kent", 8)));
        when(repository.countByRegion(PREVIOUS_START, CURRENT_START)).thenReturn(List.of(total("Kent", 2)));

        List<RegionInsight> result = service.regionTrends(null, 7, 5);

        assertThat(result).extracting(RegionInsight::region).containsExactly("Kent");
        assertThat(result.get(0).status()).isEqualTo("SPIKE");
    }

    @Test
    void returnsEmptyListWhenThereIsNoData() {
        when(repository.findLatestSightingTime()).thenReturn(null);

        assertThat(service.regionTrends(null, 7, 5)).isEmpty();
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
