package com.asg.shipchandling.salesquotationsch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResponse {
    private boolean success;
    private String message;
    private int totalRows;
    private int successfulRows;
    private int failedRows;
    private List<String> errors;
}

