package com.alsharif.shipchandling.requestforquotation.dto.request;

import lombok.*;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApRequestForQtnSupDtlDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long supplierPoid;
    private String remarks;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
}