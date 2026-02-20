package com.asg.shipchandling.salesquotationsch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single candidate match (product + similarity score) for a DESCRIPTION row.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DescriptionMatchCandidateDto {
    private Long stockPoid;
    private String stockCode;
    private String stockName;
    private Long stockUnitPoid;
    /** Jaro-Winkler similarity score 0.0–1.0 */
    private double similarityScore;
}
