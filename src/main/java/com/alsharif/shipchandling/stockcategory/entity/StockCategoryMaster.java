package com.alsharif.shipchandling.stockcategory.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "STOCK_CATEGORY_MASTER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "category_seq", sequenceName = "CATEGORY_POID_SEQ", allocationSize = 1)
public class StockCategoryMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CATEGORY_POID", nullable = false)
    private Long categoryPoid;

    @Column(name = "CATEGORY_CODE", length = 20, nullable = false, unique = true)
    private String categoryCode;

    @Column(name = "CATEGORY_NAME", length = 100, nullable = false, unique = true)
    private String categoryName;

    @Column(name = "CATEGORY_NAME2", length = 100)
    private String categoryName2;

    @Column(name = "CATEGORY_TYPE", length = 30)
    private String categoryType = "GROUP";

    @Column(name = "PARENT_CATEGORY_POID")
    private Long parentCategoryPoid;

    @Column(name = "STOCK_GL_POID")
    private BigDecimal stockGlPoid;

    @Column(name = "SALES_GL_POID")
    private BigDecimal salesGlPoid;

    @Column(name = "COST_OF_SALES_GL_POID")
    private BigDecimal costOfSalesGlPoid;

    @Column(name = "OUTPUT_TAX_POID")
    private BigDecimal outputTaxPoid;

    @Column(name = "INPUT_TAX_POID")
    private BigDecimal inputTaxPoid;

    @Column(name = "COST_CENTER_POID")
    private BigDecimal costCenterPoid;

    @Column(name = "SEQNO")
    private Integer seqno;

    @Column(name = "ACTIVE", length = 1)
    private String active = "Y";

    @Column(name = "DELETED", length = 1)
    private String deleted = "N";

    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastmodifiedBy;

    @UpdateTimestamp
    @Column(name = "LASTMODIFIED_DATE")
    private Timestamp lastmodifiedDate;
}
