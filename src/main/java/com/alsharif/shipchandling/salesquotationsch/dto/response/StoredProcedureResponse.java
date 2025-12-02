package com.alsharif.shipchandling.salesquotationsch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredProcedureResponse {
    private boolean success;
    private String message;
    private String errorMessage;
}

