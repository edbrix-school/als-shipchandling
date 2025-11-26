package com.alsharif.shipchandling.commonlov.repository;

import com.alsharif.shipchandling.commonlov.dto.LovItem;
import com.alsharif.shipchandling.commonlov.dto.LovResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ConnectionCallback;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LovRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private LovRepository lovRepository;

    private String lovName;
    private Long docKeyPoid;
    private String filterValue;

    @BeforeEach
    void setUp() {
        lovName = "RFQ_STATUS";
        docKeyPoid = 123L;
        filterValue = "FILTER";
    }

    @Test
    void testGetLovList_Success() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        CallableStatement mockCallableStatement = mock(CallableStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(jdbcTemplate.execute(any(ConnectionCallback.class))).thenAnswer(invocation -> {
            ConnectionCallback<?> callback = invocation.getArgument(0);
            return callback.doInConnection(mockConnection);
        });

        when(mockConnection.prepareCall(anyString())).thenReturn(mockCallableStatement);
        when(mockCallableStatement.getObject(7)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getLong("POID")).thenReturn(1L, 2L);
        when(mockResultSet.getString("CODE")).thenReturn("CODE1", "CODE2");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("DESC1", "DESC2");

        // Act
        LovResponse result = lovRepository.getLovList(lovName, docKeyPoid, filterValue);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());

        LovItem item1 = result.getItems().get(0);
        assertEquals(1L, item1.getPoid());
        assertEquals("CODE1", item1.getCode());
        assertEquals("DESC1", item1.getDescription());

        LovItem item2 = result.getItems().get(1);
        assertEquals(2L, item2.getPoid());
        assertEquals("CODE2", item2.getCode());
        assertEquals("DESC2", item2.getDescription());

        verify(mockCallableStatement, times(1)).setLong(1, 1L);
        verify(mockCallableStatement, times(1)).setLong(2, 1L);
        verify(mockCallableStatement, times(1)).setLong(3, 1L);
        verify(mockCallableStatement, times(1)).setString(4, lovName);
        verify(mockCallableStatement, times(1)).setString(5, "");
        verify(mockCallableStatement, times(1)).setString(6, filterValue);
        verify(mockCallableStatement, times(1)).registerOutParameter(7, oracle.jdbc.OracleTypes.CURSOR);
        verify(mockCallableStatement, times(1)).execute();
    }

    @Test
    void testGetLovList_WithNullDocKeyPoid() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        CallableStatement mockCallableStatement = mock(CallableStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(jdbcTemplate.execute(any(ConnectionCallback.class))).thenAnswer(invocation -> {
            ConnectionCallback<?> callback = invocation.getArgument(0);
            return callback.doInConnection(mockConnection);
        });

        when(mockConnection.prepareCall(anyString())).thenReturn(mockCallableStatement);
        when(mockCallableStatement.getObject(7)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        LovResponse result = lovRepository.getLovList(lovName, null, filterValue);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(0, result.getItems().size());

        verify(mockCallableStatement, times(1)).setObject(5, null);
        verify(mockCallableStatement, never()).setString(eq(5), anyString());
    }

    @Test
    void testGetLovList_WithNullFilterValue() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        CallableStatement mockCallableStatement = mock(CallableStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(jdbcTemplate.execute(any(ConnectionCallback.class))).thenAnswer(invocation -> {
            ConnectionCallback<?> callback = invocation.getArgument(0);
            return callback.doInConnection(mockConnection);
        });

        when(mockConnection.prepareCall(anyString())).thenReturn(mockCallableStatement);
        when(mockCallableStatement.getObject(7)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        LovResponse result = lovRepository.getLovList(lovName, docKeyPoid, null);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(0, result.getItems().size());

        verify(mockCallableStatement, times(1)).setString(6, "");
    }

    @Test
    void testGetLovList_WithNullResultSet() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        CallableStatement mockCallableStatement = mock(CallableStatement.class);

        when(jdbcTemplate.execute(any(ConnectionCallback.class))).thenAnswer(invocation -> {
            ConnectionCallback<?> callback = invocation.getArgument(0);
            return callback.doInConnection(mockConnection);
        });

        when(mockConnection.prepareCall(anyString())).thenReturn(mockCallableStatement);
        when(mockCallableStatement.getObject(7)).thenReturn(null);

        // Act
        LovResponse result = lovRepository.getLovList(lovName, docKeyPoid, filterValue);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(0, result.getItems().size());
    }

    @Test
    void testGetLovList_WithEmptyResultSet() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        CallableStatement mockCallableStatement = mock(CallableStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(jdbcTemplate.execute(any(ConnectionCallback.class))).thenAnswer(invocation -> {
            ConnectionCallback<?> callback = invocation.getArgument(0);
            return callback.doInConnection(mockConnection);
        });

        when(mockConnection.prepareCall(anyString())).thenReturn(mockCallableStatement);
        when(mockCallableStatement.getObject(7)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        LovResponse result = lovRepository.getLovList(lovName, docKeyPoid, filterValue);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(0, result.getItems().size());
    }

    @Test
    void testGetLovList_WithSingleItem() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        CallableStatement mockCallableStatement = mock(CallableStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(jdbcTemplate.execute(any(ConnectionCallback.class))).thenAnswer(invocation -> {
            ConnectionCallback<?> callback = invocation.getArgument(0);
            return callback.doInConnection(mockConnection);
        });

        when(mockConnection.prepareCall(anyString())).thenReturn(mockCallableStatement);
        when(mockCallableStatement.getObject(7)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getLong("POID")).thenReturn(100L);
        when(mockResultSet.getString("CODE")).thenReturn("SINGLE_CODE");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("SINGLE_DESC");

        // Act
        LovResponse result = lovRepository.getLovList(lovName, docKeyPoid, filterValue);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());

        LovItem item = result.getItems().get(0);
        assertEquals(100L, item.getPoid());
        assertEquals("SINGLE_CODE", item.getCode());
        assertEquals("SINGLE_DESC", item.getDescription());
    }

    @Test
    void testGetLovList_WithSQLException() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        CallableStatement mockCallableStatement = mock(CallableStatement.class);

        when(jdbcTemplate.execute(any(ConnectionCallback.class))).thenAnswer(invocation -> {
            ConnectionCallback<?> callback = invocation.getArgument(0);
            return callback.doInConnection(mockConnection);
        });

        when(mockConnection.prepareCall(anyString())).thenReturn(mockCallableStatement);
        when(mockCallableStatement.execute()).thenThrow(new SQLException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lovRepository.getLovList(lovName, docKeyPoid, filterValue);
        });

        assertNotNull(exception);
        // Exception is thrown - verify it's a RuntimeException (either with message from inner catch or without from outer catch)
    }

    @Test
    void testGetLovList_WithGeneralException() {
        // Arrange
        when(jdbcTemplate.execute(any(ConnectionCallback.class))).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            lovRepository.getLovList(lovName, docKeyPoid, filterValue);
        });

        assertNotNull(exception);
    }

    @Test
    void testGetLovList_ResultSetWithNullValues() throws SQLException {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        CallableStatement mockCallableStatement = mock(CallableStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(jdbcTemplate.execute(any(ConnectionCallback.class))).thenAnswer(invocation -> {
            ConnectionCallback<?> callback = invocation.getArgument(0);
            return callback.doInConnection(mockConnection);
        });

        when(mockConnection.prepareCall(anyString())).thenReturn(mockCallableStatement);
        when(mockCallableStatement.getObject(7)).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getLong("POID")).thenReturn(0L);
        when(mockResultSet.getString("CODE")).thenReturn(null);
        when(mockResultSet.getString("DESCRIPTION")).thenReturn(null);

        // Act
        LovResponse result = lovRepository.getLovList(lovName, docKeyPoid, filterValue);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());

        LovItem item = result.getItems().get(0);
        assertEquals(0L, item.getPoid());
        assertNull(item.getCode());
        assertNull(item.getDescription());
    }
}

