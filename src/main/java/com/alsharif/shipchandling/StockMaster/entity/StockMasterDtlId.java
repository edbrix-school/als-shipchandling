package com.alsharif.shipchandling.StockMaster.entity;

import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor
@AllArgsConstructor
public class StockMasterDtlId {
    private Long stockPoid; 
    private Long detRowId;
}
