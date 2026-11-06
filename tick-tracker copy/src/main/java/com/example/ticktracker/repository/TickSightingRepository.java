package com.example.ticktracker.repository;
import com.example.ticktracker.domain.TickSighting;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TickSightingRepository extends JpaRepository<TickSighting, String>{

}