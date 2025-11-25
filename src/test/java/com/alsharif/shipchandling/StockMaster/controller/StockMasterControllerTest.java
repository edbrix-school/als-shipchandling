package com.alsharif.shipchandling.StockMaster.controller;

import com.alsharif.shipchandling.StockMaster.Controller.StockMasterController;
import com.alsharif.shipchandling.StockMaster.dto.*;
import com.alsharif.shipchandling.StockMaster.entity.StockMasterEntity;
import com.alsharif.shipchandling.StockMaster.service.StockMasterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockMasterController.class)
class StockMasterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockMasterService stockMasterService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TEST_STOCK_POID = 1L;
    private static final Long TEST_GROUP_POID = 10L;
    private static final Long TEST_COMPANY_POID = 20L;
    private static final String TEST_USER_ID = "testUser";
    private static final Long TEST_DET_ROW_ID = 1L;

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    // ========== Get Stock Master Tests ==========

    @Test
    void testGetStockMasterById_Success() throws Exception {
        // Arrange
        StockMasterViewResponse response = createStockMasterViewResponse();
        when(stockMasterService.getStockMasterById(TEST_STOCK_POID, false, TEST_GROUP_POID))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/{stockPoid}", TEST_STOCK_POID)
                        .param("includeDetails", "false")
                        .param("groupPoid", String.valueOf(TEST_GROUP_POID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock master fetched successfully"))
                .andExpect(jsonPath("$.result.data.stockPoid").value(TEST_STOCK_POID));

        verify(stockMasterService, times(1)).getStockMasterById(TEST_STOCK_POID, false, TEST_GROUP_POID);
    }

    @Test
    void testGetStockMasterById_WithDetails() throws Exception {
        // Arrange
        StockMasterViewResponse response = createStockMasterViewResponse();
        when(stockMasterService.getStockMasterById(TEST_STOCK_POID, true, TEST_GROUP_POID))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/{stockPoid}", TEST_STOCK_POID)
                        .param("includeDetails", "true")
                        .param("groupPoid", String.valueOf(TEST_GROUP_POID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(stockMasterService, times(1)).getStockMasterById(TEST_STOCK_POID, true, TEST_GROUP_POID);
    }

    @Test
    void testGetStockMasterById_NotFound() throws Exception {
        // Arrange
        when(stockMasterService.getStockMasterById(TEST_STOCK_POID, false, TEST_GROUP_POID))
                .thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/{stockPoid}", TEST_STOCK_POID)
                        .param("groupPoid", String.valueOf(TEST_GROUP_POID)))
                .andExpect(status().isNotFound());
    }

    // ========== Get Stock Masters List Tests ==========

    @Test
    void testGetStockMasters_FlatList() throws Exception {
        // Arrange
        StockMasterEntity stock = createStockMasterEntity();
        List<StockMasterEntity> stockList = Collections.singletonList(stock);
        Page<StockMasterEntity> page = new PageImpl<>(stockList, PageRequest.of(0, 10), 1);

        Map<String, String> filters = new HashMap<>();
        filters.put("groupPoid", String.valueOf(TEST_GROUP_POID));

        when(stockMasterService.getStockMasters(anyMap(), any()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/List")
                        .param("groupPoid", String.valueOf(TEST_GROUP_POID))
                        .param("tree", "false")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.data.totalElements").value(1));

        verify(stockMasterService, times(1)).getStockMasters(anyMap(), any());
    }

    @Test
    void testGetStockMasters_Tree() throws Exception {
        // Arrange
        List<Map<String, Object>> tree = new ArrayList<>();
        Map<String, Object> categoryNode = new HashMap<>();
        categoryNode.put("categoryPoid", 1L);
        categoryNode.put("categoryName", "Test Category");
        tree.add(categoryNode);

        when(stockMasterService.getStockMastersTree(TEST_GROUP_POID))
                .thenReturn(tree);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/List")
                        .param("groupPoid", String.valueOf(TEST_GROUP_POID))
                        .param("tree", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.data.categories").isArray());

        verify(stockMasterService, times(1)).getStockMastersTree(TEST_GROUP_POID);
    }

    // ========== Validation Tests ==========

    @Test
    void testValidateStockCode_Success() throws Exception {
        // Arrange
        ValidationResponse response = new ValidationResponse(true, "Stock code is available");
        when(stockMasterService.validateStockCode("STOCK001", TEST_GROUP_POID, null))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/validate-code")
                        .header("groupPoid", TEST_GROUP_POID)
                        .param("stockCode", "STOCK001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.data.isUnique").value(true));

        verify(stockMasterService, times(1)).validateStockCode("STOCK001", TEST_GROUP_POID, null);
    }

    @Test
    void testValidateStockCode_WithExclude() throws Exception {
        // Arrange
        ValidationResponse response = new ValidationResponse(true, "Stock code is available");
        when(stockMasterService.validateStockCode("STOCK001", TEST_GROUP_POID, TEST_STOCK_POID))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/validate-code")
                        .header("groupPoid", TEST_GROUP_POID)
                        .param("stockCode", "STOCK001")
                        .param("excludeStockPoid", String.valueOf(TEST_STOCK_POID)))
                .andExpect(status().isOk());

        verify(stockMasterService, times(1)).validateStockCode("STOCK001", TEST_GROUP_POID, TEST_STOCK_POID);
    }

    @Test
    void testValidateStockName_Success() throws Exception {
        // Arrange
        ValidationResponse response = new ValidationResponse(true, "Stock name is available");
        when(stockMasterService.validateStockName("Test Stock", TEST_GROUP_POID, null))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/validate-name")
                        .header("groupPoid", TEST_GROUP_POID)
                        .param("stockName", "Test Stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.isUnique").value(true));

        verify(stockMasterService, times(1)).validateStockName("Test Stock", TEST_GROUP_POID, null);
    }

    // ========== Dependencies Check Tests ==========

    @Test
    void testCheckStockMasterDependencies_Success() throws Exception {
        // Arrange
        StockMasterDependenciesDto dto = new StockMasterDependenciesDto();
        dto.setStockPoid(TEST_STOCK_POID);
        dto.setCanDelete(true);
        dto.setStockBalanceCount(0L);
        dto.setTransactionCount(0L);

        when(stockMasterService.checkStockMasterDependencies(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/{stockPoid}/dependencies", TEST_STOCK_POID)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.data.canDelete").value(true));

        verify(stockMasterService, times(1)).checkStockMasterDependencies(TEST_STOCK_POID, TEST_GROUP_POID);
    }

    // ========== Create Stock Master Tests ==========

    @Test
    void testCreateStockMaster_Success() throws Exception {
        // Arrange
        CreateStockMasterRequest request = createCreateStockMasterRequest();
        StockMasterDto responseDto = createStockMasterDto();

        when(stockMasterService.createStockMaster(
                any(CreateStockMasterRequest.class), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/stockmaster/Create")
                        .header("groupPoid", TEST_GROUP_POID)
                        .header("companyPoid", TEST_COMPANY_POID)
                        .header("userId", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock item created successfully"));

        verify(stockMasterService, times(1)).createStockMaster(
                any(CreateStockMasterRequest.class), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    // ========== Update Stock Master Tests ==========

    @Test
    void testUpdateStockMaster_Success() throws Exception {
        // Arrange
        UpdateStockMasterRequest request = createUpdateStockMasterRequest();
        StockMasterDto responseDto = createStockMasterDto();

        when(stockMasterService.updateStockMaster(
                eq(TEST_STOCK_POID), any(UpdateStockMasterRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/stockmaster/{stockPoid}", TEST_STOCK_POID)
                        .header("groupPoid", TEST_GROUP_POID)
                        .header("companyPoid", TEST_COMPANY_POID)
                        .header("userId", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(stockMasterService, times(1)).updateStockMaster(
                eq(TEST_STOCK_POID), any(UpdateStockMasterRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    // ========== Delete Stock Master Tests ==========

    @Test
    void testDeleteStockMaster_Success() throws Exception {
        // Arrange
        doNothing().when(stockMasterService).deleteStockMaster(TEST_STOCK_POID, TEST_GROUP_POID);

        // Act & Assert
        mockMvc.perform(delete("/stockmaster/{stockPoid}", TEST_STOCK_POID)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock item deleted successfully"));

        verify(stockMasterService, times(1)).deleteStockMaster(TEST_STOCK_POID, TEST_GROUP_POID);
    }

    // ========== Supplier Detail Tests ==========

    @Test
    void testAddSupplierDetail_Success() throws Exception {
        // Arrange
        CreateStockMasterDtlRequest request = new CreateStockMasterDtlRequest();
        request.setSupplierPoid(50L);
        request.setSupplierStockCode("SUP001");
        request.setRemarks("Test supplier");

        StockMasterDtlDto responseDto = new StockMasterDtlDto();
        responseDto.setStockPoid(TEST_STOCK_POID);
        responseDto.setDetRowId(TEST_DET_ROW_ID);
        responseDto.setSupplierPoid(50L);

        when(stockMasterService.addSupplierDetail(
                eq(TEST_STOCK_POID), any(CreateStockMasterDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/stockmaster/{stockPoid}/supplier-details", TEST_STOCK_POID)
                        .header("groupPoid", TEST_GROUP_POID)
                        .header("userId", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Supplier detail added successfully"));

        verify(stockMasterService, times(1)).addSupplierDetail(
                eq(TEST_STOCK_POID), any(CreateStockMasterDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID));
    }

    @Test
    void testUpdateSupplierDetail_Success() throws Exception {
        // Arrange
        CreateStockMasterDtlRequest request = new CreateStockMasterDtlRequest();
        request.setSupplierPoid(60L);
        request.setRemarks("Updated remarks");

        StockMasterDtlDto responseDto = new StockMasterDtlDto();
        responseDto.setStockPoid(TEST_STOCK_POID);
        responseDto.setDetRowId(TEST_DET_ROW_ID);

        when(stockMasterService.updateSupplierDetail(
                eq(TEST_STOCK_POID), eq(TEST_DET_ROW_ID), any(CreateStockMasterDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/stockmaster/{stockPoid}/supplier-details/{detRowId}",
                        TEST_STOCK_POID, TEST_DET_ROW_ID)
                        .header("groupPoid", TEST_GROUP_POID)
                        .header("userId", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Supplier detail updated successfully"));

        verify(stockMasterService, times(1)).updateSupplierDetail(
                eq(TEST_STOCK_POID), eq(TEST_DET_ROW_ID), any(CreateStockMasterDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID));
    }

    @Test
    void testDeleteSupplierDetail_Success() throws Exception {
        // Arrange
        doNothing().when(stockMasterService).deleteSupplierDetail(
                TEST_STOCK_POID, TEST_DET_ROW_ID, TEST_GROUP_POID);

        // Act & Assert
        mockMvc.perform(delete("/stockmaster/{stockPoid}/supplier-details/{detRowId}",
                        TEST_STOCK_POID, TEST_DET_ROW_ID)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Supplier detail deleted successfully"));

        verify(stockMasterService, times(1)).deleteSupplierDetail(
                TEST_STOCK_POID, TEST_DET_ROW_ID, TEST_GROUP_POID);
    }

    @Test
    void testGetSupplierDetails_Success() throws Exception {
        // Arrange
        StockMasterDtlDto dtlDto = new StockMasterDtlDto();
        dtlDto.setStockPoid(TEST_STOCK_POID);
        dtlDto.setDetRowId(TEST_DET_ROW_ID);
        List<StockMasterDtlDto> supplierDetails = Collections.singletonList(dtlDto);

        when(stockMasterService.getSupplierDetails(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(supplierDetails);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/{stockPoid}/supplier-details", TEST_STOCK_POID)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.data").isArray());

        verify(stockMasterService, times(1)).getSupplierDetails(TEST_STOCK_POID, TEST_GROUP_POID);
    }

    // ========== Warehouse Detail Tests ==========

    @Test
    void testAddWarehouseDetail_Success() throws Exception {
        // Arrange
        CreateStockMasterWarehouseDtlRequest request = new CreateStockMasterWarehouseDtlRequest();
        request.setLocationPoid(100L);
        request.setAisleNo("A1");
        request.setBayNo("B1");

        StockMasterWarehouseDtlDto responseDto = new StockMasterWarehouseDtlDto();
        responseDto.setStockPoid(TEST_STOCK_POID);
        responseDto.setDetRowId(TEST_DET_ROW_ID);

        when(stockMasterService.addWarehouseDetail(
                eq(TEST_STOCK_POID), any(CreateStockMasterWarehouseDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/stockmaster/{stockPoid}/warehouse-details", TEST_STOCK_POID)
                        .header("groupPoid", TEST_GROUP_POID)
                        .header("userId", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Warehouse detail added successfully"));

        verify(stockMasterService, times(1)).addWarehouseDetail(
                eq(TEST_STOCK_POID), any(CreateStockMasterWarehouseDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID));
    }

    @Test
    void testUpdateWarehouseDetail_Success() throws Exception {
        // Arrange
        CreateStockMasterWarehouseDtlRequest request = new CreateStockMasterWarehouseDtlRequest();
        request.setLocationPoid(100L);
        request.setAisleNo("A2");

        StockMasterWarehouseDtlDto responseDto = new StockMasterWarehouseDtlDto();
        responseDto.setStockPoid(TEST_STOCK_POID);
        responseDto.setDetRowId(TEST_DET_ROW_ID);

        when(stockMasterService.updateWarehouseDetail(
                eq(TEST_STOCK_POID), eq(TEST_DET_ROW_ID), any(CreateStockMasterWarehouseDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/stockmaster/{stockPoid}/warehouse-details/{detRowId}",
                        TEST_STOCK_POID, TEST_DET_ROW_ID)
                        .header("groupPoid", TEST_GROUP_POID)
                        .header("userId", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Warehouse detail updated successfully"));

        verify(stockMasterService, times(1)).updateWarehouseDetail(
                eq(TEST_STOCK_POID), eq(TEST_DET_ROW_ID), any(CreateStockMasterWarehouseDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID));
    }

    @Test
    void testDeleteWarehouseDetail_Success() throws Exception {
        // Arrange
        doNothing().when(stockMasterService).deleteWarehouseDetail(
                TEST_STOCK_POID, TEST_DET_ROW_ID, TEST_GROUP_POID);

        // Act & Assert
        mockMvc.perform(delete("/stockmaster/{stockPoid}/warehouse-details/{detRowId}",
                        TEST_STOCK_POID, TEST_DET_ROW_ID)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Warehouse detail deleted successfully"));

        verify(stockMasterService, times(1)).deleteWarehouseDetail(
                TEST_STOCK_POID, TEST_DET_ROW_ID, TEST_GROUP_POID);
    }

    @Test
    void testGetWarehouseDetails_Success() throws Exception {
        // Arrange
        StockMasterWarehouseDtlDto whDto = new StockMasterWarehouseDtlDto();
        whDto.setStockPoid(TEST_STOCK_POID);
        whDto.setDetRowId(TEST_DET_ROW_ID);
        List<StockMasterWarehouseDtlDto> warehouseDetails = Collections.singletonList(whDto);

        when(stockMasterService.getWarehouseDetails(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(warehouseDetails);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/{stockPoid}/warehouse-details", TEST_STOCK_POID)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.data").isArray());

        verify(stockMasterService, times(1)).getWarehouseDetails(TEST_STOCK_POID, TEST_GROUP_POID);
    }

    // ========== Get Stock by Barcode Tests ==========

    @Test
    void testGetStockByBarcode_Success() throws Exception {
        // Arrange
        StockMasterDto stockDto = createStockMasterDto();
        when(stockMasterService.getStockMasterByBarcode("123456789", TEST_GROUP_POID))
                .thenReturn(stockDto);

        // Act & Assert
        mockMvc.perform(get("/stockmaster/by-barcode/{barcode}", "123456789")
                        .param("groupPoid", String.valueOf(TEST_GROUP_POID)))
                .andExpect(status().isOk());

        verify(stockMasterService, times(1)).getStockMasterByBarcode("123456789", TEST_GROUP_POID);
    }

    // ========== Helper Methods ==========

    private StockMasterViewResponse createStockMasterViewResponse() {
        StockMasterViewResponse response = new StockMasterViewResponse();
        response.setStockPoid(TEST_STOCK_POID);
        response.setStockCode("STOCK001");
        response.setStockName("Test Stock");
        response.setGroupPoid(TEST_GROUP_POID);
        response.setActive("Y");
        response.setDeleted("N");
        return response;
    }

    private StockMasterEntity createStockMasterEntity() {
        StockMasterEntity entity = new StockMasterEntity();
        entity.setStockPoid(TEST_STOCK_POID);
        entity.setStockCode("STOCK001");
        entity.setStockName("Test Stock");
        entity.setGroupPoid(TEST_GROUP_POID);
        entity.setActive("Y");
        entity.setDeleted("N");
        return entity;
    }

    private StockMasterDto createStockMasterDto() {
        StockMasterDto dto = new StockMasterDto();
        dto.setStockPoid(TEST_STOCK_POID);
        dto.setStockCode("STOCK001");
        dto.setStockName("Test Stock");
        dto.setGroupPoid(TEST_GROUP_POID);
        return dto;
    }

    private CreateStockMasterRequest createCreateStockMasterRequest() {
        CreateStockMasterRequest request = new CreateStockMasterRequest();
        request.setStockName("Test Stock");
        request.setCategoryPoid(1L);
        request.setStockUnitPoid(1L);
        request.setTaxPoid(1L);
        request.setInputTaxPoid(1L);
        request.setActive("Y");
        return request;
    }

    private UpdateStockMasterRequest createUpdateStockMasterRequest() {
        UpdateStockMasterRequest request = new UpdateStockMasterRequest();
        request.setStockName("Updated Stock");
        request.setCategoryPoid(1L);
        request.setStockUnitPoid(1L);
        request.setTaxPoid(1L);
        request.setInputTaxPoid(1L);
        return request;
    }
}

