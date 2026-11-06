package com.example.ticktracker.ingest;

public class IngestionInProgressException extends RuntimeException{
    public IngestionInProgressException(){
        super("An ingestion is already running. Please wait for it to finish.");
    }
}