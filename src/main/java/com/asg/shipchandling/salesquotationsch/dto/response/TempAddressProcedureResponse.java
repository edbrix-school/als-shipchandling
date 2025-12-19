package com.asg.shipchandling.salesquotationsch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempAddressProcedureResponse {
    private boolean success;
    private String message;
    private String errorMessage;
    private Long newAddressPoid;
}



