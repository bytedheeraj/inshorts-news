package com.example.inshorts.controller;

import com.example.inshorts.dto.NewsQueryRequest;
import com.example.inshorts.dto.NewsResponse;
import com.example.inshorts.entity.News;
import com.example.inshorts.service.NewsService;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
public class NewsController {
    
    @Autowired
    private NewsService newsService;
    
    /**
     * Main endpoint for contextual news retrieval
     */
    @PostMapping("/query")
    public ResponseEntity<NewsResponse> processNewsQuery(@Valid @RequestBody NewsQueryRequest request) {

        NewsResponse response = newsService.processNewsQuery(request);
        System.out.println("starting query....");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get news by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<News>> getNewsByCategory(@PathVariable String category) {
        List<News> news = newsService.getNewsByCategory(category);
        return ResponseEntity.ok(news);
    }
    
    /**
     * Get news by source
     */
    @GetMapping("/source/{source}")
    public ResponseEntity<List<News>> getNewsBySource(@PathVariable String source) {
        List<News> news = newsService.getNewsBySource(source);
        return ResponseEntity.ok(news);
    }
    
    /**
     * Get news by relevance score threshold
     */
    @GetMapping("/score")
    public ResponseEntity<List<News>> getNewsByScore(@RequestParam(defaultValue = "0.7") Double threshold) {
        List<News> news = newsService.getNewsByScore(threshold);
        return ResponseEntity.ok(news);
    }
    
    /**
     * Search news by text query
     */
    @GetMapping("/search")
    public ResponseEntity<List<News>> searchNews(@RequestParam String q) {
        List<News> news = newsService.searchNews(q);
        return ResponseEntity.ok(news);
    }
    
    /**
     * Get nearby news within specified radius
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<News>> getNearbyNews(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radius) {
        List<News> news = newsService.getNearbyNews(latitude, longitude, radius);
        return ResponseEntity.ok(news);
    }
    
    /**
     * Get all news articles
     */
    @GetMapping("/all")
    public ResponseEntity<List<News>> getAllNews() {
        List<News> news = newsService.getAllNews();
        return ResponseEntity.ok(news);
    }
    
    /**
     * Save a single news article
     */
    @PostMapping("/save")
    public ResponseEntity<News> saveNews(@RequestBody News news) {
        News savedNews = newsService.saveNews(news);
        return ResponseEntity.ok(savedNews);
    }
    
    /**
     * Save multiple news articles
     */
    @PostMapping("/save-all")
    public ResponseEntity<List<News>> saveAllNews(@RequestBody List<News> newsList) {
        List<News> savedNews = newsService.saveAllNews(newsList);
        return ResponseEntity.ok(savedNews);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("News Service is running!");
    }
}
