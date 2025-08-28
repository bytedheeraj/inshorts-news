package com.example.inshorts.repository;

import com.example.inshorts.entity.NewsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends MongoRepository<NewsEntity, String> {
    
    // Existing list methods
    List<NewsEntity> findByCategoryContainingIgnoreCase(String category);
    List<NewsEntity> findBySourceNameContainingIgnoreCase(String sourceName);
    List<NewsEntity> findByRelevanceScoreGreaterThan(Double threshold);
    List<NewsEntity> findByTitleOrDescriptionContaining(String searchTerm);
    List<NewsEntity> findByCategory(String category);
    
    // Paginated methods
    Page<NewsEntity> findByCategoryContainingIgnoreCase(String category, Pageable pageable);
    Page<NewsEntity> findBySourceNameContainingIgnoreCase(String sourceName, Pageable pageable);
    Page<NewsEntity> findByRelevanceScoreGreaterThan(Double threshold, Pageable pageable);
    Page<NewsEntity> findByTitleOrDescriptionContaining(String searchTerm, Pageable pageable);
    
    // Nearby news methods
    @Query(value = "{}", fields = "{'id': 1, 'title': 1, 'description': 1, 'url': 1, 'publicationDate': 1, 'sourceName': 1, 'category': 1, 'relevanceScore': 1, 'latitude': 1, 'longitude': 1, 'distance': 1}")
    List<NewsEntity> findNearbyNews(Double latitude, Double longitude, Double maxDistance);
    
    @Query(value = "{}", fields = "{'id': 1, 'title': 1, 'description': 1, 'url': 1, 'publicationDate': 1, 'sourceName': 1, 'category': 1, 'relevanceScore': 1, 'latitude': 1, 'longitude': 1, 'distance': 1}")
    Page<NewsEntity> findNearbyNews(Double latitude, Double longitude, Double maxDistance, Pageable pageable);
}
