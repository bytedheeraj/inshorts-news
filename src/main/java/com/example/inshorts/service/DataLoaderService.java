package com.example.inshorts.service;

import com.example.inshorts.entity.NewsEntity;
import com.example.inshorts.repository.NewsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
public class DataLoaderService implements CommandLineRunner {

    @Autowired
    private NewsRepository newsRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting to load news data from JSON file...");
        try {
            List<NewsEntity> newsEntityList = loadNewsFromJson();

            if (newsEntityList != null && !newsEntityList.isEmpty()) {
                // Clear existing data
                newsRepository.deleteAll();
                log.info("Cleared existing news data");

                // Save all news articles
                List<NewsEntity> savedNews = newsRepository.saveAll(newsEntityList);
                log.info("Successfully loaded {} news articles into MongoDB", savedNews.size());

                // Log some sample data
                savedNews.stream().limit(3).forEach(news ->
                    log.info("Loaded: {} - {}", news.getTitle(), news.getSourceName())
                );
            } else {
                log.warn("No news data found in JSON file");
            }

        } catch (Exception e) {
            log.error("Error loading news data: {}", e.getMessage(), e);
        }
    }

    private List<NewsEntity> loadNewsFromJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        ClassPathResource resource = new ClassPathResource("news_data.json");

        try (InputStream inputStream = resource.getInputStream()) {
            List<NewsEntity> newsEntityList = mapper.readValue(inputStream, new TypeReference<List<NewsEntity>>() {});
            log.info("Parsed {} news articles from JSON file", newsEntityList.size());
            return newsEntityList;
        } catch (IOException e) {
            log.error("Failed to read news_data.json: {}", e.getMessage());
            throw e;
        }
    }
}
