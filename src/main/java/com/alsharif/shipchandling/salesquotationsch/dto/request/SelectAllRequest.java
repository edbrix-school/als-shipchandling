package com.alsharif.shipchandling.salesquotationsch.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectAllRequest {
    private Long groupPoid;
    private Long companyPoid;
    private Long transactionPoid;
    private String selectStatus;
}

