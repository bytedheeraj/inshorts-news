package com.example.inshorts.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

public class TrendingNewsRequest {
    
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    
    @DecimalMin(value = "1.0", message = "Limit must be at least 1")
    private Integer limit = 10; // Default limit
    
    @DecimalMin(value = "0.1", message = "Radius must be at least 0.1 km")
    private Double radius = 50.0; // Default radius in km
    
    // Default constructor
    public TrendingNewsRequest() {}
    
    // Constructor with required fields
    public TrendingNewsRequest(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Constructor with all fields
    public TrendingNewsRequest(Double latitude, Double longitude, Integer limit, Double radius) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.limit = limit;
        this.radius = radius;
    }
    
    // Getters and Setters
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
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public Double getRadius() {
        return radius;
    }
    
    public void setRadius(Double radius) {
        this.radius = radius;
    }
    
    @Override
    public String toString() {
        return "TrendingNewsRequest{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", limit=" + limit +
                ", radius=" + radius +
                '}';
    }
}
