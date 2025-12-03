package com.asg.shipchandling.salesquotationsch.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateCheckboxRequest {
    private Long groupPoid;
    private Long companyPoid;
    private Long transactionPoid;
    private Long detRowId;
    private Long stockPoid;
    private String deliverySelect;
}

