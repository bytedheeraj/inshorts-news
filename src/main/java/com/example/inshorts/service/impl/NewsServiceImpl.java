package com.example.inshorts.service.impl;

import com.example.inshorts.dto.News;
import com.example.inshorts.dto.NewsQueryRequest;
import com.example.inshorts.dto.NewsResponse;
import com.example.inshorts.entity.NewsEntity;
import com.example.inshorts.enums.IntentType;
import com.example.inshorts.repository.NewsRepository;
import com.example.inshorts.service.LLMService;
import com.example.inshorts.service.NewsMapper;
import com.example.inshorts.service.NewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GeoNearOperation;
import org.springframework.data.mongodb.core.query.NearQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private LLMService llmService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private NewsMapper newsMapper;

    @Override
    public NewsResponse processNewsQuery(NewsQueryRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Processing news query: {}", request.getQuery());

        try {
            NewsQueryRequest processedRequest = llmService.processQuery(request);
            log.info("LLM processed query - Intent: {}, Entities: {}, Category: {}",
                processedRequest.getIntent(), processedRequest.getEntities(), processedRequest.getCategory());

            List<NewsEntity> articles = null;
            String intent = processedRequest.getIntent();

            switch (IntentType.fromString(intent)) {
                case CATEGORY:
                    articles = getNewsByCategoryEntities(processedRequest.getCategory());
                    break;
                case SOURCE:
                    articles = newsRepository.findBySourceNameContainingIgnoreCase(processedRequest.getEntities());
                    break;
                case NEARBY:
                    articles = getNearbyNewsEntities(processedRequest.getLatitude(), processedRequest.getLongitude(), 10.0);
                    break;
                case SCORE:
                    articles = newsRepository.findByRelevanceScoreGreaterThan(0.7);
                    break;
                case SEARCH:
                default:
                    articles = newsRepository.findByTitleOrDescriptionContaining(processedRequest.getQuery());
                    break;
            }

            long processingTime = System.currentTimeMillis() - startTime;

            List<News> dtoArticles = articles == null ? List.of() : articles.stream().map(newsMapper::toDto).collect(Collectors.toList());

            NewsResponse response = NewsResponse.builder()
                    .articles(dtoArticles)
                    .totalCount(dtoArticles.size())
                    .query(processedRequest.getQuery())
                    .intent(intent)
                    .entities(processedRequest.getEntities())
                    .category(processedRequest.getCategory())
                    .processingTimeMs(processingTime)
                    .build();

            log.info("Query processed successfully. Found {} articles in {}ms",
                response.getTotalCount(), processingTime);

            return response;

        } catch (Exception e) {
            log.error("Error processing news query: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process news query", e);
        }
    }

    // Renamed to reflect returning entities
    private List<NewsEntity> getNewsByCategoryEntities(String categoryCsv) {
        log.info("Fetching news by category: {}", categoryCsv);

        String[] categories = categoryCsv.split(",\\s*");
        Map<String, NewsEntity> uniqueNews = new LinkedHashMap<>();

        for (String category : categories) {
            String trimmed = category.trim();
            if (!trimmed.isEmpty()) {
                List<NewsEntity> news = newsRepository.findByCategoryContainingIgnoreCase(trimmed);
                news.forEach(n -> uniqueNews.putIfAbsent(n.getId(), n));
            }
        }

        List<NewsEntity> allNews = new ArrayList<>(uniqueNews.values());
        log.info("Found {} unique news articles for categories: {}", allNews.size(), categoryCsv);
        return allNews;
    }

    private List<NewsEntity> getNearbyNewsEntities(Double latitude, Double longitude, Double maxDistanceKm) {
        log.info("Fetching news within {}km of coordinates: ({}, {})", maxDistanceKm, latitude, longitude);
        try {
            NearQuery nearQuery = NearQuery.near(latitude, longitude)
                    .maxDistance(maxDistanceKm * 1000)
                    .spherical(true);

            GeoNearOperation geoNearOp = Aggregation.geoNear(nearQuery, "news_data");
            Aggregation aggregation = Aggregation.newAggregation(geoNearOp);
            AggregationResults<NewsEntity> results = mongoTemplate.aggregate(aggregation, "news_data", NewsEntity.class);
            List<NewsEntity> news = results.getMappedResults();
            log.info("Found {} news articles within {}km of ({}, {})", news.size(), maxDistanceKm, latitude, longitude);
            return news;
        } catch (Exception e) {
            log.error("Error in geospatial query: {}", e.getMessage(), e);
            return newsRepository.findAll().stream()
                    .filter(n -> n.getLatitude() != null && n.getLongitude() != null)
                    .filter(n -> calculateDistance(latitude, longitude, n.getLatitude(), n.getLongitude()) <= maxDistanceKm)
                    .collect(Collectors.toList());
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public List<News> getNewsByCategory(String category) {
        return getNewsByCategoryEntities(category).stream().map(newsMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<News> getNewsBySource(String source) {
        return newsRepository.findBySourceNameContainingIgnoreCase(source).stream().map(newsMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<News> getNewsByScore(Double threshold) {
        return newsRepository.findByRelevanceScoreGreaterThan(threshold).stream().map(newsMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<News> searchNews(String query) {
        return newsRepository.findByTitleOrDescriptionContaining(query).stream().map(newsMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<News> getNearbyNews(Double latitude, Double longitude, Double maxDistanceKm) {
        return getNearbyNewsEntities(latitude, longitude, maxDistanceKm).stream().map(newsMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<News> getAllNews() {
        return newsRepository.findAll().stream().map(newsMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public News saveNews(News news) {
        NewsEntity entity = newsMapper.toEntity(news);
        return newsMapper.toDto(newsRepository.save(entity));
    }

    @Override
    public List<News> saveAllNews(List<News> newsList) {
        List<NewsEntity> entities = newsList.stream().map(newsMapper::toEntity).collect(Collectors.toList());
        return newsRepository.saveAll(entities).stream().map(newsMapper::toDto).collect(Collectors.toList());
    }
}
