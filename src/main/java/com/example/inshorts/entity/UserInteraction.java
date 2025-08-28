package com.example.inshorts.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "user_interactions")
public class UserInteraction {
    
    @Id
    private String id;
    
    @Field("news_id")
    private String newsId;
    
    @Field("user_id")
    private String userId;
    
    @Field("interaction_type")
    private InteractionType interactionType;
    
    @Field("timestamp")
    private LocalDateTime timestamp;
    
    @Field("user_latitude")
    private Double userLatitude;
    
    @Field("user_longitude")
    private Double userLongitude;
    
    @Field("weight")
    private Double weight;
    
    public enum InteractionType {
        VIEW(1.0),
        CLICK(2.0),
        SHARE(3.0),
        BOOKMARK(2.5);
        
        private final double weight;
        
        InteractionType(double weight) {
            this.weight = weight;
        }
        
        public double getWeight() {
            return weight;
        }
    }
    
    // Default constructor
    public UserInteraction() {}
    
    // Constructor with all fields
    public UserInteraction(String newsId, String userId, InteractionType interactionType, 
                         Double userLatitude, Double userLongitude) {
        this.newsId = newsId;
        this.userId = userId;
        this.interactionType = interactionType;
        this.timestamp = LocalDateTime.now();
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.weight = interactionType.getWeight();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNewsId() {
        return newsId;
    }
    
    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public InteractionType getInteractionType() {
        return interactionType;
    }
    
    public void setInteractionType(InteractionType interactionType) {
        this.interactionType = interactionType;
        this.weight = interactionType.getWeight();
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Double getUserLatitude() {
        return userLatitude;
    }
    
    public void setUserLatitude(Double userLatitude) {
        this.userLatitude = userLatitude;
    }
    
    public Double getUserLongitude() {
        return userLongitude;
    }
    
    public void setUserLongitude(Double userLongitude) {
        this.userLongitude = userLongitude;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    @Override
    public String toString() {
        return "UserInteraction{" +
                "id='" + id + '\'' +
                ", newsId='" + newsId + '\'' +
                ", userId='" + userId + '\'' +
                ", interactionType=" + interactionType +
                ", timestamp=" + timestamp +
                ", userLatitude=" + userLatitude +
                ", userLongitude=" + userLongitude +
                ", weight=" + weight +
                '}';
    }
}
