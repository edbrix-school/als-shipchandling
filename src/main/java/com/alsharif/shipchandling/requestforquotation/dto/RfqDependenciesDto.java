package com.alsharif.shipchandling.requestforquotation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RfqDependenciesDto {
    private Long transactionPoid;
    private Boolean canDelete;
    private String reason;
    private Long purchaseOrderCount;
    private String message;
}