package com.alsharif.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



// Cost Booked Details DTO (Read Only)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvCostbkdDtlDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long bookingPoidFk;
    private String docRefFk;
    private Long supplierPoid;
    private String supplierName; // From AR_SCH_SALES_INV_COSTBKD_DTL.SUPPLIER_NAME
    private String bookType; // From AR_SCH_SALES_INV_COSTBKD_DTL.BOOK_TYPE
    private Long costAmt;
    private String remarks;
}
