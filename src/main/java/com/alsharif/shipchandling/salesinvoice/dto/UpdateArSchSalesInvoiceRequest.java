package com.alsharif.shipchandling.salesinvoice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateArSchSalesInvoiceRequest {
    // Same fields as CreateArSchSalesInvoiceRequest (excluding docRef which is
    // read-only)
    // ... (same as CreateArSchSalesInvoiceRequest)
}