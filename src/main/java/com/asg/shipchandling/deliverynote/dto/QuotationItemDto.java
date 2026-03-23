package com.asg.shipchandling.deliverynote.dto;

import com.asg.shipchandling.commonlov.dto.LovItem;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationItemDto {
    private Long stockPoid;
    private LovItem stockDet;
    private Long stockUnitPoid;
    private LovItem stockUnitDet;
    private Long quantity;
    private BigDecimal price;
    private Long discount;
    private Long amount;
    private String remarks;
    private Long qtnDetRowId;
    private Long totCost;
    private String itemType;
    private String checkAll;
}