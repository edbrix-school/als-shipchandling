package com.alsharif.shipchandling.requestforquotation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApRequestForQtnHdrDto {
    private Long transactionPoid;
    private String docRef;
    private Timestamp transactionDate;
    private Long groupPoid;
    private Long companyPoid;
    private String description;
    private String status;
    private String type;
    private Long divisionPoid;
    private Timestamp expectedDate;
    private String remarks;
    private String descriptionPrintYn;
    private String currencyCode;
    private BigDecimal currencyRate;
    private Long salesQtnPoid;
    private String salesQtnRef;
    private String salesInvDocRef;
    private String deleted;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
    
    // Detail tables
    private List<ApRequestForQtnItemDtlDto> itemDetails;
    private List<ApRequestForQtnSupDtlDto> supplierDetails;
}