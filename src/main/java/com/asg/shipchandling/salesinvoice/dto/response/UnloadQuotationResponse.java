package com.asg.shipchandling.salesinvoice.dto.response;

import com.asg.shipchandling.salesinvoice.dto.SalesInvoiceHdrDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnloadQuotationResponse {
    private Boolean success;
    private String message;
    private SalesInvoiceHdrDto invoice;
}
