package com.alsharif.shipchandling.salesinvoice.repository;

import com.alsharif.shipchandling.exceptions.CustomException;
import com.alsharif.shipchandling.salesinvoice.dto.QuotationCurrencyDto;
import com.alsharif.shipchandling.salesinvoice.dto.QuotationItemDto;
import com.alsharif.shipchandling.salesinvoice.dto.request.CalculateDiscountCommissionRequest;
import com.alsharif.shipchandling.salesinvoice.dto.request.LoadQuotationItemsRequest;
import com.alsharif.shipchandling.salesinvoice.dto.response.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesInvoiceStoredProcRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private Connection connection;

    @Mock
    private CallableStatement callableStatement;

    @Mock
    private ResultSet resultSet;

    private SalesInvoiceStoredProcRepository repository;

    private static final Long TEST_CUSTOMER_POID = 50L;
    private static final Long TEST_TRANSACTION_POID = 100L;
    private static final Long TEST_GROUP_POID = 1L;
    private static final Long TEST_COMPANY_POID = 2L;

    @BeforeEach
    void setUp() throws Exception {
        repository = new SalesInvoiceStoredProcRepository();
        // Inject JdbcTemplate using reflection since @Autowired won't work in unit tests
        ReflectionTestUtils.setField(repository, "jdbcTemplate", jdbcTemplate);

        // Setup common mocks for Connection and CallableStatement
        @SuppressWarnings("unchecked")
        org.springframework.jdbc.core.ConnectionCallback<Object> callbackStub = 
            any(org.springframework.jdbc.core.ConnectionCallback.class);
        when(jdbcTemplate.execute(callbackStub))
            .thenAnswer(invocation -> {
                org.springframework.jdbc.core.ConnectionCallback<?> callback = 
                    invocation.getArgument(0);
                return callback.doInConnection(connection);
            });
    }

    @Test
    void testCallCustomerValidateProc_Success() throws Exception {
        // Arrange
        String creditType = "NORMAL";
        String authorizedId = "AUTH001";
        String procResult = "SUCCESS";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(2)).thenReturn(procResult);

        // Act
        ValidationResponse result = repository.callCustomerValidateProc(
                TEST_CUSTOMER_POID, creditType, authorizedId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(procResult, result.getMessage());
        
        verify(callableStatement, times(1)).setLong(1, TEST_CUSTOMER_POID);
        verify(callableStatement, times(1)).registerOutParameter(2, Types.VARCHAR);
        verify(callableStatement, times(1)).setString(3, creditType);
        verify(callableStatement, times(1)).setString(4, authorizedId);
        verify(callableStatement, times(1)).execute();
    }

    @Test
    void testCallCustomerValidateProc_Error() throws Exception {
        // Arrange
        String creditType = "NORMAL";
        String authorizedId = "AUTH001";
        String procResult = "ERROR: Invalid customer";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(2)).thenReturn(procResult);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            repository.callCustomerValidateProc(TEST_CUSTOMER_POID, creditType, authorizedId);
        });

        assertTrue(exception.getMessage().contains("PROC_VALIDATE_CUSTOMER failed"));
    }

    @Test
    void testCallCustomerValidateProc_SQLException() throws Exception {
        // Arrange
        String creditType = "NORMAL";
        String authorizedId = "AUTH001";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenThrow(new java.sql.SQLException("Database error"));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            repository.callCustomerValidateProc(TEST_CUSTOMER_POID, creditType, authorizedId);
        });

        assertTrue(exception.getMessage().contains("Error calling PROC_VALIDATE_CUSTOMER"));
    }

    @Test
    void testCallCustomerEditValidateProc_Success() throws Exception {
        // Arrange
        String procResult = "True";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(3)).thenReturn(procResult);

        // Act
        boolean result = repository.callCustomerEditValidateProc(
                TEST_TRANSACTION_POID, TEST_CUSTOMER_POID);

        // Assert
        assertTrue(result);
        
        verify(callableStatement, times(1)).setLong(1, TEST_TRANSACTION_POID);
        verify(callableStatement, times(1)).setLong(2, TEST_CUSTOMER_POID);
        verify(callableStatement, times(1)).registerOutParameter(3, Types.VARCHAR);
    }

    @Test
    void testCallCustomerEditValidateProc_False() throws Exception {
        // Arrange
        String procResult = "False";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(3)).thenReturn(procResult);

        // Act
        boolean result = repository.callCustomerEditValidateProc(
                TEST_TRANSACTION_POID, TEST_CUSTOMER_POID);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCallUnloadQuotationProc_Success() throws Exception {
        // Arrange
        String qtnPoid = "QTN-001";
        String procResult = "SUCCESS";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(3)).thenReturn(procResult);

        // Act
        UnloadQuotationResponse result = repository.callUnloadQuotationProc(
                TEST_TRANSACTION_POID, qtnPoid);

        // Assert
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(procResult, result.getMessage());
        
        verify(callableStatement, times(1)).setLong(1, TEST_TRANSACTION_POID);
        verify(callableStatement, times(1)).setString(2, qtnPoid);
        verify(callableStatement, times(1)).registerOutParameter(3, Types.VARCHAR);
    }

    @Test
    void testCallLoadDeliveryNoteProc_Success() throws Exception {
        // Arrange
        String procResult = "SUCCESS";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(2)).thenReturn(procResult);

        // Act
        ValidationResponse result = repository.callLoadDeliveryNoteProc(TEST_TRANSACTION_POID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(procResult, result.getMessage());
    }

    @Test
    void testCallLoadDeliveryNoteProc_NoData() throws Exception {
        // Arrange
        String procResult = "No_Data";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(2)).thenReturn(procResult);

        // Act
        ValidationResponse result = repository.callLoadDeliveryNoteProc(TEST_TRANSACTION_POID);

        // Assert
        assertNotNull(result);
        assertFalse(result.getSuccess());
    }

    @Test
    void testCallLoadQuotationItemsProc_Success() throws Exception {
        // Arrange
        LoadQuotationItemsRequest request = new LoadQuotationItemsRequest();
        request.setQtnPoid("QTN-001");
        request.setIncentiveAmt(100L);
        request.setIncentiveAmt2(200L);
        request.setIncentiveAmt3(300L);

        String procResult = "SUCCESS";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(6)).thenReturn(procResult);
        when(callableStatement.getObject(7)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // One row
        when(resultSet.getObject("DISCOUNT_PERCENT")).thenReturn(10L);
        when(resultSet.getLong("DISCOUNT_AMT")).thenReturn(50L);
        when(resultSet.getObject("INCENTIVE_PERCENT")).thenReturn(5L);
        when(resultSet.getLong("INCENTIVE_PERCENT2")).thenReturn(10L);
        when(resultSet.getLong("INCENTIVE_PERCENT3")).thenReturn(15L);
        when(resultSet.getLong("INCENTIVE_AMT")).thenReturn(100L);
        when(resultSet.getLong("INCENTIVE_AMT2")).thenReturn(200L);
        when(resultSet.getLong("INCENTIVE_AMT3")).thenReturn(300L);
        when(resultSet.getLong("TOTAL_GP_AMT")).thenReturn(500L);
        when(resultSet.getObject("TOTAL_GP_PERCENT")).thenReturn(20L);
        when(resultSet.getLong("INV_AMOUNT")).thenReturn(1000L);

        // Act
        LoadQuotationItemsResponse result = repository.callLoadQuotationItemsProc(
                TEST_TRANSACTION_POID, request);

        // Assert
        assertNotNull(result);
        assertEquals("Quotation items loaded successfully", result.getMessage());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        
        QuotationItemDto item = result.getItems().get(0);
        assertEquals(10L, item.getDiscountPercent());
        assertEquals(50L, item.getDiscountAmt());
        assertEquals(5L, item.getIncentivePercent());
        
        verify(callableStatement, times(1)).setLong(1, TEST_TRANSACTION_POID);
        verify(callableStatement, times(1)).setString(2, "QTN-001");
        verify(callableStatement, times(1)).registerOutParameter(6, Types.VARCHAR);
        verify(callableStatement, times(1)).registerOutParameter(7, Types.REF_CURSOR);
    }

    @Test
    void testCallLoadQuotationItemsProc_NullResultSet() throws Exception {
        // Arrange
        LoadQuotationItemsRequest request = new LoadQuotationItemsRequest();
        request.setQtnPoid("QTN-001");
        // Set incentive amounts to 0L to avoid null handling issues with JDBC mocks
        // (The repository passes null to setLong which is problematic to mock)
        request.setIncentiveAmt(0L);
        request.setIncentiveAmt2(0L);
        request.setIncentiveAmt3(0L);
        String procResult = "SUCCESS";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(6)).thenReturn(procResult);
        when(callableStatement.getObject(7)).thenReturn(null); // Null ResultSet

        // Act
        LoadQuotationItemsResponse result = repository.callLoadQuotationItemsProc(
                TEST_TRANSACTION_POID, request);

        // Assert
        assertNotNull(result);
        assertEquals("Quotation items loaded successfully", result.getMessage());
        assertNotNull(result.getItems());
        assertEquals(0, result.getItems().size());
    }

    @Test
    void testCallCalculateGpProc_Success() throws Exception {
        // Arrange
        String qtnId = "QTN-001";
        String procResult = "UPDATED";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(3)).thenReturn(procResult);

        // Act
        ValidationResponse result = repository.callCalculateGpProc(TEST_TRANSACTION_POID, qtnId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(procResult, result.getMessage());
    }

    @Test
    void testCallCalculateDueDateProc_Success() throws Exception {
        // Arrange
        Timestamp transactionDate = Timestamp.from(Instant.now());
        Timestamp dueDate = Timestamp.from(Instant.now().plusSeconds(86400 * 30));
        Long creditDays = 30L;
        String calculationType = "FROM_DATE";
        
        Timestamp resultDueDate = Timestamp.from(Instant.now().plusSeconds(86400 * 30));
        Long resultDueDays = 30L;
        String procResult = "True";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getTimestamp(6)).thenReturn(resultDueDate);
        when(callableStatement.getLong(7)).thenReturn(resultDueDays);
        when(callableStatement.getString(8)).thenReturn(procResult);

        // Act
        CalculateDueDateResponse result = repository.callCalculateDueDateProc(
                TEST_GROUP_POID, TEST_COMPANY_POID, transactionDate,
                dueDate, creditDays, calculationType, TEST_CUSTOMER_POID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals(procResult, result.getMessage());
        assertEquals(resultDueDate, result.getDueDate());
        assertEquals(resultDueDays, result.getDueDays());
        
        verify(callableStatement, times(1)).setTimestamp(1, transactionDate);
        verify(callableStatement, times(1)).setTimestamp(2, dueDate);
        verify(callableStatement, times(1)).setLong(3, creditDays);
        verify(callableStatement, times(1)).setString(4, calculationType);
        verify(callableStatement, times(1)).setLong(5, TEST_CUSTOMER_POID);
    }

    @Test
    void testCallCalculateItemDiscountCommissionProc_Success() throws Exception {
        // Arrange
        CalculateDiscountCommissionRequest request = new CalculateDiscountCommissionRequest();
        request.setInvDiscount(100L);
        request.setIncentiveAmt(50L);
        request.setIncentiveAmt2(25L);
        request.setIncentiveAmt3(10L);
        request.setType("Amount");
        request.setIncentivePercent(5L);
        request.setIncentivePercent2(10L);
        request.setIncentivePercent3(15L);
        
        Long detRowId = 1L;
        String qtnPoid = "QTN-001";
        Long userId = 999L;
        String procResult = "SUCCESS";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(12)).thenReturn(procResult);
        when(callableStatement.getObject(13)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // Empty ResultSet

        // Act
        CalculateDiscountCommissionResponse result = 
            repository.callCalculateItemDiscountCommissionProc(
                TEST_TRANSACTION_POID, request, detRowId, qtnPoid, userId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getSuccess());
        assertEquals("Discount/Commission calculated successfully", result.getMessage());
        assertNotNull(result.getItems());
        
        verify(callableStatement, times(1)).setLong(1, userId);
        verify(callableStatement, times(1)).setLong(2, TEST_TRANSACTION_POID);
        verify(callableStatement, times(1)).setString(3, qtnPoid);
        verify(callableStatement, times(1)).registerOutParameter(12, Types.VARCHAR);
        verify(callableStatement, times(1)).registerOutParameter(13, Types.REF_CURSOR);
    }

    @Test
    void testCallAuthorizationProc_Success() throws Exception {
        // Arrange
        String authorizedId = "AUTH001";
        String userId = "USER001";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);

        // Act
        repository.callAuthorizationProc(TEST_TRANSACTION_POID, authorizedId, userId);

        // Assert
        verify(callableStatement, times(1)).setLong(1, TEST_TRANSACTION_POID);
        verify(callableStatement, times(1)).setString(2, authorizedId);
        verify(callableStatement, times(1)).setString(3, userId);
        verify(callableStatement, times(1)).execute();
    }

    @Test
    void testCallLoadQuotationCurrencyProc_Success() throws Exception {
        // Arrange
        String qtnPoId = "QTN-001";
        String procResult = "SUCCESS";

        when(connection.prepareCall(anyString())).thenReturn(callableStatement);
        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(3)).thenReturn(procResult);
        when(callableStatement.getObject(4)).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // One row
        when(resultSet.getLong("CURRENCY_CODE")).thenReturn(840L); // USD
        when(resultSet.getLong("CURRENCY_RATE")).thenReturn(1L);

        // Act
        LoadQuotationCurrencyResponse result = repository.callLoadQuotationCurrencyProc(
                TEST_TRANSACTION_POID, qtnPoId);

        // Assert
        assertNotNull(result);
        assertEquals("Quotation currency loaded successfully", result.getMessage());
        assertNotNull(result.getQuotationCurrencyList());
        assertEquals(1, result.getQuotationCurrencyList().size());
        
        QuotationCurrencyDto currency = result.getQuotationCurrencyList().get(0);
        assertEquals(840L, currency.getCurrencyCode());
        assertEquals(1L, currency.getCurrencyRate());
    }
}
