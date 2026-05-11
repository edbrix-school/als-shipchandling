package com.asg.shipchandling.StockMaster.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockMasterWarehouseDtlRequest {
     private LocalDate transactionDate;
    private Long locationPoid;
    private String aisleNo;
    private String bayNo;
    private String shelfNo;
    private String binNo;
    private BigDecimal reorderLevel;
    private BigDecimal reorderQty;
    private Long detRowId;
    private String actionType;
}
