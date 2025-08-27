package com.example.inshorts.service;

import com.example.inshorts.dto.NewsQueryRequest;
import com.example.inshorts.dto.NewsResponse;
import com.example.inshorts.entity.News;

import java.util.List;

public interface NewsService {
    
    /**
     * Process a news query and return relevant articles
     */
    NewsResponse processNewsQuery(NewsQueryRequest request);
    
    /**
     * Get news by category
     */
    List<News> getNewsByCategory(String category);
    
    /**
     * Get news by source
     */
    List<News> getNewsBySource(String source);
    
    /**
     * Get news by relevance score threshold
     */
    List<News> getNewsByScore(Double threshold);
    
    /**
     * Search news by text query
     */
    List<News> searchNews(String query);
    
    /**
     * Get news within a certain radius of given coordinates
     */
    List<News> getNearbyNews(Double latitude, Double longitude, Double radiusKm);
    
    /**
     * Get all news articles
     */
    List<News> getAllNews();
    
    /**
     * Save a news article
     */
    News saveNews(News news);
    
    /**
     * Save multiple news articles
     */
    List<News> saveAllNews(List<News> newsList);
}
