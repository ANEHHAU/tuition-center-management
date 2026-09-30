package com.tuition.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseSearchRequest {
    private String keyword;      // tìm theo fullName OR email OR phone
    
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 20;
    
    @Builder.Default
    private String sortBy = "name";   // "name" | "createdAt" | "email" | ...
    
    @Builder.Default
    private String sortDir = "asc";   // "asc" | "desc"
}
