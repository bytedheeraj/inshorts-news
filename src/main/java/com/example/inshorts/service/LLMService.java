package com.example.inshorts.service;

import com.example.inshorts.dto.NewsQueryRequest;

public interface LLMService {
    
    /**
     * Process a natural language query to extract entities, category, and intent
     * @param request The news query request
     * @return Processed request with extracted information
     */
    NewsQueryRequest processQuery(NewsQueryRequest request);
    
    /**
     * Extract entities from the query text
     * @param query The user's query text
     * @return Comma-separated list of entities
     */
    String extractEntities(String query);
    
    /**
     * Extract category-related terms from the query text
     * @param query The user's query text
     * @return Comma-separated list of categories
     */
    String extractConcepts(String query);
    
    /**
     * Determine the user's intent from the query
     * @param query The user's query text
     * @return The determined intent (category, score, search, source, nearby)
     */
    String determineIntent(String query);

    /**
     * Generate a summary for a given text using the LLM.
     * @param text The text to summarize.
     * @return The generated summary.
     */
    String generateSummary(String text);
}
