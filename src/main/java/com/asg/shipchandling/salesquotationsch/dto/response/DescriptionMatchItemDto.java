package com.asg.shipchandling.salesquotationsch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single matched item from DESCRIPTION column fuzzy match.
 * Contains STOCK_POID with quantity and unit for JSON response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescriptionMatchItemDto {
    /** Excel row number (1-based) */
    private int rowNumber;
    /** Original DESCRIPTION cell value */
    private String originalDescription;
    /** Matched STOCK_POID from DB */
    private Long stockPoid;
    /** Stock code for display */
    private String stockCode;
    /** Stock name for display */
    private String stockName;
    /** Quantity extracted from description or default 1 */
    private java.math.BigDecimal quantity;
    /** Unit code (e.g. MT, CBM) if extracted from description */
    private String unitCode;
    /** Stock unit POID from matched stock (default unit) */
    private Long stockUnitPoid;
    /** Similarity score 0.0–1.0 (e.g. Jaro-Winkler) */
    private double similarityScore;
    /** True if match was above threshold */
    private boolean matched;
    /** Error or no-match reason when matched is false */
    private String message;
}
