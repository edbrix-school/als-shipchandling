package com.asg.shipchandling.StockMaster.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "STOCK_MASTER_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(StockMasterDtlId.class)
public class StockMasterDTLEntity {
    @Id
    @Column(name = "STOCK_POID", nullable = false)
    private Long stockPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "SUPPLIER_POID")
    private Long supplierPoid;

    @Column(name = "SUPPLIER_STOCK_CODE", length = 100)
    private String supplierStockCode;

    @Column(name = "REMARKS", length = 100)
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
