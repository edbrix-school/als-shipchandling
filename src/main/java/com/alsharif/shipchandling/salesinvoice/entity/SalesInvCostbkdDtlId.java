package com.alsharif.shipchandling.salesinvoice.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Composite Key Class
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvCostbkdDtlId implements java.io.Serializable {
    private Long transactionPoid;
    private Long detRowId;
}