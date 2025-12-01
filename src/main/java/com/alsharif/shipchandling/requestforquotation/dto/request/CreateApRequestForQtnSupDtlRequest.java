package com.alsharif.shipchandling.requestforquotation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApRequestForQtnSupDtlRequest {
    private Long supplierPoid;
    private String remarks;
    
    // Action field for CRUD operations: "isCreated", "isUpdated", "isDeleted", "noChange"
    private String action;
    
    // Optional detRowId for identifying existing items during updates/deletes
    private Long detRowId;
}