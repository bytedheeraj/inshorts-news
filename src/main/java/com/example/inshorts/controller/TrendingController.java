package com.example.inshorts.controller;

import com.example.inshorts.dto.TrendingArticle;
import com.example.inshorts.service.TrendingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trending")
public class TrendingController {

    private static final Logger logger = LoggerFactory.getLogger(TrendingController.class);

    @Autowired
    private TrendingService trendingService;

    /**
     * Get trending news for a specific location
     *
     * @param lat   User's latitude
     * @param lon   User's longitude
     * @param limit Maximum number of articles to return (default: 5)
     * @return List of trending articles
     */
    @GetMapping
    public ResponseEntity<List<TrendingArticle>> getTrendingNews(
            @RequestParam("lat") Double lat,
            @RequestParam("lon") Double lon,
            @RequestParam(value = "limit", defaultValue = "5") Integer limit) {

        logger.info("Trending news request - Location: ({}, {}), Limit: {}", lat, lon, limit);
        try {
            List<TrendingArticle> trendingNews = trendingService.getTrendingNews(lat, lon, limit);

            logger.info("Successfully retrieved {} trending articles for location ({}, {})",
                    trendingNews.size(), lat, lon);

            return ResponseEntity.ok(trendingNews);

        } catch (Exception e) {
            logger.error("Error retrieving trending news for location ({}, {}): {}",
                    lat, lon, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get trending news by category for a specific location
     *
     * @param category News category
     * @param lat      User's latitude
     * @param lon      User's longitude
     * @param limit    Maximum number of articles to return (default: 5)
     * @return List of trending articles in the category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TrendingArticle>> getTrendingNewsByCategory(
            @PathVariable String category,
            @RequestParam("lat") Double lat,
            @RequestParam("lon") Double lon,
            @RequestParam(value = "limit", defaultValue = "5") Integer limit) {

        logger.info("Trending news by category request - Category: {}, Location: ({}, {}), Limit: {}",
                category, lat, lon, limit);

        try {
            List<TrendingArticle> trendingNews = trendingService.getTrendingNewsByCategory(category, lat, lon, limit);

            logger.info("Successfully retrieved {} trending articles for category '{}' at location ({}, {})",
                    trendingNews.size(), category, lat, lon);

            return ResponseEntity.ok(trendingNews);

        } catch (Exception e) {
            logger.error("Error retrieving trending news for category '{}' at location ({}, {}): {}",
                    category, lat, lon, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Simulate user events for trending calculation
     * This endpoint is typically called by a scheduled job or admin
     *
     * @return Success message
     */
    @PostMapping("/simulate-events")
    public ResponseEntity<String> simulateUserEvents() {
        logger.info("User event simulation request received");

        try {
            trendingService.simulateUserEvents();

            logger.info("User event simulation completed successfully");
            return ResponseEntity.ok("User events simulated successfully");

        } catch (Exception e) {
            logger.error("Error simulating user events: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to simulate user events");
        }
    }

    /**
     * Health check endpoint for trending service
     *
     * @return Service status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.debug("Trending service health check");
        return ResponseEntity.ok("Trending service is running");
    }
}

