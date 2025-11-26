package com.alsharif.shipchandling.deliverynote.repository;

import com.alsharif.shipchandling.exceptions.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesDeliveryNoteRepositoryTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private CallableStatement callableStatement;

    private SalesDeliveryNoteRepository repository;

    private static final Long TEST_COMPANY_POID = 2L;
    private static final Long TEST_TRANSACTION_POID = 100L;
    private static final Long TEST_GROUP_POID = 1L;
    private static final String TEST_USER_ID = "testUser";

    @BeforeEach
    void setUp() throws SQLException {
        repository = new SalesDeliveryNoteRepository();
        // Inject DataSource using reflection since @Autowired won't work in unit tests
        ReflectionTestUtils.setField(repository, "dataSource", dataSource);

        // Setup common mocks - use lenient to allow unused stubs in some tests
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(connection.prepareCall(anyString())).thenReturn(callableStatement);
    }

    @Test
    void testCallSalesSCDNCustomerValidateProc_Success_True() throws SQLException {
        // Arrange
        String procResult = "true";

        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(3)).thenReturn(procResult);

        // Act
        boolean result = repository.callSalesSCDNCustomerValidateProc(
                TEST_COMPANY_POID, TEST_TRANSACTION_POID);

        // Assert
        assertTrue(result);

        verify(connection, times(1)).prepareCall(contains("PROC_SALES_SCDN_CUST_VALIDATE"));
        verify(callableStatement, times(1)).setLong(1, TEST_TRANSACTION_POID);
        verify(callableStatement, times(1)).setLong(2, TEST_COMPANY_POID);
        verify(callableStatement, times(1)).registerOutParameter(3, Types.VARCHAR);
        verify(callableStatement, times(1)).execute();
    }

    @Test
    void testCallSalesSCDNCustomerValidateProc_Success_False() throws SQLException {
        // Arrange
        String procResult = "false";

        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(3)).thenReturn(procResult);

        // Act
        boolean result = repository.callSalesSCDNCustomerValidateProc(
                TEST_COMPANY_POID, TEST_TRANSACTION_POID);

        // Assert
        assertFalse(result);
    }

    @Test
    void testCallSalesSCDNCustomerValidateProc_CaseInsensitive() throws SQLException {
        // Arrange
        String procResult = "TRUE";

        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(3)).thenReturn(procResult);

        // Act
        boolean result = repository.callSalesSCDNCustomerValidateProc(
                TEST_COMPANY_POID, TEST_TRANSACTION_POID);

        // Assert
        assertTrue(result);
    }

    @Test
    void testCallSalesSCDNCustomerValidateProc_Error() throws SQLException {
        // Arrange
        String procResult = "ERROR: Invalid customer";

        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(3)).thenReturn(procResult);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            repository.callSalesSCDNCustomerValidateProc(TEST_COMPANY_POID, TEST_TRANSACTION_POID);
        });

        assertTrue(exception.getMessage().contains("PROC_SALES_SCDN_CUST_VALIDATE failed"));
    }

    @Test
    void testCallSalesSCDNCustomerValidateProc_SQLException() throws SQLException {
        // Arrange
        when(callableStatement.execute()).thenThrow(new SQLException("Database error"));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            repository.callSalesSCDNCustomerValidateProc(TEST_COMPANY_POID, TEST_TRANSACTION_POID);
        });

        assertTrue(exception.getMessage().contains("Error calling PROC_SALES_SCDN_CUST_VALIDATE"));
    }

    @Test
    void testCallUpdateDeletedDetailsProc_Success() throws SQLException {
        // Arrange
        String procResult = "SUCCESS";

        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(5)).thenReturn(procResult);

        // Act
        String result = repository.callUpdateDeletedDetailsProc(
                TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID, TEST_TRANSACTION_POID);

        // Assert
        assertNotNull(result);
        assertEquals(procResult, result);

        verify(connection, times(1)).prepareCall(contains("PROC_DN_UPDATE_DELETED_DTLSQH"));
        verify(callableStatement, times(1)).setLong(1, TEST_GROUP_POID);
        verify(callableStatement, times(1)).setLong(2, TEST_COMPANY_POID);
        verify(callableStatement, times(1)).setString(3, TEST_USER_ID);
        verify(callableStatement, times(1)).setLong(4, TEST_TRANSACTION_POID);
        verify(callableStatement, times(1)).registerOutParameter(5, Types.VARCHAR);
        verify(callableStatement, times(1)).execute();
    }

    @Test
    void testCallUpdateDeletedDetailsProc_NullResult() throws SQLException {
        // Arrange
        String procResult = null;

        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(5)).thenReturn(procResult);

        // Act
        String result = repository.callUpdateDeletedDetailsProc(
                TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID, TEST_TRANSACTION_POID);

        // Assert
        assertNull(result);
    }

    @Test
    void testCallUpdateDeletedDetailsProc_Error() throws SQLException {
        // Arrange
        String procResult = "ERROR: Failed to update";

        when(callableStatement.execute()).thenReturn(true);
        when(callableStatement.getString(5)).thenReturn(procResult);

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            repository.callUpdateDeletedDetailsProc(
                    TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID, TEST_TRANSACTION_POID);
        });

        assertTrue(exception.getMessage().contains("PROC_DN_UPDATE_DELETED_DTLSQH failed"));
    }

    @Test
    void testCallUpdateDeletedDetailsProc_SQLException() throws SQLException {
        // Arrange
        when(callableStatement.execute()).thenThrow(new SQLException("Connection error"));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            repository.callUpdateDeletedDetailsProc(
                    TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID, TEST_TRANSACTION_POID);
        });

        assertTrue(exception.getMessage().contains("Error calling PROC_DN_UPDATE_DELETED_DTLSQH"));
    }

    @Test
    void testCallUpdateDeletedDetailsProc_ConnectionFailure() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection pool exhausted"));

        // Act & Assert
        CustomException exception = assertThrows(CustomException.class, () -> {
            repository.callUpdateDeletedDetailsProc(
                    TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID, TEST_TRANSACTION_POID);
        });

        assertTrue(exception.getMessage().contains("Error calling PROC_DN_UPDATE_DELETED_DTLSQH"));
    }
}
