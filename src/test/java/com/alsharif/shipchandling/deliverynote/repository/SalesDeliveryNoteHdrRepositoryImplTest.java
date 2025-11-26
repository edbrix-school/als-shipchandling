package com.alsharif.shipchandling.deliverynote.repository;

import com.alsharif.shipchandling.deliverynote.entity.SalesDeliveryNoteHdr;
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
class SalesDeliveryNoteHdrRepositoryImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query countQuery;

    @Mock
    private Query mainQuery;

    private SalesDeliveryNoteHdrRepositoryImpl repository;

    private static final Long TEST_COMPANY_POID = 2L;
    private static final Long TEST_TRANSACTION_POID = 100L;
    private static final Long TEST_CUSTOMER_POID = 50L;

    private Pageable pageable;
    private Object[] testRowData;

    @BeforeEach
    void setUp() {
        repository = new SalesDeliveryNoteHdrRepositoryImpl();
        // Inject EntityManager using reflection since @PersistenceContext won't work in unit tests
        ReflectionTestUtils.setField(repository, "entityManager", entityManager);

        pageable = PageRequest.of(0, 10);

        // Create test row data: 34 columns (0-32 for entity, 33 for customer name)
        testRowData = new Object[34];
        testRowData[0] = TEST_TRANSACTION_POID; // transactionPoid
        testRowData[1] = "DN-001"; // docRef
        testRowData[2] = Timestamp.from(Instant.now()); // transactionDate
        testRowData[3] = TEST_COMPANY_POID; // companyPoid
        testRowData[4] = TEST_CUSTOMER_POID; // customerPoid
        testRowData[5] = "USD"; // currencyCode
        testRowData[6] = 1L; // currencyRate
        testRowData[7] = "PENDING"; // deliveryStatus
        testRowData[8] = 10L; // salesmanPoid
        testRowData[9] = "CASH"; // paymentMode
        testRowData[10] = "FOB"; // deliveryTerms
        testRowData[11] = 20L; // linePoid
        testRowData[12] = "VESSEL-001"; // vesselPoid (String)
        testRowData[13] = "Test Vessel"; // vesselName
        testRowData[14] = "VOY-001"; // voyageRef
        testRowData[15] = 30L; // portPoid
        testRowData[16] = "Port Name"; // portDescription
        testRowData[17] = "QTN-001"; // qtnRefNo
        testRowData[18] = "Agent Name"; // vesselAgent
        testRowData[19] = "Delivery Address"; // deliveryToAddress
        testRowData[20] = "Y"; // descriptionPrintYn
        testRowData[21] = "Address Details"; // partyAddressDetails
        testRowData[22] = 40L; // printDivisionPoid
        testRowData[23] = "CUSTOMER"; // partyType
        testRowData[24] = null; // principalPoid
        testRowData[25] = 100L; // totalDiscount
        testRowData[26] = 1000L; // totalAmount
        testRowData[27] = "Remarks"; // remarks
        testRowData[28] = "N"; // deleted
        testRowData[29] = "testUser"; // createdBy
        testRowData[30] = Timestamp.from(Instant.now()); // createdDate
        testRowData[31] = "testUser"; // lastmodifiedBy
        testRowData[32] = Timestamp.from(Instant.now()); // lastmodifiedDate
        testRowData[33] = "Test Customer"; // customerName
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
                TEST_COMPANY_POID,
                null, null, null, null,
                null, null, null,
                pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        Object[] content = result.getContent().get(0);
        assertNotNull(content);
        assertEquals(2, content.length); // [entity, customerName]
        assertTrue(content[0] instanceof SalesDeliveryNoteHdr);
        assertEquals("Test Customer", content[1]);

        verify(entityManager, times(2)).createNativeQuery(anyString());
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
                TEST_COMPANY_POID,
                "PENDING", TEST_CUSTOMER_POID, 10L, "QTN-001",
                fromDate, toDate, "search",
                pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());

        // Verify query parameters were set
        verify(mainQuery, atLeast(8)).setParameter(anyString(), any());
        verify(countQuery, atLeast(8)).setParameter(anyString(), any());
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
                TEST_COMPANY_POID,
                null, null, null, null,
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
                TEST_COMPANY_POID,
                null, null, null, null,
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
                TEST_COMPANY_POID,
                null, null, null, null,
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
        Object[] rowWithNullCustomer = new Object[34];
        System.arraycopy(testRowData, 0, rowWithNullCustomer, 0, 33);
        rowWithNullCustomer[33] = null; // null customer name

        List<Object[]> resultList = Collections.singletonList(rowWithNullCustomer);
        Long totalCount = 1L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery).thenReturn(mainQuery);
        when(countQuery.getSingleResult()).thenReturn(totalCount);
        when(mainQuery.getResultList()).thenReturn(resultList);

        // Act
        Page<Object[]> result = repository.findAllWithFiltersAndCustomerName(
                TEST_COMPANY_POID,
                null, null, null, null,
                null, null, null,
                pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        Object[] content = result.getContent().get(0);
        assertNull(content[1]); // customerName should be null
    }

    @Test
    void testFindAllWithFiltersAndCustomerName_WithNullValues() {
        // Arrange
        Object[] rowWithNulls = new Object[34];
        for (int i = 0; i < 34; i++) {
            rowWithNulls[i] = null;
        }
        rowWithNulls[0] = TEST_TRANSACTION_POID; // transactionPoid
        rowWithNulls[3] = TEST_COMPANY_POID; // companyPoid
        rowWithNulls[4] = TEST_CUSTOMER_POID; // customerPoid

        List<Object[]> resultList = Collections.singletonList(rowWithNulls);
        Long totalCount = 1L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery).thenReturn(mainQuery);
        when(countQuery.getSingleResult()).thenReturn(totalCount);
        when(mainQuery.getResultList()).thenReturn(resultList);

        // Act
        Page<Object[]> result = repository.findAllWithFiltersAndCustomerName(
                TEST_COMPANY_POID,
                null, null, null, null,
                null, null, null,
                pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        Object[] content = result.getContent().get(0);
        assertTrue(content[0] instanceof SalesDeliveryNoteHdr);
    }

    @Test
    void testFindAllWithFiltersAndCustomerName_WithCharacterTypeFields() {
        // Arrange - Simulate Character type from Oracle CHAR columns
        Object[] rowWithCharTypes = new Object[34];
        System.arraycopy(testRowData, 0, rowWithCharTypes, 0, 34);
        rowWithCharTypes[7] = 'P'; // Character instead of String

        List<Object[]> resultList = Collections.singletonList(rowWithCharTypes);
        Long totalCount = 1L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery).thenReturn(mainQuery);
        when(countQuery.getSingleResult()).thenReturn(totalCount);
        when(mainQuery.getResultList()).thenReturn(resultList);

        // Act
        Page<Object[]> result = repository.findAllWithFiltersAndCustomerName(
                TEST_COMPANY_POID,
                null, null, null, null,
                null, null, null,
                pageable);

        // Assert - Should handle Character types correctly via toStringSafe
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testFindAllWithFiltersAndCustomerName_EntityMapping() {
        // Arrange
        List<Object[]> resultList = Collections.singletonList(testRowData);
        Long totalCount = 1L;

        when(entityManager.createNativeQuery(anyString())).thenReturn(countQuery).thenReturn(mainQuery);
        when(countQuery.getSingleResult()).thenReturn(totalCount);
        when(mainQuery.getResultList()).thenReturn(resultList);

        // Act
        Page<Object[]> result = repository.findAllWithFiltersAndCustomerName(
                TEST_COMPANY_POID,
                null, null, null, null,
                null, null, null,
                pageable);

        // Assert - Verify entity fields are correctly mapped
        assertNotNull(result);
        SalesDeliveryNoteHdr entity = (SalesDeliveryNoteHdr) result.getContent().get(0)[0];
        assertEquals(TEST_TRANSACTION_POID, entity.getTransactionPoid());
        assertEquals("DN-001", entity.getDocRef());
        assertEquals(TEST_COMPANY_POID, entity.getCompanyPoid());
        assertEquals(TEST_CUSTOMER_POID, entity.getCustomerPoid());
        assertEquals("USD", entity.getCurrencyCode());
        assertEquals(1L, entity.getCurrencyRate());
        assertEquals("PENDING", entity.getDeliveryStatus());
        assertEquals("Test Vessel", entity.getVesselName());
    }
}
