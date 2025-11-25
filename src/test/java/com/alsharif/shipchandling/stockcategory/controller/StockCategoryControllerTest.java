package com.alsharif.shipchandling.stockcategory.controller;

import com.alsharif.shipchandling.exceptions.CustomException;
import com.alsharif.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import com.alsharif.shipchandling.stockcategory.dto.*;
import com.alsharif.shipchandling.stockcategory.dto.request.CreateStockCategoryRequest;
import com.alsharif.shipchandling.stockcategory.dto.request.UpdateStockCategoryRequest;
import com.alsharif.shipchandling.stockcategory.service.StockCategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockCategoryController.class)
class StockCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockCategoryService stockCategoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TEST_GROUP_POID = 1L;
    private static final Long TEST_CATEGORY_POID = 100L;
    private static final String TEST_USER_ID = "testUser";
    private static final String TEST_CATEGORY_CODE = "CAT001";
    private static final String TEST_CATEGORY_NAME = "Test Category";

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    // ========== Create Stock Category Tests ==========

    @Test
    void testCreateStockCategory_Success() throws Exception {
        // Arrange
        CreateStockCategoryRequest request = new CreateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("GROUP");
        request.setActive("Y");

        StockCategoryMasterDto responseDto = new StockCategoryMasterDto();
        responseDto.setCategoryPoid(TEST_CATEGORY_POID);
        responseDto.setCategoryCode(TEST_CATEGORY_CODE);
        responseDto.setCategoryName(TEST_CATEGORY_NAME);
        responseDto.setCategoryType("GROUP");

        when(stockCategoryService.createStockCategory(any(CreateStockCategoryRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/stock-categories")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock category created successfully"))
                .andExpect(jsonPath("$.result.data.categoryPoid").value(TEST_CATEGORY_POID));

        verify(stockCategoryService, times(1)).createStockCategory(
                any(CreateStockCategoryRequest.class), eq(TEST_GROUP_POID), eq(TEST_USER_ID));
    }

    @Test
    void testCreateStockCategory_MissingHeaders() throws Exception {
        // Arrange
        CreateStockCategoryRequest request = new CreateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);

        // Act & Assert - Missing Group POID
        mockMvc.perform(post("/stock-categories")
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateStockCategory_DuplicateCode() throws Exception {
        // Arrange
        CreateStockCategoryRequest request = new CreateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);

        when(stockCategoryService.createStockCategory(any(CreateStockCategoryRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID)))
                .thenThrow(new ResourceAlreadyExistsException("categoryCode", TEST_CATEGORY_CODE));

        // Act & Assert
        mockMvc.perform(post("/stock-categories")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    // ========== Get Stock Category Tests ==========

    @Test
    void testGetStockCategoryByPoid_Success() throws Exception {
        // Arrange
        StockCategoryMasterDto responseDto = new StockCategoryMasterDto();
        responseDto.setCategoryPoid(TEST_CATEGORY_POID);
        responseDto.setCategoryCode(TEST_CATEGORY_CODE);
        responseDto.setCategoryName(TEST_CATEGORY_NAME);

        when(stockCategoryService.getStockCategoryByPoid(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/{categoryPoid}", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .param("documentId", "800-300")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock category fetched successfully"))
                .andExpect(jsonPath("$.result.data.categoryPoid").value(TEST_CATEGORY_POID));

        verify(stockCategoryService, times(1)).getStockCategoryByPoid(
                eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID));
    }

    @Test
    void testGetStockCategoryByPoid_NotFound() throws Exception {
        // Arrange
        when(stockCategoryService.getStockCategoryByPoid(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenThrow(new ResourceNotFoundException("Stock Category", "categoryPoid", TEST_CATEGORY_POID));

        // Act & Assert
        mockMvc.perform(get("/stock-categories/{categoryPoid}", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .param("documentId", "800-300")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isNotFound());
    }

    // ========== Update Stock Category Tests ==========

    @Test
    void testUpdateStockCategory_Success() throws Exception {
        // Arrange
        UpdateStockCategoryRequest request = new UpdateStockCategoryRequest();
        request.setCategoryCode("UPDATED_CODE");
        request.setCategoryName("Updated Category Name");
        request.setCategoryType("GROUP");
        request.setActive("Y");

        StockCategoryMasterDto responseDto = new StockCategoryMasterDto();
        responseDto.setCategoryPoid(TEST_CATEGORY_POID);
        responseDto.setCategoryCode("UPDATED_CODE");
        responseDto.setCategoryName("Updated Category Name");

        when(stockCategoryService.updateStockCategory(
                eq(TEST_CATEGORY_POID), any(UpdateStockCategoryRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/stock-categories/{categoryPoid}", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock category updated successfully"));

        verify(stockCategoryService, times(1)).updateStockCategory(
                eq(TEST_CATEGORY_POID), any(UpdateStockCategoryRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID));
    }

    @Test
    void testUpdateStockCategory_NotFound() throws Exception {
        // Arrange
        UpdateStockCategoryRequest request = new UpdateStockCategoryRequest();
        request.setCategoryCode("UPDATED_CODE");
        request.setCategoryName("Updated Name");
        request.setCategoryType("GROUP");

        when(stockCategoryService.updateStockCategory(
                eq(TEST_CATEGORY_POID), any(UpdateStockCategoryRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID)))
                .thenThrow(new ResourceNotFoundException("Stock Category", "categoryPoid", TEST_CATEGORY_POID));

        // Act & Assert
        mockMvc.perform(put("/stock-categories/{categoryPoid}", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateStockCategory_ValidationError() throws Exception {
        // Arrange
        UpdateStockCategoryRequest request = new UpdateStockCategoryRequest();
        request.setCategoryCode("UPDATED_CODE");
        request.setCategoryName("Updated Name");
        request.setCategoryType("GROUP");

        when(stockCategoryService.updateStockCategory(
                eq(TEST_CATEGORY_POID), any(UpdateStockCategoryRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_USER_ID)))
                .thenThrow(new CustomException("Cannot update deleted category"));

        // Act & Assert
        mockMvc.perform(put("/stock-categories/{categoryPoid}", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    // ========== Delete Stock Category Tests ==========

    @Test
    void testDeleteStockCategory_Success() throws Exception {
        // Arrange
        doNothing().when(stockCategoryService).deleteStockCategory(
                eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID));

        // Act & Assert
        mockMvc.perform(delete("/stock-categories/{categoryPoid}", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock category deleted successfully"));

        verify(stockCategoryService, times(1)).deleteStockCategory(
                eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID));
    }

    @Test
    void testDeleteStockCategory_NotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Stock Category", "categoryPoid", TEST_CATEGORY_POID))
                .when(stockCategoryService).deleteStockCategory(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID));

        // Act & Assert
        mockMvc.perform(delete("/stock-categories/{categoryPoid}", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteStockCategory_WithDependencies() throws Exception {
        // Arrange
        doThrow(new CustomException("Cannot delete category. It has 2 child categories and 5 stock items."))
                .when(stockCategoryService).deleteStockCategory(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID));

        // Act & Assert
        mockMvc.perform(delete("/stock-categories/{categoryPoid}", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isInternalServerError());
    }

    // ========== Get Stock Category Tree Tests ==========

    @Test
    void testGetStockCategoryTree_Success() throws Exception {
        // Arrange
        StockCategoryTreeDto treeDto = new StockCategoryTreeDto();
        treeDto.setCategoryPoid(TEST_CATEGORY_POID);
        treeDto.setCode(TEST_CATEGORY_CODE);
        treeDto.setDescription(TEST_CATEGORY_NAME);
        treeDto.setHasChildren(false);

        List<StockCategoryTreeDto> treeList = Collections.singletonList(treeDto);

        when(stockCategoryService.getStockCategoryTree(eq(TEST_GROUP_POID), isNull(), isNull()))
                .thenReturn(treeList);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/tree")
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock category tree fetched successfully"))
                .andExpect(jsonPath("$.result.data").isArray());

        verify(stockCategoryService, times(1)).getStockCategoryTree(eq(TEST_GROUP_POID), isNull(), isNull());
    }

    @Test
    void testGetStockCategoryTree_WithFilters() throws Exception {
        // Arrange
        List<StockCategoryTreeDto> treeList = new ArrayList<>();

        when(stockCategoryService.getStockCategoryTree(eq(TEST_GROUP_POID), eq("GROUP"), eq("Y")))
                .thenReturn(treeList);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/tree")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .param("categoryType", "GROUP")
                        .param("active", "Y"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(stockCategoryService, times(1)).getStockCategoryTree(eq(TEST_GROUP_POID), eq("GROUP"), eq("Y"));
    }

    // ========== Get All Stock Categories Tests ==========

    @Test
    void testGetAllStockCategories_Success() throws Exception {
        // Arrange
        StockCategoryMasterDto dto = new StockCategoryMasterDto();
        dto.setCategoryPoid(TEST_CATEGORY_POID);
        dto.setCategoryCode(TEST_CATEGORY_CODE);
        dto.setCategoryName(TEST_CATEGORY_NAME);

        List<StockCategoryMasterDto> categoryList = Collections.singletonList(dto);

        when(stockCategoryService.getAllStockCategories(eq(TEST_GROUP_POID), isNull(), isNull()))
                .thenReturn(categoryList);

        // Act & Assert
        mockMvc.perform(get("/stock-categories")
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Stock categories fetched successfully"))
                .andExpect(jsonPath("$.result.data").isArray());

        verify(stockCategoryService, times(1)).getAllStockCategories(eq(TEST_GROUP_POID), isNull(), isNull());
    }

    @Test
    void testGetAllStockCategories_WithFilters() throws Exception {
        // Arrange
        List<StockCategoryMasterDto> categoryList = new ArrayList<>();

        when(stockCategoryService.getAllStockCategories(eq(TEST_GROUP_POID), eq("SUB_GROUP"), eq("Y")))
                .thenReturn(categoryList);

        // Act & Assert
        mockMvc.perform(get("/stock-categories")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .param("categoryType", "SUB_GROUP")
                        .param("active", "Y"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(stockCategoryService, times(1)).getAllStockCategories(eq(TEST_GROUP_POID), eq("SUB_GROUP"), eq("Y"));
    }

    // ========== Get Child Categories Tests ==========

    @Test
    void testGetChildCategories_Success() throws Exception {
        // Arrange
        StockCategoryMasterDto childDto = new StockCategoryMasterDto();
        childDto.setCategoryPoid(200L);
        childDto.setCategoryCode("CHILD001");
        childDto.setCategoryName("Child Category");

        List<StockCategoryMasterDto> children = Collections.singletonList(childDto);

        when(stockCategoryService.getStockCategoryByPoid(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenReturn(new StockCategoryMasterDto());
        when(stockCategoryService.getChildCategories(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenReturn(children);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/{parentCategoryPoid}/children", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Child categories fetched successfully"))
                .andExpect(jsonPath("$.result.data").isArray());

        verify(stockCategoryService, times(1)).getChildCategories(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID));
    }

    @Test
    void testGetChildCategories_ParentNotFound() throws Exception {
        // Arrange
        when(stockCategoryService.getStockCategoryByPoid(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenThrow(new ResourceNotFoundException("Stock Category", "categoryPoid", TEST_CATEGORY_POID));

        // Act & Assert
        mockMvc.perform(get("/stock-categories/{parentCategoryPoid}/children", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isNotFound());
    }

    // ========== Validate Category Code Tests ==========

    @Test
    void testValidateCategoryCode_Available() throws Exception {
        // Arrange
        when(stockCategoryService.validateCategoryCode(eq(TEST_CATEGORY_CODE), eq(TEST_GROUP_POID), isNull()))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/validate-code")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .param("categoryCode", TEST_CATEGORY_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category code is available"))
                .andExpect(jsonPath("$.result.data.isUnique").value(true));

        verify(stockCategoryService, times(1)).validateCategoryCode(
                eq(TEST_CATEGORY_CODE), eq(TEST_GROUP_POID), isNull());
    }

    @Test
    void testValidateCategoryCode_NotAvailable() throws Exception {
        // Arrange
        when(stockCategoryService.validateCategoryCode(eq(TEST_CATEGORY_CODE), eq(TEST_GROUP_POID), isNull()))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/validate-code")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .param("categoryCode", TEST_CATEGORY_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category code already exists"))
                .andExpect(jsonPath("$.result.data.isUnique").value(false));
    }

    @Test
    void testValidateCategoryCode_WithExclude() throws Exception {
        // Arrange
        when(stockCategoryService.validateCategoryCode(eq(TEST_CATEGORY_CODE), eq(TEST_GROUP_POID), eq(TEST_CATEGORY_POID)))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/validate-code")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .param("categoryCode", TEST_CATEGORY_CODE)
                        .param("excludeCategoryPoid", String.valueOf(TEST_CATEGORY_POID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(stockCategoryService, times(1)).validateCategoryCode(
                eq(TEST_CATEGORY_CODE), eq(TEST_GROUP_POID), eq(TEST_CATEGORY_POID));
    }

    // ========== Validate Category Name Tests ==========

    @Test
    void testValidateCategoryName_Available() throws Exception {
        // Arrange
        when(stockCategoryService.validateCategoryName(eq(TEST_CATEGORY_NAME), eq(TEST_GROUP_POID), isNull()))
                .thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/validate-name")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .param("categoryName", TEST_CATEGORY_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category name is available"))
                .andExpect(jsonPath("$.result.data.isUnique").value(true));
    }

    @Test
    void testValidateCategoryName_NotAvailable() throws Exception {
        // Arrange
        when(stockCategoryService.validateCategoryName(eq(TEST_CATEGORY_NAME), eq(TEST_GROUP_POID), isNull()))
                .thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/validate-name")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .param("categoryName", TEST_CATEGORY_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category name already exists"))
                .andExpect(jsonPath("$.result.data.isUnique").value(false));
    }

    // ========== Get Parent Category GL Values Tests ==========

    @Test
    void testGetParentCategoryGlValues_Success() throws Exception {
        // Arrange
        StockCategoryGlValuesDto glValues = new StockCategoryGlValuesDto();
        glValues.setParentCategoryPoid(TEST_CATEGORY_POID);
        glValues.setParentCategoryCode(TEST_CATEGORY_CODE);
        glValues.setParentCategoryName(TEST_CATEGORY_NAME);
        glValues.setStockGlPoid(BigDecimal.valueOf(1001L));
        glValues.setSalesGlPoid(BigDecimal.valueOf(1002L));
        glValues.setCostOfSalesGlPoid(BigDecimal.valueOf(1003L));

        when(stockCategoryService.getParentCategoryGlValues(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenReturn(glValues);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/{parentCategoryPoid}/gl-values", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Parent category GL values fetched successfully"))
                .andExpect(jsonPath("$.result.data.parentCategoryPoid").value(TEST_CATEGORY_POID));

        verify(stockCategoryService, times(1)).getParentCategoryGlValues(
                eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID));
    }

    @Test
    void testGetParentCategoryGlValues_NotFound() throws Exception {
        // Arrange
        when(stockCategoryService.getParentCategoryGlValues(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenThrow(new ResourceNotFoundException("Parent category", "categoryPoid", TEST_CATEGORY_POID));

        // Act & Assert
        mockMvc.perform(get("/stock-categories/{parentCategoryPoid}/gl-values", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isNotFound());
    }

    // ========== Check Category Dependencies Tests ==========

    @Test
    void testCheckCategoryDependencies_CanDelete() throws Exception {
        // Arrange
        CategoryDependenciesDto dependencies = new CategoryDependenciesDto();
        dependencies.setCategoryPoid(TEST_CATEGORY_POID);
        dependencies.setCanDelete(true);
        dependencies.setChildCategoryCount(0L);
        dependencies.setStockItemCount(0L);
        dependencies.setReason("No dependencies");
        dependencies.setMessage("Category can be deleted. No dependencies found.");

        when(stockCategoryService.checkCategoryDependencies(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenReturn(dependencies);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/{categoryPoid}/dependencies", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Dependency check completed"))
                .andExpect(jsonPath("$.result.data.canDelete").value(true));

        verify(stockCategoryService, times(1)).checkCategoryDependencies(
                eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID));
    }

    @Test
    void testCheckCategoryDependencies_CannotDelete() throws Exception {
        // Arrange
        CategoryDependenciesDto dependencies = new CategoryDependenciesDto();
        dependencies.setCategoryPoid(TEST_CATEGORY_POID);
        dependencies.setCanDelete(false);
        dependencies.setChildCategoryCount(2L);
        dependencies.setStockItemCount(5L);
        dependencies.setReason("Category has dependencies");

        when(stockCategoryService.checkCategoryDependencies(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenReturn(dependencies);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/{categoryPoid}/dependencies", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data.canDelete").value(false))
                .andExpect(jsonPath("$.result.data.childCategoryCount").value(2))
                .andExpect(jsonPath("$.result.data.stockItemCount").value(5));
    }

    @Test
    void testCheckCategoryDependencies_NotFound() throws Exception {
        // Arrange
        when(stockCategoryService.checkCategoryDependencies(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenThrow(new ResourceNotFoundException("Stock Category", "categoryPoid", TEST_CATEGORY_POID));

        // Act & Assert
        mockMvc.perform(get("/stock-categories/{categoryPoid}/dependencies", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isNotFound());
    }

    // ========== Get Category Hierarchy Tests ==========

    @Test
    void testGetCategoryHierarchy_Success() throws Exception {
        // Arrange
        StockCategoryHierarchyDto hierarchyDto = new StockCategoryHierarchyDto();
        hierarchyDto.setCategoryPoid(TEST_CATEGORY_POID);
        hierarchyDto.setCategoryCode(TEST_CATEGORY_CODE);
        hierarchyDto.setCategoryName(TEST_CATEGORY_NAME);
        hierarchyDto.setLevel(0);

        List<StockCategoryHierarchyDto> hierarchy = Collections.singletonList(hierarchyDto);

        when(stockCategoryService.getCategoryHierarchy(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenReturn(hierarchy);

        // Act & Assert
        mockMvc.perform(get("/stock-categories/{categoryPoid}/hierarchy", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Category hierarchy fetched successfully"))
                .andExpect(jsonPath("$.result.data").isArray());

        verify(stockCategoryService, times(1)).getCategoryHierarchy(
                eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID));
    }

    @Test
    void testGetCategoryHierarchy_NotFound() throws Exception {
        // Arrange
        when(stockCategoryService.getCategoryHierarchy(eq(TEST_CATEGORY_POID), eq(TEST_GROUP_POID)))
                .thenThrow(new ResourceNotFoundException("Stock Category", "categoryPoid", TEST_CATEGORY_POID));

        // Act & Assert
        mockMvc.perform(get("/stock-categories/{categoryPoid}/hierarchy", TEST_CATEGORY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID))
                .andExpect(status().isNotFound());
    }
}

