package com.example.ticktracker.ingest;

import com.example.ticktracker.client.ElancoApiClient;
import com.example.ticktracker.client.SightingDto;
import com.example.ticktracker.domain.RejectedRecord;
import com.example.ticktracker.domain.TickSighting;
import com.example.ticktracker.repository.RejectedRecordRepository;
import com.example.ticktracker.repository.TickSightingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class IngestionService {
    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    private static final int MAX_PAYLOAD_LENGTH = 4000;

    private final ElancoApiClient apiClient;
    private final SightingValidator validator;
    private final TickSightingRepository sightingRepository;
    private final RejectedRecordRepository rejectedRepository;
    private final ObjectMapper objectMapper;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public IngestionService(ElancoApiClient apiClient,
        SightingValidator validator, TickSightingRepository sightingRepository,
        RejectedRecordRepository rejectedRepository, ObjectMapper objectMapper){
            this.apiClient = apiClient;
            this.validator = validator;
            this.sightingRepository = sightingRepository;
            this.rejectedRepository = rejectedRepository;
            this.objectMapper = objectMapper;
        }

    public IngestionResult ingest(){
        if(!running.compareAndSet(false,true)){
            throw new IngestionInProgressException();
        }
        try {
            List<SightingDto> raw = apiClient.fetchAllSightings();

            Map<String, TickSighting> valid = new LinkedHashMap<>();
            List<RejectedRecord> rejected = new ArrayList<>();
            int repeatedInDownload = 0;

            for (SightingDto dto: raw){
                ValidationResult result = validator.validate(dto);
                if (!result.isValid()){
                    rejected.add(new RejectedRecord(toPayload(dto), result.rejectionReason()));
                } else if (valid.putIfAbsent(result.sighting().getId(), result.sighting()) != null){
                    repeatedInDownload++;
                }
            }
            Set<String> alreadyStored = sightingRepository.findAllById(valid.keySet()).stream()
                .map(TickSighting::getId)
                .collect(Collectors.toSet());
            List<TickSighting> toInsert = valid.values().stream()
                .filter(s -> !alreadyStored.contains(s.getId()))
                .toList();
            sightingRepository.saveAll(toInsert);
            rejectedRepository.deleteAllInBatch();
            rejectedRepository.saveAll(rejected);
            IngestionResult summary = new IngestionResult(
                raw.size(), toInsert.size(), alreadyStored.size() + repeatedInDownload, rejected.size());
            log.info("Ingestion finished: {}",summary);
            return summary;
        }finally{
            running.set(false);
        }
    }

    private String toPayload(SightingDto dto){
        String text;
        try {
            text = objectMapper.writeValueAsString(dto);
        } catch(JsonProcessingException e){
            text = String.valueOf(dto);
        }
        return text.length() > MAX_PAYLOAD_LENGTH ? text.substring(0, MAX_PAYLOAD_LENGTH) : text;
    }
}
