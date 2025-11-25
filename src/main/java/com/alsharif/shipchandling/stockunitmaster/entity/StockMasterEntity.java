package com.alsharif.shipchandling.stockunitmaster.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "STOCK_MASTER")
@Data
public class StockMasterEntity { 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STOCK_POID")
    private Long stockPoid;

    @Column(name = "GROUP_POID")
    private Long groupPoid;

    @Column(name = "STOCK_UNIT_POID")
    private Long stockUnitPoid;

    @Column(name = "STOCK_CODE")
    private String stockCode;

    @Column(name = "STOCK_NAME")
    private String stockName;

    @Column(name = "ACTIVE")
    private Boolean active;
}

