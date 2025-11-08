package com.alsharif.shipchandling.salesinvoice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadQuotationResponse {
    private Boolean success;
    private String message;
    private String currencyCode;
    private Long currencyRate;
    private List<QuotationItemDto> items;
}
