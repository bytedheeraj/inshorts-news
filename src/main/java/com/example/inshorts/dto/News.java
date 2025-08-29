package com.example.inshorts.dto;

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
public class News {

    private String id;

    private String title;
    private String description;

    private String url;
    private LocalDateTime publicationDate;

    private String sourceName;

    private List<String> category;

    private Double relevanceScore;

    private Double latitude;

    private Double longitude;
    
    private String llmSummary;
}
