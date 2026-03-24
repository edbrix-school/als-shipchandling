package com.asg.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;



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
    private BigDecimal costAmt;
    private String remarks;
}
