package com.alsharif.shipchandling.stockcategory.service;

import com.alsharif.shipchandling.exceptions.CustomException;
import com.alsharif.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import com.alsharif.shipchandling.stockcategory.dto.*;
import com.alsharif.shipchandling.stockcategory.dto.request.CreateStockCategoryRequest;
import com.alsharif.shipchandling.stockcategory.dto.request.UpdateStockCategoryRequest;
import com.alsharif.shipchandling.stockcategory.entity.StockCategoryMaster;
import com.alsharif.shipchandling.stockcategory.repository.StockCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class StockCategoryServiceImplTest {

    @Mock
    private StockCategoryRepository stockCategoryRepository;

    @InjectMocks
    private StockCategoryServiceImpl stockCategoryService;

    private static final Long TEST_GROUP_POID = 1L;
    private static final Long TEST_CATEGORY_POID = 100L;
    private static final Long TEST_PARENT_CATEGORY_POID = 50L;
    private static final String TEST_USER_ID = "testUser";
    private static final String TEST_CATEGORY_CODE = "CAT001";
    private static final String TEST_CATEGORY_NAME = "Test Category";

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    // ========== Create Stock Category Tests ==========

    @Test
    void testCreateStockCategory_Success_GROUP() {
        // Arrange
        CreateStockCategoryRequest request = new CreateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("GROUP");
        request.setActive("Y");

        StockCategoryMaster savedCategory = createTestCategory();
        savedCategory.setCategoryPoid(TEST_CATEGORY_POID);

        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(TEST_CATEGORY_CODE, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(TEST_CATEGORY_NAME, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.save(any(StockCategoryMaster.class))).thenReturn(savedCategory);
        doNothing().when(stockCategoryRepository).flush();
        when(stockCategoryRepository.findByCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(Optional.of(savedCategory));

        // Act
        StockCategoryMasterDto result = stockCategoryService.createStockCategory(request, TEST_GROUP_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_CATEGORY_POID, result.getCategoryPoid());
        assertEquals(TEST_CATEGORY_CODE, result.getCategoryCode());
        verify(stockCategoryRepository, times(1)).save(any(StockCategoryMaster.class));
        verify(stockCategoryRepository, times(1)).flush();
    }

    @Test
    void testCreateStockCategory_Success_SUB_GROUP() {
        // Arrange
        CreateStockCategoryRequest request = new CreateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("SUB_GROUP");
        request.setParentCategoryPoid(TEST_PARENT_CATEGORY_POID);
        request.setActive("Y");

        StockCategoryMaster parentCategory = createTestCategory();
        parentCategory.setCategoryPoid(TEST_PARENT_CATEGORY_POID);
        parentCategory.setCategoryType("GROUP");

        StockCategoryMaster savedCategory = createTestCategory();
        savedCategory.setCategoryPoid(TEST_CATEGORY_POID);
        savedCategory.setCategoryType("SUB_GROUP");
        savedCategory.setParentCategoryPoid(TEST_PARENT_CATEGORY_POID);

        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(TEST_CATEGORY_CODE, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(TEST_CATEGORY_NAME, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(parentCategory));
        when(stockCategoryRepository.save(any(StockCategoryMaster.class))).thenReturn(savedCategory);
        doNothing().when(stockCategoryRepository).flush();
        when(stockCategoryRepository.findByCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(Optional.of(savedCategory));

        // Act
        StockCategoryMasterDto result = stockCategoryService.createStockCategory(request, TEST_GROUP_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_CATEGORY_POID, result.getCategoryPoid());
        verify(stockCategoryRepository, times(1)).save(any(StockCategoryMaster.class));
    }

    @Test
    void testCreateStockCategory_DuplicateCode() {
        // Arrange
        CreateStockCategoryRequest request = new CreateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("GROUP");

        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(TEST_CATEGORY_CODE, TEST_GROUP_POID))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            stockCategoryService.createStockCategory(request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testCreateStockCategory_DuplicateName() {
        // Arrange
        CreateStockCategoryRequest request = new CreateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("GROUP");

        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(TEST_CATEGORY_CODE, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(TEST_CATEGORY_NAME, TEST_GROUP_POID))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            stockCategoryService.createStockCategory(request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testCreateStockCategory_SUB_GROUP_MissingParent() {
        // Arrange
        CreateStockCategoryRequest request = new CreateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("SUB_GROUP");
        request.setParentCategoryPoid(null);

        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(TEST_CATEGORY_CODE, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(TEST_CATEGORY_NAME, TEST_GROUP_POID))
                .thenReturn(false);

        // Act & Assert
        assertThrows(CustomException.class, () -> {
            stockCategoryService.createStockCategory(request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testCreateStockCategory_SUB_GROUP_ParentNotFound() {
        // Arrange
        CreateStockCategoryRequest request = new CreateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("SUB_GROUP");
        request.setParentCategoryPoid(TEST_PARENT_CATEGORY_POID);

        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(TEST_CATEGORY_CODE, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(TEST_CATEGORY_NAME, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockCategoryService.createStockCategory(request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testCreateStockCategory_SUB_GROUP_ParentNotGROUP() {
        // Arrange
        CreateStockCategoryRequest request = new CreateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("SUB_GROUP");
        request.setParentCategoryPoid(TEST_PARENT_CATEGORY_POID);

        StockCategoryMaster parentCategory = createTestCategory();
        parentCategory.setCategoryPoid(TEST_PARENT_CATEGORY_POID);
        parentCategory.setCategoryType("SUB_GROUP"); // Wrong type

        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(TEST_CATEGORY_CODE, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(TEST_CATEGORY_NAME, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(parentCategory));

        // Act & Assert
        assertThrows(CustomException.class, () -> {
            stockCategoryService.createStockCategory(request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testCreateStockCategory_DefaultValues() {
        // Arrange
        CreateStockCategoryRequest request = new CreateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType(null); // Should default to GROUP
        request.setActive(null); // Should default to Y

        StockCategoryMaster savedCategory = createTestCategory();
        savedCategory.setCategoryPoid(TEST_CATEGORY_POID);
        savedCategory.setCategoryType("GROUP");
        savedCategory.setActive("Y");

        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(TEST_CATEGORY_CODE, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(TEST_CATEGORY_NAME, TEST_GROUP_POID))
                .thenReturn(false);
        when(stockCategoryRepository.save(any(StockCategoryMaster.class))).thenReturn(savedCategory);
        doNothing().when(stockCategoryRepository).flush();
        when(stockCategoryRepository.findByCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(Optional.of(savedCategory));

        // Act
        StockCategoryMasterDto result = stockCategoryService.createStockCategory(request, TEST_GROUP_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<StockCategoryMaster> captor = ArgumentCaptor.forClass(StockCategoryMaster.class);
        verify(stockCategoryRepository).save(captor.capture());
        assertEquals("GROUP", captor.getValue().getCategoryType());
        assertEquals("Y", captor.getValue().getActive());
        assertEquals("N", captor.getValue().getDeleted());
    }

    // ========== Get Stock Category Tests ==========

    @Test
    void testGetStockCategoryByPoid_Success() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);
        category.setDeleted("N");

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(category));

        // Act
        StockCategoryMasterDto result = stockCategoryService.getStockCategoryByPoid(TEST_CATEGORY_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_CATEGORY_POID, result.getCategoryPoid());
        assertEquals(TEST_CATEGORY_CODE, result.getCategoryCode());
    }

    @Test
    void testGetStockCategoryByPoid_NotFound() {
        // Arrange
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockCategoryService.getStockCategoryByPoid(TEST_CATEGORY_POID, TEST_GROUP_POID);
        });
    }

    @Test
    void testGetStockCategoryByPoid_Deleted() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);
        category.setDeleted("Y");

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(category));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockCategoryService.getStockCategoryByPoid(TEST_CATEGORY_POID, TEST_GROUP_POID);
        });
    }

    // ========== Update Stock Category Tests ==========

    @Test
    void testUpdateStockCategory_Success() {
        // Arrange
        StockCategoryMaster existingCategory = createTestCategory();
        existingCategory.setCategoryPoid(TEST_CATEGORY_POID);
        existingCategory.setDeleted("N");

        UpdateStockCategoryRequest request = new UpdateStockCategoryRequest();
        request.setCategoryCode("UPDATED_CODE");
        request.setCategoryName("Updated Name");
        request.setCategoryType("GROUP");
        request.setActive("Y");

        StockCategoryMaster savedCategory = createTestCategory();
        savedCategory.setCategoryPoid(TEST_CATEGORY_POID);
        savedCategory.setCategoryCode("UPDATED_CODE");
        savedCategory.setCategoryName("Updated Name");

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingCategory));
        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                "UPDATED_CODE", TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                "Updated Name", TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(false);
        when(stockCategoryRepository.save(any(StockCategoryMaster.class))).thenReturn(savedCategory);
        doNothing().when(stockCategoryRepository).flush();
        when(stockCategoryRepository.findByCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(Optional.of(savedCategory));

        // Act
        StockCategoryMasterDto result = stockCategoryService.updateStockCategory(
                TEST_CATEGORY_POID, request, TEST_GROUP_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals("UPDATED_CODE", result.getCategoryCode());
        verify(stockCategoryRepository, times(1)).save(any(StockCategoryMaster.class));
    }

    @Test
    void testUpdateStockCategory_NotFound() {
        // Arrange
        UpdateStockCategoryRequest request = new UpdateStockCategoryRequest();
        request.setCategoryCode("UPDATED_CODE");
        request.setCategoryName("Updated Name");
        request.setCategoryType("GROUP");

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockCategoryService.updateStockCategory(TEST_CATEGORY_POID, request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testUpdateStockCategory_Deleted() {
        // Arrange
        StockCategoryMaster existingCategory = createTestCategory();
        existingCategory.setCategoryPoid(TEST_CATEGORY_POID);
        existingCategory.setDeleted("Y");

        UpdateStockCategoryRequest request = new UpdateStockCategoryRequest();
        request.setCategoryCode("UPDATED_CODE");
        request.setCategoryName("Updated Name");
        request.setCategoryType("GROUP");

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingCategory));

        // Act & Assert
        assertThrows(CustomException.class, () -> {
            stockCategoryService.updateStockCategory(TEST_CATEGORY_POID, request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testUpdateStockCategory_DuplicateCode() {
        // Arrange
        StockCategoryMaster existingCategory = createTestCategory();
        existingCategory.setCategoryPoid(TEST_CATEGORY_POID);
        existingCategory.setDeleted("N");

        UpdateStockCategoryRequest request = new UpdateStockCategoryRequest();
        request.setCategoryCode("UPDATED_CODE");
        request.setCategoryName("Updated Name");
        request.setCategoryType("GROUP");

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingCategory));
        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                "UPDATED_CODE", TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            stockCategoryService.updateStockCategory(TEST_CATEGORY_POID, request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testUpdateStockCategory_SUB_GROUP_SelfReference() {
        // Arrange
        StockCategoryMaster existingCategory = createTestCategory();
        existingCategory.setCategoryPoid(TEST_CATEGORY_POID);
        existingCategory.setDeleted("N");

        UpdateStockCategoryRequest request = new UpdateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("SUB_GROUP");
        request.setParentCategoryPoid(TEST_CATEGORY_POID); // Self-reference

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingCategory));
        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                TEST_CATEGORY_CODE, TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                TEST_CATEGORY_NAME, TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(false);

        // Act & Assert
        assertThrows(CustomException.class, () -> {
            stockCategoryService.updateStockCategory(TEST_CATEGORY_POID, request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testUpdateStockCategory_SUB_GROUP_WithChildren() {
        // Arrange
        StockCategoryMaster existingCategory = createTestCategory();
        existingCategory.setCategoryPoid(TEST_CATEGORY_POID);
        existingCategory.setDeleted("N");

        StockCategoryMaster parentCategory = createTestCategory();
        parentCategory.setCategoryPoid(TEST_PARENT_CATEGORY_POID);
        parentCategory.setCategoryType("GROUP");

        UpdateStockCategoryRequest request = new UpdateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("SUB_GROUP");
        request.setParentCategoryPoid(TEST_PARENT_CATEGORY_POID);

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingCategory));
        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                TEST_CATEGORY_CODE, TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                TEST_CATEGORY_NAME, TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(false);
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(parentCategory));
        when(stockCategoryRepository.countChildrenByParentCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(2L); // Has children

        // Act & Assert
        assertThrows(CustomException.class, () -> {
            stockCategoryService.updateStockCategory(TEST_CATEGORY_POID, request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testUpdateStockCategory_ChangeToGROUP_RemovesParent() {
        // Arrange
        StockCategoryMaster existingCategory = createTestCategory();
        existingCategory.setCategoryPoid(TEST_CATEGORY_POID);
        existingCategory.setDeleted("N");
        existingCategory.setCategoryType("SUB_GROUP");
        existingCategory.setParentCategoryPoid(TEST_PARENT_CATEGORY_POID);

        UpdateStockCategoryRequest request = new UpdateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("GROUP");
        request.setParentCategoryPoid(TEST_PARENT_CATEGORY_POID); // Will be set to null

        StockCategoryMaster savedCategory = createTestCategory();
        savedCategory.setCategoryPoid(TEST_CATEGORY_POID);
        savedCategory.setCategoryType("GROUP");
        savedCategory.setParentCategoryPoid(null);

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingCategory));
        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                TEST_CATEGORY_CODE, TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                TEST_CATEGORY_NAME, TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(false);
        when(stockCategoryRepository.save(any(StockCategoryMaster.class))).thenReturn(savedCategory);
        doNothing().when(stockCategoryRepository).flush();
        when(stockCategoryRepository.findByCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(Optional.of(savedCategory));

        // Act
        StockCategoryMasterDto result = stockCategoryService.updateStockCategory(
                TEST_CATEGORY_POID, request, TEST_GROUP_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        ArgumentCaptor<StockCategoryMaster> captor = ArgumentCaptor.forClass(StockCategoryMaster.class);
        verify(stockCategoryRepository).save(captor.capture());
        assertNull(captor.getValue().getParentCategoryPoid());
    }

    // ========== Delete Stock Category Tests ==========

    @Test
    void testDeleteStockCategory_Success() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(category));
        when(stockCategoryRepository.countChildrenByParentCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(0L);
        when(stockCategoryRepository.countStockItemsByCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(0L);
        when(stockCategoryRepository.save(any(StockCategoryMaster.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        stockCategoryService.deleteStockCategory(TEST_CATEGORY_POID, TEST_GROUP_POID);

        // Assert
        ArgumentCaptor<StockCategoryMaster> captor = ArgumentCaptor.forClass(StockCategoryMaster.class);
        verify(stockCategoryRepository).save(captor.capture());
        assertEquals("Y", captor.getValue().getDeleted());
    }

    @Test
    void testDeleteStockCategory_NotFound() {
        // Arrange
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockCategoryService.deleteStockCategory(TEST_CATEGORY_POID, TEST_GROUP_POID);
        });
    }

    @Test
    void testDeleteStockCategory_WithChildren() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(category));
        when(stockCategoryRepository.countChildrenByParentCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(2L);
        when(stockCategoryRepository.countStockItemsByCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(0L);

        // Act & Assert
        assertThrows(CustomException.class, () -> {
            stockCategoryService.deleteStockCategory(TEST_CATEGORY_POID, TEST_GROUP_POID);
        });
    }

    @Test
    void testDeleteStockCategory_WithStockItems() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(category));
        when(stockCategoryRepository.countChildrenByParentCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(0L);
        when(stockCategoryRepository.countStockItemsByCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(5L);

        // Act & Assert
        assertThrows(CustomException.class, () -> {
            stockCategoryService.deleteStockCategory(TEST_CATEGORY_POID, TEST_GROUP_POID);
        });
    }

    // ========== Get Stock Category Tree Tests ==========

    @Test
    void testGetStockCategoryTree_Success() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);
        category.setParentCategoryPoid(null);

        List<StockCategoryMaster> categories = Collections.singletonList(category);

        when(stockCategoryRepository.findParentCategoriesByGroupPoid(TEST_GROUP_POID))
                .thenReturn(categories);
        when(stockCategoryRepository.countChildrenByParentCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(0L);

        // Act
        List<StockCategoryTreeDto> result = stockCategoryService.getStockCategoryTree(TEST_GROUP_POID, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_CATEGORY_POID, result.get(0).getCategoryPoid());
    }

    @Test
    void testGetStockCategoryTree_WithFilters() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);
        category.setCategoryType("GROUP");
        category.setActive("Y");
        category.setParentCategoryPoid(null);

        List<StockCategoryMaster> categories = Collections.singletonList(category);

        when(stockCategoryRepository.findParentCategoriesByGroupPoid(TEST_GROUP_POID))
                .thenReturn(categories);
        when(stockCategoryRepository.countChildrenByParentCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(0L);

        // Act
        List<StockCategoryTreeDto> result = stockCategoryService.getStockCategoryTree(
                TEST_GROUP_POID, "GROUP", "Y");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetStockCategoryTree_FilteredOut() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);
        category.setCategoryType("SUB_GROUP");
        category.setActive("N");
        category.setParentCategoryPoid(null);

        List<StockCategoryMaster> categories = Collections.singletonList(category);

        when(stockCategoryRepository.findParentCategoriesByGroupPoid(TEST_GROUP_POID))
                .thenReturn(categories);

        // Act
        List<StockCategoryTreeDto> result = stockCategoryService.getStockCategoryTree(
                TEST_GROUP_POID, "GROUP", "Y");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size()); // Filtered out
    }

    // ========== Get All Stock Categories Tests ==========

    @Test
    void testGetAllStockCategories_Success() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);

        List<StockCategoryMaster> categories = Collections.singletonList(category);

        when(stockCategoryRepository.findByGroupPoidAndDeletedNotOrDeletedIsNull(TEST_GROUP_POID, "Y"))
                .thenReturn(categories);

        // Act
        List<StockCategoryMasterDto> result = stockCategoryService.getAllStockCategories(
                TEST_GROUP_POID, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetAllStockCategories_WithFilters() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);
        category.setCategoryType("SUB_GROUP");
        category.setActive("Y");

        List<StockCategoryMaster> categories = Collections.singletonList(category);

        when(stockCategoryRepository.findByGroupPoidAndDeletedNotOrDeletedIsNull(TEST_GROUP_POID, "Y"))
                .thenReturn(categories);

        // Act
        List<StockCategoryMasterDto> result = stockCategoryService.getAllStockCategories(
                TEST_GROUP_POID, "SUB_GROUP", "Y");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== Get Child Categories Tests ==========

    @Test
    void testGetChildCategories_Success() {
        // Arrange
        StockCategoryMaster child = createTestCategory();
        child.setCategoryPoid(200L);
        child.setParentCategoryPoid(TEST_CATEGORY_POID);

        List<StockCategoryMaster> children = Collections.singletonList(child);

        when(stockCategoryRepository.findChildrenByParentCategoryPoidAndGroupPoid(
                TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(children);

        // Act
        List<StockCategoryMasterDto> result = stockCategoryService.getChildCategories(
                TEST_CATEGORY_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(200L, result.get(0).getCategoryPoid());
    }

    @Test
    void testGetChildCategories_Empty() {
        // Arrange
        when(stockCategoryRepository.findChildrenByParentCategoryPoidAndGroupPoid(
                TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Collections.emptyList());

        // Act
        List<StockCategoryMasterDto> result = stockCategoryService.getChildCategories(
                TEST_CATEGORY_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    // ========== Validate Category Code Tests ==========

    @Test
    void testValidateCategoryCode_Unique() {
        // Arrange
        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(TEST_CATEGORY_CODE, TEST_GROUP_POID))
                .thenReturn(false);

        // Act
        boolean result = stockCategoryService.validateCategoryCode(TEST_CATEGORY_CODE, TEST_GROUP_POID, null);

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateCategoryCode_Duplicate() {
        // Arrange
        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(TEST_CATEGORY_CODE, TEST_GROUP_POID))
                .thenReturn(true);

        // Act
        boolean result = stockCategoryService.validateCategoryCode(TEST_CATEGORY_CODE, TEST_GROUP_POID, null);

        // Assert
        assertFalse(result);
    }

    @Test
    void testValidateCategoryCode_WithExclude_Unique() {
        // Arrange
        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                TEST_CATEGORY_CODE, TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(false);

        // Act
        boolean result = stockCategoryService.validateCategoryCode(
                TEST_CATEGORY_CODE, TEST_GROUP_POID, TEST_CATEGORY_POID);

        // Assert
        assertTrue(result);
    }

    // ========== Validate Category Name Tests ==========

    @Test
    void testValidateCategoryName_Unique() {
        // Arrange
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(TEST_CATEGORY_NAME, TEST_GROUP_POID))
                .thenReturn(false);

        // Act
        boolean result = stockCategoryService.validateCategoryName(TEST_CATEGORY_NAME, TEST_GROUP_POID, null);

        // Assert
        assertTrue(result);
    }

    @Test
    void testValidateCategoryName_Duplicate() {
        // Arrange
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(TEST_CATEGORY_NAME, TEST_GROUP_POID))
                .thenReturn(true);

        // Act
        boolean result = stockCategoryService.validateCategoryName(TEST_CATEGORY_NAME, TEST_GROUP_POID, null);

        // Assert
        assertFalse(result);
    }

    // ========== Get Parent Category GL Values Tests ==========

    @Test
    void testGetParentCategoryGlValues_Success() {
        // Arrange
        StockCategoryMaster parent = createTestCategory();
        parent.setCategoryPoid(TEST_PARENT_CATEGORY_POID);
        parent.setCategoryType("GROUP");
        parent.setStockGlPoid(BigDecimal.valueOf(1001L));
        parent.setSalesGlPoid(BigDecimal.valueOf(1002L));
        parent.setCostOfSalesGlPoid(BigDecimal.valueOf(1003L));

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(parent));

        // Act
        StockCategoryGlValuesDto result = stockCategoryService.getParentCategoryGlValues(
                TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PARENT_CATEGORY_POID, result.getParentCategoryPoid());
        assertNotNull(result.getStockGlPoid());
        assertNotNull(result.getSalesGlPoid());
    }

    @Test
    void testGetParentCategoryGlValues_NotFound() {
        // Arrange
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockCategoryService.getParentCategoryGlValues(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID);
        });
    }

    @Test
    void testGetParentCategoryGlValues_NotGROUP() {
        // Arrange
        StockCategoryMaster parent = createTestCategory();
        parent.setCategoryPoid(TEST_PARENT_CATEGORY_POID);
        parent.setCategoryType("SUB_GROUP"); // Wrong type

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(parent));

        // Act & Assert
        assertThrows(CustomException.class, () -> {
            stockCategoryService.getParentCategoryGlValues(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID);
        });
    }

    // ========== Check Category Dependencies Tests ==========

    @Test
    void testCheckCategoryDependencies_CanDelete() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(category));
        when(stockCategoryRepository.countChildrenByParentCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(0L);
        when(stockCategoryRepository.countStockItemsByCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(0L);

        // Act
        CategoryDependenciesDto result = stockCategoryService.checkCategoryDependencies(
                TEST_CATEGORY_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getCanDelete());
        assertEquals(0L, result.getChildCategoryCount());
        assertEquals(0L, result.getStockItemCount());
    }

    @Test
    void testCheckCategoryDependencies_CannotDelete() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(category));
        when(stockCategoryRepository.countChildrenByParentCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(2L);
        when(stockCategoryRepository.countStockItemsByCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(5L);

        // Act
        CategoryDependenciesDto result = stockCategoryService.checkCategoryDependencies(
                TEST_CATEGORY_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertFalse(result.getCanDelete());
        assertEquals(2L, result.getChildCategoryCount());
        assertEquals(5L, result.getStockItemCount());
    }

    @Test
    void testCheckCategoryDependencies_NotFound() {
        // Arrange
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockCategoryService.checkCategoryDependencies(TEST_CATEGORY_POID, TEST_GROUP_POID);
        });
    }

    // ========== Get Category Hierarchy Tests ==========

    @Test
    void testGetCategoryHierarchy_SingleLevel() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);
        category.setParentCategoryPoid(null); // Root

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(category));

        // Act
        List<StockCategoryHierarchyDto> result = stockCategoryService.getCategoryHierarchy(
                TEST_CATEGORY_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_CATEGORY_POID, result.get(0).getCategoryPoid());
        assertEquals(0, result.get(0).getLevel());
    }

    @Test
    void testGetCategoryHierarchy_MultiLevel() {
        // Arrange
        StockCategoryMaster root = createTestCategory();
        root.setCategoryPoid(10L);
        root.setParentCategoryPoid(null);

        StockCategoryMaster parent = createTestCategory();
        parent.setCategoryPoid(TEST_PARENT_CATEGORY_POID);
        parent.setParentCategoryPoid(10L);

        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);
        category.setParentCategoryPoid(TEST_PARENT_CATEGORY_POID);

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(category));
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(parent));
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(10L, TEST_GROUP_POID))
                .thenReturn(Optional.of(root));

        // Act
        List<StockCategoryHierarchyDto> result = stockCategoryService.getCategoryHierarchy(
                TEST_CATEGORY_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(10L, result.get(0).getCategoryPoid()); // Root first
        assertEquals(TEST_CATEGORY_POID, result.get(2).getCategoryPoid()); // Current last
    }

    @Test
    void testGetCategoryHierarchy_NotFound() {
        // Arrange
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockCategoryService.getCategoryHierarchy(TEST_CATEGORY_POID, TEST_GROUP_POID);
        });
    }

    @Test
    void testGetCategoryHierarchy_ParentNotFound() {
        // Arrange
        StockCategoryMaster category = createTestCategory();
        category.setCategoryPoid(TEST_CATEGORY_POID);
        category.setParentCategoryPoid(TEST_PARENT_CATEGORY_POID);

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(category));
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.empty()); // Parent not found

        // Act
        List<StockCategoryHierarchyDto> result = stockCategoryService.getCategoryHierarchy(
                TEST_CATEGORY_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // Only current category
    }

    // ========== Circular Reference Validation Tests ==========

    @Test
    void testUpdateStockCategory_CircularReference() {
        // Arrange
        StockCategoryMaster existingCategory = createTestCategory();
        existingCategory.setCategoryPoid(TEST_CATEGORY_POID);
        existingCategory.setDeleted("N");

        UpdateStockCategoryRequest request = new UpdateStockCategoryRequest();
        request.setCategoryCode(TEST_CATEGORY_CODE);
        request.setCategoryName(TEST_CATEGORY_NAME);
        request.setCategoryType("SUB_GROUP");
        request.setParentCategoryPoid(TEST_PARENT_CATEGORY_POID);

        StockCategoryMaster parent = createTestCategory();
        parent.setCategoryPoid(TEST_PARENT_CATEGORY_POID);
        parent.setParentCategoryPoid(TEST_CATEGORY_POID); // Circular reference

        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingCategory));
        when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                TEST_CATEGORY_CODE, TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(false);
        when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                TEST_CATEGORY_NAME, TEST_GROUP_POID, TEST_CATEGORY_POID))
                .thenReturn(false);
        when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(TEST_PARENT_CATEGORY_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(parent));
        when(stockCategoryRepository.findByCategoryPoid(TEST_PARENT_CATEGORY_POID))
                .thenReturn(Optional.of(parent));

        // Act & Assert
        assertThrows(CustomException.class, () -> {
            stockCategoryService.updateStockCategory(TEST_CATEGORY_POID, request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    // ========== Helper Methods ==========

    private StockCategoryMaster createTestCategory() {
        StockCategoryMaster category = new StockCategoryMaster();
        category.setCategoryPoid(TEST_CATEGORY_POID);
        category.setCategoryCode(TEST_CATEGORY_CODE);
        category.setCategoryName(TEST_CATEGORY_NAME);
        category.setCategoryType("GROUP");
        category.setGroupPoid(TEST_GROUP_POID);
        category.setActive("Y");
        category.setDeleted("N");
        category.setCreatedBy(TEST_USER_ID);
        category.setLastmodifiedBy(TEST_USER_ID);
        category.setCreatedDate(Timestamp.from(Instant.now()));
        category.setLastmodifiedDate(Timestamp.from(Instant.now()));
        return category;
    }
}

