package com.asg.shipchandling.requestforquotation.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;


@Entity
@Table(name = "GLOBAL_TAX_MASTER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalTaxMaster extends BaseEntity {

    @Id
    @Column(name = "TAX_POID", nullable = false)
    private BigDecimal taxPoid;

    @Column(name = "GROUP_POID", nullable = false)
    private BigDecimal groupPoid;

    @Column(name = "TAX_CODE", length = 50)
    private String taxCode;

    @Column(name = "TAX_NAME", length = 200)
    private String taxName;

    @Column(name = "TAX_NAME2", length = 200)
    private String taxName2;

    @Column(name = "TAX_CATEGORY", length = 10)
    private String taxCategory;

    @Column(name = "PERCENTAGE")
    private BigDecimal percentage;

    @Column(name = "TAX_TYPE", length = 20)
    private String taxType;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Integer seqno;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "TAX_INPUT_OUTPUT", length = 20)
    private String taxInputOutput;

    @Column(name = "GL_CREDIT_DEBIT", length = 20)
    private String glCreditDebit;

    @Column(name = "GL_POID")
    private BigDecimal glPoid;

    @Column(name = "NBR_TAX_CODE", length = 50)
    private String nbrTaxCode;

    @Column(name = "NBR_TAX_NAME", length = 200)
    private String nbrTaxName;

}