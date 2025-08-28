package com.example.inshorts.service;

import com.example.inshorts.dto.NewsQueryRequest;
import com.example.inshorts.dto.NewsResponse;
import com.example.inshorts.dto.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NewsService {
    
    /**
     * Process a news query and return relevant articles
     */
    NewsResponse processNewsQuery(NewsQueryRequest request);
    
    /**
     * Get news by category with pagination
     */
    Page<News> getNewsByCategory(String category, Pageable pageable);
    
    /**
     * Get news by source with pagination
     */
    Page<News> getNewsBySource(String source, Pageable pageable);
    
    /**
     * Get news by relevance score threshold with pagination
     */
    Page<News> getNewsByScore(Double threshold, Pageable pageable);
    
    /**
     * Search news by text query with pagination
     */
    Page<News> searchNews(String query, Pageable pageable);
    
    /**
     * Get news within a certain radius with pagination
     */
    Page<News> getNearbyNews(Double latitude, Double longitude, Double radiusKm, Pageable pageable);
    
    /**
     * Get all news articles with pagination
     */
    Page<News> getAllNews(Pageable pageable);
    
    /**
     * Save a news article
     */
    News saveNews(News newsEntity);
    
    /**
     * Save multiple news articles
     */
    List<News> saveAllNews(List<News> newsEntityList);
}
