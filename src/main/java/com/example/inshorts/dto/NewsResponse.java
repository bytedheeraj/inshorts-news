package com.example.inshorts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsResponse {
	
	private List<News> articles;
	private int totalCount;
	private String query;
	private String intent;
	private String entities;
	private String category;
	private long processingTimeMs;
	private PageMeta meta;
}
