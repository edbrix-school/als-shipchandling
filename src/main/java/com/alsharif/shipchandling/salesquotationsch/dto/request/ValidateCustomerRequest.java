package com.alsharif.shipchandling.salesquotationsch.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateCustomerRequest {
    private Long groupPoid;
    private Long companyPoid;
    private Long customerPoid;
    private Long principalPoid;
    private Long addressPoid;
}

