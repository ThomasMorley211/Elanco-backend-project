package com.example.ticktracker.ingest;
import com.example.ticktracker.domain.TickSighting;
public record ValidationReuslt(TickSighting sighting, String rejectionReason){
    public static ValidationResult accepted(TickSighting sighting){
        return new ValidationResult(sighting,null);

    }
    public static ValidationResult rejected(String reason){
        return new ValidationResult(null, reason);
    }
    public boolean isValid() {
        return sighting != null;
    }
}

