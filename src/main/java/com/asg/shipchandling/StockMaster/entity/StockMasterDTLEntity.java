package com.asg.shipchandling.StockMaster.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;


@Entity
@Table(name = "STOCK_MASTER_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(StockMasterDtlId.class)
public class StockMasterDTLEntity extends BaseEntity {
    @Id
    @Column(name = "STOCK_POID", nullable = false)
    private Long stockPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @AuditIgnore
    private Long detRowId;

    @Column(name = "SUPPLIER_POID")
    private Long supplierPoid;

    @Column(name = "SUPPLIER_STOCK_CODE", length = 100)
    private String supplierStockCode;

    @Column(name = "REMARKS", length = 100)
    private String remarks;

}
