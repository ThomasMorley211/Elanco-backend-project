package com.example.tickertracker.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

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
