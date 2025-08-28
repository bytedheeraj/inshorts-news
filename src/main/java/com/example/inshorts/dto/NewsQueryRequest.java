package com.example.inshorts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewsQueryRequest {
	
	@NotBlank(message = "Query is required")
	private String query;
	
	@NotNull(message = "Latitude is required")
	private Double latitude;
	
	@NotNull(message = "Longitude is required")
	private Double longitude;
	
	// Optional fields for direct API usage
	private String entities;
	private String category;
	private String intent;

}
