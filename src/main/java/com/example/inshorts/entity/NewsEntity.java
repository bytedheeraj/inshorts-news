package com.example.inshorts.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "news_data")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsEntity {
    
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

    // GeoJSON Point for geospatial queries
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private double[] location; // [longitude, latitude] as per GeoJSON spec
    
    // Keep these for backward compatibility and data loading
    @JsonProperty("latitude")
    @Field("latitude")
    private Double latitude;
    
    @JsonProperty("longitude")
    @Field("longitude")
    private Double longitude;
}
