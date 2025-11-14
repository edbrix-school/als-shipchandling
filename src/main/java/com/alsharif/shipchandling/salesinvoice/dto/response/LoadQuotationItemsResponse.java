package com.alsharif.shipchandling.salesinvoice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

import com.alsharif.shipchandling.salesinvoice.dto.QuotationItemDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadQuotationItemsResponse {
    private Boolean success;
    private String message;
    private List<QuotationItemDto> items;
}
