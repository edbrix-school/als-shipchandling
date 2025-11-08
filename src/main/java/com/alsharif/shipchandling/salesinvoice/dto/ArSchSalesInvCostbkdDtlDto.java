package com.alsharif.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



// Cost Booked Details DTO (Read Only)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArSchSalesInvCostbkdDtlDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long bookingPoidFk;
    private String docRefFk;
    private Long supplierPoid;
    private Long costAmt;
    private String remarks;
}
