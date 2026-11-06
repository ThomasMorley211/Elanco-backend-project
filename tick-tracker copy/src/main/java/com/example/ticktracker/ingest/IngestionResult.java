package com.example.ticktracker.ingest;
/**
 * @param fetched
 * @param inserted
 * @param duplicatesSkipped
 * @param rejected
 */
public record IngestionResult(int fetched, int inserted, int duplicatesSkipped, int rejected){
}