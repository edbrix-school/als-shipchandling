package com.alsharif.shipchandling.salesinvoice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceDtlId implements java.io.Serializable {
    private Long transactionPoid;
    private Long detRowId;
}
