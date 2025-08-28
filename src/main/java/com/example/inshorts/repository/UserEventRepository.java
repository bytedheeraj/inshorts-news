package com.example.inshorts.repository;

import com.example.inshorts.entity.UserEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserEventRepository extends MongoRepository<UserEvent, String> {

    // Find events for a specific article
    List<UserEvent> findByArticleId(String articleId);

    // Find events within a time range
    List<UserEvent> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);

    // Find events for a specific article within time range
    List<UserEvent> findByArticleIdAndTimestampBetween(String articleId, LocalDateTime startTime, LocalDateTime endTime);

    // Find events by type within a time range
    List<UserEvent> findByEventTypeAndTimestampBetween(UserEvent.EventType eventType, 
                                                      LocalDateTime startTime, LocalDateTime endTime);

    // Find events within geographical bounds
    @Query("{'user_latitude': {$gte: ?0, $lte: ?1}, 'user_longitude': {$gte: ?2, $lte: ?3}}")
    List<UserEvent> findByLocationBounds(Double minLat, Double maxLat, Double minLon, Double maxLon);

    // Find events within geographical bounds and time range
    @Query("{'user_latitude': {$gte: ?0, $lte: ?1}, 'user_longitude': {$gte: ?2, $lte: ?3}, 'timestamp': {$gte: ?4, $lte: ?5}}")
    List<UserEvent> findByLocationBoundsAndTimeRange(Double minLat, Double maxLat, Double minLon, Double maxLon,
                                                    LocalDateTime startTime, LocalDateTime endTime);

    // Count events for an article within time range
    long countByArticleIdAndTimestampBetween(String articleId, LocalDateTime startTime, LocalDateTime endTime);

    // Find recent events for trending calculation
    @Query(value = "{}", sort = "{'timestamp': -1}")
    List<UserEvent> findRecentEvents(LocalDateTime since);
}
