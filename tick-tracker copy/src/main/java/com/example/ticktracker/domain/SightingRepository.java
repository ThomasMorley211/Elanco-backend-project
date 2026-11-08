package com.example.ticktracker.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SightingRepository
        extends JpaRepository<TickSighting, String>, JpaSpecificationExecutor<TickSighting> {

    interface RegionTotal {
        String getRegion();
        long getTotal();
    }

    @Query("select s.location as region, count(s) as total from TickSighting s "
            + "where s.date >= :from and s.date < :to "
            + "group by s.location order by count(s) desc")
    List<RegionTotal> countByRegion(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("select s.date from TickSighting s "
            + "where s.date >= :from and s.date < :to "
            + "and (:region = '' or lower(s.location) = :region)")
    List<LocalDateTime> findSightingTimes(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("region") String region);

    @Query("select max(s.date) from TickSighting s")
    LocalDateTime findLatestSightingTime();
}
