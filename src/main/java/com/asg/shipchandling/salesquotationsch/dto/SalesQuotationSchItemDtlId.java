package com.asg.shipchandling.salesquotationsch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Composite Key Class
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchItemDtlId implements java.io.Serializable {
    private Long transactionPoid;
    private Long detRowId;
}

