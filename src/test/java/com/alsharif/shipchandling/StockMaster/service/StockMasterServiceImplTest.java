package com.alsharif.shipchandling.StockMaster.service;

import com.alsharif.shipchandling.StockMaster.dto.*;
import com.alsharif.shipchandling.StockMaster.entity.*;
import com.alsharif.shipchandling.StockMaster.repository.*;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMasterServiceImplTest {

    @Mock
    private StockMasterRepository stockMasterRepository;

    @Mock
    private StockMasterDtlRepository dtlRepository;

    @Mock
    private StockMasterWarehouseDtlRepository warehouseRepository;

    @Mock
    private StockCategoryMasterRepository categoryMasterRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StockMasterServiceImpl stockMasterService;

    private static final Long TEST_STOCK_POID = 1L;
    private static final Long TEST_GROUP_POID = 10L;
    private static final Long TEST_COMPANY_POID = 20L;
    private static final String TEST_USER_ID = "testUser";
    private static final Long TEST_DET_ROW_ID = 1L;
    private static final Long TEST_CATEGORY_POID = 5L;

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    // ========== Get Stock Master By ID Tests ==========

    @Test
    void testGetStockMasterById_Success() {
        // Arrange
        StockMasterEntity entity = createStockMasterEntity();
        when(stockMasterRepository.findById(TEST_STOCK_POID)).thenReturn(Optional.of(entity));

        // Act
        StockMasterViewResponse result = stockMasterService.getStockMasterById(TEST_STOCK_POID, false, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_STOCK_POID, result.getStockPoid());
        assertEquals("STOCK001", result.getStockCode());
        verify(stockMasterRepository, times(1)).findById(TEST_STOCK_POID);
    }

    @Test
    void testGetStockMasterById_WithDetails() {
        // Arrange
        StockMasterEntity entity = createStockMasterEntity();
        StockMasterDTLEntity dtlEntity = createStockMasterDTLEntity();
        StockMasterWarehouseDtl whEntity = createStockMasterWarehouseDtl();
        StockCategoryMasterEntity category = createStockCategoryMasterEntity();

        when(stockMasterRepository.findById(TEST_STOCK_POID)).thenReturn(Optional.of(entity));
        when(categoryMasterRepository.findByCategoryPoid(TEST_CATEGORY_POID))
                .thenReturn(Optional.of(category));
        when(dtlRepository.findByStockPoid(TEST_STOCK_POID))
                .thenReturn(Collections.singletonList(dtlEntity));
        when(warehouseRepository.findByStockPoid(TEST_STOCK_POID))
                .thenReturn(Collections.singletonList(whEntity));

        // Act
        StockMasterViewResponse result = stockMasterService.getStockMasterById(TEST_STOCK_POID, true, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getSupplierDetails());
        assertNotNull(result.getWarehouseDetails());
        assertEquals(1, result.getSupplierDetails().size());
        assertEquals(1, result.getWarehouseDetails().size());
    }

    @Test
    void testGetStockMasterById_NotFound() {
        // Arrange
        when(stockMasterRepository.findById(TEST_STOCK_POID)).thenReturn(Optional.empty());

        // Act
        StockMasterViewResponse result = stockMasterService.getStockMasterById(TEST_STOCK_POID, false, TEST_GROUP_POID);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetStockMasterById_Deleted() {
        // Arrange
        StockMasterEntity entity = createStockMasterEntity();
        entity.setDeleted("Y");
        when(stockMasterRepository.findById(TEST_STOCK_POID)).thenReturn(Optional.of(entity));

        // Act
        StockMasterViewResponse result = stockMasterService.getStockMasterById(TEST_STOCK_POID, false, TEST_GROUP_POID);

        // Assert
        assertNull(result);
    }

    @Test
    void testGetStockMasterById_GroupMismatch() {
        // Arrange
        StockMasterEntity entity = createStockMasterEntity();
        entity.setGroupPoid(999L);
        when(stockMasterRepository.findById(TEST_STOCK_POID)).thenReturn(Optional.of(entity));

        // Act
        StockMasterViewResponse result = stockMasterService.getStockMasterById(TEST_STOCK_POID, false, TEST_GROUP_POID);

        // Assert
        assertNull(result);
    }

    // ========== Get Stock Masters Tests ==========

    @Test
    void testGetStockMasters_Success() {
        // Arrange
        StockMasterEntity entity = createStockMasterEntity();
        List<StockMasterEntity> stockList = Collections.singletonList(entity);
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockMasterEntity> page = new PageImpl<>(stockList, pageable, 1);

        Map<String, String> filters = new HashMap<>();
        filters.put("groupPoid", String.valueOf(TEST_GROUP_POID));

        when(stockMasterRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<StockMasterEntity> result = stockMasterService.getStockMasters(filters, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(stockMasterRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testGetStockMasters_WithFilters() {
        // Arrange
        StockMasterEntity entity = createStockMasterEntity();
        List<StockMasterEntity> stockList = Collections.singletonList(entity);
        Pageable pageable = PageRequest.of(0, 10);
        Page<StockMasterEntity> page = new PageImpl<>(stockList, pageable, 1);

        Map<String, String> filters = new HashMap<>();
        filters.put("groupPoid", String.valueOf(TEST_GROUP_POID));
        filters.put("active", "Y");
        filters.put("categoryPoid", String.valueOf(TEST_CATEGORY_POID));
        filters.put("search", "test");

        when(stockMasterRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<StockMasterEntity> result = stockMasterService.getStockMasters(filters, pageable);

        // Assert
        assertNotNull(result);
        verify(stockMasterRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    // ========== Get Stock Masters Tree Tests ==========

    @Test
    void testGetStockMastersTree_Success() {
        // Arrange
        StockMasterEntity entity = createStockMasterEntity();
        StockCategoryMasterEntity rootCategory = createStockCategoryMasterEntity();
        rootCategory.setParentCategoryPoid(null);

        when(stockMasterRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(entity));
        when(categoryMasterRepository.findByGroupPoid(TEST_GROUP_POID))
                .thenReturn(Collections.singletonList(rootCategory));

        // Act
        List<Map<String, Object>> result = stockMasterService.getStockMastersTree(TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        verify(stockMasterRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void testGetStockMastersTree_WithChildCategories() {
        // Arrange
        StockMasterEntity entity = createStockMasterEntity();
        StockCategoryMasterEntity rootCategory = createStockCategoryMasterEntity();
        rootCategory.setParentCategoryPoid(null);
        rootCategory.setCategoryPoid(1L);

        StockCategoryMasterEntity childCategory = createStockCategoryMasterEntity();
        childCategory.setCategoryPoid(2L);
        childCategory.setParentCategoryPoid(1L);

        when(stockMasterRepository.findAll(any(Specification.class)))
                .thenReturn(Collections.singletonList(entity));
        when(categoryMasterRepository.findByGroupPoid(TEST_GROUP_POID))
                .thenReturn(Arrays.asList(rootCategory, childCategory));

        // Act
        List<Map<String, Object>> result = stockMasterService.getStockMastersTree(TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // ========== Validation Tests ==========

    @Test
    void testValidateStockCode_Success() {
        // Arrange
        when(stockMasterRepository.existsByStockCodeIgnoreCaseAndGroupPoid("STOCK001", TEST_GROUP_POID))
                .thenReturn(false);

        // Act
        ValidationResponse result = stockMasterService.validateStockCode("STOCK001", TEST_GROUP_POID, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsUnique());
        assertEquals("Stock code is available", result.getMessage());
    }

    @Test
    void testValidateStockCode_AlreadyExists() {
        // Arrange
        when(stockMasterRepository.existsByStockCodeIgnoreCaseAndGroupPoid("STOCK001", TEST_GROUP_POID))
                .thenReturn(true);

        // Act
        ValidationResponse result = stockMasterService.validateStockCode("STOCK001", TEST_GROUP_POID, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsUnique());
        assertEquals("Stock code already exists", result.getMessage());
    }

    @Test
    void testValidateStockCode_WithExclude() {
        // Arrange
        when(stockMasterRepository.existsByStockCodeIgnoreCaseAndGroupPoidAndStockPoidNot(
                "STOCK001", TEST_GROUP_POID, TEST_STOCK_POID))
                .thenReturn(false);

        // Act
        ValidationResponse result = stockMasterService.validateStockCode("STOCK001", TEST_GROUP_POID, TEST_STOCK_POID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsUnique());
    }

    @Test
    void testValidateStockCode_Empty() {
        // Act
        ValidationResponse result = stockMasterService.validateStockCode("", TEST_GROUP_POID, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsUnique());
        assertEquals("Stock code cannot be empty", result.getMessage());
    }

    @Test
    void testValidateStockName_Success() {
        // Arrange
        when(stockMasterRepository.existsByStockNameAndGroupPoid("Test Stock", TEST_GROUP_POID))
                .thenReturn(false);

        // Act
        ValidationResponse result = stockMasterService.validateStockName("Test Stock", TEST_GROUP_POID, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsUnique());
        assertEquals("Stock name is available", result.getMessage());
    }

    @Test
    void testValidateStockName_AlreadyExists() {
        // Arrange
        when(stockMasterRepository.existsByStockNameAndGroupPoid("Test Stock", TEST_GROUP_POID))
                .thenReturn(true);

        // Act
        ValidationResponse result = stockMasterService.validateStockName("Test Stock", TEST_GROUP_POID, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.getIsUnique());
        assertEquals("Stock name already exists", result.getMessage());
    }

    @Test
    void testValidateStockName_WithExclude() {
        // Arrange
        when(stockMasterRepository.existsByStockNameAndGroupPoidAndStockPoidNot(
                "Test Stock", TEST_GROUP_POID, TEST_STOCK_POID))
                .thenReturn(false);

        // Act
        ValidationResponse result = stockMasterService.validateStockName("Test Stock", TEST_GROUP_POID, TEST_STOCK_POID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsUnique());
    }

    // ========== Check Dependencies Tests ==========

    @Test
    void testCheckStockMasterDependencies_Success() {
        // Arrange
        StockMasterEntity entity = createStockMasterEntity();
        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(entity));

        // Act
        StockMasterDependenciesDto result = stockMasterService.checkStockMasterDependencies(TEST_STOCK_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_STOCK_POID, result.getStockPoid());
        assertTrue(result.getCanDelete());
        assertEquals(0L, result.getStockBalanceCount());
        assertEquals(0L, result.getTransactionCount());
    }

    @Test
    void testCheckStockMasterDependencies_NotFound() {
        // Arrange
        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockMasterService.checkStockMasterDependencies(TEST_STOCK_POID, TEST_GROUP_POID);
        });
    }

    // ========== Delete Stock Master Tests ==========

    @Test
    void testDeleteStockMaster_Success() {
        // Arrange
        StockMasterEntity entity = createStockMasterEntity();
        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(entity));
        when(stockMasterRepository.save(any(StockMasterEntity.class))).thenReturn(entity);
        doNothing().when(dtlRepository).deleteByStockPoid(TEST_STOCK_POID);
        doNothing().when(warehouseRepository).deleteByStockPoid(TEST_STOCK_POID);

        // Act
        stockMasterService.deleteStockMaster(TEST_STOCK_POID, TEST_GROUP_POID);

        // Assert
        verify(dtlRepository, times(1)).deleteByStockPoid(TEST_STOCK_POID);
        verify(warehouseRepository, times(1)).deleteByStockPoid(TEST_STOCK_POID);
        verify(stockMasterRepository, times(1)).save(any(StockMasterEntity.class));
        ArgumentCaptor<StockMasterEntity> captor = ArgumentCaptor.forClass(StockMasterEntity.class);
        verify(stockMasterRepository).save(captor.capture());
        assertEquals("Y", captor.getValue().getDeleted());
    }

    @Test
    void testDeleteStockMaster_NotFound() {
        // Arrange
        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockMasterService.deleteStockMaster(TEST_STOCK_POID, TEST_GROUP_POID);
        });
    }

    // ========== Create Stock Master Tests ==========

    @Test
    void testCreateStockMaster_Success() {
        // Arrange
        CreateStockMasterRequest request = createCreateStockMasterRequest();
        StockMasterEntity savedEntity = createStockMasterEntity();
        savedEntity.setStockPoid(TEST_STOCK_POID);

        when(stockMasterRepository.saveAndFlush(any(StockMasterEntity.class))).thenReturn(savedEntity);
        when(stockMasterRepository.findByStockPoid(TEST_STOCK_POID)).thenReturn(Optional.of(savedEntity));

        // Act
        StockMasterDto result = stockMasterService.createStockMaster(
                request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        verify(stockMasterRepository, times(1)).saveAndFlush(any(StockMasterEntity.class));
        verify(stockMasterRepository, times(1)).findByStockPoid(TEST_STOCK_POID);
    }

    @Test
    void testCreateStockMaster_WithSupplierDetails() {
        // Arrange
        CreateStockMasterRequest request = createCreateStockMasterRequest();
        CreateStockMasterDtlRequest dtlRequest = new CreateStockMasterDtlRequest();
        dtlRequest.setSupplierPoid(50L);
        dtlRequest.setSupplierStockCode("SUP001");
        request.setSupplierDetails(Collections.singletonList(dtlRequest));

        StockMasterEntity savedEntity = createStockMasterEntity();
        savedEntity.setStockPoid(TEST_STOCK_POID);

        when(stockMasterRepository.saveAndFlush(any(StockMasterEntity.class))).thenReturn(savedEntity);
        when(stockMasterRepository.findByStockPoid(TEST_STOCK_POID)).thenReturn(Optional.of(savedEntity));
        when(dtlRepository.save(any(StockMasterDTLEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockMasterDto result = stockMasterService.createStockMaster(
                request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        verify(dtlRepository, times(1)).save(any(StockMasterDTLEntity.class));
        verify(dtlRepository, times(1)).flush();
    }

    @Test
    void testCreateStockMaster_WithWarehouseDetails() {
        // Arrange
        CreateStockMasterRequest request = createCreateStockMasterRequest();
        CreateStockMasterWarehouseDtlRequest whRequest = new CreateStockMasterWarehouseDtlRequest();
        whRequest.setLocationPoid(100L);
        whRequest.setAisleNo("A1");
        request.setWarehouseDetails(Collections.singletonList(whRequest));

        StockMasterEntity savedEntity = createStockMasterEntity();
        savedEntity.setStockPoid(TEST_STOCK_POID);

        when(stockMasterRepository.saveAndFlush(any(StockMasterEntity.class))).thenReturn(savedEntity);
        when(stockMasterRepository.findByStockPoid(TEST_STOCK_POID)).thenReturn(Optional.of(savedEntity));
        when(warehouseRepository.save(any(StockMasterWarehouseDtl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockMasterDto result = stockMasterService.createStockMaster(
                request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        verify(warehouseRepository, times(1)).save(any(StockMasterWarehouseDtl.class));
        verify(warehouseRepository, times(1)).flush();
    }

    @Test
    void testCreateStockMaster_NullStockName() {
        // Arrange
        CreateStockMasterRequest request = createCreateStockMasterRequest();
        request.setStockName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            stockMasterService.createStockMaster(request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);
        });
    }

    @Test
    void testCreateStockMaster_EmptyStockName() {
        // Arrange
        CreateStockMasterRequest request = createCreateStockMasterRequest();
        request.setStockName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            stockMasterService.createStockMaster(request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);
        });
    }

    @Test
    void testCreateStockMaster_NullCategoryPoid() {
        // Arrange
        CreateStockMasterRequest request = createCreateStockMasterRequest();
        request.setCategoryPoid(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            stockMasterService.createStockMaster(request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);
        });
    }

    @Test
    void testCreateStockMaster_NullStockUnitPoid() {
        // Arrange
        CreateStockMasterRequest request = createCreateStockMasterRequest();
        request.setStockUnitPoid(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            stockMasterService.createStockMaster(request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);
        });
    }

    @Test
    void testCreateStockMaster_NullTaxPoid() {
        // Arrange
        CreateStockMasterRequest request = createCreateStockMasterRequest();
        request.setTaxPoid(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            stockMasterService.createStockMaster(request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);
        });
    }

    @Test
    void testCreateStockMaster_NullInputTaxPoid() {
        // Arrange
        CreateStockMasterRequest request = createCreateStockMasterRequest();
        request.setInputTaxPoid(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            stockMasterService.createStockMaster(request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);
        });
    }

    // ========== Update Stock Master Tests ==========

    @Test
    void testUpdateStockMaster_Success() {
        // Arrange
        UpdateStockMasterRequest request = createUpdateStockMasterRequest();
        StockMasterEntity existingEntity = createStockMasterEntity();
        existingEntity.setDeleted("N");

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingEntity));
        when(stockMasterRepository.save(any(StockMasterEntity.class))).thenReturn(existingEntity);

        // Act
        StockMasterDto result = stockMasterService.updateStockMaster(
                TEST_STOCK_POID, request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        verify(stockMasterRepository, times(1)).save(any(StockMasterEntity.class));
    }

    @Test
    void testUpdateStockMaster_WithSupplierDetails() {
        // Arrange
        UpdateStockMasterRequest request = createUpdateStockMasterRequest();
        CreateStockMasterDtlRequest dtlRequest = new CreateStockMasterDtlRequest();
        dtlRequest.setSupplierPoid(50L);
        request.setSupplierDetails(Collections.singletonList(dtlRequest));

        StockMasterEntity existingEntity = createStockMasterEntity();
        existingEntity.setDeleted("N");

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingEntity));
        when(stockMasterRepository.save(any(StockMasterEntity.class))).thenReturn(existingEntity);
        doNothing().when(dtlRepository).deleteByStockPoid(TEST_STOCK_POID);
        when(dtlRepository.save(any(StockMasterDTLEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockMasterDto result = stockMasterService.updateStockMaster(
                TEST_STOCK_POID, request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        verify(dtlRepository, times(1)).deleteByStockPoid(TEST_STOCK_POID);
        verify(dtlRepository, times(1)).save(any(StockMasterDTLEntity.class));
    }

    @Test
    void testUpdateStockMaster_WithWarehouseDetails() {
        // Arrange
        UpdateStockMasterRequest request = createUpdateStockMasterRequest();
        CreateStockMasterWarehouseDtlRequest whRequest = new CreateStockMasterWarehouseDtlRequest();
        whRequest.setLocationPoid(100L);
        request.setWarehouseDetails(Collections.singletonList(whRequest));

        StockMasterEntity existingEntity = createStockMasterEntity();
        existingEntity.setDeleted("N");

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingEntity));
        when(stockMasterRepository.save(any(StockMasterEntity.class))).thenReturn(existingEntity);
        doNothing().when(warehouseRepository).deleteByStockPoid(TEST_STOCK_POID);
        when(warehouseRepository.save(any(StockMasterWarehouseDtl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockMasterDto result = stockMasterService.updateStockMaster(
                TEST_STOCK_POID, request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        verify(warehouseRepository, times(1)).deleteByStockPoid(TEST_STOCK_POID);
        verify(warehouseRepository, times(1)).save(any(StockMasterWarehouseDtl.class));
    }

    @Test
    void testUpdateStockMaster_ConsumablesLogic() {
        // Arrange
        UpdateStockMasterRequest request = createUpdateStockMasterRequest();
        request.setIsConsumables("N");
        request.setConsumptionQty(BigDecimal.valueOf(10));
        request.setConsumptionUnitPoid(1L);
        request.setMinimumRequiredQty(BigDecimal.valueOf(5));

        StockMasterEntity existingEntity = createStockMasterEntity();
        existingEntity.setDeleted("N");

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingEntity));
        when(stockMasterRepository.save(any(StockMasterEntity.class))).thenReturn(existingEntity);

        // Act
        StockMasterDto result = stockMasterService.updateStockMaster(
                TEST_STOCK_POID, request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        assertNull(request.getConsumptionQty());
        assertNull(request.getConsumptionUnitPoid());
        assertNull(request.getMinimumRequiredQty());
    }

    @Test
    void testUpdateStockMaster_NotFound() {
        // Arrange
        UpdateStockMasterRequest request = createUpdateStockMasterRequest();
        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockMasterService.updateStockMaster(TEST_STOCK_POID, request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);
        });
    }

    @Test
    void testUpdateStockMaster_Deleted() {
        // Arrange
        UpdateStockMasterRequest request = createUpdateStockMasterRequest();
        StockMasterEntity existingEntity = createStockMasterEntity();
        existingEntity.setDeleted("Y");

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(existingEntity));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            stockMasterService.updateStockMaster(TEST_STOCK_POID, request, TEST_GROUP_POID, TEST_COMPANY_POID, TEST_USER_ID);
        });
    }

    // ========== Supplier Detail Tests ==========

    @Test
    void testAddSupplierDetail_Success() {
        // Arrange
        CreateStockMasterDtlRequest request = new CreateStockMasterDtlRequest();
        request.setSupplierPoid(50L);
        request.setSupplierStockCode("SUP001");
        request.setRemarks("Test remarks");

        StockMasterEntity stock = createStockMasterEntity();
        stock.setDeleted("N");
        StockMasterDTLEntity savedDtl = createStockMasterDTLEntity();

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(stock));
        when(dtlRepository.findMaxDetRowIdByStockPoid(TEST_STOCK_POID)).thenReturn(null);
        when(dtlRepository.countByStockPoidAndRemarks(TEST_STOCK_POID, "Test remarks")).thenReturn(0L);
        when(dtlRepository.save(any(StockMasterDTLEntity.class))).thenReturn(savedDtl);

        // Act
        StockMasterDtlDto result = stockMasterService.addSupplierDetail(
                TEST_STOCK_POID, request, TEST_GROUP_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        verify(dtlRepository, times(1)).save(any(StockMasterDTLEntity.class));
    }

    @Test
    void testAddSupplierDetail_DuplicateRemarks() {
        // Arrange
        CreateStockMasterDtlRequest request = new CreateStockMasterDtlRequest();
        request.setRemarks("Duplicate remarks");

        StockMasterEntity stock = createStockMasterEntity();
        stock.setDeleted("N");

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(stock));
        when(dtlRepository.countByStockPoidAndRemarks(TEST_STOCK_POID, "Duplicate remarks")).thenReturn(1L);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            stockMasterService.addSupplierDetail(TEST_STOCK_POID, request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testAddSupplierDetail_StockDeleted() {
        // Arrange
        CreateStockMasterDtlRequest request = new CreateStockMasterDtlRequest();
        StockMasterEntity stock = createStockMasterEntity();
        stock.setDeleted("Y");

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(stock));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            stockMasterService.addSupplierDetail(TEST_STOCK_POID, request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testUpdateSupplierDetail_Success() {
        // Arrange
        CreateStockMasterDtlRequest request = new CreateStockMasterDtlRequest();
        request.setSupplierPoid(60L);
        request.setRemarks("Updated remarks");

        StockMasterEntity stock = createStockMasterEntity();
        stock.setDeleted("N");
        StockMasterDTLEntity existingDtl = createStockMasterDTLEntity();

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(stock));
        when(dtlRepository.findById(new StockMasterDtlId(TEST_STOCK_POID, TEST_DET_ROW_ID)))
                .thenReturn(Optional.of(existingDtl));
        when(dtlRepository.save(any(StockMasterDTLEntity.class))).thenReturn(existingDtl);

        // Act
        StockMasterDtlDto result = stockMasterService.updateSupplierDetail(
                TEST_STOCK_POID, TEST_DET_ROW_ID, request, TEST_GROUP_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        verify(dtlRepository, times(1)).save(any(StockMasterDTLEntity.class));
    }

    @Test
    void testUpdateSupplierDetail_NotFound() {
        // Arrange
        CreateStockMasterDtlRequest request = new CreateStockMasterDtlRequest();
        StockMasterEntity stock = createStockMasterEntity();
        stock.setDeleted("N");

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(stock));
        when(dtlRepository.findById(new StockMasterDtlId(TEST_STOCK_POID, TEST_DET_ROW_ID)))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockMasterService.updateSupplierDetail(TEST_STOCK_POID, TEST_DET_ROW_ID, request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testDeleteSupplierDetail_Success() {
        // Arrange
        StockMasterEntity stock = createStockMasterEntity();
        stock.setDeleted("N");
        StockMasterDTLEntity dtl = createStockMasterDTLEntity();

        when(stockMasterRepository.findByStockPoid(TEST_STOCK_POID))
                .thenReturn(Optional.of(stock));
        when(dtlRepository.findById(new StockMasterDtlId(TEST_STOCK_POID, TEST_DET_ROW_ID)))
                .thenReturn(Optional.of(dtl));
        doNothing().when(dtlRepository).delete(dtl);

        // Act
        stockMasterService.deleteSupplierDetail(TEST_STOCK_POID, TEST_DET_ROW_ID, TEST_GROUP_POID);

        // Assert
        verify(dtlRepository, times(1)).delete(dtl);
    }

    @Test
    void testDeleteSupplierDetail_GroupMismatch() {
        // Arrange
        StockMasterEntity stock = createStockMasterEntity();
        stock.setGroupPoid(999L);

        when(stockMasterRepository.findByStockPoid(TEST_STOCK_POID))
                .thenReturn(Optional.of(stock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            stockMasterService.deleteSupplierDetail(TEST_STOCK_POID, TEST_DET_ROW_ID, TEST_GROUP_POID);
        });
    }

    @Test
    void testGetSupplierDetails_Success() {
        // Arrange
        StockMasterEntity stock = createStockMasterEntity();
        StockMasterDTLEntity dtl = createStockMasterDTLEntity();

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(stock));
        when(dtlRepository.findByStockPoid(TEST_STOCK_POID))
                .thenReturn(Collections.singletonList(dtl));

        // Act
        List<StockMasterDtlDto> result = stockMasterService.getSupplierDetails(TEST_STOCK_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== Warehouse Detail Tests ==========

    @Test
    void testAddWarehouseDetail_Success() {
        // Arrange
        CreateStockMasterWarehouseDtlRequest request = new CreateStockMasterWarehouseDtlRequest();
        request.setLocationPoid(100L);
        request.setAisleNo("A1");

        StockMasterEntity stock = createStockMasterEntity();
        stock.setDeleted("N");
        StockMasterWarehouseDtl savedWh = createStockMasterWarehouseDtl();

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(stock));
        when(warehouseRepository.findMaxDetRowIdByStockPoid(TEST_STOCK_POID)).thenReturn(null);
        when(warehouseRepository.save(any(StockMasterWarehouseDtl.class))).thenReturn(savedWh);

        // Act
        StockMasterWarehouseDtlDto result = stockMasterService.addWarehouseDetail(
                TEST_STOCK_POID, request, TEST_GROUP_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        verify(warehouseRepository, times(1)).save(any(StockMasterWarehouseDtl.class));
    }

    @Test
    void testAddWarehouseDetail_StockDeleted() {
        // Arrange
        CreateStockMasterWarehouseDtlRequest request = new CreateStockMasterWarehouseDtlRequest();
        StockMasterEntity stock = createStockMasterEntity();
        stock.setDeleted("Y");

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(stock));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            stockMasterService.addWarehouseDetail(TEST_STOCK_POID, request, TEST_GROUP_POID, TEST_USER_ID);
        });
    }

    @Test
    void testUpdateWarehouseDetail_Success() {
        // Arrange
        CreateStockMasterWarehouseDtlRequest request = new CreateStockMasterWarehouseDtlRequest();
        request.setLocationPoid(100L);
        request.setAisleNo("A2");

        StockMasterEntity stock = createStockMasterEntity();
        stock.setDeleted("N");
        StockMasterWarehouseDtl existingWh = createStockMasterWarehouseDtl();

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(stock));
        when(warehouseRepository.findById(new StockMasterWarehouseDtlId(TEST_STOCK_POID, TEST_DET_ROW_ID)))
                .thenReturn(Optional.of(existingWh));
        when(warehouseRepository.save(any(StockMasterWarehouseDtl.class))).thenReturn(existingWh);

        // Act
        StockMasterWarehouseDtlDto result = stockMasterService.updateWarehouseDetail(
                TEST_STOCK_POID, TEST_DET_ROW_ID, request, TEST_GROUP_POID, TEST_USER_ID);

        // Assert
        assertNotNull(result);
        verify(warehouseRepository, times(1)).save(any(StockMasterWarehouseDtl.class));
    }

    @Test
    void testDeleteWarehouseDetail_Success() {
        // Arrange
        StockMasterEntity stock = createStockMasterEntity();
        stock.setDeleted("N");

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(stock));
        doNothing().when(warehouseRepository).deleteById(new StockMasterWarehouseDtlId(TEST_STOCK_POID, TEST_DET_ROW_ID));

        // Act
        stockMasterService.deleteWarehouseDetail(TEST_STOCK_POID, TEST_DET_ROW_ID, TEST_GROUP_POID);

        // Assert
        verify(warehouseRepository, times(1)).deleteById(any(StockMasterWarehouseDtlId.class));
    }

    @Test
    void testGetWarehouseDetails_Success() {
        // Arrange
        StockMasterEntity stock = createStockMasterEntity();
        StockMasterWarehouseDtl wh = createStockMasterWarehouseDtl();

        when(stockMasterRepository.findByStockPoidAndGroupPoid(TEST_STOCK_POID, TEST_GROUP_POID))
                .thenReturn(Optional.of(stock));
        when(warehouseRepository.findByStockPoid(TEST_STOCK_POID))
                .thenReturn(Collections.singletonList(wh));

        // Act
        List<StockMasterWarehouseDtlDto> result = stockMasterService.getWarehouseDetails(TEST_STOCK_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ========== Get Stock by Barcode Tests ==========

    @Test
    void testGetStockMasterByBarcode_Success() {
        // Arrange
        StockMasterEntity entity = createStockMasterEntity();
        entity.setBarcode("123456789");

        when(stockMasterRepository.findByBarcodeOrSupplierBarcode("123456789", TEST_GROUP_POID))
                .thenReturn(Optional.of(entity));

        // Act
        StockMasterDto result = stockMasterService.getStockMasterByBarcode("123456789", TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        verify(stockMasterRepository, times(1)).findByBarcodeOrSupplierBarcode("123456789", TEST_GROUP_POID);
    }

    @Test
    void testGetStockMasterByBarcode_NotFound() {
        // Arrange
        when(stockMasterRepository.findByBarcodeOrSupplierBarcode("123456789", TEST_GROUP_POID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockMasterService.getStockMasterByBarcode("123456789", TEST_GROUP_POID);
        });
    }

    // ========== Helper Methods ==========

    private StockMasterEntity createStockMasterEntity() {
        StockMasterEntity entity = new StockMasterEntity();
        entity.setStockPoid(TEST_STOCK_POID);
        entity.setStockCode("STOCK001");
        entity.setStockName("Test Stock");
        entity.setCategoryPoid(TEST_CATEGORY_POID);
        entity.setStockUnitPoid(1L);
        entity.setTaxPoid(1L);
        entity.setInputTaxPoid(1L);
        entity.setGroupPoid(TEST_GROUP_POID);
        entity.setActive("Y");
        entity.setDeleted("N");
        entity.setCreatedBy(TEST_USER_ID);
        return entity;
    }

    private StockMasterDTLEntity createStockMasterDTLEntity() {
        StockMasterDTLEntity entity = new StockMasterDTLEntity();
        entity.setStockPoid(TEST_STOCK_POID);
        entity.setDetRowId(TEST_DET_ROW_ID);
        entity.setSupplierPoid(50L);
        entity.setSupplierStockCode("SUP001");
        entity.setRemarks("Test remarks");
        return entity;
    }

    private StockMasterWarehouseDtl createStockMasterWarehouseDtl() {
        StockMasterWarehouseDtl entity = new StockMasterWarehouseDtl();
        entity.setStockPoid(TEST_STOCK_POID);
        entity.setDetRowId(TEST_DET_ROW_ID);
        entity.setLocationPoid(100L);
        entity.setAisleNo("A1");
        entity.setBayNo("B1");
        return entity;
    }

    private StockCategoryMasterEntity createStockCategoryMasterEntity() {
        StockCategoryMasterEntity entity = new StockCategoryMasterEntity();
        entity.setCategoryPoid(TEST_CATEGORY_POID);
        entity.setCategoryCode("CAT001");
        entity.setCategoryName("Test Category");
        entity.setGroupPoid(TEST_GROUP_POID);
        return entity;
    }

    private CreateStockMasterRequest createCreateStockMasterRequest() {
        CreateStockMasterRequest request = new CreateStockMasterRequest();
        request.setStockName("Test Stock");
        request.setCategoryPoid(TEST_CATEGORY_POID);
        request.setStockUnitPoid(1L);
        request.setTaxPoid(1L);
        request.setInputTaxPoid(1L);
        request.setActive("Y");
        return request;
    }

    private UpdateStockMasterRequest createUpdateStockMasterRequest() {
        UpdateStockMasterRequest request = new UpdateStockMasterRequest();
        request.setStockName("Updated Stock");
        request.setCategoryPoid(TEST_CATEGORY_POID);
        request.setStockUnitPoid(1L);
        request.setTaxPoid(1L);
        request.setInputTaxPoid(1L);
        return request;
    }
}

