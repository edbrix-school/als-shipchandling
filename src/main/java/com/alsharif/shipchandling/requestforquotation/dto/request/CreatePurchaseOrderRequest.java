package com.alsharif.shipchandling.requestforquotation.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderRequest {
    private Long supplierPoid;
}