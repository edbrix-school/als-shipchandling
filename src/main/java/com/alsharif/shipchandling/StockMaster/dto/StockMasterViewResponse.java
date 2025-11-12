package com.alsharif.shipchandling.StockMaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import com.alsharif.shipchandling.StockMaster.entity.StockMasterDTLEntity;
import com.alsharif.shipchandling.StockMaster.entity.StockMasterWarehouseDtl;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMasterViewResponse {
    private Long stockPoid;
    private String stockCode;
    private String stockName;
    private String stockDescription;
    private String active;
    private String deleted;
    private Long groupPoid;
    private BigDecimal retailPrice;
    private BigDecimal stockCost;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;

    private List<StockMasterDTLEntity> supplierDetails;
    private List<StockMasterWarehouseDtl> warehouseDetails;
}
