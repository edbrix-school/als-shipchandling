package com.asg.shipchandling.stockcategory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PoidDetailsDto {
    private Long poid;
    private String code;
    private String description;
}

