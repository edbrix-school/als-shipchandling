package com.asg.shipchandling.salesinvoice.dto.response;

import com.asg.shipchandling.salesinvoice.dto.QuotationSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadQuotationSummaryResponse {
    private Boolean success;
    private String message;
    private QuotationSummaryDto quotationSummary;
}
