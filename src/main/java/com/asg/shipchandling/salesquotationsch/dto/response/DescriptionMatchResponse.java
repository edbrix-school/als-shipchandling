package com.asg.shipchandling.salesquotationsch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response for match-items-by-description endpoint.
 * Returns list of matched STOCK_POID with quantity and unit in JSON format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescriptionMatchResponse {
    private boolean success;
    private String message;
    /** Total data rows processed from Excel */
    private int totalRows;
    /** Rows that matched a product above threshold */
    private int matchedRows;
    /** Rows that did not match or had errors */
    private int unmatchedRows;
    /** Similarity threshold used (e.g. 0.85) */
    private double similarityThreshold;
    /** Per-row match details */
    private List<DescriptionMatchItemDto> items;
}
