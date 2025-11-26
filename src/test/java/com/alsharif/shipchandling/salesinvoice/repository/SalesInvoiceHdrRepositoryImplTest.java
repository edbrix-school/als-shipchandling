package com.alsharif.shipchandling.salesinvoice.repository;

import com.alsharif.shipchandling.salesinvoice.entity.SalesInvoiceHdr;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesInvoiceHdrRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query countQuery;

    @Mock
    private Query mainQuery;

    private SalesInvoiceHdrRepositoryImpl repository;

    private static final Long TEST_GROUP_POID = 1L;
    private static final Long TEST_COMPANY_POID = 2L;
    private static final Long TEST_TRANSACTION_POID = 100L;

    private Pageable pageable;
    private Object[] testRowData;

    @BeforeEach
    void setUp() {
        repository = new SalesInvoiceHdrRepositoryImpl();
        // Inject EntityManager using reflection since @PersistenceContext won't work in unit tests
        ReflectionTestUtils.setField(repository, "entityManager", entityManager);
        
        pageable = PageRequest.of(0, 10);
        
        // Create test row data: 57 columns (0-55 for entity, 56 for customer name)
        testRowData = new Object[57];
        testRowData[0] = TEST_TRANSACTION_POID; // transactionPoid
        testRowData[1] = "INV-001"; // docRef
        testRowData[2] = Timestamp.from(Instant.now()); // transactionDate
        testRowData[3] = TEST_GROUP_POID; // groupPoid
        testRowData[4] = TEST_COMPANY_POID; // companyPoid
        testRowData[5] = "CUSTOMER"; // partyType
        testRowData[6] = 50L; // customerPoid
        testRowData[7] = null; // principalPoid
        testRowData[8] = null; // customerAddrPoid
        testRowData[9] = "USD"; // currencyCode
        testRowData[10] = 1L; // currencyRate
        // Fill remaining fields with nulls or default values for simplicity
        for (int i = 11; i < 56; i++) {
            testRowData[i] = null;
        }
        testRowData[56] = "Test Customer"; // customerName
    }

    @Test
    void testFindAllWithFiltersAndCustomerName_Success() {
        // Arrange
        List<Object[]> resultList = Collections.singletonList(testRowData);
        Long totalCount = 1L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery).thenReturn(mainQuery);
        when(countQuery.getSingleResult()).thenReturn(totalCount);
        when(mainQuery.getResultList()).thenReturn(resultList);

        // Act
        Page<Object[]> result = repository.findAllWithFiltersAndCustomerName(
                TEST_GROUP_POID, TEST_COMPANY_POID,
                null, null, null, null, null,
                null, null, null,
                pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        Object[] content = result.getContent().get(0);
        assertNotNull(content);
        assertEquals(2, content.length); // [entity, customerName]
        assertTrue(content[0] instanceof SalesInvoiceHdr);
        assertEquals("Test Customer", content[1]);

        verify(entityManager, times(2)).createNativeQuery(anyString());
        verify(entityManager, times(1)).detach(any(SalesInvoiceHdr.class));
        verify(countQuery, times(1)).getSingleResult();
        verify(mainQuery, times(1)).getResultList();
        verify(mainQuery, times(1)).setFirstResult(0);
        verify(mainQuery, times(1)).setMaxResults(10);
    }

    @Test
    void testFindAllWithFiltersAndCustomerName_WithAllFilters() {
        // Arrange
        List<Object[]> resultList = Collections.emptyList();
        Long totalCount = 0L;
        Timestamp fromDate = Timestamp.from(Instant.now().minusSeconds(86400));
        Timestamp toDate = Timestamp.from(Instant.now());

        when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery).thenReturn(mainQuery);
        when(countQuery.getSingleResult()).thenReturn(totalCount);
        when(mainQuery.getResultList()).thenReturn(resultList);

        // Act
        Page<Object[]> result = repository.findAllWithFiltersAndCustomerName(
                TEST_GROUP_POID, TEST_COMPANY_POID,
                "IN_PROGRESS", "N", 50L, 100L, "QTN-001",
                fromDate, toDate, "search",
                pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());

        // Verify query parameters were set
        verify(mainQuery, atLeast(9)).setParameter(anyString(), any());
        verify(countQuery, atLeast(9)).setParameter(anyString(), any());
    }

    @Test
    void testFindAllWithFiltersAndCustomerName_EmptyResult() {
        // Arrange
        List<Object[]> resultList = Collections.emptyList();
        Long totalCount = 0L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery).thenReturn(mainQuery);
        when(countQuery.getSingleResult()).thenReturn(totalCount);
        when(mainQuery.getResultList()).thenReturn(resultList);

        // Act
        Page<Object[]> result = repository.findAllWithFiltersAndCustomerName(
                TEST_GROUP_POID, TEST_COMPANY_POID,
                null, null, null, null, null,
                null, null, null,
                pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void testFindAllWithFiltersAndCustomerName_MultipleResults() {
        // Arrange
        List<Object[]> resultList = new ArrayList<>();
        resultList.add(testRowData);
        resultList.add(testRowData);
        Long totalCount = 2L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery).thenReturn(mainQuery);
        when(countQuery.getSingleResult()).thenReturn(totalCount);
        when(mainQuery.getResultList()).thenReturn(resultList);

        // Act
        Page<Object[]> result = repository.findAllWithFiltersAndCustomerName(
                TEST_GROUP_POID, TEST_COMPANY_POID,
                null, null, null, null, null,
                null, null, null,
                pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void testFindAllWithFiltersAndCustomerName_WithPagination() {
        // Arrange
        Pageable pageableSecondPage = PageRequest.of(1, 5);
        List<Object[]> resultList = Collections.singletonList(testRowData);
        Long totalCount = 15L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery).thenReturn(mainQuery);
        when(countQuery.getSingleResult()).thenReturn(totalCount);
        when(mainQuery.getResultList()).thenReturn(resultList);

        // Act
        Page<Object[]> result = repository.findAllWithFiltersAndCustomerName(
                TEST_GROUP_POID, TEST_COMPANY_POID,
                null, null, null, null, null,
                null, null, null,
                pageableSecondPage);

        // Assert
        assertNotNull(result);
        assertEquals(15, result.getTotalElements());
        assertEquals(5, result.getPageable().getPageSize());
        assertEquals(1, result.getPageable().getPageNumber());
        
        // Verify pagination parameters
        verify(mainQuery, times(1)).setFirstResult(5); // offset = page * size = 1 * 5
        verify(mainQuery, times(1)).setMaxResults(5); // page size = 5
    }

    @Test
    void testFindAllWithFiltersAndCustomerName_WithNullCustomerName() {
        // Arrange
        Object[] rowWithNullCustomer = new Object[57];
        System.arraycopy(testRowData, 0, rowWithNullCustomer, 0, 56);
        rowWithNullCustomer[56] = null; // null customer name
        
        List<Object[]> resultList = Collections.singletonList(rowWithNullCustomer);
        Long totalCount = 1L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery).thenReturn(mainQuery);
        when(countQuery.getSingleResult()).thenReturn(totalCount);
        when(mainQuery.getResultList()).thenReturn(resultList);

        // Act
        Page<Object[]> result = repository.findAllWithFiltersAndCustomerName(
                TEST_GROUP_POID, TEST_COMPANY_POID,
                null, null, null, null, null,
                null, null, null,
                pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        Object[] content = result.getContent().get(0);
        assertNull(content[1]); // customerName should be null
    }

    @Test
    void testFindAllWithFiltersAndCustomerName_WithCharacterTypeFields() {
        // Arrange - Simulate Character type from Oracle CHAR columns
        Object[] rowWithCharTypes = new Object[57];
        System.arraycopy(testRowData, 0, rowWithCharTypes, 0, 57);
        rowWithCharTypes[5] = 'C'; // Character instead of String
        
        List<Object[]> resultList = Collections.singletonList(rowWithCharTypes);
        Long totalCount = 1L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery).thenReturn(mainQuery);
        when(countQuery.getSingleResult()).thenReturn(totalCount);
        when(mainQuery.getResultList()).thenReturn(resultList);

        // Act
        Page<Object[]> result = repository.findAllWithFiltersAndCustomerName(
                TEST_GROUP_POID, TEST_COMPANY_POID,
                null, null, null, null, null,
                null, null, null,
                pageable);

        // Assert - Should handle Character types correctly via toStringSafe
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}
