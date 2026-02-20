package com.asg.shipchandling.stockcategory.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCategoryTreeDto {
    private Long categoryPoid;
    private String code;
    private String description;
    private String itemType;
    private Long parentCategoryPoid;
    private Integer seqno;
    private String deleted;
    private Boolean hasChildren;
    private Boolean isExpanded;
}
