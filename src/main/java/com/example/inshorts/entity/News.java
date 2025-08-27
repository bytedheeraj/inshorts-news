package com.example.inshorts.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "news_data")
public class News {
    
    @Id
    private String id;
    
    private String title;
    
    @Field("description")
    private String description;
    
    private String url;
    
    @JsonProperty("publication_date")
    @Field("publication_date")
    private LocalDateTime publicationDate;
    
    @JsonProperty("source_name")
    @Field("source_name")
    private String sourceName;
    
    private List<String> category;
    
    @JsonProperty("relevance_score")
    @Field("relevance_score")
    private Double relevanceScore;
    
    private Double latitude;
    
    private Double longitude;
    
    // Default constructor
    public News() {}
    
    // Constructor with all fields
    public News(String id, String title, String description, String url, 
                LocalDateTime publicationDate, String sourceName, List<String> category, 
                Double relevanceScore, Double latitude, Double longitude) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.url = url;
        this.publicationDate = publicationDate;
        this.sourceName = sourceName;
        this.category = category;
        this.relevanceScore = relevanceScore;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public LocalDateTime getPublicationDate() {
        return publicationDate;
    }
    
    public void setPublicationDate(LocalDateTime publicationDate) {
        this.publicationDate = publicationDate;
    }
    
    public String getSourceName() {
        return sourceName;
    }
    
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    public List<String> getCategory() {
        return category;
    }
    
    public void setCategory(List<String> category) {
        this.category = category;
    }
    
    public Double getRelevanceScore() {
        return relevanceScore;
    }
    
    public void setRelevanceScore(Double relevanceScore) {
        this.relevanceScore = relevanceScore;
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
    
    @Override
    public String toString() {
        return "News{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", publicationDate=" + publicationDate +
                ", sourceName='" + sourceName + '\'' +
                ", category=" + category +
                ", relevanceScore=" + relevanceScore +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
