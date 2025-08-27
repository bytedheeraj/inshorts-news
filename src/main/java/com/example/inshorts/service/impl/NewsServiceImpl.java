package com.example.inshorts.service.impl;

import com.example.inshorts.dto.NewsQueryRequest;
import com.example.inshorts.dto.NewsResponse;
import com.example.inshorts.entity.News;
import com.example.inshorts.repository.NewsRepository;
import com.example.inshorts.service.LLMService;
import com.example.inshorts.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GeoNearOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class NewsServiceImpl implements NewsService {
    
    private static final Logger logger = LoggerFactory.getLogger(NewsServiceImpl.class);
    
    @Autowired
    private NewsRepository newsRepository;
    
    @Autowired
    private LLMService llmService;
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public NewsResponse processNewsQuery(NewsQueryRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("Processing news query: {}", request.getQuery());
        
        try {
            // Process query with LLM to extract entities, concepts, and intent
            NewsQueryRequest processedRequest = llmService.processQuery(request);
            logger.info("LLM processed query - Intent: {}, Entities: {}, Concepts: {}", 
                processedRequest.getIntent(), processedRequest.getEntities(), processedRequest.getConcepts());
            
            List<News> articles = null;
            String intent = processedRequest.getIntent();
            
            // Route to appropriate method based on detected intent
            switch (intent) {
                case "category":
                    articles = getNewsByCategory(processedRequest.getConcepts());
                    break;
                case "source":
                    articles = getNewsBySource(processedRequest.getEntities());
                    break;
                case "nearby":
                    articles = getNearbyNews(processedRequest.getLatitude(), processedRequest.getLongitude(), 10.0);
                    break;
                case "score":
                    articles = getNewsByScore(0.7);
                    break;
                case "search":
                default:
                    articles = searchNews(processedRequest.getQuery());
                    break;
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            NewsResponse response = new NewsResponse();
            response.setArticles(articles);
            response.setTotalCount(articles != null ? articles.size() : 0);
            response.setQuery(processedRequest.getQuery());
            response.setIntent(intent);
            response.setEntities(processedRequest.getEntities());
            response.setConcepts(processedRequest.getConcepts());
            response.setProcessingTimeMs(processingTime);
            
            logger.info("Query processed successfully. Found {} articles in {}ms", 
                response.getTotalCount(), processingTime);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error processing news query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process news query", e);
        }
    }
    
    @Override
    public List<News> getNewsByCategory(String category) {
        logger.info("Fetching news by category: {}", category);
        
        // Split concepts by comma and search for each one
        String[] concepts = category.split(",\\s*");
        List<News> allNews = new ArrayList<>();
        
        for (String concept : concepts) {
            String trimmedConcept = concept.trim();
            if (!trimmedConcept.isEmpty()) {
                List<News> news = newsRepository.findByCategoryContainingIgnoreCase(trimmedConcept);
                allNews.addAll(news);
            }
        }
        
        // Remove duplicates based on ID
        Map<String, News> uniqueNews = new LinkedHashMap<>();
        for (News news : allNews) {
            uniqueNews.put(news.getId(), news);
        }
        
        List<News> result = new ArrayList<>(uniqueNews.values());
        logger.info("Found {} unique news articles for categories: {}", result.size(), category);
        return result;
    }
    
    @Override
    public List<News> getNewsBySource(String source) {
        logger.info("Fetching news by source: {}", source);
        List<News> news = newsRepository.findBySourceNameContainingIgnoreCase(source);
        logger.info("Found {} news articles for source: {}", news.size(), source);
        return news;
    }
    
    @Override
    public List<News> getNewsByScore(Double threshold) {
        logger.info("Fetching news with relevance score above: {}", threshold);
        List<News> news = newsRepository.findByRelevanceScoreGreaterThan(threshold);
        logger.info("Found {} news articles with score above {}", news.size(), threshold);
        return news;
    }
    
    @Override
    public List<News> searchNews(String query) {
        logger.info("Searching news for query: {}", query);
        List<News> news = newsRepository.findByTitleOrDescriptionContaining(query);
        logger.info("Found {} news articles for search query: {}", news.size(), query);
        return news;
    }
    
    @Override
    public List<News> getNearbyNews(Double latitude, Double longitude, Double maxDistanceKm) {
        logger.info("Fetching news within {}km of coordinates: ({}, {})", maxDistanceKm, latitude, longitude);
        
        try {
            // Use MongoDB's geospatial aggregation for nearby search
            NearQuery nearQuery = NearQuery.near(latitude, longitude)
                    .maxDistance(maxDistanceKm * 1000) // Convert km to meters
                    .spherical(true);
            
            GeoNearOperation geoNearOp = Aggregation.geoNear(nearQuery, "distance");
            
            Aggregation aggregation = Aggregation.newAggregation(geoNearOp);
            AggregationResults<News> results = mongoTemplate.aggregate(aggregation, "news_data", News.class);
            
            List<News> news = results.getMappedResults();
            logger.info("Found {} news articles within {}km of ({}, {})", 
                news.size(), maxDistanceKm, latitude, longitude);
            
            return news;
            
        } catch (Exception e) {
            logger.error("Error in geospatial query: {}", e.getMessage(), e);
            // Fallback to simple distance calculation
            return getNearbyNewsFallback(latitude, longitude, maxDistanceKm);
        }
    }
    
    private List<News> getNearbyNewsFallback(Double latitude, Double longitude, Double maxDistanceKm) {
        logger.info("Using fallback nearby news method");
        // Simple fallback: get all news and filter by distance
        List<News> allNews = newsRepository.findAll();
        
        return allNews.stream()
                .filter(news -> {
                    if (news.getLatitude() == null || news.getLongitude() == null) {
                        return false;
                    }
                    double distance = calculateDistance(latitude, longitude, 
                        news.getLatitude(), news.getLongitude());
                    return distance <= maxDistanceKm;
                })
                .toList();
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
    
    @Override
    public List<News> getAllNews() {
        logger.info("Fetching all news articles");
        List<News> news = newsRepository.findAll();
        logger.info("Found {} total news articles", news.size());
        return news;
    }
    
    @Override
    public News saveNews(News news) {
        logger.info("Saving news article: {}", news.getTitle());
        News savedNews = newsRepository.save(news);
        logger.info("Successfully saved news article with ID: {}", savedNews.getId());
        return savedNews;
    }
    
    @Override
    public List<News> saveAllNews(List<News> newsList) {
        logger.info("Saving {} news articles", newsList.size());
        List<News> savedNews = newsRepository.saveAll(newsList);
        logger.info("Successfully saved {} news articles", savedNews.size());
        return savedNews;
    }
}
