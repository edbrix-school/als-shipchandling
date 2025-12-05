package com.asg.shipchandling.salesinvoice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceListDto {
    @JsonProperty("TRANSACTION_POID")
    private Long transactionPoid;
    @JsonProperty("DATE")
    private Timestamp date; // transactionDate
    @JsonProperty("DOC_REF")
    private String docRef;
    @JsonProperty("QTN_REF")
    private String qtnRef; // qtnPoid
    @JsonProperty("PARTY_NAME")
    private String partyName; // customerName or principal name
    @JsonProperty("VESSEL_NAME")
    private String vesselName;
    @JsonProperty("DELETED")
    private String deleted;
    @JsonProperty("CREATED_BY")
    private String createdBy;
    @JsonProperty("UPDATED_BY")
    private String updatedBy; // lastmodifiedBy
    @JsonProperty("CREATED_DATE")
    private Timestamp createdDate;
    @JsonProperty("UPDATED_DATE")
    private Timestamp updatedDate; // lastmodifiedDate
}

