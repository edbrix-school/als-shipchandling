package com.asg.shipchandling.StockMaster.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "STOCK_MASTER_WAREHOUSE_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(StockMasterWarehouseDtlId.class)
@AttributeOverride(name = "lastModifiedBy", column = @Column(name = "LAST_MODIFIED_BY"))
@AttributeOverride(name = "lastModifiedDate", column = @Column(name = "LAST_MODIFIED_DATE"))
public class StockMasterWarehouseDtl extends BaseEntity {
     @Id
    @Column(name = "STOCK_POID", nullable = false)
    private Long stockPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

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
}
