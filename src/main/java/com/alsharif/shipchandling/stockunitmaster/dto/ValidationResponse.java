package com.alsharif.shipchandling.stockunitmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
     private boolean isUnique;
    private String message;
}
