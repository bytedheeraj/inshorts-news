package com.example.inshorts.service;

import com.example.inshorts.dto.TrendingArticle;

import java.util.List;

public interface TrendingService {

    /**
     * Get trending news articles for a specific location
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param limit Maximum number of articles to return
     * @return List of trending articles
     */
    List<TrendingArticle> getTrendingNews(Double latitude, Double longitude, Integer limit);

    /**
     * Simulate user events for trending calculation
     * This method generates realistic user interaction data
     */
    void simulateUserEvents();

    /**
     * Calculate trending score for an article based on user engagement
     * @param articleId The article ID
     * @param userLatitude User's latitude for geographical relevance
     * @param userLongitude User's longitude for geographical relevance
     * @return Calculated trending score
     */
    Double calculateTrendingScore(String articleId, Double userLatitude, Double userLongitude);

    /**
     * Generate LLM summary for an article
     * @param title Article title
     * @param description Article description
     * @return Generated summary
     */
    String generateArticleSummary(String title, String description);

    /**
     * Get trending articles by category for a location
     * @param category News category
     * @param latitude User's latitude
     * @param longitude User's longitude
     * @param limit Maximum number of articles
     * @return List of trending articles in the category
     */
    List<TrendingArticle> getTrendingNewsByCategory(String category, Double latitude, Double longitude, Integer limit);
}

