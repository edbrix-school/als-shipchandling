package com.alsharif.shipchandling.salesinvoice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyInvoiceResponse {
    private Boolean success;
    private String message;
    private String verified;
}
