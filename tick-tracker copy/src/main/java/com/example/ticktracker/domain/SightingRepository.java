package com.example.tickertracker.domain; // TODO: change to your package

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Replaces the commit 3/6 version: same as before, plus the two reporting queries.
 * An empty-string region means "no region filter" (avoids null-parameter typing
 * problems on some databases).
 */
public interface SightingRepository
        extends JpaRepository<Sighting, Long>, JpaSpecificationExecutor<Sighting> {

    interface RegionTotal {
        String getRegion();

        long getTotal();
    }

    @Query("""
            select s.region as region, count(s) as total
            from Sighting s
            where s.sightedAt >= :from and s.sightedAt < :to
            group by s.region
            order by count(s) desc
            """)
    List<RegionTotal> countByRegion(@Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

    @Query("""
            select s.sightedAt
            from Sighting s
            where s.sightedAt >= :from and s.sightedAt < :to
              and (:region = '' or lower(s.region) = :region)
            """)
    List<LocalDateTime> findSightingTimes(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          @Param("region") String region);
}
