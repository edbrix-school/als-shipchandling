package com.alsharif.shipchandling.requestforquotation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApRequestForQtnRequest {
    
    @NotNull(message = "Transaction date is required")
    private Timestamp transactionDate;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Timestamp expectedDate;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;

    private String descriptionPrintYn;

    private Long divisionPoid;

    private String currencyCode;
    private BigDecimal currencyRate;

    private String type;

    // Detail tables
    private List<CreateApRequestForQtnItemDtlRequest> itemDetails;
    private List<CreateApRequestForQtnSupDtlRequest> supplierDetails;
}