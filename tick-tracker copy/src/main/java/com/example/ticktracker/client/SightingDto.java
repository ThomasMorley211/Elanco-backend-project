package com.example.ticktracker.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public record SightingDto(
    String id,
    String date,
    String location,
    String species,
    String latinName){
    }
