package com.example.ticktracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
@Entity 
@Table(name = "tick_sighting", indexes= {
    @Index(name = "idx_sighting_date", columnList = "sighting_date"),
    @Index(name = "idx_sighting_location", columnList = "location")
})

public class TickSighting {
    @Id
    @Column(length = 64)
    private String id;
    
    @Column(name = "sighting_date", nullable = false)
    private LocalDateTime date;
    @Column(nullable = false, length = 100)
    private String location;
    @Column(nullable = false, length = 100)
    private String species;
    @Column(name = "latin_name", nullable = false, length = 150)
    private String latinName;

    protected TickSighting(){

    }

    public TickSighting(String id, LocalDateTime date, String location, String species, String latinName){
        this.id = id;
        this.date = date;
        this.location = location;
        this.species = species;
        this.latinName = latinName;
    }
    public String getId() {return id;}
    public LocalDateTime getDate() { return date;}
    public String getLocation(){return location;}
    public String getSpecies(){return species;}
    public String getLatinName(){return latinName;}
}
