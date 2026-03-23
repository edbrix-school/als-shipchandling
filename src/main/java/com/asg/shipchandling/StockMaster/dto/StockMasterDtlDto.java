package com.asg.shipchandling.StockMaster.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Supplier Details DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMasterDtlDto {
    private Long stockPoid;
    private Long detRowId;
    private Long supplierPoid;
    private String supplierStockCode;
    private String supplierName;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastmodifiedBy;
    private LocalDateTime lastmodifiedDate;
    private String actionType;
}
