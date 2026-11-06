package com.example.ticktracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class TickTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TickTrackerApplication.class, args);
    }
}
