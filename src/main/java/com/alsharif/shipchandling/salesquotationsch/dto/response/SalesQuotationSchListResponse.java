package com.alsharif.shipchandling.salesquotationsch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import com.alsharif.shipchandling.salesquotationsch.dto.SalesQuotationSchSummaryDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchListResponse {
    private List<SalesQuotationSchSummaryDto> content;
    private boolean last;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    private Map<String, String> displayFields;
    private int pageNumber;
}

