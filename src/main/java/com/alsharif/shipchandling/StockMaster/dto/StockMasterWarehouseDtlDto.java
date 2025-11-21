package com.alsharif.shipchandling.StockMaster.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMasterWarehouseDtlDto {
      private Long stockPoid;
    private Long detRowId;
    private Date transactionDate;
    private Long locationPoid;
    private String aisleNo;
    private String bayNo;
    private String shelfNo;
    private String binNo;
    private BigDecimal reorderLevel;
    private BigDecimal reorderQty;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
}
