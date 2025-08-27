package com.example.inshorts.service;

import com.example.inshorts.entity.News;
import com.example.inshorts.repository.NewsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class DataLoaderService implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataLoaderService.class);
    
    @Autowired
    private NewsRepository newsRepository;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting to load news data from JSON file...");
        
        try {
            // Load news data from JSON file
            List<News> newsList = loadNewsFromJson();
            
            if (newsList != null && !newsList.isEmpty()) {
                // Clear existing data
                newsRepository.deleteAll();
                logger.info("Cleared existing news data");
                
                // Save all news articles
                List<News> savedNews = newsRepository.saveAll(newsList);
                logger.info("Successfully loaded {} news articles into MongoDB", savedNews.size());
                
                // Log some sample data
                savedNews.stream().limit(3).forEach(news -> 
                    logger.info("Loaded: {} - {}", news.getTitle(), news.getSourceName())
                );
            } else {
                logger.warn("No news data found in JSON file");
            }
            
        } catch (Exception e) {
            logger.error("Error loading news data: {}", e.getMessage(), e);
        }
    }
    
    private List<News> loadNewsFromJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        // Load JSON file from classpath
        ClassPathResource resource = new ClassPathResource("news_data.json");
        
        try (InputStream inputStream = resource.getInputStream()) {
            List<News> newsList = mapper.readValue(inputStream, new TypeReference<List<News>>() {});
            logger.info("Parsed {} news articles from JSON file", newsList.size());
            return newsList;
        } catch (IOException e) {
            logger.error("Failed to read news_data.json: {}", e.getMessage());
            throw e;
        }
    }
}
