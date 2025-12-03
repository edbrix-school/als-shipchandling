package com.asg.shipchandling.salesinvoice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyInvoiceRequest {
    private String authorizedId; // Optional, required if authorization is needed
}
