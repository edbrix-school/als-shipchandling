package com.alsharif.shipchandling.StockMaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMasterDependenciesDto {
    private Long stockPoid;
    private Boolean canDelete;
    private String reason;
    private Long stockBalanceCount;
    private Long transactionCount;
    private String message;
}
