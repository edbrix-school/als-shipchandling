package com.asg.shipchandling.stockunitmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitDependenciesDto {
    private Long stockUnitPoid;
    private Boolean canDelete;
    private String reason;
    private Long stockItemCount;
    private String message;
}
