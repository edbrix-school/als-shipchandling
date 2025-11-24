package com.alsharif.shipchandling.requestforquotation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "GLOBAL_TAX_MASTER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalTaxMaster {

    @Id
    @Column(name = "TAX_POID", nullable = false)
    private BigDecimal taxPoid;

    @Column(name = "GROUP_POID", nullable = false)
    private BigDecimal groupPoid;

    @Column(name = "COMPANY_POID", nullable = false)
    private BigDecimal companyPoid;

    @Column(name = "TAX_CODE", length = 50)
    private String taxCode;

    @Column(name = "TAX_NAME", length = 200)
    private String taxName;

    @Column(name = "TAX_NAME2", length = 200)
    private String taxName2;

    @Column(name = "TAX_CATEGORY", length = 10)
    private String taxCategory;

    @Column(name = "TAX_PERCENT")
    private BigDecimal taxPercent;

    @Column(name = "PERCENTAGE")
    private BigDecimal percentage;

    @Column(name = "TAX_TYPE", length = 20)
    private String taxType;

    @Column(name = "CALCULATION_TYPE", length = 20)
    private String calculationType;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

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