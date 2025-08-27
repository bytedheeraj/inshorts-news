package com.example.inshorts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class NewsQueryRequest {
    
    @NotBlank(message = "Query is required")
    private String query;
    
    @NotNull(message = "Latitude is required")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    private Double longitude;
    
    // Optional fields for direct API usage
    private String entities;
    private String concepts;
    private String intent;
    
    // Constructors
    public NewsQueryRequest() {}
    
    public NewsQueryRequest(String query, Double latitude, Double longitude) {
        this.query = query;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and Setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public String getEntities() {
        return entities;
    }
    
    public void setEntities(String entities) {
        this.entities = entities;
    }
    
    public String getConcepts() {
        return concepts;
    }
    
    public void setConcepts(String concepts) {
        this.concepts = concepts;
    }
    
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
}
