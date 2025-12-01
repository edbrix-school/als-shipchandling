package com.alsharif.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceListDto {
    private Long transactionPoid;
    private Timestamp date; // transactionDate
    private String docRef;
    private String qtnRef; // qtnPoid
    private String partyName; // customerName or principal name
    private String vesselName;
    private String deleted;
    private String createdBy;
    private String updatedBy; // lastmodifiedBy
    private Timestamp createdDate;
    private Timestamp updatedDate; // lastmodifiedDate
}

