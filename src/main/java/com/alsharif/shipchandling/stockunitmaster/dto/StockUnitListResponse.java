package com.alsharif.shipchandling.stockunitmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUnitListResponse {
    private List<StockUnitMasterDto> content;
    private boolean last;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private Map<String, String> displayFields;
    private int pageNumber;
}

