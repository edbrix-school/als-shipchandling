package com.asg.shipchandling.requestforquotation.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderRequest {
    private Long supplierPoid;
}