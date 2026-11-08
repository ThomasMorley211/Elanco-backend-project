package com.example.ticktracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "rejected_record")
public class RejectedRecord{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_payload", length = 4000)
    private String rawPayload;
    @Column(nullable = false, length = 500)
    private String reason;
    @Column(name = "rejected_at", nullable = false)
    private Instant rejectedAt;
    protected RejectedRecord(){

    }


    public RejectedRecord(String rawPayload, String reason) {
        this.rawPayload = rawPayload;
        this.reason = reason;
        this.rejectedAt=Instant.now();
}
    public Long getId() {return id;}
    public String getRawPayload() {return rawPayload;}
    public String getReason() {return reason;}
    public Instant getRejectedAt(){return rejectedAt; }
}
