package com.example.ticktracker.web;

import com.example.ticktracker.domain.RejectedRecord;
import com.example.ticktracker.ingest.IngestionResult;
import com.example.ticktracker.ingest.IngestionService;
import com.example.ticktracker.repository.RejectedRecordRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class IngestController {
    private final IngestionService ingestionService;
    private final RejectedRecordRepository rejectedRepository;
    public IngestController(IngestionService ingestionService, RejectedRecordRepository rejectedRepository){
        this.ingestionService = ingestionService;
        this.rejectedRepository = rejectedRepository;
    }
    @PostMapping("/ingest")
    public IngestionResult ingest(){
        return ingestionService.ingest();
    }
    @GetMapping("/rejected")
    public List<RejectedRecord> rejected(){
        return rejectedRepository.findAll();
    }
}
