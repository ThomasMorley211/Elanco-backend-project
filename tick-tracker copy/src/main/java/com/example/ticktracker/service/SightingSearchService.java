package com.example.ticktracker.service;

import com.example.ticktracker.domain.SightingRepository;
import com.example.ticktracker.domain.SightingSpecifications;
import com.example.ticktracker.domain.TickSighting;
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
    public Page<TickSighting> search(LocalDate from, LocalDate to, String location,
                                     String species, int page, int size) {

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'.");
        }

        Specification<TickSighting> spec = Specification.allOf(
                SightingSpecifications.sightedOnOrAfter(from),
                SightingSpecifications.sightedOnOrBefore(to),
                SightingSpecifications.locationContains(location),
                SightingSpecifications.speciesContains(species));

        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "date").and(Sort.by("id")));

        return repository.findAll(spec, pageable);
    }
}
