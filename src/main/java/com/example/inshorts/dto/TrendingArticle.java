package com.example.inshorts.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrendingArticle {

    private String id;
    private String title;
    private String description;
    private String url;

    @JsonProperty("publication_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime publicationDate;

    @JsonProperty("source_name")
    private String sourceName;

    private List<String> category;

    @JsonProperty("relevance_score")
    private Double relevanceScore;

    @JsonProperty("llm_summary")
    private String llmSummary;

    private Double latitude;
    private Double longitude;

    @JsonProperty("trending_score")
    private Double trendingScore;

    @JsonProperty("user_engagement_count")
    private Long userEngagementCount;

}

