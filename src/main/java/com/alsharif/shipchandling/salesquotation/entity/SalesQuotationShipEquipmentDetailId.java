package com.alsharif.shipchandling.salesquotation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class SalesQuotationShipEquipmentDetailId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "TRANSACTION_POID", nullable = false)
    private BigDecimal transactionPoid;

    @Column(name = "DET_ROW_ID", nullable = false)
    private BigDecimal detailRowId;

    public SalesQuotationShipEquipmentDetailId() {
    }

    public SalesQuotationShipEquipmentDetailId(BigDecimal transactionPoid, BigDecimal detailRowId) {
        this.transactionPoid = transactionPoid;
        this.detailRowId = detailRowId;
    }

    public BigDecimal getTransactionPoid() {
        return transactionPoid;
    }

    public void setTransactionPoid(BigDecimal transactionPoid) {
        this.transactionPoid = transactionPoid;
    }

    public BigDecimal getDetailRowId() {
        return detailRowId;
    }

    public void setDetailRowId(BigDecimal detailRowId) {
        this.detailRowId = detailRowId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SalesQuotationShipEquipmentDetailId that = (SalesQuotationShipEquipmentDetailId) o;
        return Objects.equals(transactionPoid, that.transactionPoid)
                && Objects.equals(detailRowId, that.detailRowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionPoid, detailRowId);
    }
}

