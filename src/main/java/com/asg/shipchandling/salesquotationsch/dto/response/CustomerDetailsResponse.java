package com.asg.shipchandling.salesquotationsch.dto.response;

import java.util.List;

import com.asg.shipchandling.salesquotationsch.dto.SalesQuotationSchCustomerDetailsDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetailsResponse {
    private String message;
    private boolean success;
    private List<SalesQuotationSchCustomerDetailsDto> customerDetails;
}