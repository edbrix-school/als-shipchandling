package com.asg.shipchandling.StockMaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockMasterDtlRequest {
    private Long detRowId;  // Used to identify existing records for update/delete/noChange
    @NotNull(message = "Supplier is required")
    private Long supplierPoid;
    @NotBlank(message = "Supplier stock code is required")
    private String supplierStockCode;
    private String remarks;
    private String actionType;
}
