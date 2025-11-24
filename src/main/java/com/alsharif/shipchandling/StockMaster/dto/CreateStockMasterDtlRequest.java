package com.alsharif.shipchandling.StockMaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockMasterDtlRequest {
    private Long supplierPoid;
    private String supplierStockCode;
    private String remarks;
}
