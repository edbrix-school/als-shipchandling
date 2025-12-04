package com.asg.shipchandling.StockMaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockMasterDtlRequest {
    private Long detRowId;  // Used to identify existing records for update/delete/noChange
    private Long supplierPoid;
    private String supplierStockCode;
    private String remarks;
    private String actionType;
}
