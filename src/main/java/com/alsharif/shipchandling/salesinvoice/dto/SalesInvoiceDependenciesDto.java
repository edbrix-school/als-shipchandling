package com.alsharif.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceDependenciesDto {
    private Long transactionPoid;
    private Boolean canDelete;
    private String reason;
    private Long receiptCount;
    private Long creditNoteCount;
    private String message;
}
