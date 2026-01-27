package com.asg.shipchandling.stockunitmaster.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "STOCK_UNIT_MASTER")
public class StockUnitMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STOCK_UNIT_POID", nullable = false)
    private Long stockUnitPoid;

    @Column(name = "STOCK_UNIT_CODE", length = 20, nullable = false)
    private String stockUnitCode;

    @Column(name = "STOCK_UNIT_NAME", length = 100,nullable = false, unique = true)
    private String stockUnitName;

    @Column(name = "STOCK_UNIT_NAME2", length = 100)
    private String stockUnitName2;

    @Column(name = "GROUP_POID", length = 22,nullable = false)
    @AuditIgnore
    private Long groupPoid;

    @Column(name = "CREATED_BY", length = 20)
    @AuditIgnore
    private String createdBy;

    @Column(name = "CREATED_DATE")
    @AuditIgnore
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY")
    @AuditIgnore
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    @AuditIgnore
    private LocalDateTime lastModifiedDate;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "DELETED")
    @AuditIgnore
    private String deleted;

    @Column(name = "CLASSIFIED")
    private String classified;

}
