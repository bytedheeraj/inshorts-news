package com.example.inshorts.service.impl;

import com.example.inshorts.dto.NewsQueryRequest;
import com.example.inshorts.exception.LLMServiceException;
import com.example.inshorts.service.LLMService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Primary
public class RealLLMServiceImpl implements LLMService {
    
    private static final Logger logger = LoggerFactory.getLogger(RealLLMServiceImpl.class);
    
    @Value("${llm.huggingface.url}")
    private String huggingFaceApiUrl;

    @Value("${llm.huggingface.token}")
    private String huggingFaceApiKey;
    
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public RealLLMServiceImpl() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public NewsQueryRequest processQuery(NewsQueryRequest request) {
        try {
            String intent = determineIntent(request.getQuery());
            String entities = extractEntities(request.getQuery());
            String category = extractConcepts(request.getQuery());

            request.setIntent(intent);
            request.setEntities(entities);
            request.setCategory(category);

            logger.info("LLM Processing completed - Entities: {}, Category: {}, Intent: {}", entities, category, intent);
            return request;
        } catch (Exception e) {
            logger.error("Error calling real LLM service: {}", e.getMessage());
            throw new LLMServiceException("Failed to process query with LLM", e);
        }
    }
    
    @Override
    public String extractEntities(String query) {
        try {
            String prompt = String.format(
                "Extract named entities from this text. Return only the entities as a JSON array. " +
                "Include people names, organizations, locations, and events. " +
                "Text: \"%s\"", query
            );
            
            String response = callHuggingFaceAPI(prompt);
            List<String> entities = parseEntitiesFromResponse(response);
            return String.join(", ", entities);
            
        } catch (Exception e) {
            logger.error("Error extracting entities with LLM", e);
            throw new LLMServiceException("Failed to extract entities", e);
        }
    }
    
    @Override
    public String extractConcepts(String query) {
        try {
            String prompt = String.format(
                "Extract key concepts and topics from this text. Return only the concepts as a JSON array. " +
                "Include business terms, technology concepts, news categories, etc. " +
                "Text: \"%s\"", query
            );
            
            String response = callHuggingFaceAPI(prompt);
            List<String> concepts = parseConceptsFromResponse(response);
            return String.join(", ", concepts);
            
        } catch (Exception e) {
            logger.error("Error extracting concepts with LLM", e);
            throw new LLMServiceException("Failed to extract concepts", e);
        }
    }
    
    @Override
    public String determineIntent(String query) {
        try {
            String prompt = String.format(
                "Determine the user's intent from this query. Return only one of: category, source, nearby, score, search. " +
                "category: user wants news from specific category like technology, business, sports. " +
                "source: user wants news from specific source like New York Times, Reuters. " +
                "nearby: user wants news near a specific location. " +
                "score: user wants high relevance score news. " +
                "search: user wants to search for specific terms. " +
                "Query: \"%s\"", query
            );
            
            String response = callHuggingFaceAPI(prompt);
            return parseIntentFromResponse(response);
            
        } catch (Exception e) {
            logger.error("Error determining intent with LLM", e);
            throw new LLMServiceException("Failed to determine intent", e);
        }
    }
    
    @Override
    public String generateSummary(String text) {
        try {
            String prompt = String.format(
                "Summarize this news article in 2-3 sentences: %s", text
            );
            
            String response = callHuggingFaceAPI(prompt);
            return response.length() > 200 ? response.substring(0, 200) + "..." : response;
            
        } catch (Exception e) {
            logger.error("Error generating summary with LLM", e);
            throw new LLMServiceException("Failed to generate summary", e);
        }
    }
    
    private String callHuggingFaceAPI(String prompt) throws IOException {
        // Create JSON payload for Hugging Face API
        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", prompt);
        payload.put("parameters", Map.of("max_length", 100, "temperature", 0.7));
        
        String jsonPayload = objectMapper.writeValueAsString(payload);
        
        RequestBody body = RequestBody.create(
            jsonPayload, 
            MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(huggingFaceApiUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + huggingFaceApiKey)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }
            
            String responseBody = response.body().string();
            logger.debug("Hugging Face API response: {}", responseBody);
            return responseBody;
            
        } catch (Exception e) {
            logger.error("Error calling Hugging Face API", e);
            throw e;
        }
    }
    
    private List<String> parseEntitiesFromResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            List<String> entities = new ArrayList<>();
            
            // Parse the response based on Hugging Face API format
            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    if (node.has("generated_text")) {
                        String text = node.get("generated_text").asText();
                        // Extract entities from the generated text
                        entities.addAll(extractEntitiesFromText(text));
                    }
                }
            }
            
            return entities.isEmpty() ? extractEntitiesFallbackList("") : entities;
            
        } catch (Exception e) {
            logger.error("Error parsing entities from LLM response", e);
            return extractEntitiesFallbackList("");
        }
    }
    
    private List<String> parseConceptsFromResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            List<String> concepts = new ArrayList<>();
            
            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    if (node.has("generated_text")) {
                        String text = node.get("generated_text").asText();
                        concepts.addAll(extractConceptsFromText(text));
                    }
                }
            }
            
            return concepts.isEmpty() ? extractConceptsFallbackList("") : concepts;
            
        } catch (Exception e) {
            logger.error("Error parsing concepts from LLM response", e);
            return extractConceptsFallbackList("");
        }
    }
    
    private String parseIntentFromResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            
            if (jsonNode.isArray()) {
                for (JsonNode node : jsonNode) {
                    if (node.has("generated_text")) {
                        String text = node.get("generated_text").asText().toLowerCase();
                        
                        // Extract intent from the generated text
                        if (text.contains("category")) return "category";
                        if (text.contains("source")) return "source";
                        if (text.contains("nearby")) return "nearby";
                        if (text.contains("score")) return "score";
                        if (text.contains("search")) return "search";
                    }
                }
            }
            
            return determineIntentFallback("");
            
        } catch (Exception e) {
            logger.error("Error parsing intent from LLM response", e);
            return determineIntentFallback("");
        }
    }
    
    private List<String> extractEntitiesFromText(String text) {
        List<String> entities = new ArrayList<>();
        
        // Simple entity extraction from LLM response
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.matches("\\b[A-Z][a-z]+\\s+[A-Z][a-z]+\\b")) {
                entities.add(word);
            }
        }
        
        return entities;
    }
    
    private List<String> extractConceptsFromText(String text) {
        List<String> concepts = new ArrayList<>();
        
        // Simple concept extraction from LLM response
        String[] words = text.toLowerCase().split("\\s+");
        for (String word : words) {
            if (word.length() > 3 && !word.matches("\\b(the|and|for|with|from|this|that|they|have|will|been|were)\\b")) {
                concepts.add(word);
            }
        }
        
        return concepts;
    }
    
    // Fallback methods using keyword matching
    private String extractEntitiesFallback(String query) {
        List<String> entities = extractEntitiesFallbackList(query);
        return String.join(", ", entities);
    }
    
    private List<String> extractEntitiesFallbackList(String query) {
        List<String> entities = new ArrayList<>();
        
        // Person names
        String[] personPatterns = {"\\b[A-Z][a-z]+\\s+[A-Z][a-z]+\\b"};
        for (String pattern : personPatterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(query);
            while (m.find()) {
                entities.add(m.group());
            }
        }
        
        // Organizations
        String[] organizations = {"Twitter", "Google", "Apple", "Microsoft", "New York Times", "Reuters", "CNN", "BBC", "DW"};
        for (String org : organizations) {
            if (query.toLowerCase().contains(org.toLowerCase())) {
                entities.add(org);
            }
        }
        
        // Locations
        String[] locations = {"Palo Alto", "San Francisco", "New York", "London", "Paris", "Tokyo"};
        for (String location : locations) {
            if (query.toLowerCase().contains(location.toLowerCase())) {
                entities.add(location);
            }
        }
        
        return entities;
    }
    
    private String extractConceptsFallback(String query) {
        List<String> concepts = extractConceptsFallbackList(query);
        return String.join(", ", concepts);
    }
    
    private List<String> extractConceptsFallbackList(String query) {
        List<String> concepts = new ArrayList<>();
        
        String[] businessTerms = {"acquisition", "merger", "technology", "business", "finance", "investment"};
        String[] newsCategories = {"news", "latest", "developments", "updates", "breaking"};
        String[] techTerms = {"AI", "artificial intelligence", "machine learning", "software", "hardware"};
        String[] worldCategories = {"world", "international", "global", "foreign"};
        String[] nationalCategories = {"national", "domestic", "local", "country"};
        String[] sportsCategories = {"sports", "football", "soccer", "basketball", "tennis"};
        
        String lowerQuery = query.toLowerCase();
        
        for (String term : businessTerms) {
            if (lowerQuery.contains(term)) concepts.add(term);
        }
        for (String term : newsCategories) {
            if (lowerQuery.contains(term)) concepts.add(term);
        }
        for (String term : techTerms) {
            if (lowerQuery.contains(term)) concepts.add(term);
        }
        for (String term : worldCategories) {
            if (lowerQuery.contains(term)) concepts.add(term);
        }
        for (String term : nationalCategories) {
            if (lowerQuery.contains(term)) concepts.add(term);
        }
        for (String term : sportsCategories) {
            if (lowerQuery.contains(term)) concepts.add(term);
        }
        
        return concepts;
    }
    
    private String determineIntentFallback(String query) {
        String lowerQuery = query.toLowerCase();
        
        // Intent detection based on keywords - check more specific intents first
        if (lowerQuery.contains("score") || lowerQuery.contains("relevance") || 
            lowerQuery.contains("high quality") || lowerQuery.contains("top") ||
            lowerQuery.contains("high relevance")) {
            return "score";
        }
        
        if (lowerQuery.contains("new york times") || lowerQuery.contains("reuters") || 
            lowerQuery.contains("cnn") || lowerQuery.contains("bbc") ||
            lowerQuery.contains("source") || lowerQuery.contains("from")) {
            return "source";
        }
        
        if (lowerQuery.contains("near") || lowerQuery.contains("location") || 
            lowerQuery.contains("palo alto") || lowerQuery.contains("san francisco") ||
            lowerQuery.contains("nearby") || lowerQuery.contains("around")) {
            return "nearby";
        }
        
        if (lowerQuery.contains("technology") || lowerQuery.contains("business") || 
            lowerQuery.contains("sports") || lowerQuery.contains("politics") || 
            lowerQuery.contains("world") || lowerQuery.contains("national") ||
            lowerQuery.contains("find") || lowerQuery.contains("show") ||
            lowerQuery.contains("get") || lowerQuery.contains("category")) {
            return "category";
        }
        
        return "search"; // default
    }
}
