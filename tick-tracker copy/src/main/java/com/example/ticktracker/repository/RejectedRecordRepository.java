package com.example.ticktracker.repository;
import com.example.ticktracker.domain.RejectedRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectedRecordRepository extends JpaRepository<RejectedRecord, Long>{
    
}