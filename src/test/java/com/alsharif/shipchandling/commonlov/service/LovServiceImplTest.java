package com.alsharif.shipchandling.commonlov.service;

import com.alsharif.shipchandling.commonlov.dto.LovItem;
import com.alsharif.shipchandling.commonlov.dto.LovResponse;
import com.alsharif.shipchandling.commonlov.repository.LovRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LovServiceImplTest {

    @Mock
    private LovRepository lovRepository;

    @InjectMocks
    private LovServiceImpl lovService;

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
    void testGetLovList_Success() {
        // Arrange
        List<LovItem> items = new ArrayList<>();
        items.add(new LovItem(1L, "CODE1", "DESC1"));
        items.add(new LovItem(2L, "CODE2", "DESC2"));
        LovResponse expectedResponse = new LovResponse(items);

        when(lovRepository.getLovList(anyString(), anyLong(), anyString()))
                .thenReturn(expectedResponse);

        // Act
        LovResponse result = lovService.getLovList(lovName, docKeyPoid, filterValue);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());
        assertEquals(expectedResponse, result);
        verify(lovRepository, times(1)).getLovList(lovName, docKeyPoid, filterValue);
    }

    @Test
    void testGetLovList_WithNullDocKeyPoid() {
        // Arrange
        List<LovItem> items = new ArrayList<>();
        LovResponse expectedResponse = new LovResponse(items);

        when(lovRepository.getLovList(anyString(), isNull(), anyString()))
                .thenReturn(expectedResponse);

        // Act
        LovResponse result = lovService.getLovList(lovName, null, filterValue);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(0, result.getItems().size());
        verify(lovRepository, times(1)).getLovList(lovName, null, filterValue);
    }

    @Test
    void testGetLovList_WithNullFilterValue() {
        // Arrange
        List<LovItem> items = new ArrayList<>();
        items.add(new LovItem(1L, "CODE1", "DESC1"));
        LovResponse expectedResponse = new LovResponse(items);

        when(lovRepository.getLovList(anyString(), anyLong(), isNull()))
                .thenReturn(expectedResponse);

        // Act
        LovResponse result = lovService.getLovList(lovName, docKeyPoid, null);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        verify(lovRepository, times(1)).getLovList(lovName, docKeyPoid, null);
    }

    @Test
    void testGetLovList_WithAllNullParameters() {
        // Arrange
        LovResponse expectedResponse = new LovResponse(null);

        when(lovRepository.getLovList(anyString(), isNull(), isNull()))
                .thenReturn(expectedResponse);

        // Act
        LovResponse result = lovService.getLovList(lovName, null, null);

        // Assert
        assertNotNull(result);
        assertNull(result.getItems());
        verify(lovRepository, times(1)).getLovList(lovName, null, null);
    }

    @Test
    void testGetLovList_WithEmptyList() {
        // Arrange
        List<LovItem> emptyList = new ArrayList<>();
        LovResponse expectedResponse = new LovResponse(emptyList);

        when(lovRepository.getLovList(anyString(), anyLong(), anyString()))
                .thenReturn(expectedResponse);

        // Act
        LovResponse result = lovService.getLovList(lovName, docKeyPoid, filterValue);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getItems());
        assertEquals(0, result.getItems().size());
        verify(lovRepository, times(1)).getLovList(lovName, docKeyPoid, filterValue);
    }

    @Test
    void testGetLovList_WithEmptyLovName() {
        // Arrange
        List<LovItem> items = new ArrayList<>();
        LovResponse expectedResponse = new LovResponse(items);

        when(lovRepository.getLovList(anyString(), anyLong(), anyString()))
                .thenReturn(expectedResponse);

        // Act
        LovResponse result = lovService.getLovList("", docKeyPoid, filterValue);

        // Assert
        assertNotNull(result);
        verify(lovRepository, times(1)).getLovList("", docKeyPoid, filterValue);
    }
}

