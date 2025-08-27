package com.example.inshorts.dto;

import com.example.inshorts.entity.News;
import java.util.List;

public class NewsResponse {
    
    private List<News> articles;
    private int totalCount;
    private String query;
    private String intent;
    private String entities;
    private String concepts;
    private long processingTimeMs;
    
    // Constructors
    public NewsResponse() {}
    
    public NewsResponse(List<News> articles, int totalCount, String query, 
                       String intent, String entities, String concepts, long processingTimeMs) {
        this.articles = articles;
        this.totalCount = totalCount;
        this.query = query;
        this.intent = intent;
        this.entities = entities;
        this.concepts = concepts;
        this.processingTimeMs = processingTimeMs;
    }
    
    // Getters and Setters
    public List<News> getArticles() {
        return articles;
    }
    
    public void setArticles(List<News> articles) {
        this.articles = articles;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getIntent() {
        return intent;
    }
    
    public void setIntent(String intent) {
        this.intent = intent;
    }
    
    public String getEntities() {
        return entities;
    }
    
    public void setEntities(String entities) {
        this.entities = entities;
    }
    
    public String getConcepts() {
        return concepts;
    }
    
    public void setConcepts(String concepts) {
        this.concepts = concepts;
    }
    
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}
