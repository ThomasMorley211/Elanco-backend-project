package com.example.ticktracker.service; // TODO: change to your package

import com.example.ticktracker.domain.Sighting;
import com.example.ticktracker.domain.SightingRepository;
import com.example.ticktracker.domain.SightingSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class SightingSearchService {

    private final SightingRepository repository;

    public SightingSearchService(SightingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Sighting> search(LocalDate from, LocalDate to, String region,
                                 String location, int page, int size) {

        // Caught by GlobalExceptionHandler -> 400 ProblemDetail
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'.");
        }

        Specification<Sighting> spec = Specification
                .where(SightingSpecifications.sightedOnOrAfter(from))
                .and(SightingSpecifications.sightedOnOrBefore(to))
                .and(SightingSpecifications.regionEquals(region))
                .and(SightingSpecifications.locationContains(location));

        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "sightedAt").and(Sort.by("id")));

        return repository.findAll(spec, pageable);
    }
}
