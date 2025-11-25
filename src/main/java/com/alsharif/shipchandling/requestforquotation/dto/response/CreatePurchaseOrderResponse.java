package com.alsharif.shipchandling.requestforquotation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderResponse {
    private Boolean success;
    private String message;
    private String purchaseOrderDocRef;
    private Long purchaseOrderPoid;
}