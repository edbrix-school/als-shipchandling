package com.alsharif.shipchandling.salesinvoice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSalesDnDtlRequest {
    private Long detRowId;
    private Long dnPoidFk;
    private Long quotationPoidFk;
    private String remarks;
    // Action type: "isCreated" (create new), "isUpdated" (update existing), "noChanges" (no changes), 
    // "isDeleted" or "delRowId" (delete). Note: This field is only used in create/update requests, not in response/view APIs
    private String actionType;
}
