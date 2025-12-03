package com.asg.shipchandling.StockMaster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "STOCK_CATEGORY_MASTER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCategoryMasterEntity {
    
    @Id
    @Column(name = "CATEGORY_POID", nullable = false)
    private Long categoryPoid;

    @Column(name = "CATEGORY_CODE", length = 100)
    private String categoryCode;

    @Column(name = "CATEGORY_NAME", length = 500)
    private String categoryName;

    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @Column(name = "PARENT_CATEGORY_POID")
    private Long parentCategoryPoid;
}



