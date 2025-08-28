package com.example.inshorts.controller;

import com.example.inshorts.dto.ApiResponse;
import com.example.inshorts.dto.News;
import com.example.inshorts.dto.NewsQueryRequest;
import com.example.inshorts.dto.NewsResponse;
import com.example.inshorts.dto.PageMeta;

import com.example.inshorts.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/news")
public class NewsController {
    
    @Autowired
    private NewsService newsService;
    
    /**
     * Main endpoint for contextual news retrieval
     */
    @PostMapping("/query")
    public ResponseEntity<ApiResponse<NewsResponse>> processNewsQuery(@Valid @RequestBody NewsQueryRequest request) {
        try {
            NewsResponse response = newsService.processNewsQuery(request);
            // Attach simple meta (no pagination here)
            response.setMeta(PageMeta.builder()
                    .page(1)
                    .pageSize(response.getArticles() != null ? response.getArticles().size() : 0)
                    .totalPages(1)
                    .totalCount(response.getTotalCount())
                    .build());
            return ResponseEntity.ok(ApiResponse.ok(response, response.getMeta()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
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
        List<News> list = newsService.getNewsByCategory(category);
        list.sort(Comparator.comparing(News::getPublicationDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        int from = Math.min(page * size, list.size());
        int to = Math.min(from + size, list.size());
        List<News> slice = list.subList(from, to);
        PageMeta meta = PageMeta.builder()
                .page(page)
                .pageSize(size)
                .totalCount(list.size())
                .totalPages((int) Math.ceil((double) list.size() / size))
                .build();
        return ResponseEntity.ok(ApiResponse.ok(slice, meta));
    }
    
    /**
     * Get news by source (sorted by publicationDate desc), paginated
     */
    @GetMapping("/source/{source}")
    public ResponseEntity<ApiResponse<List<News>>> getNewsBySource(
            @PathVariable String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        List<News> list = newsService.getNewsBySource(source);
        list.sort(Comparator.comparing(News::getPublicationDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        int from = Math.min(page * size, list.size());
        int to = Math.min(from + size, list.size());
        List<News> slice = list.subList(from, to);
        PageMeta meta = PageMeta.builder()
                .page(page)
                .pageSize(size)
                .totalCount(list.size())
                .totalPages((int) Math.ceil((double) list.size() / size))
                .build();
        return ResponseEntity.ok(ApiResponse.ok(slice, meta));
    }
    
    /**
     * Get news by relevance score threshold (sorted by relevanceScore desc), paginated
     */
    @GetMapping("/score")
    public ResponseEntity<ApiResponse<List<News>>> getNewsByScore(
            @RequestParam(defaultValue = "0.7") Double threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        List<News> list = newsService.getNewsByScore(threshold);
        list.sort(Comparator.comparing(News::getRelevanceScore, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        int from = Math.min(page * size, list.size());
        int to = Math.min(from + size, list.size());
        List<News> slice = list.subList(from, to);
        PageMeta meta = PageMeta.builder()
                .page(page)
                .pageSize(size)
                .totalCount(list.size())
                .totalPages((int) Math.ceil((double) list.size() / size))
                .build();
        return ResponseEntity.ok(ApiResponse.ok(slice, meta));
    }
    
    /**
     * Search news by text query (sorted by publicationDate desc as simple relevance), paginated
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<News>>> searchNews(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        List<News> list = newsService.searchNews(q);
        list.sort(Comparator.comparing(News::getPublicationDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        int from = Math.min(page * size, list.size());
        int to = Math.min(from + size, list.size());
        List<News> slice = list.subList(from, to);
        PageMeta meta = PageMeta.builder()
                .page(page)
                .pageSize(size)
                .totalCount(list.size())
                .totalPages((int) Math.ceil((double) list.size() / size))
                .build();
        return ResponseEntity.ok(ApiResponse.ok(slice, meta));
    }
    
    /**
     * Get nearby news within specified radius (sorted by publicationDate desc), paginated
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<News>>> getNearbyNews(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radius,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        List<News> list = newsService.getNearbyNews(latitude, longitude, radius);
        list.sort(Comparator.comparing(News::getPublicationDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        int from = Math.min(page * size, list.size());
        int to = Math.min(from + size, list.size());
        List<News> slice = list.subList(from, to);
        PageMeta meta = PageMeta.builder()
                .page(page)
                .pageSize(size)
                .totalCount(list.size())
                .totalPages((int) Math.ceil((double) list.size() / size))
                .build();
        return ResponseEntity.ok(ApiResponse.ok(slice, meta));
    }
    
    /**
     * Get all news articles (paginated)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<News>>> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        List<News> list = newsService.getAllNews();
        list.sort(Comparator.comparing(News::getPublicationDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        int from = Math.min(page * size, list.size());
        int to = Math.min(from + size, list.size());
        List<News> slice = list.subList(from, to);
        PageMeta meta = PageMeta.builder()
                .page(page)
                .pageSize(size)
                .totalCount(list.size())
                .totalPages((int) Math.ceil((double) list.size() / size))
                .build();
        return ResponseEntity.ok(ApiResponse.ok(slice, meta));
    }
    
    /**
     * Save a single news article
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<News>> saveNews(@RequestBody News newsEntity) {
        News savedNews = newsService.saveNews(newsEntity);
        return ResponseEntity.ok(ApiResponse.ok(savedNews, null));
    }
    
    /**
     * Save multiple news articles
     */
    @PostMapping("/save-all")
    public ResponseEntity<ApiResponse<List<News>>> saveAllNews(@RequestBody List<News> newsEntityList) {
        List<News> savedNews = newsService.saveAllNews(newsEntityList);
        return ResponseEntity.ok(ApiResponse.ok(savedNews, null));
    }
}
