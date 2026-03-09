package com.asg.shipchandling.StockMaster.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMasterWarehouseDtlDto {
      private Long stockPoid;
    private Long detRowId;
    private LocalDate transactionDate;
    private Long locationPoid;
    private String aisleNo;
    private String bayNo;
    private String shelfNo;
    private String binNo;
    private BigDecimal reorderLevel;
    private BigDecimal reorderQty;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastmodifiedBy;
    private LocalDateTime lastmodifiedDate;
    private String actionType;
}
