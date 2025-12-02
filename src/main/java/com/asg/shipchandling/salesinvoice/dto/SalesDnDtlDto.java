package com.asg.shipchandling.salesinvoice.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Delivery Note Details DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesDnDtlDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long dnPoidFk;
    private Long quotationPoidFk;
    private String remarks;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
    
    // LOV Details
    private LovDetailDto dnDetails;
    
    // Inner class for LOV details
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LovDetailDto {
        private Long poid;
        private String code;
        private String description;
    }
}
