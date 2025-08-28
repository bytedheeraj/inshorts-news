package com.example.inshorts.service.impl;

import com.example.inshorts.constants.AppConstants;
import com.example.inshorts.dto.TrendingArticle;
import com.example.inshorts.entity.NewsEntity;
import com.example.inshorts.entity.UserEvent;
import com.example.inshorts.repository.NewsRepository;
import com.example.inshorts.repository.UserEventRepository;
import com.example.inshorts.service.LLMService;
import com.example.inshorts.service.TrendingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GeoNearOperation;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TrendingServiceImpl implements TrendingService {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private UserEventRepository userEventRepository;

    @Autowired
    private LLMService llmService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    @Cacheable(value = "trending", key = "#latitude + '_' + #longitude + '_' + #limit")
    public List<TrendingArticle> getTrendingNews(Double latitude, Double longitude, Integer limit) {
        log.info("Fetching trending news for location: ({}, {}) with limit: {}", latitude, longitude, limit);
        
        try {
            // Get nearby news articles using MongoDB geospatial query
            List<NewsEntity> nearbyNews = getNearbyNews(latitude, longitude, AppConstants.TRENDING_RADIUS); // 50km radius
            
            if (nearbyNews.isEmpty()) {
                log.warn("No nearby news found for location: ({}, {})", latitude, longitude);
                return new ArrayList<>();
            }

            // Enrich news with trending data and LLM summaries
            List<TrendingArticle> trendingArticles = nearbyNews.stream()
                    .map(news -> enrichNewsWithTrendingData(news, latitude, longitude))
                    .sorted((a, b) -> Double.compare(b.getTrendingScore(), a.getTrendingScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

            log.info("Found {} trending articles for location: ({}, {})", trendingArticles.size(), latitude, longitude);
            return trendingArticles;

        } catch (Exception e) {
            log.error("Error fetching trending news: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch trending news", e);
        }
    }

    @Override
    @Cacheable(value = "trending", key = "'category_' + #category + '_' + #latitude + '_' + #longitude + '_' + #limit")
    public List<TrendingArticle> getTrendingNewsByCategory(String category, Double latitude, Double longitude, Integer limit) {
        log.info("Fetching trending news for category: {} at location: ({}, {})", category, latitude, longitude);
        
        try {
            List<NewsEntity> categoryNews = newsRepository.findByCategoryContainingIgnoreCase(category);
            
            if (categoryNews.isEmpty()) {
                log.warn("No news found for category: {}", category);
                return new ArrayList<>();
            }

            // Filter by distance and enrich with trending data
            List<TrendingArticle> trendingArticles = categoryNews.stream()
                    .filter(news -> calculateDistance(latitude, longitude, news.getLatitude(), news.getLongitude()) <= 50.0)
                    .map(news -> enrichNewsWithTrendingData(news, latitude, longitude))
                    .sorted((a, b) -> Double.compare(b.getTrendingScore(), a.getTrendingScore()))
                    .limit(limit)
                    .collect(Collectors.toList());

            log.info("Found {} trending articles for category: {} at location: ({}, {})", 
                    trendingArticles.size(), category, latitude, longitude);
            return trendingArticles;

        } catch (Exception e) {
            log.error("Error fetching trending news by category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch trending news by category", e);
        }
    }

    @Override
    @CacheEvict(value = "trending", allEntries = true)
    public void simulateUserEvents() {
        log.info("Starting user event simulation...");
        
        try {
            List<NewsEntity> allNews = newsRepository.findAll();
            if (allNews.isEmpty()) {
                log.warn("No news articles found for event simulation");
                return;
            }

            List<UserEvent> events = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            Random random = new Random();

            for (NewsEntity newsEntity : allNews) {
                // Generate 5-20 events per article
                int eventCount = random.nextInt(16) + 5;
                
                for (int i = 0; i < eventCount; i++) {
                    UserEvent event = generateUserEvent(newsEntity, now, random);
                    events.add(event);
                }
            }

            // Save all events
            userEventRepository.saveAll(events);
            log.info("Successfully simulated {} user events for {} articles", events.size(), allNews.size());

        } catch (Exception e) {
            log.error("Error simulating user events: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to simulate user events", e);
        }
    }

    @Override
    public Double calculateTrendingScore(String articleId, Double userLat, Double userLon) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime last24Hours = now.minusHours(24);

            // Get recent events for this article
            List<UserEvent> recentEvents = userEventRepository.findByArticleIdAndTimestampBetween(
                    articleId, last24Hours, now);

            if (recentEvents.isEmpty()) {
                return 0.0;
            }

            double score = 0.0;
            for (UserEvent event : recentEvents) {
                // Base score from event type
                score += event.getWeight();

                // Recency bonus (more recent events get higher scores)
                long hoursAgo = java.time.Duration.between(event.getTimestamp(), now).toHours();
                double recencyBonus = Math.max(0, 24 - hoursAgo) / 24.0;
                score += recencyBonus * 0.5;

                // Geographic relevance bonus
                double distance = calculateDistance(userLat, userLon, event.getUserLatitude(), event.getUserLongitude());
                double geoBonus = Math.max(0, 50 - distance) / 50.0;
                score += geoBonus * 0.3;
            }

            return Math.min(10.0, score); // Cap at 10.0

        } catch (Exception e) {
            log.error("Error calculating trending score: {}", e.getMessage(), e);
            return 0.0;
        }
    }

    // Private helper methods

    private List<NewsEntity> getNearbyNews(Double latitude, Double longitude, Double maxDistanceKm) {
        try {
            // Use MongoDB's geospatial aggregation
            NearQuery nearQuery = NearQuery.near(latitude, longitude)
                    .maxDistance(maxDistanceKm * 1000) // Convert km to meters
                    .spherical(true);

            GeoNearOperation geoNearOp = Aggregation.geoNear(nearQuery, "news_data");
            Aggregation aggregation = Aggregation.newAggregation(geoNearOp);
            AggregationResults<NewsEntity> results = mongoTemplate.aggregate(aggregation, "news_data", NewsEntity.class);

            return results.getMappedResults();

        } catch (Exception e) {
            log.error("Error in geospatial query: {}", e.getMessage(), e);
            throw new RuntimeException("Geospatial query failed", e);
        }
    }

    private TrendingArticle enrichNewsWithTrendingData(NewsEntity newsEntity, Double userLat, Double userLon) {
        // Calculate trending score
        Double trendingScore = calculateTrendingScore(newsEntity.getId(), userLat, userLon);
        
        // Get user engagement count
        Long userEngagementCount = userEventRepository.countByArticleId(newsEntity.getId());
        
        // Generate LLM summary
        String llmSummary;
        try {
            llmSummary = llmService.generateSummary(newsEntity.getTitle() + " " + newsEntity.getDescription());
        } catch (Exception e) {
            llmSummary = "Summary not available";
            log.warn("Failed to generate summary for trending article: {}", newsEntity.getTitle());
        }
        
        return TrendingArticle.builder()
                .id(newsEntity.getId())
                .title(newsEntity.getTitle())
                .description(newsEntity.getDescription())
                .url(newsEntity.getUrl())
                .publicationDate(newsEntity.getPublicationDate())
                .sourceName(newsEntity.getSourceName())
                .category(newsEntity.getCategory())
                .relevanceScore(newsEntity.getRelevanceScore())
                .llmSummary(llmSummary)
                .latitude(newsEntity.getLatitude())
                .longitude(newsEntity.getLongitude())
                .trendingScore(trendingScore)
                .userEngagementCount(userEngagementCount)
                .build();
    }

    public String generateArticleSummary(String title, String description) {
        try {
            // Use LLM service to generate summary
            String prompt = String.format("Summarize this news article in 2-3 sentences: Title: %s. Description: %s", 
                    title, description);
            
            // For now, return a simple summary. In production, call the actual LLM service
            return String.format("This article discusses %s. %s", 
                    title.toLowerCase(), 
                    description.length() > 100 ? description.substring(0, 100) + "..." : description);
                    
        } catch (Exception e) {
            log.warn("Error generating LLM summary, using fallback: {}", e.getMessage());
            return "Summary not available";
        }
    }

    private UserEvent generateUserEvent(NewsEntity newsEntity, LocalDateTime now, Random random) {
        // Generate random user location near the article location
        Double userLat = newsEntity.getLatitude() + (random.nextDouble() - 0.5) * 0.1; // ±0.05 degrees
        Double userLon = newsEntity.getLongitude() + (random.nextDouble() - 0.5) * 0.1;
        
        // Random event type with weighted distribution
        UserEvent.EventType eventType;
        double rand = random.nextDouble();
        if (rand < 0.6) {
            eventType = UserEvent.EventType.VIEW; // 60% views
        } else if (rand < 0.8) {
            eventType = UserEvent.EventType.CLICK; // 20% clicks
        } else if (rand < 0.9) {
            eventType = UserEvent.EventType.SHARE; // 10% shares
        } else {
            eventType = UserEvent.EventType.BOOKMARK; // 10% bookmarks
        }
        
        // Random timestamp within last 24 hours
        LocalDateTime eventTime = now.minusHours(random.nextInt(24))
                .minusMinutes(random.nextInt(60))
                .minusSeconds(random.nextInt(60));
        
        return new UserEvent(
            UUID.randomUUID().toString(),
            newsEntity.getId(),
            "user_" + random.nextInt(1000),
            eventType,
            userLat,
            userLon,
            eventTime
        );
    }

    private Double getEventWeight(UserEvent.EventType eventType) {
        switch (eventType) {
            case VIEW: return 1.0;
            case CLICK: return 2.0;
            case SHARE: return 3.0;
            case BOOKMARK: return 4.0;
            default: return 1.0;
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula for distance calculation
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
