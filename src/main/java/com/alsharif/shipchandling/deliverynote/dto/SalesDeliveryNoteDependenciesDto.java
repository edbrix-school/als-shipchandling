package com.alsharif.shipchandling.deliverynote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesDeliveryNoteDependenciesDto {
    private Long transactionPoid;
    private Boolean canDelete;
    private String reason;
    private Long salesInvoiceCount;
    private String message;
}
