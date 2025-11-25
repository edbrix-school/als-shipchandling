package com.alsharif.shipchandling.stockunitmaster.service;

import com.alsharif.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import com.alsharif.shipchandling.group.entity.GroupEntity;
import com.alsharif.shipchandling.group.repository.GroupRepository;
import com.alsharif.shipchandling.stockunitmaster.dto.StockUnitMasterDto;
import com.alsharif.shipchandling.stockunitmaster.dto.UnitDependenciesDto;
import com.alsharif.shipchandling.stockunitmaster.entity.StockUnitMaster;
import com.alsharif.shipchandling.stockunitmaster.repository.StockUnitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockUnitServiceImplTest {

    @Mock
    private StockUnitRepository stockUnitRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private StockUnitServiceImpl stockUnitService;

    private StockUnitMaster stockUnitMaster;
    private StockUnitMasterDto stockUnitMasterDto;
    private GroupEntity group;
    private static final Long TEST_STOCK_UNIT_POID = 1L;
    private static final Long TEST_GROUP_POID = 100L;
    private static final String TEST_STOCK_UNIT_CODE = "KG";
    private static final String TEST_STOCK_UNIT_NAME = "Kilogram";

    @BeforeEach
    void setUp() {
        // Setup Group
        group = new GroupEntity();
        group.setGroupPoid(TEST_GROUP_POID);
        group.setGroupName("Test Group");

        // Setup StockUnitMaster entity
        stockUnitMaster = new StockUnitMaster();
        stockUnitMaster.setStockUnitPoid(TEST_STOCK_UNIT_POID);
        stockUnitMaster.setStockUnitCode(TEST_STOCK_UNIT_CODE);
        stockUnitMaster.setStockUnitName(TEST_STOCK_UNIT_NAME);
        stockUnitMaster.setStockUnitName2("Kilogram");
        stockUnitMaster.setGroupPoid(TEST_GROUP_POID);
        stockUnitMaster.setActive("Y");
        stockUnitMaster.setDeleted("N");
        stockUnitMaster.setSeqNo(1);
        stockUnitMaster.setClassified("Y");
        stockUnitMaster.setCreatedBy("testUser");
        stockUnitMaster.setCreatedDate(LocalDateTime.now());
        stockUnitMaster.setLastModifiedBy("testUser");
        stockUnitMaster.setLastModifiedDate(LocalDateTime.now());

        // Setup StockUnitMasterDto
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
    }

    // ========== getStockUnitByPoid Tests ==========

    @Test
    void testGetStockUnitByPoid_Success() {
        // Arrange
        when(stockUnitRepository.existsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(true);
        when(stockUnitRepository.findByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(stockUnitMaster);

        // Act
        StockUnitMasterDto result = stockUnitService.getStockUnitByPoid(TEST_STOCK_UNIT_POID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_STOCK_UNIT_POID, result.getStockUnitPoid());
        assertEquals(TEST_STOCK_UNIT_CODE, result.getStockUnitCode());
        assertEquals(TEST_STOCK_UNIT_NAME, result.getStockUnitName());
        verify(stockUnitRepository, times(1)).existsByStockUnitPoid(TEST_STOCK_UNIT_POID);
        verify(stockUnitRepository, times(1)).findByStockUnitPoid(TEST_STOCK_UNIT_POID);
    }

    @Test
    void testGetStockUnitByPoid_NotFound() {
        // Arrange
        when(stockUnitRepository.existsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockUnitService.getStockUnitByPoid(TEST_STOCK_UNIT_POID);
        });

        verify(stockUnitRepository, times(1)).existsByStockUnitPoid(TEST_STOCK_UNIT_POID);
        verify(stockUnitRepository, never()).findByStockUnitPoid(any());
    }

    // ========== createStockUnit Tests ==========

    @Test
    void testCreateStockUnit_Success() {
        // Arrange
        stockUnitMasterDto.setStockUnitPoid(null); // New entity
        when(stockUnitRepository.existsByStockUnitCode(TEST_STOCK_UNIT_CODE)).thenReturn(false);
        when(stockUnitRepository.existsByStockUnitName(TEST_STOCK_UNIT_NAME)).thenReturn(false);
        when(groupRepository.findById(TEST_GROUP_POID)).thenReturn(Optional.of(group));
        when(stockUnitRepository.save(any(StockUnitMaster.class))).thenAnswer(invocation -> {
            StockUnitMaster entity = invocation.getArgument(0);
            entity.setStockUnitPoid(TEST_STOCK_UNIT_POID);
            return entity;
        });

        // Act
        StockUnitMasterDto result = stockUnitService.createStockUnit(stockUnitMasterDto);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_STOCK_UNIT_CODE, result.getStockUnitCode());
        assertEquals(TEST_STOCK_UNIT_NAME, result.getStockUnitName());
        verify(stockUnitRepository, times(1)).existsByStockUnitCode(TEST_STOCK_UNIT_CODE);
        verify(stockUnitRepository, times(1)).existsByStockUnitName(TEST_STOCK_UNIT_NAME);
        verify(groupRepository, times(1)).findById(TEST_GROUP_POID);
        verify(stockUnitRepository, times(1)).save(any(StockUnitMaster.class));
    }

    @Test
    void testCreateStockUnit_DuplicateCode() {
        // Arrange
        when(stockUnitRepository.existsByStockUnitCode(TEST_STOCK_UNIT_CODE)).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            stockUnitService.createStockUnit(stockUnitMasterDto);
        });

        verify(stockUnitRepository, times(1)).existsByStockUnitCode(TEST_STOCK_UNIT_CODE);
        verify(stockUnitRepository, never()).existsByStockUnitName(any());
        verify(stockUnitRepository, never()).save(any());
    }

    @Test
    void testCreateStockUnit_DuplicateName() {
        // Arrange
        when(stockUnitRepository.existsByStockUnitCode(TEST_STOCK_UNIT_CODE)).thenReturn(false);
        when(stockUnitRepository.existsByStockUnitName(TEST_STOCK_UNIT_NAME)).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            stockUnitService.createStockUnit(stockUnitMasterDto);
        });

        verify(stockUnitRepository, times(1)).existsByStockUnitCode(TEST_STOCK_UNIT_CODE);
        verify(stockUnitRepository, times(1)).existsByStockUnitName(TEST_STOCK_UNIT_NAME);
        verify(stockUnitRepository, never()).save(any());
    }

    @Test
    void testCreateStockUnit_GroupNotFound() {
        // Arrange
        when(stockUnitRepository.existsByStockUnitCode(TEST_STOCK_UNIT_CODE)).thenReturn(false);
        when(stockUnitRepository.existsByStockUnitName(TEST_STOCK_UNIT_NAME)).thenReturn(false);
        when(groupRepository.findById(TEST_GROUP_POID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockUnitService.createStockUnit(stockUnitMasterDto);
        });

        verify(stockUnitRepository, times(1)).existsByStockUnitCode(TEST_STOCK_UNIT_CODE);
        verify(stockUnitRepository, times(1)).existsByStockUnitName(TEST_STOCK_UNIT_NAME);
        verify(groupRepository, times(1)).findById(TEST_GROUP_POID);
        verify(stockUnitRepository, never()).save(any());
    }

    // ========== updateStockUnit Tests ==========

    @Test
    void testUpdateStockUnit_Success() {
        // Arrange
        stockUnitMasterDto.setStockUnitPoid(TEST_STOCK_UNIT_POID);
        stockUnitMasterDto.setStockUnitName("Updated Name");
        stockUnitMasterDto.setActive("N");

        when(stockUnitRepository.findByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(stockUnitMaster);
        when(stockUnitRepository.existsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(true);
        when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot(
                TEST_STOCK_UNIT_CODE, TEST_STOCK_UNIT_POID)).thenReturn(false);
        when(stockUnitRepository.existsByStockUnitNameIgnoreCaseAndStockUnitPoidNot(
                "Updated Name", TEST_STOCK_UNIT_POID)).thenReturn(false);
        when(groupRepository.findById(TEST_GROUP_POID)).thenReturn(Optional.of(group));
        when(stockUnitRepository.save(any(StockUnitMaster.class))).thenAnswer(invocation -> {
            StockUnitMaster entity = invocation.getArgument(0);
            entity.setLastModifiedDate(LocalDateTime.now());
            return entity;
        });

        // Act
        StockUnitMasterDto result = stockUnitService.updateStockUnit(TEST_STOCK_UNIT_POID, stockUnitMasterDto);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_STOCK_UNIT_POID, result.getStockUnitPoid());
        verify(stockUnitRepository, times(1)).existsByStockUnitPoid(TEST_STOCK_UNIT_POID);
        verify(stockUnitRepository, times(1)).save(any(StockUnitMaster.class));
    }

    @Test
    void testUpdateStockUnit_NotFound() {
        // Arrange
        when(stockUnitRepository.existsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockUnitService.updateStockUnit(TEST_STOCK_UNIT_POID, stockUnitMasterDto);
        });

        verify(stockUnitRepository, times(1)).existsByStockUnitPoid(TEST_STOCK_UNIT_POID);
        verify(stockUnitRepository, never()).save(any());
    }

    @Test
    void testUpdateStockUnit_DuplicateCode() {
        // Arrange
        when(stockUnitRepository.findByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(stockUnitMaster);
        when(stockUnitRepository.existsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(true);
        when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot(
                TEST_STOCK_UNIT_CODE, TEST_STOCK_UNIT_POID)).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            stockUnitService.updateStockUnit(TEST_STOCK_UNIT_POID, stockUnitMasterDto);
        });

        verify(stockUnitRepository, times(1)).existsByStockUnitPoid(TEST_STOCK_UNIT_POID);
        verify(stockUnitRepository, never()).save(any());
    }

    @Test
    void testUpdateStockUnit_DuplicateName() {
        // Arrange
        stockUnitMasterDto.setStockUnitName("Duplicate Name");
        when(stockUnitRepository.findByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(stockUnitMaster);
        when(stockUnitRepository.existsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(true);
        when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot(
                TEST_STOCK_UNIT_CODE, TEST_STOCK_UNIT_POID)).thenReturn(false);
        when(stockUnitRepository.existsByStockUnitNameIgnoreCaseAndStockUnitPoidNot(
                "Duplicate Name", TEST_STOCK_UNIT_POID)).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> {
            stockUnitService.updateStockUnit(TEST_STOCK_UNIT_POID, stockUnitMasterDto);
        });

        verify(stockUnitRepository, times(1)).existsByStockUnitPoid(TEST_STOCK_UNIT_POID);
        verify(stockUnitRepository, never()).save(any());
    }

    @Test
    void testUpdateStockUnit_GroupNotFound() {
        // Arrange
        when(stockUnitRepository.findByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(stockUnitMaster);
        when(stockUnitRepository.existsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(true);
        when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot(
                TEST_STOCK_UNIT_CODE, TEST_STOCK_UNIT_POID)).thenReturn(false);
        when(stockUnitRepository.existsByStockUnitNameIgnoreCaseAndStockUnitPoidNot(
                TEST_STOCK_UNIT_NAME, TEST_STOCK_UNIT_POID)).thenReturn(false);
        when(groupRepository.findById(TEST_GROUP_POID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockUnitService.updateStockUnit(TEST_STOCK_UNIT_POID, stockUnitMasterDto);
        });

        verify(stockUnitRepository, times(1)).existsByStockUnitPoid(TEST_STOCK_UNIT_POID);
        verify(stockUnitRepository, never()).save(any());
    }

    @Test
    void testUpdateStockUnit_PartialUpdate() {
        // Arrange
        stockUnitMasterDto.setStockUnitName(null); // Don't update name
        stockUnitMasterDto.setActive("N");
        stockUnitMasterDto.setSeqNo(5);

        when(stockUnitRepository.findByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(stockUnitMaster);
        when(stockUnitRepository.existsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(true);
        when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot(
                TEST_STOCK_UNIT_CODE, TEST_STOCK_UNIT_POID)).thenReturn(false);
        // When stockUnitName is null, the validation method is still called with null
        when(stockUnitRepository.existsByStockUnitNameIgnoreCaseAndStockUnitPoidNot(
                isNull(), eq(TEST_STOCK_UNIT_POID))).thenReturn(false);
        when(groupRepository.findById(TEST_GROUP_POID)).thenReturn(Optional.of(group));
        when(stockUnitRepository.save(any(StockUnitMaster.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StockUnitMasterDto result = stockUnitService.updateStockUnit(TEST_STOCK_UNIT_POID, stockUnitMasterDto);

        // Assert
        assertNotNull(result);
        verify(stockUnitRepository, times(1)).save(any(StockUnitMaster.class));
    }

    // ========== softDeleteStockUnit Tests ==========

    @Test
    void testSoftDeleteStockUnit_Success() {
        // Arrange
        when(stockUnitRepository.findByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(stockUnitMaster);
        when(stockUnitRepository.existsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(true);
        when(stockUnitRepository.save(any(StockUnitMaster.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        stockUnitService.softDeleteStockUnit(TEST_STOCK_UNIT_POID);

        // Assert
        verify(stockUnitRepository, times(1)).existsByStockUnitPoid(TEST_STOCK_UNIT_POID);
        verify(stockUnitRepository, times(1)).findByStockUnitPoid(TEST_STOCK_UNIT_POID);
        verify(stockUnitRepository, times(1)).save(any(StockUnitMaster.class));
        assertEquals("Y", stockUnitMaster.getDeleted());
        assertEquals("N", stockUnitMaster.getActive());
    }

    @Test
    void testSoftDeleteStockUnit_NotFound() {
        // Arrange
        when(stockUnitRepository.existsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockUnitService.softDeleteStockUnit(TEST_STOCK_UNIT_POID);
        });

        verify(stockUnitRepository, times(1)).existsByStockUnitPoid(TEST_STOCK_UNIT_POID);
        verify(stockUnitRepository, never()).save(any());
    }

    // ========== listStockUnitsUsingParams Tests ==========

    @Test
    void testListStockUnitsUsingParams_AllParams() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<StockUnitMaster> units = new ArrayList<>();
        units.add(stockUnitMaster);
        Page<StockUnitMaster> page = new PageImpl<>(units, pageable, 1);

        when(stockUnitRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<StockUnitMasterDto> result = stockUnitService.listStockUnitsUsingParams(
                TEST_STOCK_UNIT_CODE, TEST_STOCK_UNIT_NAME, "Y", "Y", "N", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(stockUnitRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testListStockUnitsUsingParams_NoParams() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<StockUnitMaster> units = new ArrayList<>();
        Page<StockUnitMaster> page = new PageImpl<>(units, pageable, 0);

        when(stockUnitRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<StockUnitMasterDto> result = stockUnitService.listStockUnitsUsingParams(
                null, null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(stockUnitRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testListStockUnitsUsingParams_EmptyStrings() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<StockUnitMaster> units = new ArrayList<>();
        Page<StockUnitMaster> page = new PageImpl<>(units, pageable, 0);

        when(stockUnitRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<StockUnitMasterDto> result = stockUnitService.listStockUnitsUsingParams(
                "", "", "", "", "", pageable);

        // Assert
        assertNotNull(result);
        verify(stockUnitRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testListStockUnitsUsingParams_PartialParams() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<StockUnitMaster> units = new ArrayList<>();
        units.add(stockUnitMaster);
        Page<StockUnitMaster> page = new PageImpl<>(units, pageable, 1);

        when(stockUnitRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // Act
        Page<StockUnitMasterDto> result = stockUnitService.listStockUnitsUsingParams(
                TEST_STOCK_UNIT_CODE, null, null, "Y", null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(stockUnitRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    // ========== validateStockUnitCode Tests ==========

    @Test
    void testValidateStockUnitCode_Unique_ForCreate() {
        // Arrange
        when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndGroupPoid(
                TEST_STOCK_UNIT_CODE, TEST_GROUP_POID)).thenReturn(false);

        // Act
        boolean result = stockUnitService.validateStockUnitCode(TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, null);

        // Assert
        assertTrue(result);
        verify(stockUnitRepository, times(1)).existsByStockUnitCodeIgnoreCaseAndGroupPoid(
                TEST_STOCK_UNIT_CODE, TEST_GROUP_POID);
    }

    @Test
    void testValidateStockUnitCode_Duplicate_ForCreate() {
        // Arrange
        when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndGroupPoid(
                TEST_STOCK_UNIT_CODE, TEST_GROUP_POID)).thenReturn(true);

        // Act
        boolean result = stockUnitService.validateStockUnitCode(TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, null);

        // Assert
        assertFalse(result);
        verify(stockUnitRepository, times(1)).existsByStockUnitCodeIgnoreCaseAndGroupPoid(
                TEST_STOCK_UNIT_CODE, TEST_GROUP_POID);
    }

    @Test
    void testValidateStockUnitCode_Unique_ForUpdate() {
        // Arrange
        when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndGroupPoidAndStockUnitPoidNot(
                TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, TEST_STOCK_UNIT_POID)).thenReturn(false);

        // Act
        boolean result = stockUnitService.validateStockUnitCode(
                TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, TEST_STOCK_UNIT_POID);

        // Assert
        assertTrue(result);
        verify(stockUnitRepository, times(1)).existsByStockUnitCodeIgnoreCaseAndGroupPoidAndStockUnitPoidNot(
                TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, TEST_STOCK_UNIT_POID);
    }

    @Test
    void testValidateStockUnitCode_Duplicate_ForUpdate() {
        // Arrange
        when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndGroupPoidAndStockUnitPoidNot(
                TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, TEST_STOCK_UNIT_POID)).thenReturn(true);

        // Act
        boolean result = stockUnitService.validateStockUnitCode(
                TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, TEST_STOCK_UNIT_POID);

        // Assert
        assertFalse(result);
        verify(stockUnitRepository, times(1)).existsByStockUnitCodeIgnoreCaseAndGroupPoidAndStockUnitPoidNot(
                TEST_STOCK_UNIT_CODE, TEST_GROUP_POID, TEST_STOCK_UNIT_POID);
    }

    // ========== validateStockUnitName Tests ==========

    @Test
    void testValidateStockUnitName_Unique_ForCreate() {
        // Arrange
        when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid(
                TEST_STOCK_UNIT_NAME, TEST_GROUP_POID)).thenReturn(false);

        // Act
        boolean result = stockUnitService.validateStockUnitName(TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, null);

        // Assert
        assertTrue(result);
        verify(stockUnitRepository, times(1)).existsBystockUnitNameIgnoreCaseAndGroupPoid(
                TEST_STOCK_UNIT_NAME, TEST_GROUP_POID);
    }

    @Test
    void testValidateStockUnitName_Duplicate_ForCreate() {
        // Arrange
        when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid(
                TEST_STOCK_UNIT_NAME, TEST_GROUP_POID)).thenReturn(true);

        // Act
        boolean result = stockUnitService.validateStockUnitName(TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, null);

        // Assert
        assertFalse(result);
        verify(stockUnitRepository, times(1)).existsBystockUnitNameIgnoreCaseAndGroupPoid(
                TEST_STOCK_UNIT_NAME, TEST_GROUP_POID);
    }

    @Test
    void testValidateStockUnitName_Unique_ForUpdate() {
        // Arrange
        when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoidAndStockUnitPoidNot(
                TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, TEST_STOCK_UNIT_POID)).thenReturn(false);

        // Act
        boolean result = stockUnitService.validateStockUnitName(
                TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, TEST_STOCK_UNIT_POID);

        // Assert
        assertTrue(result);
        verify(stockUnitRepository, times(1)).existsBystockUnitNameIgnoreCaseAndGroupPoidAndStockUnitPoidNot(
                TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, TEST_STOCK_UNIT_POID);
    }

    @Test
    void testValidateStockUnitName_Duplicate_ForUpdate() {
        // Arrange
        when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoidAndStockUnitPoidNot(
                TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, TEST_STOCK_UNIT_POID)).thenReturn(true);

        // Act
        boolean result = stockUnitService.validateStockUnitName(
                TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, TEST_STOCK_UNIT_POID);

        // Assert
        assertFalse(result);
        verify(stockUnitRepository, times(1)).existsBystockUnitNameIgnoreCaseAndGroupPoidAndStockUnitPoidNot(
                TEST_STOCK_UNIT_NAME, TEST_GROUP_POID, TEST_STOCK_UNIT_POID);
    }

    // ========== checkUnitDependencies Tests ==========

    @Test
    void testCheckUnitDependencies_NoDependencies() {
        // Arrange
        when(stockUnitRepository.findByStockUnitPoidAndGroupPoid(
                TEST_STOCK_UNIT_POID, TEST_GROUP_POID)).thenReturn(Optional.of(stockUnitMaster));
        when(stockUnitRepository.countStockItemsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(0L);

        // Act
        UnitDependenciesDto result = stockUnitService.checkUnitDependencies(TEST_STOCK_UNIT_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_STOCK_UNIT_POID, result.getStockUnitPoid());
        assertEquals(0L, result.getStockItemCount());
        assertTrue(result.getCanDelete());
        assertEquals("No dependencies", result.getReason());
        verify(stockUnitRepository, times(1)).findByStockUnitPoidAndGroupPoid(
                TEST_STOCK_UNIT_POID, TEST_GROUP_POID);
        verify(stockUnitRepository, times(1)).countStockItemsByStockUnitPoid(TEST_STOCK_UNIT_POID);
    }

    @Test
    void testCheckUnitDependencies_WithDependencies() {
        // Arrange
        when(stockUnitRepository.findByStockUnitPoidAndGroupPoid(
                TEST_STOCK_UNIT_POID, TEST_GROUP_POID)).thenReturn(Optional.of(stockUnitMaster));
        when(stockUnitRepository.countStockItemsByStockUnitPoid(TEST_STOCK_UNIT_POID)).thenReturn(5L);

        // Act
        UnitDependenciesDto result = stockUnitService.checkUnitDependencies(TEST_STOCK_UNIT_POID, TEST_GROUP_POID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_STOCK_UNIT_POID, result.getStockUnitPoid());
        assertEquals(5L, result.getStockItemCount());
        assertFalse(result.getCanDelete());
        assertEquals("Unit has dependencies", result.getReason());
        assertTrue(result.getMessage().contains("5"));
        verify(stockUnitRepository, times(1)).findByStockUnitPoidAndGroupPoid(
                TEST_STOCK_UNIT_POID, TEST_GROUP_POID);
        verify(stockUnitRepository, times(1)).countStockItemsByStockUnitPoid(TEST_STOCK_UNIT_POID);
    }

    @Test
    void testCheckUnitDependencies_UnitNotFound() {
        // Arrange
        when(stockUnitRepository.findByStockUnitPoidAndGroupPoid(
                TEST_STOCK_UNIT_POID, TEST_GROUP_POID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            stockUnitService.checkUnitDependencies(TEST_STOCK_UNIT_POID, TEST_GROUP_POID);
        });

        verify(stockUnitRepository, times(1)).findByStockUnitPoidAndGroupPoid(
                TEST_STOCK_UNIT_POID, TEST_GROUP_POID);
        verify(stockUnitRepository, never()).countStockItemsByStockUnitPoid(any());
    }

    // ========== getActiveStockUnits Tests ==========

    @Test
    void testGetActiveStockUnits_AllUnits() {
        // Arrange
        List<StockUnitMaster> units = new ArrayList<>();
        units.add(stockUnitMaster);
        when(stockUnitRepository.findActiveUnitsByGroupPoid(TEST_GROUP_POID)).thenReturn(units);

        // Act
        List<StockUnitMasterDto> result = stockUnitService.getActiveStockUnits(TEST_GROUP_POID, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_STOCK_UNIT_POID, result.get(0).getStockUnitPoid());
        verify(stockUnitRepository, times(1)).findActiveUnitsByGroupPoid(TEST_GROUP_POID);
    }

    @Test
    void testGetActiveStockUnits_WithClassifiedFilter() {
        // Arrange
        List<StockUnitMaster> units = new ArrayList<>();
        units.add(stockUnitMaster);
        when(stockUnitRepository.findActiveUnitsByGroupPoid(TEST_GROUP_POID)).thenReturn(units);

        // Act
        List<StockUnitMasterDto> result = stockUnitService.getActiveStockUnits(TEST_GROUP_POID, "Y", null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stockUnitRepository, times(1)).findActiveUnitsByGroupPoid(TEST_GROUP_POID);
    }

    @Test
    void testGetActiveStockUnits_WithSearchFilter() {
        // Arrange
        List<StockUnitMaster> units = new ArrayList<>();
        units.add(stockUnitMaster);
        when(stockUnitRepository.findActiveUnitsByGroupPoid(TEST_GROUP_POID)).thenReturn(units);

        // Act
        List<StockUnitMasterDto> result = stockUnitService.getActiveStockUnits(TEST_GROUP_POID, null, "KG");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stockUnitRepository, times(1)).findActiveUnitsByGroupPoid(TEST_GROUP_POID);
    }

    @Test
    void testGetActiveStockUnits_WithSearchFilter_NoMatch() {
        // Arrange
        List<StockUnitMaster> units = new ArrayList<>();
        units.add(stockUnitMaster);
        when(stockUnitRepository.findActiveUnitsByGroupPoid(TEST_GROUP_POID)).thenReturn(units);

        // Act
        List<StockUnitMasterDto> result = stockUnitService.getActiveStockUnits(TEST_GROUP_POID, null, "XYZ");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(stockUnitRepository, times(1)).findActiveUnitsByGroupPoid(TEST_GROUP_POID);
    }

    @Test
    void testGetActiveStockUnits_WithClassifiedFilter_NoMatch() {
        // Arrange
        List<StockUnitMaster> units = new ArrayList<>();
        units.add(stockUnitMaster);
        when(stockUnitRepository.findActiveUnitsByGroupPoid(TEST_GROUP_POID)).thenReturn(units);

        // Act
        List<StockUnitMasterDto> result = stockUnitService.getActiveStockUnits(TEST_GROUP_POID, "N", null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(stockUnitRepository, times(1)).findActiveUnitsByGroupPoid(TEST_GROUP_POID);
    }

    @Test
    void testGetActiveStockUnits_EmptyList() {
        // Arrange
        when(stockUnitRepository.findActiveUnitsByGroupPoid(TEST_GROUP_POID)).thenReturn(new ArrayList<>());

        // Act
        List<StockUnitMasterDto> result = stockUnitService.getActiveStockUnits(TEST_GROUP_POID, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(stockUnitRepository, times(1)).findActiveUnitsByGroupPoid(TEST_GROUP_POID);
    }

    @Test
    void testGetActiveStockUnits_SearchByName() {
        // Arrange
        List<StockUnitMaster> units = new ArrayList<>();
        units.add(stockUnitMaster);
        when(stockUnitRepository.findActiveUnitsByGroupPoid(TEST_GROUP_POID)).thenReturn(units);

        // Act
        List<StockUnitMasterDto> result = stockUnitService.getActiveStockUnits(TEST_GROUP_POID, null, "Kilo");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stockUnitRepository, times(1)).findActiveUnitsByGroupPoid(TEST_GROUP_POID);
    }

    @Test
    void testGetActiveStockUnits_SearchByCode() {
        // Arrange
        List<StockUnitMaster> units = new ArrayList<>();
        units.add(stockUnitMaster);
        when(stockUnitRepository.findActiveUnitsByGroupPoid(TEST_GROUP_POID)).thenReturn(units);

        // Act
        List<StockUnitMasterDto> result = stockUnitService.getActiveStockUnits(TEST_GROUP_POID, null, "kg");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stockUnitRepository, times(1)).findActiveUnitsByGroupPoid(TEST_GROUP_POID);
    }

    @Test
    void testGetActiveStockUnits_EmptySearch() {
        // Arrange
        List<StockUnitMaster> units = new ArrayList<>();
        units.add(stockUnitMaster);
        when(stockUnitRepository.findActiveUnitsByGroupPoid(TEST_GROUP_POID)).thenReturn(units);

        // Act
        List<StockUnitMasterDto> result = stockUnitService.getActiveStockUnits(TEST_GROUP_POID, null, "");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(stockUnitRepository, times(1)).findActiveUnitsByGroupPoid(TEST_GROUP_POID);
    }
}

