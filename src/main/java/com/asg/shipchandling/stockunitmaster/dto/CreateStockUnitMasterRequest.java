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

    @NotBlank(message = "Unit code is required")
    @Size(max = 20, message = "Unit code must not exceed 20 characters")
    private String stockUnitCode;

    @NotBlank(message = "Unit name is required")
    @Size(max = 100, message = "Unit name must not exceed 100 characters")
    private String stockUnitName;

    @Size(max = 100, message = "Unit name 2 must not exceed 100 characters")
    private String stockUnitName2;

    @NotNull(message = "Group POID is required")
    private Long groupPoid;

    private String createdBy;

    private String active;

    private Integer seqNo;

    private String classified;
}

