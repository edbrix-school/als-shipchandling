package com.alsharif.shipchandling.salesinvoice.repository;

import com.alsharif.shipchandling.salesinvoice.entity.SalesInvoiceDtl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesInvoiceDtlRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private SalesInvoiceDtlRepositoryImpl repository;

    private static final Long TEST_TRANSACTION_POID = 100L;
    private static final Long TEST_DET_ROW_ID = 1L;

    private Object[] testRowData;

    @BeforeEach
    void setUp() {
        repository = new SalesInvoiceDtlRepositoryImpl();
        // Inject EntityManager using reflection since @PersistenceContext won't work in unit tests
        ReflectionTestUtils.setField(repository, "entityManager", entityManager);

        // Create test row data matching the SQL query structure (31 columns)
        testRowData = new Object[31];
        testRowData[0] = TEST_TRANSACTION_POID; // TRANSACTION_POID
        testRowData[1] = TEST_DET_ROW_ID; // DET_ROW_ID
        testRowData[2] = null; // DN_POID_LINK_FK
        testRowData[3] = null; // DET_ROW_ID_CHRG_FK
        testRowData[4] = 100L; // STOCK_POID
        testRowData[5] = 10L; // QUANTITY
        testRowData[6] = BigDecimal.valueOf(100.50); // PRICE
        testRowData[7] = 5L; // DISCOUNT
        testRowData[8] = 1000L; // AMOUNT
        testRowData[9] = "Test remarks"; // REMARKS
        testRowData[10] = 1L; // STOCK_UNIT_POID
        testRowData[11] = "testUser"; // CREATED_BY
        testRowData[12] = Timestamp.from(Instant.now()); // CREATED_DATE
        testRowData[13] = "testUser"; // LASTMODIFIED_BY
        testRowData[14] = Timestamp.from(Instant.now()); // LASTMODIFIED_DATE
        testRowData[15] = 300L; // QUOTATION_POID (Long, not String)
        testRowData[16] = 800L; // COST_AMT
        testRowData[17] = 1L; // QTN_DET_ROW_ID
        testRowData[18] = BigDecimal.valueOf(80.00); // PURCHASE_PRICE
        testRowData[19] = 10L; // PURCHASE_QTY
        testRowData[20] = 1000L; // NET_SALES
        testRowData[21] = 50L; // NET_DISCOUNT
        testRowData[22] = 200L; // ITEM_GP
        testRowData[23] = 20L; // ITEM_GP_PER
        testRowData[24] = "STOCK"; // ITEM_TYPE
        testRowData[25] = 5L; // TAX_PERCENTAGE
        testRowData[26] = 50L; // TAX_AMOUNT
        testRowData[27] = 1L; // TAX_POID
        testRowData[28] = 1000L; // BASE_AMT
        testRowData[29] = 100L; // INCENTIVE
        testRowData[30] = "COST-001"; // COST_POID
    }

    @Test
    void testFindByTransactionPoidNative_Success() {
        // Arrange
        List<Object[]> resultList = Collections.singletonList(testRowData);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(resultList);

        // Act
        List<SalesInvoiceDtl> result = repository.findByTransactionPoidNative(TEST_TRANSACTION_POID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        SalesInvoiceDtl entity = result.get(0);
        assertEquals(TEST_TRANSACTION_POID, entity.getTransactionPoid());
        assertEquals(TEST_DET_ROW_ID, entity.getDetRowId());
        assertEquals(100L, entity.getStockPoid());
        assertEquals(10L, entity.getQuantity());
        assertEquals(BigDecimal.valueOf(100.50), entity.getPrice());
        assertEquals(5L, entity.getDiscount());
        assertEquals(1000L, entity.getAmount());
        assertEquals("Test remarks", entity.getRemarks());

        verify(entityManager, times(1)).createNativeQuery(anyString());
        verify(query, times(1)).setParameter("transactionPoid", TEST_TRANSACTION_POID);
        verify(query, times(1)).getResultList();
    }

    @Test
    void testFindByTransactionPoidNative_EmptyResult() {
        // Arrange
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        // Act
        List<SalesInvoiceDtl> result = repository.findByTransactionPoidNative(TEST_TRANSACTION_POID);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testFindByTransactionPoidNative_MultipleResults() {
        // Arrange - create a new array to avoid reference issues
        Object[] row2 = new Object[31];
        row2[0] = TEST_TRANSACTION_POID; // TRANSACTION_POID
        row2[1] = 2L; // DET_ROW_ID (different)
        row2[2] = null; // DN_POID_LINK_FK
        row2[3] = null; // DET_ROW_ID_CHRG_FK
        row2[4] = 100L; // STOCK_POID
        row2[5] = 10L; // QUANTITY
        row2[6] = BigDecimal.valueOf(100.50); // PRICE
        row2[7] = 5L; // DISCOUNT
        row2[8] = 1000L; // AMOUNT
        row2[9] = "Test remarks"; // REMARKS
        row2[10] = 1L; // STOCK_UNIT_POID
        row2[11] = "testUser"; // CREATED_BY
        row2[12] = Timestamp.from(Instant.now()); // CREATED_DATE
        row2[13] = "testUser"; // LASTMODIFIED_BY
        row2[14] = Timestamp.from(Instant.now()); // LASTMODIFIED_DATE
        row2[15] = 300L; // QUOTATION_POID (Long)
        row2[16] = 800L; // COST_AMT
        row2[17] = 1L; // QTN_DET_ROW_ID
        row2[18] = BigDecimal.valueOf(80.00); // PURCHASE_PRICE
        row2[19] = 10L; // PURCHASE_QTY
        row2[20] = 1000L; // NET_SALES
        row2[21] = 50L; // NET_DISCOUNT
        row2[22] = 200L; // ITEM_GP
        row2[23] = 20L; // ITEM_GP_PER
        row2[24] = "STOCK"; // ITEM_TYPE
        row2[25] = 5L; // TAX_PERCENTAGE
        row2[26] = 50L; // TAX_AMOUNT
        row2[27] = 1L; // TAX_POID
        row2[28] = 1000L; // BASE_AMT
        row2[29] = 100L; // INCENTIVE
        row2[30] = "COST-001"; // COST_POID
        
        List<Object[]> resultList = List.of(testRowData, row2);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(resultList);

        // Act
        List<SalesInvoiceDtl> result = repository.findByTransactionPoidNative(TEST_TRANSACTION_POID);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TEST_DET_ROW_ID, result.get(0).getDetRowId());
        assertEquals(2L, result.get(1).getDetRowId());
    }

    @Test
    void testFindByTransactionPoidNative_WithNullValues() {
        // Arrange
        Object[] rowWithNulls = new Object[31];
        for (int i = 0; i < 31; i++) {
            rowWithNulls[i] = null;
        }
        rowWithNulls[0] = TEST_TRANSACTION_POID; // Only transactionPoid is set
        rowWithNulls[1] = TEST_DET_ROW_ID; // And detRowId
        
        List<Object[]> resultList = Collections.singletonList(rowWithNulls);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(resultList);

        // Act
        List<SalesInvoiceDtl> result = repository.findByTransactionPoidNative(TEST_TRANSACTION_POID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        SalesInvoiceDtl entity = result.get(0);
        assertEquals(TEST_TRANSACTION_POID, entity.getTransactionPoid());
        assertEquals(TEST_DET_ROW_ID, entity.getDetRowId());
        assertNull(entity.getStockPoid());
        assertNull(entity.getQuantity());
        assertNull(entity.getPrice());
    }

    @Test
    void testFindByTransactionPoidNative_PriceAsString() {
        // Arrange - create new array with correct types
        Object[] rowWithStringPrice = new Object[31];
        rowWithStringPrice[0] = TEST_TRANSACTION_POID; // TRANSACTION_POID (Number)
        rowWithStringPrice[1] = TEST_DET_ROW_ID; // DET_ROW_ID (Number)
        rowWithStringPrice[2] = null; // DN_POID_LINK_FK
        rowWithStringPrice[3] = null; // DET_ROW_ID_CHRG_FK
        rowWithStringPrice[4] = 100L; // STOCK_POID
        rowWithStringPrice[5] = 10L; // QUANTITY
        rowWithStringPrice[6] = "100.50"; // PRICE as String
        rowWithStringPrice[7] = 5L; // DISCOUNT
        rowWithStringPrice[8] = 1000L; // AMOUNT
        rowWithStringPrice[9] = "Test remarks"; // REMARKS
        rowWithStringPrice[10] = 1L; // STOCK_UNIT_POID
        rowWithStringPrice[11] = "testUser"; // CREATED_BY
        rowWithStringPrice[12] = Timestamp.from(Instant.now()); // CREATED_DATE
        rowWithStringPrice[13] = "testUser"; // LASTMODIFIED_BY
        rowWithStringPrice[14] = Timestamp.from(Instant.now()); // LASTMODIFIED_DATE
        rowWithStringPrice[15] = 300L; // QUOTATION_POID (Long)
        rowWithStringPrice[16] = 800L; // COST_AMT
        rowWithStringPrice[17] = 1L; // QTN_DET_ROW_ID
        rowWithStringPrice[18] = BigDecimal.valueOf(80.00); // PURCHASE_PRICE
        rowWithStringPrice[19] = 10L; // PURCHASE_QTY
        rowWithStringPrice[20] = 1000L; // NET_SALES
        rowWithStringPrice[21] = 50L; // NET_DISCOUNT
        rowWithStringPrice[22] = 200L; // ITEM_GP
        rowWithStringPrice[23] = 20L; // ITEM_GP_PER
        rowWithStringPrice[24] = "STOCK"; // ITEM_TYPE
        rowWithStringPrice[25] = 5L; // TAX_PERCENTAGE
        rowWithStringPrice[26] = 50L; // TAX_AMOUNT
        rowWithStringPrice[27] = 1L; // TAX_POID
        rowWithStringPrice[28] = 1000L; // BASE_AMT
        rowWithStringPrice[29] = 100L; // INCENTIVE
        rowWithStringPrice[30] = "COST-001"; // COST_POID
        
        List<Object[]> resultList = Collections.singletonList(rowWithStringPrice);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(resultList);

        // Act
        List<SalesInvoiceDtl> result = repository.findByTransactionPoidNative(TEST_TRANSACTION_POID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        SalesInvoiceDtl entity = result.get(0);
        assertEquals(0, new BigDecimal("100.50").compareTo(entity.getPrice()));
    }

    @Test
    void testFindByTransactionPoidNative_PriceAsNumber() {
        // Arrange - create new array with correct types
        Object[] rowWithNumberPrice = new Object[31];
        rowWithNumberPrice[0] = TEST_TRANSACTION_POID; // TRANSACTION_POID (Number)
        rowWithNumberPrice[1] = TEST_DET_ROW_ID; // DET_ROW_ID (Number)
        rowWithNumberPrice[2] = null; // DN_POID_LINK_FK
        rowWithNumberPrice[3] = null; // DET_ROW_ID_CHRG_FK
        rowWithNumberPrice[4] = 100L; // STOCK_POID
        rowWithNumberPrice[5] = 10L; // QUANTITY
        rowWithNumberPrice[6] = 100L; // PRICE as Long
        rowWithNumberPrice[7] = 5L; // DISCOUNT
        rowWithNumberPrice[8] = 1000L; // AMOUNT
        rowWithNumberPrice[9] = "Test remarks"; // REMARKS
        rowWithNumberPrice[10] = 1L; // STOCK_UNIT_POID
        rowWithNumberPrice[11] = "testUser"; // CREATED_BY
        rowWithNumberPrice[12] = Timestamp.from(Instant.now()); // CREATED_DATE
        rowWithNumberPrice[13] = "testUser"; // LASTMODIFIED_BY
        rowWithNumberPrice[14] = Timestamp.from(Instant.now()); // LASTMODIFIED_DATE
        rowWithNumberPrice[15] = 300L; // QUOTATION_POID (Long)
        rowWithNumberPrice[16] = 800L; // COST_AMT
        rowWithNumberPrice[17] = 1L; // QTN_DET_ROW_ID
        rowWithNumberPrice[18] = BigDecimal.valueOf(80.00); // PURCHASE_PRICE
        rowWithNumberPrice[19] = 10L; // PURCHASE_QTY
        rowWithNumberPrice[20] = 1000L; // NET_SALES
        rowWithNumberPrice[21] = 50L; // NET_DISCOUNT
        rowWithNumberPrice[22] = 200L; // ITEM_GP
        rowWithNumberPrice[23] = 20L; // ITEM_GP_PER
        rowWithNumberPrice[24] = "STOCK"; // ITEM_TYPE
        rowWithNumberPrice[25] = 5L; // TAX_PERCENTAGE
        rowWithNumberPrice[26] = 50L; // TAX_AMOUNT
        rowWithNumberPrice[27] = 1L; // TAX_POID
        rowWithNumberPrice[28] = 1000L; // BASE_AMT
        rowWithNumberPrice[29] = 100L; // INCENTIVE
        rowWithNumberPrice[30] = "COST-001"; // COST_POID
        
        List<Object[]> resultList = Collections.singletonList(rowWithNumberPrice);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(resultList);

        // Act
        List<SalesInvoiceDtl> result = repository.findByTransactionPoidNative(TEST_TRANSACTION_POID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        SalesInvoiceDtl entity = result.get(0);
        assertEquals(0, BigDecimal.valueOf(100).compareTo(entity.getPrice()));
    }

    @Test
    void testFindByTransactionPoidNative_RemarksAsCharacter() {
        // Arrange - create new array with correct types
        Object[] rowWithCharRemarks = new Object[31];
        rowWithCharRemarks[0] = TEST_TRANSACTION_POID; // TRANSACTION_POID (Number)
        rowWithCharRemarks[1] = TEST_DET_ROW_ID; // DET_ROW_ID (Number)
        rowWithCharRemarks[2] = null; // DN_POID_LINK_FK
        rowWithCharRemarks[3] = null; // DET_ROW_ID_CHRG_FK
        rowWithCharRemarks[4] = 100L; // STOCK_POID
        rowWithCharRemarks[5] = 10L; // QUANTITY
        rowWithCharRemarks[6] = BigDecimal.valueOf(100.50); // PRICE
        rowWithCharRemarks[7] = 5L; // DISCOUNT
        rowWithCharRemarks[8] = 1000L; // AMOUNT
        rowWithCharRemarks[9] = 'A'; // REMARKS as Character
        rowWithCharRemarks[10] = 1L; // STOCK_UNIT_POID
        rowWithCharRemarks[11] = "testUser"; // CREATED_BY
        rowWithCharRemarks[12] = Timestamp.from(Instant.now()); // CREATED_DATE
        rowWithCharRemarks[13] = "testUser"; // LASTMODIFIED_BY
        rowWithCharRemarks[14] = Timestamp.from(Instant.now()); // LASTMODIFIED_DATE
        rowWithCharRemarks[15] = 300L; // QUOTATION_POID (Long)
        rowWithCharRemarks[16] = 800L; // COST_AMT
        rowWithCharRemarks[17] = 1L; // QTN_DET_ROW_ID
        rowWithCharRemarks[18] = BigDecimal.valueOf(80.00); // PURCHASE_PRICE
        rowWithCharRemarks[19] = 10L; // PURCHASE_QTY
        rowWithCharRemarks[20] = 1000L; // NET_SALES
        rowWithCharRemarks[21] = 50L; // NET_DISCOUNT
        rowWithCharRemarks[22] = 200L; // ITEM_GP
        rowWithCharRemarks[23] = 20L; // ITEM_GP_PER
        rowWithCharRemarks[24] = "STOCK"; // ITEM_TYPE
        rowWithCharRemarks[25] = 5L; // TAX_PERCENTAGE
        rowWithCharRemarks[26] = 50L; // TAX_AMOUNT
        rowWithCharRemarks[27] = 1L; // TAX_POID
        rowWithCharRemarks[28] = 1000L; // BASE_AMT
        rowWithCharRemarks[29] = 100L; // INCENTIVE
        rowWithCharRemarks[30] = "COST-001"; // COST_POID
        
        List<Object[]> resultList = Collections.singletonList(rowWithCharRemarks);
        
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(resultList);

        // Act
        List<SalesInvoiceDtl> result = repository.findByTransactionPoidNative(TEST_TRANSACTION_POID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        SalesInvoiceDtl entity = result.get(0);
        assertEquals("A", entity.getRemarks()); // Should convert Character to String
    }
}
