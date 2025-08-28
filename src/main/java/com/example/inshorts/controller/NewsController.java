package com.example.inshorts.controller;

import com.example.inshorts.dto.ApiResponse;
import com.example.inshorts.dto.News;
import com.example.inshorts.dto.NewsQueryRequest;
import com.example.inshorts.dto.NewsResponse;
import com.example.inshorts.dto.PageMeta;
import com.example.inshorts.exception.NewsServiceException;
import com.example.inshorts.exception.ValidationException;
import com.example.inshorts.service.NewsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
@Slf4j
public class NewsController {
    
    @Autowired
    private NewsService newsService;
    
    /**
     * Main endpoint for contextual news retrieval
     */
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<NewsResponse>> processNewsQuery(@Valid @RequestBody NewsQueryRequest request) {
        log.info("Processing news query: {}", request.getQuery());
        
        try {
            // Validate request parameters
            validateNewsQueryRequest(request);
            
            NewsResponse response = newsService.processNewsQuery(request);
            
            // Attach simple meta (no pagination here)
            response.setMeta(PageMeta.builder()
                    .page(1)
                    .pageSize(response.getArticles() != null ? response.getArticles().size() : 0)
                    .totalPages(1)
                    .totalCount(response.getTotalCount())
                    .build());
            
            log.info("Query processed successfully. Found {} articles", response.getTotalCount());
            return ResponseEntity.ok(ApiResponse.ok(response, response.getMeta()));
            
        } catch (ValidationException e) {
            log.warn("Validation error in news query: {}", e.getMessage());
            throw e; // Let GlobalExceptionHandler handle it
        } catch (Exception ex) {
            log.error("Error processing news query: {}", ex.getMessage(), ex);
            throw new NewsServiceException("Failed to process news query", ex);
        }
    }
    
    /**
     * Get news by category (sorted by publicationDate desc), paginated
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<News>>> getNewsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        log.info("Fetching news by category: {}, page: {}, size: {}", category, page, size);
        
        try {
            // Validate parameters
            validatePaginationParams(page, size);
            validateCategory(category);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<News> newsPage = newsService.getNewsByCategory(category, pageable);
            
            PageMeta meta = PageMeta.builder()
                    .page(page)
                    .pageSize(size)
                    .totalCount((int) newsPage.getTotalElements())
                    .totalPages(newsPage.getTotalPages())
                    .build();
            
            log.info("Retrieved {} articles for category: {}", newsPage.getContent().size(), category);
            return ResponseEntity.ok(ApiResponse.ok(newsPage.getContent(), meta));
            
        } catch (ValidationException e) {
            log.warn("Validation error in category query: {}", e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error("Error fetching news by category: {}", ex.getMessage(), ex);
            throw new NewsServiceException("Failed to fetch news by category", ex);
        }
    }
    
    /**
     * Get news by source (sorted by publicationDate desc), paginated
     */
    @GetMapping("/source/{source}")
    public ResponseEntity<ApiResponse<List<News>>> getNewsBySource(
            @PathVariable String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        log.info("Fetching news by source: {}, page: {}, size: {}", source, page, size);
        
        try {
            // Validate parameters
            validatePaginationParams(page, size);
            validateSource(source);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<News> newsPage = newsService.getNewsBySource(source, pageable);
            
            PageMeta meta = PageMeta.builder()
                    .page(page)
                    .pageSize(size)
                    .totalCount((int) newsPage.getTotalElements())
                    .totalPages(newsPage.getTotalPages())
                    .build();
            
            log.info("Retrieved {} articles for source: {}", newsPage.getContent().size(), source);
            return ResponseEntity.ok(ApiResponse.ok(newsPage.getContent(), meta));
            
        } catch (ValidationException e) {
            log.warn("Validation error in source query: {}", e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error("Error fetching news by source: {}", ex.getMessage(), ex);
            throw new NewsServiceException("Failed to fetch news by source", ex);
        }
    }
    
    /**
     * Get news by relevance score threshold (sorted by relevanceScore desc), paginated
     */
    @GetMapping("/score")
    public ResponseEntity<ApiResponse<List<News>>> getNewsByScore(
            @RequestParam Double threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        log.info("Fetching news by score threshold: {}, page: {}, size: {}", threshold, page, size);
        
        try {
            // Validate parameters
            validatePaginationParams(page, size);
            validateScoreThreshold(threshold);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<News> newsPage = newsService.getNewsByScore(threshold, pageable);
            
            PageMeta meta = PageMeta.builder()
                    .page(page)
                    .pageSize(size)
                    .totalCount((int) newsPage.getTotalElements())
                    .totalPages(newsPage.getTotalPages())
                    .build();
            
            log.info("Retrieved {} articles with score >= {}", newsPage.getContent().size(), threshold);
            return ResponseEntity.ok(ApiResponse.ok(newsPage.getContent(), meta));
            
        } catch (ValidationException e) {
            log.warn("Validation error in score query: {}", e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error("Error fetching news by score: {}", ex.getMessage(), ex);
            throw new NewsServiceException("Failed to fetch news by score", ex);
        }
    }
    
    /**
     * Search news by text query (sorted by publicationDate desc as simple relevance), paginated
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<News>>> searchNews(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        log.info("Searching news with query: '{}', page: {}, size: {}", q, page, size);
        
        try {
            // Validate parameters
            validatePaginationParams(page, size);
            validateSearchQuery(q);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<News> newsPage = newsService.searchNews(q, pageable);
            
            PageMeta meta = PageMeta.builder()
                    .page(page)
                    .pageSize(size)
                    .totalCount((int) newsPage.getTotalElements())
                    .totalPages(newsPage.getTotalPages())
                    .build();
            
            log.info("Search found {} articles for query: '{}'", newsPage.getContent().size(), q);
            return ResponseEntity.ok(ApiResponse.ok(newsPage.getContent(), meta));
            
        } catch (ValidationException e) {
            log.warn("Validation error in search query: {}", e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error("Error searching news: {}", ex.getMessage(), ex);
            throw new NewsServiceException("Failed to search news", ex);
        }
    }
    
    /**
     * Get news within a certain radius (sorted by distance), paginated
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<News>>> getNearbyNews(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radius,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        log.info("Fetching nearby news at ({}, {}), radius: {}km, page: {}, size: {}", 
                latitude, longitude, radius, page, size);
        
        try {
            // Validate parameters
            validatePaginationParams(page, size);
            validateCoordinates(latitude, longitude);
            validateRadius(radius);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<News> newsPage = newsService.getNearbyNews(latitude, longitude, radius, pageable);
            
            PageMeta meta = PageMeta.builder()
                    .page(page)
                    .pageSize(size)
                    .totalCount((int) newsPage.getTotalElements())
                    .totalPages(newsPage.getTotalPages())
                    .build();
            
            log.info("Retrieved {} nearby articles", newsPage.getContent().size());
            return ResponseEntity.ok(ApiResponse.ok(newsPage.getContent(), meta));
            
        } catch (ValidationException e) {
            log.warn("Validation error in nearby query: {}", e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error("Error fetching nearby news: {}", ex.getMessage(), ex);
            throw new NewsServiceException("Failed to fetch nearby news", ex);
        }
    }
    
    /**
     * Get all news articles (sorted by publicationDate desc), paginated
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<News>>> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        log.info("Fetching all news, page: {}, size: {}", page, size);
        
        try {
            // Validate parameters
            validatePaginationParams(page, size);
            
            Pageable pageable = PageRequest.of(page, size);
            Page<News> newsPage = newsService.getAllNews(pageable);
            
            PageMeta meta = PageMeta.builder()
                    .page(page)
                    .pageSize(size)
                    .totalCount((int) newsPage.getTotalElements())
                    .totalPages(newsPage.getTotalPages())
                    .build();
            
            log.info("Retrieved {} articles from all news", newsPage.getContent().size());
            return ResponseEntity.ok(ApiResponse.ok(newsPage.getContent(), meta));
            
        } catch (ValidationException e) {
            log.warn("Validation error in all news query: {}", e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error("Error fetching all news: {}", ex.getMessage(), ex);
            throw new NewsServiceException("Failed to fetch all news", ex);
        }
    }
    
    /**
     * Save a single news article
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<News>> saveNews(@Valid @RequestBody News newsEntity) {
        log.info("Saving news article: {}", newsEntity.getTitle());
        
        try {
            News savedNews = newsService.saveNews(newsEntity);
            log.info("Successfully saved news article with ID: {}", savedNews.getId());
            return ResponseEntity.ok(ApiResponse.ok(savedNews, null));
            
        } catch (Exception ex) {
            log.error("Error saving news article: {}", ex.getMessage(), ex);
            throw new NewsServiceException("Failed to save news article", ex);
        }
    }
    
    /**
     * Save multiple news articles
     */
    @PostMapping("/save-all")
    public ResponseEntity<ApiResponse<List<News>>> saveAllNews(@Valid @RequestBody List<News> newsEntityList) {
        log.info("Saving {} news articles", newsEntityList.size());
        
        try {
            List<News> savedNews = newsService.saveAllNews(newsEntityList);
            log.info("Successfully saved {} news articles", savedNews.size());
            return ResponseEntity.ok(ApiResponse.ok(savedNews, null));
            
        } catch (Exception ex) {
            log.error("Error saving news articles: {}", ex.getMessage(), ex);
            throw new NewsServiceException("Failed to save news articles", ex);
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("News service is running");
    }
    
    // Validation methods
    private void validateNewsQueryRequest(NewsQueryRequest request) {
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new ValidationException("Query cannot be empty", "query");
        }
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new ValidationException("Latitude and longitude are required", "coordinates");
        }
        validateCoordinates(request.getLatitude(), request.getLongitude());
    }
    
    private void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new ValidationException("Page number must be non-negative", "page");
        }
        if (size <= 0 || size > 100) {
            throw new ValidationException("Page size must be between 1 and 100", "size");
        }
    }
    
    private void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Category cannot be empty", "category");
        }
    }
    
    private void validateSource(String source) {
        if (source == null || source.trim().isEmpty()) {
            throw new ValidationException("Source cannot be empty", "source");
        }
    }
    
    private void validateScoreThreshold(Double threshold) {
        if (threshold == null || threshold < 0.0 || threshold > 1.0) {
            throw new ValidationException("Score threshold must be between 0.0 and 1.0", "threshold");
        }
    }
    
    private void validateSearchQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new ValidationException("Search query cannot be empty", "q");
        }
        if (query.trim().length() < 2) {
            throw new ValidationException("Search query must be at least 2 characters", "q");
        }
    }
    
    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new ValidationException("Latitude must be between -90 and 90", "latitude");
        }
        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new ValidationException("Longitude must be between -180 and 180", "longitude");
        }
    }
    
    private void validateRadius(Double radius) {
        if (radius == null || radius <= 0 || radius > 1000) {
            throw new ValidationException("Radius must be between 0 and 1000 km", "radius");
        }
    }
}
