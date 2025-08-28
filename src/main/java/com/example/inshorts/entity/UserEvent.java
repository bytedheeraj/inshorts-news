package com.example.inshorts.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "user_events")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {

    @Id
    private String id;

    @Field("article_id")
    private String articleId;

    @Field("user_id")
    private String userId;

    @Field("event_type")
    private EventType eventType;

    @Field("user_latitude")
    private Double userLatitude;

    @Field("user_longitude")
    private Double userLongitude;

    @Field("timestamp")
    private LocalDateTime timestamp;

    @Field("weight")
    private Double weight;

    public enum EventType {
        VIEW(1.0),
        CLICK(2.0),
        SHARE(3.0),
        BOOKMARK(2.5);

        private final double baseWeight;

        EventType(double baseWeight) {
            this.baseWeight = baseWeight;
        }

        public double getBaseWeight() {
            return baseWeight;
        }
    }

    // Constructor with all fields except weight (weight will be calculated)
    public UserEvent(String id, String articleId, String userId, EventType eventType,
                    Double userLatitude, Double userLongitude, LocalDateTime timestamp) {
        this.id = id;
        this.articleId = articleId;
        this.userId = userId;
        this.eventType = eventType;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.timestamp = timestamp;
        this.weight = calculateWeight();
    }

    private double calculateWeight() {
        if (timestamp == null) return eventType.getBaseWeight();
        
        // Decay factor based on time (newer events have higher weight)
        long hoursSinceEvent = java.time.Duration.between(timestamp, LocalDateTime.now()).toHours();
        double timeDecay = Math.exp(-hoursSinceEvent / 24.0); // 24-hour half-life
        
        return eventType.getBaseWeight() * timeDecay;
    }
}
