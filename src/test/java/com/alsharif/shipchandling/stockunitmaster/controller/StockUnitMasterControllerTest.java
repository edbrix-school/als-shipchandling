package com.alsharif.shipchandling.stockunitmaster.controller;

import com.alsharif.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import com.alsharif.shipchandling.stockunitmaster.dto.StockUnitMasterDto;
import com.alsharif.shipchandling.stockunitmaster.dto.UnitDependenciesDto;
import com.alsharif.shipchandling.stockunitmaster.service.StockUnitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockUnitMasterController.class)
class StockUnitMasterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockUnitService stockUnitService;

    @Autowired
    private ObjectMapper objectMapper;

    private StockUnitMasterDto stockUnitMasterDto;
    private static final Long TEST_STOCK_UNIT_POID = 1L;
    private static final Long TEST_GROUP_POID = 100L;
    private static final String TEST_STOCK_UNIT_CODE = "KG";
    private static final String TEST_STOCK_UNIT_NAME = "Kilogram";
    private static final String TEST_DOCUMENT_ID = "800-320";
    private static final String TEST_ACTION_REQUESTED = "VIEW";

    @BeforeEach
    void setUp() {
        stockUnitMasterDto = new StockUnitMasterDto();
        stockUnitMasterDto.setStockUnitPoid(TEST_STOCK_UNIT_POID);
        stockUnitMasterDto.setStockUnitCode(TEST_STOCK_UNIT_CODE);
        stockUnitMasterDto.setStockUnitName(TEST_STOCK_UNIT_NAME);
        stockUnitMasterDto.setStockUnitName2("Kilogram");
        stockUnitMasterDto.setGroupPoid(TEST_GROUP_POID);
        stockUnitMasterDto.setActive("Y");
        stockUnitMasterDto.setSeqNo(1);
        stockUnitMasterDto.setClassified("Y");
        stockUnitMasterDto.setCreatedBy("testUser");
        stockUnitMasterDto.setCreatedDate(OffsetDateTime.now());
    }

    // ========== getStockUnitByPoid Tests ==========

    @Test
    void testGetStockUnitByPoid_Success() throws Exception {
        // Arrange
        when(stockUnitService.getStockUnitByPoid(TEST_STOCK_UNIT_POID)).thenReturn(stockUnitMasterDto);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/{stockUnitPoid}", TEST_STOCK_UNIT_POID)
                        .param("documentId", TEST_DOCUMENT_ID)
                        .param("actionRequested", TEST_ACTION_REQUESTED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task fetched successfully"))
                .andExpect(jsonPath("$.result.data.stockUnitPoid").value(TEST_STOCK_UNIT_POID))
                .andExpect(jsonPath("$.result.data.stockUnitCode").value(TEST_STOCK_UNIT_CODE))
                .andExpect(jsonPath("$.result.data.stockUnitName").value(TEST_STOCK_UNIT_NAME));

        verify(stockUnitService, times(1)).getStockUnitByPoid(TEST_STOCK_UNIT_POID);
    }

    @Test
    void testGetStockUnitByPoid_NotFound() throws Exception {
        // Arrange
        when(stockUnitService.getStockUnitByPoid(TEST_STOCK_UNIT_POID))
                .thenThrow(new ResourceNotFoundException("StockUnit", "stockUnitPoid", TEST_STOCK_UNIT_POID));

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/{stockUnitPoid}", TEST_STOCK_UNIT_POID)
                        .param("documentId", TEST_DOCUMENT_ID)
                        .param("actionRequested", TEST_ACTION_REQUESTED))
                .andExpect(status().isNotFound());

        verify(stockUnitService, times(1)).getStockUnitByPoid(TEST_STOCK_UNIT_POID);
    }

    // ========== createStockUnit Tests ==========

    @Test
    void testCreateStockUnit_Success() throws Exception {
        // Arrange
        stockUnitMasterDto.setStockUnitPoid(null); // New entity
        when(stockUnitService.createStockUnit(any(StockUnitMasterDto.class))).thenReturn(stockUnitMasterDto);

        // Act & Assert
        mockMvc.perform(post("/stockunitmaster/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockUnitMasterDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock unit created successfully"))
                .andExpect(jsonPath("$.result.data.stockUnitCode").value(TEST_STOCK_UNIT_CODE));

        verify(stockUnitService, times(1)).createStockUnit(any(StockUnitMasterDto.class));
    }

    @Test
    void testCreateStockUnit_DuplicateCode() throws Exception {
        // Arrange
        when(stockUnitService.createStockUnit(any(StockUnitMasterDto.class)))
                .thenThrow(new ResourceAlreadyExistsException("stockUnitCode", TEST_STOCK_UNIT_CODE));

        // Act & Assert
        mockMvc.perform(post("/stockunitmaster/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockUnitMasterDto)))
                .andExpect(status().isConflict());

        verify(stockUnitService, times(1)).createStockUnit(any(StockUnitMasterDto.class));
    }


    // ========== updateStockUnit Tests ==========

    @Test
    void testUpdateStockUnit_Success() throws Exception {
        // Arrange
        stockUnitMasterDto.setStockUnitName("Updated Name");
        when(stockUnitService.updateStockUnit(eq(TEST_STOCK_UNIT_POID), any(StockUnitMasterDto.class)))
                .thenReturn(stockUnitMasterDto);

        // Act & Assert
        mockMvc.perform(put("/stockunitmaster/{stockUnitPoid}", TEST_STOCK_UNIT_POID)
                        .param("documentId", TEST_DOCUMENT_ID)
                        .param("actionRequested", TEST_ACTION_REQUESTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockUnitMasterDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock unit updated successfully"))
                .andExpect(jsonPath("$.result.data.stockUnitPoid").value(TEST_STOCK_UNIT_POID));

        verify(stockUnitService, times(1)).updateStockUnit(eq(TEST_STOCK_UNIT_POID), any(StockUnitMasterDto.class));
    }

    @Test
    void testUpdateStockUnit_NotFound() throws Exception {
        // Arrange
        when(stockUnitService.updateStockUnit(eq(TEST_STOCK_UNIT_POID), any(StockUnitMasterDto.class)))
                .thenThrow(new ResourceNotFoundException("StockUnit", "stockUnitPoid", TEST_STOCK_UNIT_POID));

        // Act & Assert
        mockMvc.perform(put("/stockunitmaster/{stockUnitPoid}", TEST_STOCK_UNIT_POID)
                        .param("documentId", TEST_DOCUMENT_ID)
                        .param("actionRequested", TEST_ACTION_REQUESTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockUnitMasterDto)))
                .andExpect(status().isNotFound());

        verify(stockUnitService, times(1)).updateStockUnit(eq(TEST_STOCK_UNIT_POID), any(StockUnitMasterDto.class));
    }

    @Test
    void testUpdateStockUnit_DuplicateCode() throws Exception {
        // Arrange
        when(stockUnitService.updateStockUnit(eq(TEST_STOCK_UNIT_POID), any(StockUnitMasterDto.class)))
                .thenThrow(new ResourceAlreadyExistsException("Stock Unit Code already exists, please enter unique code.", TEST_STOCK_UNIT_CODE));

        // Act & Assert
        mockMvc.perform(put("/stockunitmaster/{stockUnitPoid}", TEST_STOCK_UNIT_POID)
                        .param("documentId", TEST_DOCUMENT_ID)
                        .param("actionRequested", TEST_ACTION_REQUESTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockUnitMasterDto)))
                .andExpect(status().isConflict());

        verify(stockUnitService, times(1)).updateStockUnit(eq(TEST_STOCK_UNIT_POID), any(StockUnitMasterDto.class));
    }

    // ========== getStockUnitList Tests ==========

    @Test
    void testGetStockUnitList_Success() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<StockUnitMasterDto> units = new ArrayList<>();
        units.add(stockUnitMasterDto);
        Page<StockUnitMasterDto> page = new PageImpl<>(units, pageable, 1);

        when(stockUnitService.listStockUnitsUsingParams(
                eq(TEST_STOCK_UNIT_CODE), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/list")
                        .param("stockUnitCode", TEST_STOCK_UNIT_CODE)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock units fetched successfully"));

        verify(stockUnitService, times(1)).listStockUnitsUsingParams(
                eq(TEST_STOCK_UNIT_CODE), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void testGetStockUnitList_WithAllParams() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockUnitMasterDto> page = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(stockUnitService.listStockUnitsUsingParams(
                anyString(), anyString(), anyString(), anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/list")
                        .param("stockUnitCode", TEST_STOCK_UNIT_CODE)
                        .param("stockUnitName", TEST_STOCK_UNIT_NAME)
                        .param("classified", "Y")
                        .param("active", "Y")
                        .param("deleted", "N")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(stockUnitService, times(1)).listStockUnitsUsingParams(
                eq(TEST_STOCK_UNIT_CODE), eq(TEST_STOCK_UNIT_NAME), eq("Y"), eq("Y"), eq("N"), any(Pageable.class));
    }

    @Test
    void testGetStockUnitList_NoParams() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockUnitMasterDto> page = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(stockUnitService.listStockUnitsUsingParams(
                isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/list")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(stockUnitService, times(1)).listStockUnitsUsingParams(
                isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    // ========== softDeleteStockUnit Tests ==========

    @Test
    void testSoftDeleteStockUnit_Success() throws Exception {
        // Arrange
        doNothing().when(stockUnitService).softDeleteStockUnit(TEST_STOCK_UNIT_POID);

        // Act & Assert
        mockMvc.perform(delete("/stockunitmaster/{stockUnitPoid}", TEST_STOCK_UNIT_POID)
                        .param("documentId", TEST_DOCUMENT_ID)
                        .param("actionRequested", TEST_ACTION_REQUESTED))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock unit has been soft deleted successfully"));

        verify(stockUnitService, times(1)).softDeleteStockUnit(TEST_STOCK_UNIT_POID);
    }

    @Test
    void testSoftDeleteStockUnit_NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("StockUnit", "stockUnitPoid", TEST_STOCK_UNIT_POID))
                .when(stockUnitService).softDeleteStockUnit(TEST_STOCK_UNIT_POID);

        // Act & Assert
        mockMvc.perform(delete("/stockunitmaster/{stockUnitPoid}", TEST_STOCK_UNIT_POID)
                        .param("documentId", TEST_DOCUMENT_ID)
                        .param("actionRequested", TEST_ACTION_REQUESTED))
                .andExpect(status().isNotFound());

        verify(stockUnitService, times(1)).softDeleteStockUnit(TEST_STOCK_UNIT_POID);
    }

    // ========== validateStockUnitCode Tests ==========

    @Test
    void testValidateStockUnitCode_Unique() throws Exception {
        // Arrange
        when(stockUnitService.validateStockUnitCode(TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, null))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/validate-code")
                        .param("stockUnitCode", TEST_STOCK_UNIT_CODE)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock unit code is available"))
                .andExpect(jsonPath("$.result.data.unique").value(true))
                .andExpect(jsonPath("$.result.data.message").value("Stock unit code is available"));

        verify(stockUnitService, times(1)).validateStockUnitCode(TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, null);
    }

    @Test
    void testValidateStockUnitCode_Duplicate() throws Exception {
        // Arrange
        when(stockUnitService.validateStockUnitCode(TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, null))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/validate-code")
                        .param("stockUnitCode", TEST_STOCK_UNIT_CODE)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock unit code already exists"))
                .andExpect(jsonPath("$.result.data.unique").value(false))
                .andExpect(jsonPath("$.result.data.message").value("Stock unit code already exists"));

        verify(stockUnitService, times(1)).validateStockUnitCode(TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, null);
    }

    @Test
    void testValidateStockUnitCode_WithExcludePoid() throws Exception {
        // Arrange
        when(stockUnitService.validateStockUnitCode(TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, TEST_STOCK_UNIT_POID))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/validate-code")
                        .param("stockUnitCode", TEST_STOCK_UNIT_CODE)
                        .param("excludeStockUnitPoid", String.valueOf(TEST_STOCK_UNIT_POID))
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(stockUnitService, times(1)).validateStockUnitCode(
                TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, TEST_STOCK_UNIT_POID);
    }

    // ========== validateStockUnitName Tests ==========

    @Test
    void testValidateStockUnitName_Unique() throws Exception {
        // Arrange
        when(stockUnitService.validateStockUnitName(TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, null))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/validate-name")
                        .param("stockUnitName", TEST_STOCK_UNIT_NAME)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock unit name is available"))
                .andExpect(jsonPath("$.result.data.unique").value(true))
                .andExpect(jsonPath("$.result.data.message").value("Stock unit name is available"));

        verify(stockUnitService, times(1)).validateStockUnitName(TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, null);
    }

    @Test
    void testValidateStockUnitName_Duplicate() throws Exception {
        // Arrange
        when(stockUnitService.validateStockUnitName(TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, null))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/validate-name")
                        .param("stockUnitName", TEST_STOCK_UNIT_NAME)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock unit name already exists"))
                .andExpect(jsonPath("$.result.data.unique").value(false))
                .andExpect(jsonPath("$.result.data.message").value("Stock unit name already exists"));

        verify(stockUnitService, times(1)).validateStockUnitName(TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, null);
    }

    @Test
    void testValidateStockUnitName_WithExcludePoid() throws Exception {
        // Arrange
        when(stockUnitService.validateStockUnitName(TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, TEST_STOCK_UNIT_POID))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/validate-name")
                        .param("stockUnitName", TEST_STOCK_UNIT_NAME)
                        .param("excludeStockUnitPoid", String.valueOf(TEST_STOCK_UNIT_POID))
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(stockUnitService, times(1)).validateStockUnitName(
                TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, TEST_STOCK_UNIT_POID);
    }

    // ========== checkUnitDependencies Tests ==========

    @Test
    void testCheckUnitDependencies_CanDelete() throws Exception {
        // Arrange
        UnitDependenciesDto dependencies = new UnitDependenciesDto();
        dependencies.setStockUnitPoid(TEST_STOCK_UNIT_POID);
        dependencies.setCanDelete(true);
        dependencies.setStockItemCount(0L);
        dependencies.setReason("No dependencies");
        dependencies.setMessage("Unit can be deleted. No dependencies found.");

        when(stockUnitService.checkUnitDependencies(TEST_STOCK_UNIT_POID, TEST_GROUP_POID))
                .thenReturn(dependencies);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/{stockUnitPoid}/dependencies", TEST_STOCK_UNIT_POID)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Dependency check completed"))
                .andExpect(jsonPath("$.result.data.canDelete").value(true))
                .andExpect(jsonPath("$.result.data.stockItemCount").value(0));

        verify(stockUnitService, times(1)).checkUnitDependencies(TEST_STOCK_UNIT_POID, TEST_GROUP_POID);
    }

    @Test
    void testCheckUnitDependencies_CannotDelete() throws Exception {
        // Arrange
        UnitDependenciesDto dependencies = new UnitDependenciesDto();
        dependencies.setStockUnitPoid(TEST_STOCK_UNIT_POID);
        dependencies.setCanDelete(false);
        dependencies.setStockItemCount(5L);
        dependencies.setReason("Unit has dependencies");
        dependencies.setMessage("Cannot delete unit. It is used by 5 stock items.");

        when(stockUnitService.checkUnitDependencies(TEST_STOCK_UNIT_POID, TEST_GROUP_POID))
                .thenReturn(dependencies);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/{stockUnitPoid}/dependencies", TEST_STOCK_UNIT_POID)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.data.canDelete").value(false))
                .andExpect(jsonPath("$.result.data.stockItemCount").value(5));

        verify(stockUnitService, times(1)).checkUnitDependencies(TEST_STOCK_UNIT_POID, TEST_GROUP_POID);
    }

    @Test
    void testCheckUnitDependencies_NotFound() throws Exception {
        // Arrange
        when(stockUnitService.checkUnitDependencies(TEST_STOCK_UNIT_POID, TEST_GROUP_POID))
                .thenThrow(new ResourceNotFoundException("Stock Unit", "stockUnitPoid", TEST_STOCK_UNIT_POID));

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/{stockUnitPoid}/dependencies", TEST_STOCK_UNIT_POID)
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isNotFound());

        verify(stockUnitService, times(1)).checkUnitDependencies(TEST_STOCK_UNIT_POID, TEST_GROUP_POID);
    }

    // ========== getActiveStockUnits Tests ==========

    @Test
    void testGetActiveStockUnits_Success() throws Exception {
        // Arrange
        List<StockUnitMasterDto> units = new ArrayList<>();
        units.add(stockUnitMasterDto);
        when(stockUnitService.getActiveStockUnits(TEST_GROUP_POID, null, null))
                .thenReturn(units);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/active")
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Active stock units fetched successfully"))
                .andExpect(jsonPath("$.result.data[0].stockUnitPoid").value(TEST_STOCK_UNIT_POID));

        verify(stockUnitService, times(1)).getActiveStockUnits(TEST_GROUP_POID, null, null);
    }

    @Test
    void testGetActiveStockUnits_WithClassified() throws Exception {
        // Arrange
        List<StockUnitMasterDto> units = new ArrayList<>();
        units.add(stockUnitMasterDto);
        when(stockUnitService.getActiveStockUnits(TEST_GROUP_POID, "Y", null))
                .thenReturn(units);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/active")
                        .header("groupPoid", TEST_GROUP_POID)
                        .param("classified", "Y"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(stockUnitService, times(1)).getActiveStockUnits(TEST_GROUP_POID, "Y", null);
    }

    @Test
    void testGetActiveStockUnits_WithSearch() throws Exception {
        // Arrange
        List<StockUnitMasterDto> units = new ArrayList<>();
        units.add(stockUnitMasterDto);
        when(stockUnitService.getActiveStockUnits(TEST_GROUP_POID, null, "KG"))
                .thenReturn(units);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/active")
                        .header("groupPoid", TEST_GROUP_POID)
                        .param("search", "KG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(stockUnitService, times(1)).getActiveStockUnits(TEST_GROUP_POID, null, "KG");
    }

    @Test
    void testGetActiveStockUnits_WithAllParams() throws Exception {
        // Arrange
        List<StockUnitMasterDto> units = new ArrayList<>();
        units.add(stockUnitMasterDto);
        when(stockUnitService.getActiveStockUnits(TEST_GROUP_POID, "Y", "KG"))
                .thenReturn(units);

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/active")
                        .header("groupPoid", TEST_GROUP_POID)
                        .param("classified", "Y")
                        .param("search", "KG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(stockUnitService, times(1)).getActiveStockUnits(TEST_GROUP_POID, "Y", "KG");
    }

    @Test
    void testGetActiveStockUnits_EmptyList() throws Exception {
        // Arrange
        when(stockUnitService.getActiveStockUnits(TEST_GROUP_POID, null, null))
                .thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/stockunitmaster/active")
                        .header("groupPoid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.data").isArray())
                .andExpect(jsonPath("$.result.data").isEmpty());

        verify(stockUnitService, times(1)).getActiveStockUnits(TEST_GROUP_POID, null, null);
    }
}

