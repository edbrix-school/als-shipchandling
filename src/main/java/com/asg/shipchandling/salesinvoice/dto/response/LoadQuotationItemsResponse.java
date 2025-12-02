package com.asg.shipchandling.salesinvoice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

import com.asg.shipchandling.salesinvoice.dto.QuotationItemDto;
import com.asg.shipchandling.salesinvoice.dto.SalesInvoiceHdrDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadQuotationItemsResponse {
    private Boolean success;
    private String message;
    private List<QuotationItemDto> items;
    private SalesInvoiceHdrDto invoice;
}
