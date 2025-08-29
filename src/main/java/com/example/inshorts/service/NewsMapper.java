package com.example.inshorts.service;

import com.example.inshorts.dto.News;
import com.example.inshorts.entity.NewsEntity;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsMapper {

    /**
     * Convert NewsEntity to News DTO
     */
    public News toDto(NewsEntity entity) {
        if (entity == null) {
            return null;
        }

        // Extract coordinates from Point location if available, fallback to individual fields
        Double latitude = null;
        Double longitude = null;
        
        if (entity.getLocation() != null) {
            latitude = entity.getLocation().getY(); // Point.getY() returns latitude
            longitude = entity.getLocation().getX(); // Point.getX() returns longitude
        } else {
            // Fallback to individual coordinate fields
            latitude = entity.getLatitude();
            longitude = entity.getLongitude();
        }

        return News.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .url(entity.getUrl())
                .publicationDate(entity.getPublicationDate())
                .sourceName(entity.getSourceName())
                .category(entity.getCategory())
                .relevanceScore(entity.getRelevanceScore())
                .latitude(latitude)
                .longitude(longitude)
                .llmSummary(null) // Will be populated by service layer
                .build();
    }

    /**
     * Convert News DTO to NewsEntity
     */
    public NewsEntity toEntity(News dto) {
        if (dto == null) {
            return null;
        }

        NewsEntity entity = new NewsEntity();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setUrl(dto.getUrl());
        entity.setPublicationDate(dto.getPublicationDate());
        entity.setSourceName(dto.getSourceName());
        entity.setCategory(dto.getCategory());
        entity.setRelevanceScore(dto.getRelevanceScore());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        
        // Create Point location from coordinates if available
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            Point point = new Point(dto.getLongitude(), dto.getLatitude()); // GeoJSON: [longitude, latitude]
            entity.setLocation(point);
        }
        
        return entity;
    }
}

