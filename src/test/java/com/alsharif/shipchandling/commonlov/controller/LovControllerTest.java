package com.alsharif.shipchandling.commonlov.controller;

import com.alsharif.shipchandling.commonlov.dto.LovItem;
import com.alsharif.shipchandling.commonlov.dto.LovResponse;
import com.alsharif.shipchandling.commonlov.service.LovServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LovController.class)
class LovControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
    void testGetLovList_Success() throws Exception {
        // Arrange
        List<LovItem> items = new ArrayList<>();
        items.add(new LovItem(1L, "CODE1", "DESC1"));
        items.add(new LovItem(2L, "CODE2", "DESC2"));
        LovResponse response = new LovResponse(items);

        when(lovService.getLovList(anyString(), any(), any()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/lov")
                        .param("lovName", lovName)
                        .param("docKeyPoid", String.valueOf(docKeyPoid))
                        .param("filterValue", filterValue)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task fetched successfully"))
                .andExpect(jsonPath("$.result.data.items[0].poid").value(1))
                .andExpect(jsonPath("$.result.data.items[0].code").value("CODE1"))
                .andExpect(jsonPath("$.result.data.items[0].description").value("DESC1"))
                .andExpect(jsonPath("$.result.data.items[1].poid").value(2))
                .andExpect(jsonPath("$.result.data.items[1].code").value("CODE2"))
                .andExpect(jsonPath("$.result.data.items[1].description").value("DESC2"));

        verify(lovService, times(1)).getLovList(lovName, docKeyPoid, filterValue);
    }

    @Test
    void testGetLovList_WithOnlyLovName() throws Exception {
        // Arrange
        List<LovItem> items = new ArrayList<>();
        LovResponse response = new LovResponse(items);

        when(lovService.getLovList(anyString(), isNull(), isNull()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/lov")
                        .param("lovName", lovName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task fetched successfully"));

        verify(lovService, times(1)).getLovList(lovName, null, null);
    }

    @Test
    void testGetLovList_WithNullDocKeyPoid() throws Exception {
        // Arrange
        List<LovItem> items = new ArrayList<>();
        items.add(new LovItem(1L, "CODE1", "DESC1"));
        LovResponse response = new LovResponse(items);

        when(lovService.getLovList(anyString(), isNull(), anyString()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/lov")
                        .param("lovName", lovName)
                        .param("filterValue", filterValue)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true));

        verify(lovService, times(1)).getLovList(lovName, null, filterValue);
    }

    @Test
    void testGetLovList_WithNullFilterValue() throws Exception {
        // Arrange
        List<LovItem> items = new ArrayList<>();
        LovResponse response = new LovResponse(items);

        when(lovService.getLovList(anyString(), anyLong(), isNull()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/lov")
                        .param("lovName", lovName)
                        .param("docKeyPoid", String.valueOf(docKeyPoid))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true));

        verify(lovService, times(1)).getLovList(lovName, docKeyPoid, null);
    }

    @Test
    void testGetLovList_WithEmptyList() throws Exception {
        // Arrange
        List<LovItem> emptyList = new ArrayList<>();
        LovResponse response = new LovResponse(emptyList);

        when(lovService.getLovList(anyString(), any(), any()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/lov")
                        .param("lovName", lovName)
                        .param("docKeyPoid", String.valueOf(docKeyPoid))
                        .param("filterValue", filterValue)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.data.items").isEmpty());

        verify(lovService, times(1)).getLovList(lovName, docKeyPoid, filterValue);
    }

    @Test
    void testGetLovList_WithSingleItem() throws Exception {
        // Arrange
        List<LovItem> items = new ArrayList<>();
        items.add(new LovItem(100L, "SINGLE_CODE", "SINGLE_DESC"));
        LovResponse response = new LovResponse(items);

        when(lovService.getLovList(anyString(), any(), any()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/lov")
                        .param("lovName", lovName)
                        .param("docKeyPoid", String.valueOf(docKeyPoid))
                        .param("filterValue", filterValue)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.data.items[0].poid").value(100))
                .andExpect(jsonPath("$.result.data.items[0].code").value("SINGLE_CODE"))
                .andExpect(jsonPath("$.result.data.items[0].description").value("SINGLE_DESC"));

        verify(lovService, times(1)).getLovList(lovName, docKeyPoid, filterValue);
    }

    @Test
    void testGetLovList_WithDifferentLovNames() throws Exception {
        // Arrange
        List<LovItem> items = new ArrayList<>();
        LovResponse response = new LovResponse(items);

        String[] lovNames = {"RFQ_STATUS", "DIVISION", "SALES_QTN_REF", "RFQ_CONFIRMED_SUPPLIER",
                "STOCK_MASTER", "STOCK_UNIT", "SUPPLIER_MASTER", "INPUT_TAX_MASTER"};

        when(lovService.getLovList(anyString(), any(), any()))
                .thenReturn(response);

        // Act & Assert
        for (String name : lovNames) {
            mockMvc.perform(get("/lov")
                            .param("lovName", name)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.success").value(true));

            verify(lovService, atLeastOnce()).getLovList(name, null, null);
        }
    }

    @Test
    void testGetLovList_WithNullResponse() throws Exception {
        // Arrange
        LovResponse response = new LovResponse(null);

        when(lovService.getLovList(anyString(), any(), any()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/lov")
                        .param("lovName", lovName)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.success").value(true));

        verify(lovService, times(1)).getLovList(lovName, null, null);
    }
}

