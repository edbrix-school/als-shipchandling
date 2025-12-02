package com.asg.shipchandling.StockMaster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "STOCK_MASTER_WAREHOUSE_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(StockMasterWarehouseDtlId.class)
public class StockMasterWarehouseDtl {
     @Id
    @Column(name = "STOCK_POID", nullable = false)
    private Long stockPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "TRANSACTION_DATE")
    private Date transactionDate;

    @Column(name = "LOCATION_POID")
    private Long locationPoid;

    @Column(name = "AISLE_NO", length = 50)
    private String aisleNo;

    @Column(name = "BAY_NO", length = 50)
    private String bayNo;

    @Column(name = "SHELF_NO", length = 50)
    private String shelfNo;

    @Column(name = "BIN_NO", length = 50)
    private String binNo;

    @Column(name = "REORDER_LEVEL")
    private BigDecimal reorderLevel;

    @Column(name = "REORDER_QTY")
    private BigDecimal reorderQty;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "LAST_MODIFIED_BY", length = 20)
    private String lastmodifiedBy;

    @UpdateTimestamp
    @Column(name = "LAST_MODIFIED_DATE")
    private Timestamp lastmodifiedDate;
}
