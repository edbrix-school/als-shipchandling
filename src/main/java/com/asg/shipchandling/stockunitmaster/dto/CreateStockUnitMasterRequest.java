package com.asg.shipchandling.stockunitmaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockUnitMasterRequest {

    @NotBlank(message = "Stock unit name is required")
    @Size(max = 100, message = "Stock unit name must not exceed 100 characters")
    private String stockUnitName;

    @Size(max = 100, message = "Stock unit name 2 must not exceed 100 characters")
    private String stockUnitName2;

    @NotNull(message = "Group POID is required")
    private Long groupPoid;

    private String createdBy;

    private String active;

    private Integer seqNo;

    private String classified;
}

