package com.alsharif.shipchandling.deliverynote.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesDeliveryNoteHdrDto {
    private Long transactionPoid;
    private String docRef;
    private Timestamp transactionDate;
    private Long groupPoid;
    private Long companyPoid;
    private Long customerPoid;
    private String currencyCode;
    private Long currencyRate;
    private String deliveryStatus;
    private Long salesmanPoid;
    private String paymentMode;
    private String deliveryTerms;
    private Long linePoid;
    private String vesselPoid;
    private String vesselName;
    private String voyageRef;
    private Long portPoid;
    private String portDescription;
    private String qtnRefNo;
    private String vesselAgent;
    private String deliveryToAddress;
    private String descriptionPrintYn;
    private Long totalDiscount;
    private Long totalAmount;
    private String remarks;
    private String deleted;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
    
    // Detail tables (optional, included when includeDetails = true)
    private List<SalesDeliveryNoteItemDtlDto> itemDetails;
}
