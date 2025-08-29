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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
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

            List<News> dtoArticles = articles == null ? List.of() : articles.stream()
                .map(newsMapper::toDto)
                .map(this::addLLMSummary) // Add LLM summaries
                .collect(Collectors.toList());

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
    public Page<News> getNewsByCategory(String category, Pageable pageable) {
        Page<NewsEntity> newsPage = newsRepository.findByCategoryContainingIgnoreCase(
            category, 
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), 
                Sort.by("publicationDate").descending())
        );
        
        return newsPage.map(newsMapper::toDto);
    }

    @Override
    public Page<News> getNewsBySource(String source, Pageable pageable) {
        Page<NewsEntity> newsPage = newsRepository.findBySourceNameContainingIgnoreCase(
            source,
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by("publicationDate").descending())
        );
        
        return newsPage.map(newsMapper::toDto);
    }

    @Override
    public Page<News> getNewsByScore(Double threshold, Pageable pageable) {
        Page<NewsEntity> newsPage = newsRepository.findByRelevanceScoreGreaterThan(
            threshold,
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by("relevanceScore").descending())
        );
        
        return newsPage.map(newsMapper::toDto);
    }

    @Override
    public Page<News> searchNews(String query, Pageable pageable) {
        Page<NewsEntity> newsPage = newsRepository.findByTitleOrDescriptionContaining(
            query,
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by("publicationDate").descending())
        );
        
        return newsPage.map(newsMapper::toDto);
    }

    @Override
    public Page<News> getNearbyNews(Double latitude, Double longitude, Double radiusKm, Pageable pageable) {
        // Use MongoTemplate for geospatial queries with pagination
        try {
            NearQuery nearQuery = NearQuery.near(latitude, longitude)
                    .maxDistance(radiusKm * 1000)
                    .spherical(true);

            GeoNearOperation geoNearOp = Aggregation.geoNear(nearQuery, "news_data");
            
            // Add pagination and sorting
            Aggregation aggregation = Aggregation.newAggregation(
                geoNearOp,
                Aggregation.skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                Aggregation.limit(pageable.getPageSize())
            );
            
            AggregationResults<NewsEntity> results = mongoTemplate.aggregate(aggregation, "news_data", NewsEntity.class);
            List<NewsEntity> news = results.getMappedResults();
            
            // For pagination, we need to get total count separately
            long totalCount = getNearbyNewsCount(latitude, longitude, radiusKm);
            
            // Create a custom Page implementation
            return new PageImpl<>(news.stream().map(newsMapper::toDto).collect(Collectors.toList()), pageable, totalCount);
            
        } catch (Exception e) {
            log.error("Error in geospatial query with pagination: {}", e.getMessage(), e);
            // Fallback to simple filtering with pagination
            List<NewsEntity> allNearby = getNearbyNewsEntities(latitude, longitude, radiusKm);
            int start = (int) (pageable.getPageNumber() * pageable.getPageSize());
            int end = Math.min(start + pageable.getPageSize(), allNearby.size());
            List<NewsEntity> pageContent = allNearby.subList(start, end);
            
            return new PageImpl<>(pageContent.stream().map(newsMapper::toDto).collect(Collectors.toList()), pageable, allNearby.size());
        }
    }
    
    private long getNearbyNewsCount(Double latitude, Double longitude, Double radiusKm) {
        try {
            NearQuery nearQuery = NearQuery.near(latitude, longitude)
                    .maxDistance(radiusKm * 1000)
                    .spherical(true);

            GeoNearOperation geoNearOp = Aggregation.geoNear(nearQuery, "news_data");
            Aggregation aggregation = Aggregation.newAggregation(geoNearOp);
            AggregationResults<NewsEntity> results = mongoTemplate.aggregate(aggregation, "news_data", NewsEntity.class);
            return results.getMappedResults().size();
        } catch (Exception e) {
            log.error("Error getting nearby news count: {}", e.getMessage(), e);
            return getNearbyNewsEntities(latitude, longitude, radiusKm).size();
        }
    }

    @Override
    public Page<News> getAllNews(Pageable pageable) {
        Page<NewsEntity> newsPage = newsRepository.findAll(
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by("publicationDate").descending())
        );
        
        return newsPage.map(newsMapper::toDto);
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
    
    /**
     * Add LLM-generated summary to news article
     */
    private News addLLMSummary(News news) {
        try {
            String summary = llmService.generateSummary(news.getTitle() + " " + news.getDescription());
            news.setLlmSummary(summary);
        } catch (Exception e) {
            news.setLlmSummary("Summary not available");
            log.warn("Failed to generate summary for article: {}", news.getTitle());
        }
        return news;
    }
}
