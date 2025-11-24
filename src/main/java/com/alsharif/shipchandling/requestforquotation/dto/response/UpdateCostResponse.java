package com.alsharif.shipchandling.requestforquotation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCostResponse {
    private Boolean success;
    private String message;
    private Integer documentsUpdated;
}