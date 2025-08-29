package com.example.inshorts;

import com.example.inshorts.entity.NewsEntity;
import com.example.inshorts.service.NewsMapper;
import com.example.inshorts.dto.News;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InshortsApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testPointCreationAndExtraction() {
		// Test Point creation and coordinate extraction
		Point point = new Point(-122.4194, 37.7749); // longitude, latitude
		
		assertEquals(-122.4194, point.getX()); // longitude
		assertEquals(37.7749, point.getY());   // latitude
		
		// Test NewsEntity with Point
		NewsEntity entity = new NewsEntity();
		entity.setLocation(point);
		entity.setTitle("Test Article");
		
		assertNotNull(entity.getLocation());
		assertEquals(-122.4194, entity.getLocation().getX());
		assertEquals(37.7749, entity.getLocation().getY());
	}
	
	@Test
	void testNewsMapperWithPoint() {
		NewsMapper mapper = new NewsMapper();
		
		// Create entity with Point
		NewsEntity entity = new NewsEntity();
		entity.setId("test-1");
		entity.setTitle("Test Article");
		entity.setLocation(new Point(-122.4194, 37.7749));
		
		// Convert to DTO
		News dto = mapper.toDto(entity);
		
		assertNotNull(dto);
		assertEquals(-122.4194, dto.getLongitude());
		assertEquals(37.7749, dto.getLatitude());
		
		// Convert back to entity
		NewsEntity convertedEntity = mapper.toEntity(dto);
		
		assertNotNull(convertedEntity.getLocation());
		assertEquals(-122.4194, convertedEntity.getLocation().getX());
		assertEquals(37.7749, convertedEntity.getLocation().getY());
	}
}
