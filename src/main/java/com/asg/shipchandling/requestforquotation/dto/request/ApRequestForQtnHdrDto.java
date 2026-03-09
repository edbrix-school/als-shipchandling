package com.asg.shipchandling.requestforquotation.dto.request;

import com.asg.shipchandling.commonlov.dto.LovItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApRequestForQtnHdrDto {
    private Long transactionPoid;
    private String docRef;
    private LocalDateTime transactionDate;
    private Long groupPoid;
    private Long companyPoid;
    private String description;
    private String status;
    private String type;
    private Long divisionPoid;
    private LovItem divisionPoidDetail;
    private LocalDateTime expectedDate;
    private String remarks;
    private String descriptionPrintYn;
    private String currencyCode;
    private BigDecimal currencyRate;
    private Long salesQtnPoid;
    private LovItem salesQtnPoidDetails;
    private String salesQtnRef;
    private String salesInvDocRef;
    private String deleted;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastmodifiedBy;
    private LocalDateTime lastmodifiedDate;
    
    // Detail tables
    private List<ApRequestForQtnItemDtlDto> itemDetails;
    private List<ApRequestForQtnSupDtlDto> supplierDetails;
}