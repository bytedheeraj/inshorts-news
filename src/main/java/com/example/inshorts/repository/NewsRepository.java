package com.example.inshorts.repository;

import com.example.inshorts.entity.News;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends MongoRepository<News, String> {
    
    // Find news by category (case-insensitive)
    List<News> findByCategoryContainingIgnoreCase(String category);
    
    // Find news by source name (case-insensitive)
    List<News> findBySourceNameContainingIgnoreCase(String sourceName);
    
    // Find news with relevance score above threshold
    List<News> findByRelevanceScoreGreaterThan(Double threshold);
    
    // Search news by title or description (case-insensitive)
    @Query("{'$or': [{'title': {'$regex': ?0, '$options': 'i'}}, {'description': {'$regex': ?0, '$options': 'i'}}]}")
    List<News> findByTitleOrDescriptionContaining(String searchTerm);
    
    // Find news by exact category
    List<News> findByCategory(String category);
    
    // Find nearby news using MongoDB's $geoNear aggregation
    @Query(value = "{}", fields = "{'id': 1, 'title': 1, 'description': 1, 'url': 1, 'publicationDate': 1, 'sourceName': 1, 'category': 1, 'relevanceScore': 1, 'latitude': 1, 'longitude': 1, 'distance': 1}")
    List<News> findNearbyNews(Double latitude, Double longitude, Double maxDistance);
}
