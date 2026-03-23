package com.asg.shipchandling.salesinvoice.dto.response;

import java.util.List;

import com.asg.shipchandling.salesinvoice.dto.QuotationItemDto;
import com.asg.shipchandling.salesinvoice.dto.SalesInvCostbkdDtlDto;

import com.asg.shipchandling.salesinvoice.dto.SalesInvoiceHdrDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadQuotationAndCostBookingsResponse {
    private Boolean success;
    private List<String> messages;
    private List<QuotationItemDto> quotationItems;
    private List<SalesInvCostbkdDtlDto> costBookings;
    private SalesInvoiceHdrDto invoice;
}
