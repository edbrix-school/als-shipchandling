package com.alsharif.shipchandling.StockMaster.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockMasterWarehouseDtlRequest {
     private Date transactionDate;
    private Long locationPoid;
    private String aisleNo;
    private String bayNo;
    private String shelfNo;
    private String binNo;
    private BigDecimal reorderLevel;
    private BigDecimal reorderQty;
}
