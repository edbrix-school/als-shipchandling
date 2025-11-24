package com.alsharif.shipchandling.StockMaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
     private Boolean isUnique;
    private String message;
}
