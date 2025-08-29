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
    
    // Nearby news methods - these will be implemented using MongoTemplate in the service
    // since MongoDB doesn't support pagination with $geoNear in repository methods
    // The service implementation handles the geospatial queries with pagination
}
