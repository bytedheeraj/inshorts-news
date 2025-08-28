package com.example.inshorts.service;

import com.example.inshorts.dto.News;
import com.example.inshorts.entity.NewsEntity;
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

        return News.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .url(entity.getUrl())
                .publicationDate(entity.getPublicationDate())
                .sourceName(entity.getSourceName())
                .category(entity.getCategory())
                .relevanceScore(entity.getRelevanceScore())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
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
        
        return entity;
    }
}

