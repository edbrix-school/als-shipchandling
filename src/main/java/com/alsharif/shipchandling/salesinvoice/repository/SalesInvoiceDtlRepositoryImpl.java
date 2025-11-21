package com.alsharif.shipchandling.salesinvoice.repository;

import com.alsharif.shipchandling.salesinvoice.entity.SalesInvoiceDtl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SalesInvoiceDtlRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    public List<SalesInvoiceDtl> findByTransactionPoidNative(Long transactionPoid) {
        String sql = "SELECT " +
                "TRANSACTION_POID, DET_ROW_ID, DN_POID_LINK_FK, DET_ROW_ID_CHRG_FK, " +
                "STOCK_POID, QUANTITY, PRICE, DISCOUNT, AMOUNT, REMARKS, STOCK_UNIT_POID, " +
                "CREATED_BY, CREATED_DATE, LASTMODIFIED_BY, LASTMODIFIED_DATE, " +
                "QUOTATION_POID, COST_AMT, QTN_DET_ROW_ID, PURCHASE_PRICE, PURCHASE_QTY, " +
                "NET_SALES, NET_DISCOUNT, ITEM_GP, ITEM_GP_PER, ITEM_TYPE, " +
                "TAX_PERCENTAGE, TAX_AMOUNT, TAX_POID, BASE_AMT, INCENTIVE, COST_POID " +
                "FROM AR_SCH_SALES_INVOICE_DTL " +
                "WHERE TRANSACTION_POID = :transactionPoid " +
                "ORDER BY DET_ROW_ID";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("transactionPoid", transactionPoid);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .map(this::mapRowToEntity)
                .toList();
    }

    private SalesInvoiceDtl mapRowToEntity(Object[] row) {
        SalesInvoiceDtl entity = new SalesInvoiceDtl();
        
        entity.setTransactionPoid(((Number) row[0]).longValue());
        entity.setDetRowId(((Number) row[1]).longValue());
        entity.setDnPoidLinkFk(row[2] != null ? ((Number) row[2]).longValue() : null);
        entity.setDetRowIdChrgFk(row[3] != null ? ((Number) row[3]).longValue() : null);
        entity.setStockPoid(row[4] != null ? ((Number) row[4]).longValue() : null);
        entity.setQuantity(row[5] != null ? ((Number) row[5]).longValue() : null);
        
        // Handle PRICE - convert from any type to BigDecimal
        entity.setPrice(convertToBigDecimal(row[6]));
        
        entity.setDiscount(row[7] != null ? ((Number) row[7]).longValue() : null);
        entity.setAmount(row[8] != null ? ((Number) row[8]).longValue() : null);
        entity.setRemarks(toStringSafe(row[9]));
        entity.setStockUnitPoid(row[10] != null ? ((Number) row[10]).longValue() : null);
        entity.setCreatedBy(toStringSafe(row[11]));
        entity.setCreatedDate(row[12] != null ? (Timestamp) row[12] : null);
        entity.setLastmodifiedBy(toStringSafe(row[13]));
        entity.setLastmodifiedDate(row[14] != null ? (Timestamp) row[14] : null);
        entity.setQuotationPoid(row[15] != null ? ((Number) row[15]).longValue() : null);
        entity.setCostAmt(row[16] != null ? ((Number) row[16]).longValue() : null);
        entity.setQuotationDetRowId(row[17] != null ? ((Number) row[17]).longValue() : null);
        entity.setPurchasePrice(row[18] != null ? ((Number) row[18]).longValue() : null);
        entity.setPurchaseQty(row[19] != null ? ((Number) row[19]).longValue() : null);
        entity.setNetSales(row[20] != null ? ((Number) row[20]).longValue() : null);
        entity.setNetDiscount(row[21] != null ? ((Number) row[21]).longValue() : null);
        entity.setItemGp(row[22] != null ? ((Number) row[22]).longValue() : null);
        entity.setItemGpPer(row[23] != null ? ((Number) row[23]).longValue() : null);
        entity.setItemType(toStringSafe(row[24]));
        entity.setTaxPercentage(row[25] != null ? ((Number) row[25]).longValue() : null);
        entity.setTaxAmount(row[26] != null ? ((Number) row[26]).longValue() : null);
        entity.setTaxPoid(row[27] != null ? ((Number) row[27]).longValue() : null);
        entity.setBaseAmt(row[28] != null ? ((Number) row[28]).longValue() : null);
        entity.setIncentive(row[29] != null ? ((Number) row[29]).longValue() : null);
        entity.setCostPoid(toStringSafe(row[30]));
        
        return entity;
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        // Try to convert via string representation
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toStringSafe(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Character) {
            return String.valueOf((Character) value);
        }
        return value.toString();
    }
}

