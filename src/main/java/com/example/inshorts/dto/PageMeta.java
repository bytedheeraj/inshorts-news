package com.example.inshorts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageMeta {
    private int page;
    private int pageSize;
    private int totalPages;
    private long totalCount;
}

