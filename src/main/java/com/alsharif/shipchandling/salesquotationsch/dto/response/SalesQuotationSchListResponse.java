package com.alsharif.shipchandling.salesquotationsch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.alsharif.shipchandling.salesquotationsch.dto.SalesQuotationSchSummaryDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchListResponse {
    private List<SalesQuotationSchSummaryDto> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}

