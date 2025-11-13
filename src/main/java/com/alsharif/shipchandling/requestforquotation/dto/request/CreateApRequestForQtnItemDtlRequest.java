package com.alsharif.shipchandling.requestforquotation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApRequestForQtnItemDtlRequest {
    @NotNull(message = "Stock is required")
    private Long stockPoid;

    private Long stockUnitPoid;

    @NotNull(message = "Quantity is required")
    private BigDecimal qty;

    private Long supplierPoid;
    private BigDecimal price;
    private Long taxPoid;
    private String remarks;
}