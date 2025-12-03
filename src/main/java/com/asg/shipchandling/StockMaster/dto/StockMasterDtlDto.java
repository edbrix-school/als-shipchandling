package com.asg.shipchandling.StockMaster.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

// Supplier Details DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMasterDtlDto {
    private Long stockPoid;
    private Long detRowId;
    private Long supplierPoid;
    private String supplierStockCode;
    private String remarks;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
    private String actionType;
}
