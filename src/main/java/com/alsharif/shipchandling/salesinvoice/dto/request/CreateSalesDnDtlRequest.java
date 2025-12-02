package com.alsharif.shipchandling.salesinvoice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSalesDnDtlRequest {
    private Long dnPoidFk;
    private Long quotationPoidFk;
    private String remarks;
    // Action type: "isCreated" to create new, "isDeleted" or "delRowId" to delete
    // Note: This field is only used in create/update requests, not in response/view APIs
    private String actionType;
}
