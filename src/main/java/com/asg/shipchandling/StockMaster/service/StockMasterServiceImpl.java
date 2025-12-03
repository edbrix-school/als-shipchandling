package com.asg.shipchandling.StockMaster.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.asg.shipchandling.StockMaster.dto.CreateStockMasterDtlRequest;
import com.asg.shipchandling.StockMaster.dto.CreateStockMasterRequest;
import com.asg.shipchandling.StockMaster.dto.CreateStockMasterWarehouseDtlRequest;
import com.asg.shipchandling.StockMaster.dto.StockMasterDependenciesDto;
import com.asg.shipchandling.StockMaster.dto.StockMasterDtlDto;
import com.asg.shipchandling.StockMaster.dto.StockMasterDto;
import com.asg.shipchandling.StockMaster.dto.StockDetailsResponse;
import com.asg.shipchandling.StockMaster.dto.StockMasterViewResponse;
import com.asg.shipchandling.StockMaster.dto.StockMasterWarehouseDtlDto;
import com.asg.shipchandling.StockMaster.dto.UpdateStockMasterRequest;
import com.asg.shipchandling.StockMaster.dto.ValidationResponse;
import com.asg.shipchandling.StockMaster.entity.StockMasterDTLEntity;
import com.asg.shipchandling.StockMaster.entity.StockMasterDtlId;
import com.asg.shipchandling.StockMaster.entity.StockMasterEntity;
import com.asg.shipchandling.StockMaster.entity.StockMasterWarehouseDtl;
import com.asg.shipchandling.StockMaster.entity.StockMasterWarehouseDtlId;
import com.asg.shipchandling.StockMaster.repository.StockMasterDtlRepository;
import com.asg.shipchandling.StockMaster.repository.StockMasterRepository;
import com.asg.shipchandling.StockMaster.repository.StockMasterWarehouseDtlRepository;
import com.asg.shipchandling.StockMaster.repository.StockCategoryMasterRepository;
import com.asg.shipchandling.StockMaster.entity.StockCategoryMasterEntity;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.StockMaster.dto.StockMasterViewResponse.LovDetailDto;

import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockMasterServiceImpl implements StockMasterService {

    @Autowired
    private StockMasterRepository stockMasterRepository;

    @Autowired
    private StockMasterDtlRepository dtlRepository;

    @Autowired
    private StockMasterWarehouseDtlRepository warehouseRepository;

    @Autowired
    private StockCategoryMasterRepository categoryMasterRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(StockMasterServiceImpl.class);

    /**
     * ✅ Get Stock Master by ID (with optional supplier & warehouse details)
     */
    @Override
    public StockMasterViewResponse getStockMasterById(Long stockPoid, boolean includeDetails, Long groupPoid) {

        StockMasterEntity entity = stockMasterRepository.findById(stockPoid).orElse(null);
        if (entity == null) {
            throw new ResourceNotFoundException("Stock Master", "Stock Poid", stockPoid);
        }

        if (groupPoid != null && !groupPoid.equals(entity.getGroupPoid())) {
            return null;
        }

        // Map all fields from entity to response DTO
        StockMasterViewResponse response = new StockMasterViewResponse();
        response.setStockPoid(entity.getStockPoid());
        response.setStockCode(entity.getStockCode());
        response.setStockName(entity.getStockName());
        response.setStockName2(entity.getStockName2());
        response.setStockDescription(entity.getStockDescription());
        response.setCategoryPoid(entity.getCategoryPoid());
        response.setStockUnitPoid(entity.getStockUnitPoid());
        response.setPurchaseStockUnitPoid(entity.getPurchaseStockUnitPoid());
        response.setPurchaseSalesConversion(entity.getPurchaseSalesConversion());
        response.setStockCost(entity.getStockCost());
        response.setTagPrice(entity.getTagPrice());
        response.setRetailPrice(entity.getRetailPrice());
        response.setWholesalePrice(entity.getWholesalePrice());
        response.setPrice1(entity.getPrice1());
        response.setPrice2(entity.getPrice2());
        response.setPrice3(entity.getPrice3());
        response.setCurrencyCode(entity.getCurrencyCode());
        response.setTaxPoid(entity.getTaxPoid());
        response.setInputTaxPoid(entity.getInputTaxPoid());
        response.setBarcode(entity.getBarcode());
        response.setSupplierBarcode(entity.getSupplierBarcode());
        response.setStockGlPoid(entity.getStockGlPoid());
        response.setSalesGlPoid(entity.getSalesGlPoid());
        response.setCostOfSalesGlPoid(entity.getCostOfSalesGlPoid());
        response.setActive(entity.getActive());
        response.setDeleted(entity.getDeleted());
        response.setServiceItem(entity.getServiceItem());
        response.setIsConsumables(entity.getIsConsumables());
        response.setExpiryTracking(entity.getExpiryTracking());
        response.setPrintLabel(entity.getPrintLabel());
        response.setSerialNoTracking(entity.getSerialNoTracking());
        response.setWastagePercentage(entity.getWastagePercentage());
        response.setWeight(entity.getWeight());
        response.setSeqno(entity.getSeqno());
        response.setRemarks(entity.getRemarks());
        response.setOnlineCategoryName(entity.getOnlineCategoryName());
        response.setOnlineStock(entity.getOnlineStock());
        response.setIsGiftCard(entity.getIsGiftCard());
        response.setConsumptionQty(entity.getConsumptionQty());
        response.setConsumptionUnitPoid(entity.getConsumptionUnitPoid());
        response.setMinimumRequiredQty(entity.getMinimumRequiredQty());
        response.setSeasonCode(entity.getSeasonCode());
        response.setFabricType(entity.getFabricType());
        response.setOrigin(entity.getOrigin());
        response.setComposition(entity.getComposition());
        response.setItemSize(entity.getItemSize());
        response.setStockBrand(entity.getStockBrand());
        response.setStockColor(entity.getStockColor());
        response.setStockCareInstructions(entity.getStockCareInstructions());
        response.setStockDtldNarration(entity.getStockDtldNarration());
        response.setProductTags(entity.getProductTags());
        response.setGroupPoid(entity.getGroupPoid());
        response.setCreatedBy(entity.getCreatedBy());
        response.setCreatedDate(entity.getCreatedDate());
        response.setLastmodifiedBy(entity.getLastmodifiedBy());
        response.setLastmodifiedDate(entity.getLastmodifiedDate());

        // Fetch and set category name
        if (entity.getCategoryPoid() != null) {
            categoryMasterRepository.findByCategoryPoid(entity.getCategoryPoid())
                    .ifPresent(category -> response.setCategoryName(category.getCategoryName()));
        }

        // Fetch all details in a single query using JOINs
        List<Object[]> results = stockMasterRepository.findStockMasterWithDetails(stockPoid);
        if (!results.isEmpty()) {
            Object[] row = results.get(0);
            populateDetailsFromQueryResult(response, row);
        } else {
            // Fallback: set empty details if query returns no results
            setEmptyDetails(response);
        }

        if (includeDetails) {
            // Convert supplier details entities to DTOs
            List<StockMasterDTLEntity> supplierEntities = dtlRepository.findByStockPoid(stockPoid);
            List<StockMasterDtlDto> supplierDetails = supplierEntities.stream()
                    .map(this::convertDtlToDto)
                    .collect(Collectors.toList());
            response.setSupplierDetails(supplierDetails);

            // Convert warehouse details entities to DTOs
            List<StockMasterWarehouseDtl> warehouseEntities = warehouseRepository.findByStockPoid(stockPoid);
            List<StockMasterWarehouseDtlDto> warehouseDetails = warehouseEntities.stream()
                    .map(this::convertWarehouseDtlToDto)
                    .collect(Collectors.toList());
            response.setWarehouseDetails(warehouseDetails);
        }

        return response;
    }

    /**
     * Populate detail objects from native query result
     * Column order matches the query in repository
     * Total columns: 56 stock master fields + 27 detail fields (9 detail objects * 3 fields each) = 83 columns
     */
    private void populateDetailsFromQueryResult(StockMasterViewResponse response, Object[] row) {
        // Stock master fields: indices 0-55 (56 fields)
        // Detail fields start at index 56
        
        int index = 56; // Start after stock master fields
        
        // Stock Unit Details (su1: index 56-58)
        response.setStockUnitDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Purchase Stock Unit Details (su2: index 59-61)
        response.setPurchaseStockUnitDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Consumption Unit Details (su3: index 62-64)
        response.setConsumptionUnitDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Tax Details - Output Tax (tax1: index 65-67)
        response.setTaxDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Input Tax Details (tax2: index 68-70)
        response.setInputTaxDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Stock GL Details (gl1: index 71-73)
        response.setStockGlDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Sales GL Details (gl2: index 74-76)
        response.setSalesGlDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Cost of Sales GL Details (gl3: index 77-79)
        response.setCostOfSalesGlDetails(createLovDetailFromRow(row, index));
    }

    /**
     * Create LOV detail from row array starting at given index
     * Expects: [poid, code, description] at indices [index, index+1, index+2]
     */
    private LovDetailDto createLovDetailFromRow(Object[] row, int index) {
        LovDetailDto detail = new LovDetailDto();
        
        if (row.length > index) {
            // Poid (may be BigDecimal or Long)
            if (row[index] != null) {
                if (row[index] instanceof BigDecimal) {
                    detail.setPoid(((BigDecimal) row[index]).longValue());
                } else if (row[index] instanceof Number) {
                    detail.setPoid(((Number) row[index]).longValue());
                }
            }
            
            // Code
            if (row.length > index + 1 && row[index + 1] != null) {
                detail.setCode(row[index + 1].toString());
            }
            
            // Description
            if (row.length > index + 2 && row[index + 2] != null) {
                detail.setDescription(row[index + 2].toString());
            }
        }
        
        // If all fields are null, return empty detail
        if (detail.getPoid() == null && detail.getCode() == null && detail.getDescription() == null) {
            return createEmptyLovDetail();
        }
        
        return detail;
    }

    /**
     * Set empty detail objects
     */
    private void setEmptyDetails(StockMasterViewResponse response) {
        response.setStockUnitDetails(createEmptyLovDetail());
        response.setPurchaseStockUnitDetails(createEmptyLovDetail());
        response.setConsumptionUnitDetails(createEmptyLovDetail());
        response.setTaxDetails(createEmptyLovDetail());
        response.setInputTaxDetails(createEmptyLovDetail());
        response.setStockGlDetails(createEmptyLovDetail());
        response.setSalesGlDetails(createEmptyLovDetail());
        response.setCostOfSalesGlDetails(createEmptyLovDetail());
    }

    /**
     * Create empty LOV detail object
     */
    private LovDetailDto createEmptyLovDetail() {
        LovDetailDto detail = new LovDetailDto();
        detail.setPoid(null);
        detail.setCode(null);
        detail.setDescription(null);
        return detail;
    }

    /**
     * ✅ Get paginated & filtered Stock Masters (flat list)
     */
    @Override
    public Page<StockMasterEntity> getStockMasters(Map<String, String> filters, Pageable pageable) {
        Specification<StockMasterEntity> spec = Specification.where(null);

        Long groupPoid = Long.parseLong(filters.get("groupPoid"));
        spec = spec.and((root, query, cb) -> cb.equal(root.get("groupPoid"), groupPoid));

        spec = spec.and((root, query, cb) -> cb.or(
                cb.isNull(root.get("deleted")),
                cb.notEqual(root.get("deleted"), "Y")));
        if (filters.containsKey("active")) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), filters.get("active")));
        }

        if (filters.containsKey("categoryPoid")) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoryPoid"),
                    Long.parseLong(filters.get("categoryPoid"))));
        }

        if (filters.containsKey("search")) {
            String search = "%" + filters.get("search").toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("stockCode")), search),
                    cb.like(cb.lower(root.get("stockName")), search)));
        }
        return stockMasterRepository.findAll(spec, pageable);
    }

    /**
     * ✅ Get Stock Masters grouped by category (Tree structure with hierarchical categories)
     */
    @Override
    public List<Map<String, Object>> getStockMastersTree(Long groupPoid) {
        // Fetch stock items for the group using Specification
        Specification<StockMasterEntity> spec = (root, query, cb) -> cb.equal(root.get("groupPoid"), groupPoid);
        spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), "Y"));
        spec = spec.and((root, query, cb) -> cb.or(
                cb.isNull(root.get("deleted")),
                cb.notEqual(root.get("deleted"), "Y")));

        List<StockMasterEntity> allStockItems = stockMasterRepository.findAll(spec);

        // Group stock items by categoryPoid
        Map<Long, List<StockMasterEntity>> stockItemsByCategory = allStockItems.stream()
                .filter(item -> item.getCategoryPoid() != null)
                .collect(Collectors.groupingBy(StockMasterEntity::getCategoryPoid));

        // Fetch all categories for the group
        List<StockCategoryMasterEntity> allCategories = categoryMasterRepository.findByGroupPoid(groupPoid);

        // Create a map for quick category lookup
        Map<Long, StockCategoryMasterEntity> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(StockCategoryMasterEntity::getCategoryPoid, cat -> cat));

        // Build tree starting from root categories (parentCategoryPoid is null)
        List<StockCategoryMasterEntity> rootCategories = allCategories.stream()
                .filter(cat -> cat.getParentCategoryPoid() == null)
                .collect(Collectors.toList());

        List<Map<String, Object>> tree = new ArrayList<>();
        for (StockCategoryMasterEntity rootCategory : rootCategories) {
            Map<String, Object> categoryNode = buildCategoryNode(rootCategory, categoryMap, stockItemsByCategory);
            tree.add(categoryNode);
        }

        return tree;
    }

    /**
     * Recursively build category node with children and stock items
     * Stock items are only attached to the most specific category (sub-child/leaf nodes)
     */
    private Map<String, Object> buildCategoryNode(
            StockCategoryMasterEntity category,
            Map<Long, StockCategoryMasterEntity> categoryMap,
            Map<Long, List<StockMasterEntity>> stockItemsByCategory) {

        Map<String, Object> node = new HashMap<>();
        node.put("categoryPoid", category.getCategoryPoid());
        node.put("categoryCode", category.getCategoryCode());
        node.put("categoryName", category.getCategoryName());
        node.put("parentCategoryPoid", category.getParentCategoryPoid());
        node.put("groupPoid", category.getGroupPoid());

        // Find child categories (sub-children)
        List<StockCategoryMasterEntity> childCategories = categoryMap.values().stream()
                .filter(childCat -> Objects.equals(childCat.getParentCategoryPoid(), category.getCategoryPoid()))
                .collect(Collectors.toList());

        // If this category has children (sub-children), it's a parent - don't show stock items here
        // Stock items should only appear under the most specific category (leaf nodes)
        if (childCategories.isEmpty()) {
            // This is a leaf node (sub-child) - add stock items directly to this category
            List<StockMasterEntity> stockItems = stockItemsByCategory.getOrDefault(category.getCategoryPoid(), new ArrayList<>());
            node.put("stockItems", stockItems);
        } else {
            // This is a parent category - no stock items, only children
            node.put("stockItems", new ArrayList<>());
        }

        // Recursively build child categories (sub-children)
        List<Map<String, Object>> children = new ArrayList<>();
        for (StockCategoryMasterEntity childCategory : childCategories) {
            Map<String, Object> childNode = buildCategoryNode(childCategory, categoryMap, stockItemsByCategory);
            children.add(childNode);
        }
        node.put("children", children);

        return node;
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationResponse validateStockCode(String stockCode, Long groupPoid, Long stockPoid) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            return new ValidationResponse(false, "Stock code cannot be empty");
        }

        boolean exists;
        if (stockPoid != null) {
            exists = stockMasterRepository.existsByStockCodeIgnoreCaseAndGroupPoidAndStockPoidNot(
                    stockCode, groupPoid, stockPoid);
        } else {
            exists = stockMasterRepository.existsByStockCodeIgnoreCaseAndGroupPoid(stockCode, groupPoid);
        }

        ValidationResponse response = new ValidationResponse();
        response.setIsUnique(!exists);
        response.setMessage(exists ? "Stock code already exists" : "Stock code is available");
        return response;
    }

    @Override
    public ValidationResponse validateStockName(String stockName, Long groupPoid, Long excludeStockPoid) {
        boolean exists;

        if (excludeStockPoid != null) {
            exists = stockMasterRepository.existsByStockNameAndGroupPoidAndStockPoidNot(
                    stockName.trim(), groupPoid, excludeStockPoid);
        } else {
            exists = stockMasterRepository.existsByStockNameAndGroupPoid(
                    stockName.trim(), groupPoid);
        }

        if (exists) {
            return new ValidationResponse(false, "Stock name already exists");
        } else {
            return new ValidationResponse(true, "Stock name is available");
        }
    }

    // Dependency Check
    @Override
    @Transactional(readOnly = true)
    public StockMasterDependenciesDto checkStockMasterDependencies(Long stockPoid, Long groupPoid) {
        // Validate stock exists
        StockMasterEntity stock = stockMasterRepository
                .findByStockPoidAndGroupPoid(stockPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Master", "stockPoid", stockPoid));

        // TODO: Check for stock balance
        // Query: SELECT SUM(BALANCE_QTY) FROM STOCK_BALANCE WHERE STOCK_POID = ?
        Long stockBalanceCount = 0L; // TODO: Implement stock balance check

        // TODO: Check for transactions (Purchase Orders, Sales Orders, etc.)
        // Query: SELECT COUNT(*) FROM AP_PURCHASE_ORDER_ITEM_DTL WHERE STOCK_POID = ?
        // Query: SELECT COUNT(*) FROM AR_SALES_ORDER_ITEM_DTL WHERE STOCK_POID = ?
        Long transactionCount = 0L; // TODO: Implement transaction check

        StockMasterDependenciesDto dto = new StockMasterDependenciesDto();
        dto.setStockPoid(stockPoid);
        dto.setCanDelete(stockBalanceCount == 0 && transactionCount == 0);
        dto.setStockBalanceCount(stockBalanceCount);
        dto.setTransactionCount(transactionCount);

        if (dto.getCanDelete()) {
            dto.setReason("No dependencies");
            dto.setMessage("Stock item can be deleted. No dependencies found.");
        } else {
            StringBuilder message = new StringBuilder("Cannot delete stock item. ");
            if (stockBalanceCount > 0) {
                message.append(String.format("It has stock balance of %d units. ", stockBalanceCount));
            }
            if (transactionCount > 0) {
                message.append(String.format("It is used in %d transactions.", transactionCount));
            }
            dto.setReason("Stock item has dependencies");
            dto.setMessage(message.toString());
        }

        return dto;
    }

    @Override
    @Transactional
    public void deleteStockMaster(Long stockPoid, Long groupPoid) {
        StockMasterEntity stock = stockMasterRepository
                .findByStockPoidAndGroupPoid(stockPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Master", "stockPoid", stockPoid));

        // TODO: Check dependencies (stock balance, transactions, etc.)

        // Delete detail tables
        dtlRepository.deleteByStockPoid(stockPoid);
        warehouseRepository.deleteByStockPoid(stockPoid);

        stock.setDeleted("Y");
        stockMasterRepository.save(stock);
    }

    /**
     * Executes the stored procedure PROC_STOCK_MASTER_BEFORE_SAVE before saving stock master data.
     * This procedure is called to perform validation and business logic checks before the save operation.
     *
     * @param groupPoid   The group POID associated with the stock master
     * @param companyPoid The company POID associated with the stock master
     * @param userId      The user ID performing the operation
     * @param stockPoid   The stock master POID (primary key)
     * @param serviceItem Indicates if the stock item is a service item ("Y" or "N")
     * @throws RuntimeException If the procedure returns "ERROR" or if any SQL/database error occurs.
     *                          This exception will prevent the save operation from proceeding.
     */
    private void callBeforeSaveProcedure(Long groupPoid, Long companyPoid, String userId,
                                         Long stockPoid, String serviceItem) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_STOCK_MASTER_BEFORE_SAVE(?,?,?,?,?,?); END;";
        // Check result for "ERROR" and throw BusinessException if found
    }

    private void callAfterSaveProcedure(Long stockPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_STOCK_MASTER_AFTER_SAVE(?,?); END;";
        // Check result for "ERROR" and log warning if found
    }

    private StockMasterDto convertToDto(StockMasterEntity stock, boolean includeDetails) {
        StockMasterDto dto = new StockMasterDto();
        BeanUtils.copyProperties(stock, dto);

        if (includeDetails) {
            List<StockMasterDTLEntity> supplierDetails = dtlRepository.findByStockPoid(stock.getStockPoid());
            dto.setSupplierDetails(supplierDetails.stream()
                    .map(this::convertDtlToDto)
                    .collect(Collectors.toList()));

            List<StockMasterWarehouseDtl> warehouseDetails = warehouseRepository.findByStockPoid(stock.getStockPoid());
            dto.setWarehouseDetails(warehouseDetails.stream()
                    .map(this::convertWarehouseDtlToDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private StockMasterDtlDto convertDtlToDto(StockMasterDTLEntity dtl) {
        StockMasterDtlDto dto = new StockMasterDtlDto();
        BeanUtils.copyProperties(dtl, dto);
        return dto;
    }

    private StockMasterWarehouseDtlDto convertWarehouseDtlToDto(StockMasterWarehouseDtl dtl) {
        StockMasterWarehouseDtlDto dto = new StockMasterWarehouseDtlDto();
        BeanUtils.copyProperties(dtl, dto);
        return dto;
    }

    @Override
    @Transactional
    public StockMasterDto createStockMaster(CreateStockMasterRequest request, Long groupPoid,
                                            Long companyPoid, String userId) {

        // --- Validate required parent fields ---
        if (request.getStockName() == null || request.getStockName().isEmpty()) {
            throw new IllegalArgumentException("Stock name cannot be null");
        }
        if (request.getCategoryPoid() == null) {
            throw new IllegalArgumentException("Category POID cannot be null");
        }
        if (request.getStockUnitPoid() == null) {
            throw new IllegalArgumentException("Stock Unit POID cannot be null");
        }
        if (request.getTaxPoid() == null) {
            throw new IllegalArgumentException("Tax POID cannot be null");
        }
        if (request.getInputTaxPoid() == null) {
            throw new IllegalArgumentException("Input Tax POID cannot be null");
        }
        StockMasterEntity stock = new StockMasterEntity();
        BeanUtils.copyProperties(request, stock);
        // stock.setStockPoid(request.getStockPoid());
        stock.setGroupPoid(groupPoid);
        stock.setCreatedBy(userId);
        stock.setLastmodifiedBy(userId);
        stock.setActive(request.getActive() != null ? request.getActive() : "Y");
        stock.setDeleted("N");
        stock.setServiceItem(request.getServiceItem() != null ? request.getServiceItem() : "N");
        stock.setIsConsumables(request.getIsConsumables() != null ? request.getIsConsumables() : "N");
        stock.setIsGiftCard(request.getIsGiftCard() != null ? request.getIsGiftCard() : "N");

        StockMasterEntity savedStock = stockMasterRepository.saveAndFlush(stock);
        Long stockPoid = savedStock.getStockPoid();
        // System.out.println("Stock after setting values: " +
        // "StockPoid=" + stockPoid);

        // just call the function
        callBeforeSaveProcedure(groupPoid, companyPoid, userId, stockPoid, savedStock.getServiceItem());

        // --- Save Supplier Details ---
        if (request.getSupplierDetails() != null && !request.getSupplierDetails().isEmpty()) {
            processSupplierDetails(stockPoid, request.getSupplierDetails(), userId);
        }

        // --- Save Warehouse Details ---
        if (request.getWarehouseDetails() != null && !request.getWarehouseDetails().isEmpty()) {
            processWarehouseDetails(stockPoid, request.getWarehouseDetails(), userId);
        }

        // just call the function
        callAfterSaveProcedure(stockPoid);

        StockMasterEntity refreshedStock = stockMasterRepository.findByStockPoid(stockPoid)
                .orElseThrow(() -> new RuntimeException("Stock not found after save"));

        return convertToDto(refreshedStock, true);
    }

    @Override
    @Transactional
    public StockMasterDto updateStockMaster(Long stockPoid, UpdateStockMasterRequest request,
                                            Long groupPoid, Long companyPoid, String userId) {
        StockMasterEntity stock = stockMasterRepository
                .findByStockPoidAndGroupPoid(stockPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Master", "stockPoid", stockPoid));

        if ("Y".equals(stock.getDeleted())) {
            throw new IllegalStateException("Cannot update a deleted stock item");
        }

        // Validate consumables logic
        if ("N".equals(request.getIsConsumables())) {
            request.setConsumptionQty(null);
            request.setConsumptionUnitPoid(null);
            request.setMinimumRequiredQty(null);
        }

        BeanUtils.copyProperties(request, stock, "stockPoid", "stockCode", "createdBy", "createdDate");
        stock.setLastmodifiedBy(userId);

        // just call the function
        callBeforeSaveProcedure(groupPoid, companyPoid, userId, stockPoid, stock.getServiceItem());

        // Update detail tables
        if (request.getSupplierDetails() != null && !request.getSupplierDetails().isEmpty()) {
            processSupplierDetails(stockPoid, request.getSupplierDetails(), userId);
        }
        if (request.getWarehouseDetails() != null && !request.getWarehouseDetails().isEmpty()) {
            processWarehouseDetails(stockPoid, request.getWarehouseDetails(), userId);
        }

        // Save
        StockMasterEntity savedStock = stockMasterRepository.save(stock);

        // just call the function
        callAfterSaveProcedure(stockPoid);

        return convertToDto(savedStock, true);
    }

    private void processSupplierDetails(Long stockPoid, List<CreateStockMasterDtlRequest> details, String userId) {
        List<StockMasterDTLEntity> entitiesToDelete = new ArrayList<>();
        List<StockMasterDTLEntity> entitiesToSave = new ArrayList<>();

        for (CreateStockMasterDtlRequest dto : details) {
            String action = StringUtils.isBlank(dto.getActionType()) ? "" : dto.getActionType().toLowerCase();

            switch (action) {
                case "isdeleted" -> handleSupplierDeleteAction(stockPoid, dto, entitiesToDelete);
                case "iscreated", "isupdated" ->
                        handleSupplierCreateOrUpdateAction(stockPoid, dto, entitiesToSave, userId);
                default ->
                        logger.warn("Unknown actionType '{}' for detRowId={}", dto.getActionType(), dto.getSupplierPoid());
            }
        }

        if (!entitiesToDelete.isEmpty()) {
            dtlRepository.deleteAll(entitiesToDelete);
        }
        if (!entitiesToSave.isEmpty()) {
            dtlRepository.saveAll(entitiesToSave);
        }
    }

    private void handleSupplierDeleteAction(Long stockPoid, CreateStockMasterDtlRequest dto, List<StockMasterDTLEntity> entitiesToDelete) {
        if (dto.getSupplierPoid() != null) {
            dtlRepository.findByStockPoid(stockPoid).stream()
                    .filter(entity -> entity.getSupplierPoid().equals(dto.getSupplierPoid()))
                    .findFirst()
                    .ifPresentOrElse(
                            entitiesToDelete::add,
                            () -> logger.warn("No StockMasterDTLEntity found for stockPoid={} and supplierPoid={}, skipping delete.",
                                    stockPoid, dto.getSupplierPoid())
                    );
        } else {
            logger.warn("supplierPoid is null for stockPoid={}, skipping delete.", stockPoid);
        }
    }

    private void handleSupplierCreateOrUpdateAction(Long stockPoid, CreateStockMasterDtlRequest dto, List<StockMasterDTLEntity> entitiesToSave, String userId) {
        if (dto.getSupplierPoid() != null) {
            dtlRepository.findByStockPoid(stockPoid).stream()
                    .filter(entity -> entity.getSupplierPoid().equals(dto.getSupplierPoid()))
                    .findFirst()
                    .ifPresentOrElse(
                            existingEntity -> updateExistingSupplierEntity(existingEntity, dto, entitiesToSave, userId),
                            () -> createNewSupplierEntity(stockPoid, dto, entitiesToSave, userId)
                    );
        } else {
            createNewSupplierEntity(stockPoid, dto, entitiesToSave, userId);
        }
    }

    private void updateExistingSupplierEntity(StockMasterDTLEntity entity, CreateStockMasterDtlRequest dto, List<StockMasterDTLEntity> entitiesToSave, String userId) {
        entity.setSupplierStockCode(dto.getSupplierStockCode());
        entity.setRemarks(dto.getRemarks());
        entity.setLastmodifiedBy(userId);
        entity.setLastmodifiedDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        entitiesToSave.add(entity);
    }

    private void createNewSupplierEntity(Long stockPoid, CreateStockMasterDtlRequest dto, List<StockMasterDTLEntity> entitiesToSave, String userId) {
        Long maxDetRowId = dtlRepository.findMaxDetRowIdByStockPoid(stockPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        StockMasterDTLEntity newEntity = new StockMasterDTLEntity();
        newEntity.setStockPoid(stockPoid);
        newEntity.setDetRowId(detRowId);
        newEntity.setSupplierPoid(dto.getSupplierPoid());
        newEntity.setSupplierStockCode(dto.getSupplierStockCode());
        newEntity.setRemarks(dto.getRemarks());
        newEntity.setCreatedBy(userId);
        newEntity.setCreatedDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        newEntity.setLastmodifiedBy(userId);
        newEntity.setLastmodifiedDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        entitiesToSave.add(newEntity);
    }

    private void processWarehouseDetails(Long stockPoid, List<CreateStockMasterWarehouseDtlRequest> details, String userId) {
        List<StockMasterWarehouseDtl> entitiesToDelete = new ArrayList<>();
        List<StockMasterWarehouseDtl> entitiesToSave = new ArrayList<>();

        for (CreateStockMasterWarehouseDtlRequest dto : details) {
            String action = StringUtils.isBlank(dto.getActionType()) ? "" : dto.getActionType().toLowerCase();

            switch (action) {
                case "isdeleted" -> handleWarehouseDeleteAction(stockPoid, dto, entitiesToDelete);
                case "iscreated", "isupdated" ->
                        handleWarehouseCreateOrUpdateAction(stockPoid, dto, entitiesToSave, userId);
                default ->
                        logger.warn("Unknown actionType '{}' for locationPoid={}", dto.getActionType(), dto.getLocationPoid());
            }
        }

        if (!entitiesToDelete.isEmpty()) {
            warehouseRepository.deleteAll(entitiesToDelete);
        }
        if (!entitiesToSave.isEmpty()) {
            warehouseRepository.saveAll(entitiesToSave);
        }
    }

    private void handleWarehouseDeleteAction(Long stockPoid, CreateStockMasterWarehouseDtlRequest dto, List<StockMasterWarehouseDtl> entitiesToDelete) {
        if (dto.getLocationPoid() != null) {
            warehouseRepository.findByStockPoid(stockPoid).stream()
                    .filter(entity -> entity.getLocationPoid().equals(dto.getLocationPoid()))
                    .findFirst()
                    .ifPresentOrElse(
                            entitiesToDelete::add,
                            () -> logger.warn("No StockMasterWarehouseDtl found for stockPoid={} and locationPoid={}, skipping delete.",
                                    stockPoid, dto.getLocationPoid())
                    );
        } else {
            logger.warn("locationPoid is null for stockPoid={}, skipping delete.", stockPoid);
        }
    }

    private void handleWarehouseCreateOrUpdateAction(Long stockPoid, CreateStockMasterWarehouseDtlRequest dto, List<StockMasterWarehouseDtl> entitiesToSave, String userId) {
        if (dto.getLocationPoid() != null) {
            warehouseRepository.findByStockPoid(stockPoid).stream()
                    .filter(entity -> entity.getLocationPoid().equals(dto.getLocationPoid()))
                    .findFirst()
                    .ifPresentOrElse(
                            existingEntity -> updateExistingWarehouseEntity(existingEntity, dto, entitiesToSave, userId),
                            () -> createNewWarehouseEntity(stockPoid, dto, entitiesToSave, userId)
                    );
        } else {
            createNewWarehouseEntity(stockPoid, dto, entitiesToSave, userId);
        }
    }

    private void updateExistingWarehouseEntity(StockMasterWarehouseDtl entity, CreateStockMasterWarehouseDtlRequest dto, List<StockMasterWarehouseDtl> entitiesToSave, String userId) {
        entity.setTransactionDate(dto.getTransactionDate());
        entity.setAisleNo(dto.getAisleNo());
        entity.setBayNo(dto.getBayNo());
        entity.setShelfNo(dto.getShelfNo());
        entity.setBinNo(dto.getBinNo());
        entity.setReorderLevel(dto.getReorderLevel());
        entity.setReorderQty(dto.getReorderQty());
        entity.setLastmodifiedBy(userId);
        entity.setLastmodifiedDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        entitiesToSave.add(entity);
    }

    private void createNewWarehouseEntity(Long stockPoid, CreateStockMasterWarehouseDtlRequest dto, List<StockMasterWarehouseDtl> entitiesToSave, String userId) {
        Long maxDetRowId = warehouseRepository.findMaxDetRowIdByStockPoid(stockPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        StockMasterWarehouseDtl newEntity = new StockMasterWarehouseDtl();
        newEntity.setStockPoid(stockPoid);
        newEntity.setDetRowId(detRowId);
        newEntity.setTransactionDate(dto.getTransactionDate());
        newEntity.setLocationPoid(dto.getLocationPoid());
        newEntity.setAisleNo(dto.getAisleNo());
        newEntity.setBayNo(dto.getBayNo());
        newEntity.setShelfNo(dto.getShelfNo());
        newEntity.setBinNo(dto.getBinNo());
        newEntity.setReorderLevel(dto.getReorderLevel());
        newEntity.setReorderQty(dto.getReorderQty());
        newEntity.setCreatedBy(userId);
        newEntity.setCreatedDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        newEntity.setLastmodifiedBy(userId);
        newEntity.setLastmodifiedDate(java.sql.Timestamp.valueOf(LocalDateTime.now()));
        entitiesToSave.add(newEntity);
    }


    @Transactional
    @Override

    public StockMasterDtlDto addSupplierDetail(Long stockPoid,
                                               CreateStockMasterDtlRequest request,
                                               Long groupPoid,
                                               String userId) {

        StockMasterEntity stock = stockMasterRepository
                .findByStockPoidAndGroupPoid(stockPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Master", "stockPoid", stockPoid));

        if ("Y".equals(stock.getDeleted())) {
            throw new IllegalStateException("Cannot add supplier details. Stock item is deleted");
        }

        if (request.getRemarks() != null && !request.getRemarks().isEmpty()) {
            Long count = dtlRepository.countByStockPoidAndRemarks(stockPoid, request.getRemarks());
            if (count != null && count > 0) {
                throw new IllegalArgumentException("Supplier detail with the same remarks already exists.");
            }
        }

        // Generate detRowId
        Long maxDetRowId = dtlRepository.findMaxDetRowIdByStockPoid(stockPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        StockMasterDTLEntity dtl = new StockMasterDTLEntity();
        dtl.setStockPoid(stockPoid);
        dtl.setDetRowId(detRowId);
        dtl.setSupplierPoid(request.getSupplierPoid());
        dtl.setSupplierStockCode(request.getSupplierStockCode());
        dtl.setRemarks(request.getRemarks());
        dtl.setCreatedBy(userId);
        dtl.setLastmodifiedBy(userId);

        // Save entity
        StockMasterDTLEntity savedDtl = dtlRepository.save(dtl);

        // Convert to DTO before returning
        return convertDtlToDto(savedDtl);
    }

    @Override
    @Transactional
    public StockMasterDtlDto updateSupplierDetail(Long stockPoid, Long detRowId,
                                                  CreateStockMasterDtlRequest request,
                                                  Long groupPoid, String userId) {
        // Validate stock exists
        StockMasterEntity stock = stockMasterRepository
                .findByStockPoidAndGroupPoid(stockPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Master", "stockPoid", stockPoid));

        if ("Y".equals(stock.getDeleted())) {
            throw new IllegalStateException("Cannot update supplier details. Stock item is deleted");
        }

        StockMasterDTLEntity dtl = dtlRepository
                .findById(new StockMasterDtlId(stockPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Supplier Detail", "detRowId", detRowId));

        dtl.setSupplierPoid(request.getSupplierPoid());
        dtl.setSupplierStockCode(request.getSupplierStockCode());
        dtl.setRemarks(request.getRemarks());
        dtl.setLastmodifiedBy(userId);

        StockMasterDTLEntity savedDtl = dtlRepository.save(dtl);
        return convertDtlToDto(savedDtl);
    }

    @Transactional
    @Override
    public void deleteSupplierDetail(Long stockPoid, Long detRowId, Long groupPoid) {

        StockMasterEntity stock = stockMasterRepository
                .findByStockPoid(stockPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Master", "stockPoid", stockPoid));

        if (!stock.getGroupPoid().equals(groupPoid)) {
            throw new IllegalArgumentException("Stock item does not belong to the specified group.");
        }

        if ("Y".equals(stock.getDeleted())) {
            throw new IllegalStateException("Cannot delete supplier details. Stock item is deleted");
        }

        StockMasterDTLEntity dtl = dtlRepository
                .findById(new StockMasterDtlId(stockPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Supplier Detail", "detRowId", detRowId));

        dtlRepository.delete(dtl);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMasterDtlDto> getSupplierDetails(Long stockPoid, Long groupPoid) {

        stockMasterRepository.findByStockPoidAndGroupPoid(stockPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Master", "stockPoid", stockPoid));

        List<StockMasterDTLEntity> supplierDetails = dtlRepository.findByStockPoid(stockPoid);
        return supplierDetails.stream()
                .map(this::convertDtlToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StockMasterWarehouseDtlDto addWarehouseDetail(Long stockPoid, CreateStockMasterWarehouseDtlRequest request,
                                                         Long groupPoid, String userId) {
        // Validate stock exists and belongs to group
        StockMasterEntity stock = stockMasterRepository
                .findByStockPoidAndGroupPoid(stockPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Master", "stockPoid", stockPoid));

        if ("Y".equals(stock.getDeleted())) {
            throw new IllegalArgumentException("Cannot add warehouse details. Stock item is deleted");
        }

        Long maxDetRowId = warehouseRepository.findMaxDetRowIdByStockPoid(stockPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        StockMasterWarehouseDtl dtl = new StockMasterWarehouseDtl();
        dtl.setStockPoid(stockPoid);
        dtl.setDetRowId(detRowId);
        dtl.setTransactionDate(request.getTransactionDate());
        dtl.setLocationPoid(request.getLocationPoid());
        dtl.setAisleNo(request.getAisleNo());
        dtl.setBayNo(request.getBayNo());
        dtl.setShelfNo(request.getShelfNo());
        dtl.setBinNo(request.getBinNo());
        dtl.setReorderLevel(request.getReorderLevel());
        dtl.setReorderQty(request.getReorderQty());
        dtl.setCreatedBy(userId);
        dtl.setLastmodifiedBy(userId);

        StockMasterWarehouseDtl savedDtl = warehouseRepository.save(dtl);
        return convertWarehouseDtlToDto(savedDtl);
    }

    @Override
    @Transactional
    public StockMasterWarehouseDtlDto updateWarehouseDetail(Long stockPoid, Long detRowId,
                                                            CreateStockMasterWarehouseDtlRequest request,
                                                            Long groupPoid, String userId) {
        // Validate stock exists
        StockMasterEntity stock = stockMasterRepository
                .findByStockPoidAndGroupPoid(stockPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Master", "stockPoid", stockPoid));

        if ("Y".equals(stock.getDeleted())) {
            throw new IllegalArgumentException("Cannot update warehouse details. Stock item is deleted");
        }

        // Find existing warehouse detail
        StockMasterWarehouseDtl dtl = warehouseRepository
                .findById(new StockMasterWarehouseDtlId(stockPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse Detail", "detRowId", detRowId));

        // Update fields
        dtl.setTransactionDate(request.getTransactionDate());
        dtl.setLocationPoid(request.getLocationPoid());
        dtl.setAisleNo(request.getAisleNo());
        dtl.setBayNo(request.getBayNo());
        dtl.setShelfNo(request.getShelfNo());
        dtl.setBinNo(request.getBinNo());
        dtl.setReorderLevel(request.getReorderLevel());
        dtl.setReorderQty(request.getReorderQty());
        dtl.setLastmodifiedBy(userId);

        StockMasterWarehouseDtl savedDtl = warehouseRepository.save(dtl);
        return convertWarehouseDtlToDto(savedDtl);
    }

    @Override
    @Transactional
    public void deleteWarehouseDetail(Long stockPoid, Long detRowId, Long groupPoid) {
        // Validate stock exists
        StockMasterEntity stock = stockMasterRepository
                .findByStockPoidAndGroupPoid(stockPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Master", "stockPoid", stockPoid));

        if ("Y".equals(stock.getDeleted())) {
            throw new IllegalArgumentException("Cannot delete warehouse details. Stock item is deleted");
        }

        // Delete warehouse detail
        warehouseRepository.deleteById(new StockMasterWarehouseDtlId(stockPoid, detRowId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMasterWarehouseDtlDto> getWarehouseDetails(Long stockPoid, Long groupPoid) {
        // Validate stock exists
        stockMasterRepository.findByStockPoidAndGroupPoid(stockPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Master", "stockPoid", stockPoid));

        List<StockMasterWarehouseDtl> warehouseDetails = warehouseRepository.findByStockPoid(stockPoid);
        return warehouseDetails.stream()
                .map(this::convertWarehouseDtlToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockMasterDto getStockMasterByBarcode(String barcode, Long groupPoid) {
        StockMasterEntity stock = stockMasterRepository
                .findByBarcodeOrSupplierBarcode(barcode, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock Master", "barcode or supplierBarcode", barcode));

        return convertToDto(stock, true);
    }

    /**
     * Get hierarchical list of Stock Masters and Categories
     * If parentPoid is null: Returns MAIN_GROUP (root categories)
     * If parentPoid is provided: Returns SUB_GROUP (child categories) and LEDGER (stock items) for that parent
     */
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStockMastersHierarchical(Long groupPoid, Long parentPoid, String filterValue, boolean includeDeleted, Long companyPoid, Long userPoid) {
        // Log the request for audit purposes
        if (logger.isDebugEnabled()) {
            logger.debug("getStockMastersHierarchical called - groupPoid: {}, parentPoid: {}, companyPoid: {}, userPoid: {}, filterValue: {}, includeDeleted: {}", 
                    groupPoid, parentPoid, companyPoid, userPoid, filterValue, includeDeleted);
        }
        
        // Validate companyPoid and userPoid if provided
        if (companyPoid != null && companyPoid <= 0) {
            throw new IllegalArgumentException("Invalid companyPoid: " + companyPoid);
        }
        if (userPoid != null && userPoid <= 0) {
            throw new IllegalArgumentException("Invalid userPoid: " + userPoid);
        }
        List<Map<String, Object>> result = new ArrayList<>();

        if (parentPoid == null) {
            // Return MAIN_GROUP items (root categories with no parent)
            List<StockCategoryMasterEntity> rootCategories = categoryMasterRepository
                    .findByGroupPoidAndParentCategoryPoidIsNull(groupPoid);
            
            for (StockCategoryMasterEntity category : rootCategories) {
                Map<String, Object> item = convertCategoryToHierarchicalItem(category, "MAIN_GROUP", 0);
                result.add(item);
            }
        } else {
            // Check if parentPoid is a category
            Optional<StockCategoryMasterEntity> parentCategory = categoryMasterRepository
                    .findByCategoryPoid(parentPoid);
            
            if (parentCategory.isPresent()) {
                // Parent is a category - return child categories (SUB_GROUP) and stock items (LEDGER)
                StockCategoryMasterEntity parent = parentCategory.get();
                int level = calculateCategoryLevel(parent, groupPoid) + 1;

                // Get child categories
                List<StockCategoryMasterEntity> childCategories = categoryMasterRepository
                        .findByParentCategoryPoid(parentPoid);
                
                for (StockCategoryMasterEntity category : childCategories) {
                    Map<String, Object> item = convertCategoryToHierarchicalItem(category, "SUB_GROUP", level);
                    item.put("parentPoid", parentPoid);
                    result.add(item);
                }

                // Get stock items for this category
                Specification<StockMasterEntity> spec = (root, query, cb) -> 
                    cb.equal(root.get("groupPoid"), groupPoid);
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.get("categoryPoid"), parentPoid));
                
                if (!includeDeleted) {
                    spec = spec.and((root, query, cb) -> cb.or(
                        cb.isNull(root.get("deleted")),
                        cb.notEqual(root.get("deleted"), "Y")));
                }
                
                // Apply filterValue if provided
                if (filterValue != null && !filterValue.trim().isEmpty()) {
                    String searchPattern = "%" + filterValue.toLowerCase() + "%";
                    spec = spec.and((root, query, cb) -> cb.or(
                        cb.like(cb.lower(root.get("stockCode")), searchPattern),
                        cb.like(cb.lower(root.get("stockName")), searchPattern)
                    ));
                }

                List<StockMasterEntity> stockItems = stockMasterRepository.findAll(spec);
                
                for (StockMasterEntity stock : stockItems) {
                    Map<String, Object> item = convertStockToHierarchicalItem(stock, level);
                    item.put("parentPoid", parentPoid);
                    result.add(item);
                }
            } else {
                // Parent is not a category - should not happen in normal flow
                // Return empty list
            }
        }

        return result;
    }

    /**
     * Calculate the level of a category in the hierarchy
     */
    private int calculateCategoryLevel(StockCategoryMasterEntity category, Long groupPoid) {
        if (category.getParentCategoryPoid() == null) {
            return 0;
        }
        
        Optional<StockCategoryMasterEntity> parent = categoryMasterRepository
                .findByCategoryPoid(category.getParentCategoryPoid());
        
        if (parent.isPresent()) {
            return calculateCategoryLevel(parent.get(), groupPoid) + 1;
        }
        
        return 0;
    }

    /**
     * Convert category entity to hierarchical item format
     */
    private Map<String, Object> convertCategoryToHierarchicalItem(
            StockCategoryMasterEntity category, String type, int level) {
        Map<String, Object> item = new HashMap<>();
        item.put("categoryPoid", category.getCategoryPoid());
        item.put("categoryCode", category.getCategoryCode());
        item.put("categoryName", category.getCategoryName());
        item.put("type", type);
        item.put("level", level);
        item.put("active", true); // Categories are always active in this context
        item.put("deleted", false);
        item.put("groupPoid", category.getGroupPoid());
        if (category.getParentCategoryPoid() != null) {
            item.put("parentPoid", category.getParentCategoryPoid());
        }
        return item;
    }

    /**
     * Convert stock entity to hierarchical item format
     */
    private Map<String, Object> convertStockToHierarchicalItem(StockMasterEntity stock, int level) {
        Map<String, Object> item = new HashMap<>();
        
        // Copy all fields from entity
        item.put("stockPoid", stock.getStockPoid());
        item.put("stockCode", stock.getStockCode());
        item.put("stockName", stock.getStockName());
        item.put("stockName2", stock.getStockName2());
        item.put("stockDescription", stock.getStockDescription());
        item.put("categoryPoid", stock.getCategoryPoid());
        item.put("stockUnitPoid", stock.getStockUnitPoid());
        item.put("purchaseStockUnitPoid", stock.getPurchaseStockUnitPoid());
        item.put("purchaseSalesConversion", stock.getPurchaseSalesConversion());
        item.put("stockCost", stock.getStockCost());
        item.put("tagPrice", stock.getTagPrice());
        item.put("retailPrice", stock.getRetailPrice());
        item.put("wholesalePrice", stock.getWholesalePrice());
        item.put("price1", stock.getPrice1());
        item.put("price2", stock.getPrice2());
        item.put("price3", stock.getPrice3());
        item.put("currencyCode", stock.getCurrencyCode());
        item.put("taxPoid", stock.getTaxPoid());
        item.put("inputTaxPoid", stock.getInputTaxPoid());
        item.put("barcode", stock.getBarcode());
        item.put("supplierBarcode", stock.getSupplierBarcode());
        item.put("stockGlPoid", stock.getStockGlPoid());
        item.put("salesGlPoid", stock.getSalesGlPoid());
        item.put("costOfSalesGlPoid", stock.getCostOfSalesGlPoid());
        item.put("serviceItem", stock.getServiceItem());
        item.put("isConsumables", stock.getIsConsumables());
        item.put("expiryTracking", stock.getExpiryTracking());
        item.put("printLabel", stock.getPrintLabel());
        item.put("serialNoTracking", stock.getSerialNoTracking());
        item.put("wastagePercentage", stock.getWastagePercentage());
        item.put("weight", stock.getWeight());
        item.put("seqno", stock.getSeqno());
        item.put("remarks", stock.getRemarks());
        item.put("onlineCategoryName", stock.getOnlineCategoryName());
        item.put("onlineStock", stock.getOnlineStock());
        item.put("isGiftCard", stock.getIsGiftCard());
        item.put("consumptionQty", stock.getConsumptionQty());
        item.put("consumptionUnitPoid", stock.getConsumptionUnitPoid());
        item.put("minimumRequiredQty", stock.getMinimumRequiredQty());
        item.put("seasonCode", stock.getSeasonCode());
        item.put("fabricType", stock.getFabricType());
        item.put("origin", stock.getOrigin());
        item.put("composition", stock.getComposition());
        item.put("itemSize", stock.getItemSize());
        item.put("stockBrand", stock.getStockBrand());
        item.put("stockColor", stock.getStockColor());
        item.put("stockCareInstructions", stock.getStockCareInstructions());
        item.put("stockDtldNarration", stock.getStockDtldNarration());
        item.put("productTags", stock.getProductTags());
        item.put("groupPoid", stock.getGroupPoid());
        item.put("createdBy", stock.getCreatedBy());
        item.put("createdDate", stock.getCreatedDate());
        item.put("lastmodifiedBy", stock.getLastmodifiedBy());
        item.put("lastmodifiedDate", stock.getLastmodifiedDate());
        
        // Convert Y/N/null to boolean (Y=true, N/null=false)
        item.put("active", stock.getActive() != null && "Y".equalsIgnoreCase(stock.getActive()));
        item.put("deleted", stock.getDeleted() != null && "Y".equalsIgnoreCase(stock.getDeleted()));
        
        // Add hierarchical fields
        item.put("type", "LEDGER");
        item.put("level", level);
        
        return item;
    }

    /**
     * Get complete nested tree structure of Stock Masters and Categories
     * Similar to GL Master tree structure with children arrays
     */
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStockMastersTreeStructure(Long groupPoid, String filterValue, boolean includeDeleted, Long companyPoid, Long userPoid) {
        // Log the request for audit purposes
        if (logger.isDebugEnabled()) {
            logger.debug("getStockMastersTreeStructure called - groupPoid: {}, companyPoid: {}, userPoid: {}, filterValue: {}, includeDeleted: {}", 
                    groupPoid, companyPoid, userPoid, filterValue, includeDeleted);
        }
        
        // Validate companyPoid and userPoid if provided
        if (companyPoid != null && companyPoid <= 0) {
            throw new IllegalArgumentException("Invalid companyPoid: " + companyPoid);
        }
        if (userPoid != null && userPoid <= 0) {
            throw new IllegalArgumentException("Invalid userPoid: " + userPoid);
        }
        // Fetch all categories for the group
        List<StockCategoryMasterEntity> allCategories = categoryMasterRepository.findByGroupPoid(groupPoid);
        
        // Build category map for quick lookup
        Map<Long, StockCategoryMasterEntity> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(StockCategoryMasterEntity::getCategoryPoid, cat -> cat));
        
        // Build category children map
        Map<Long, List<StockCategoryMasterEntity>> categoryChildrenMap = allCategories.stream()
                .filter(cat -> cat.getParentCategoryPoid() != null)
                .collect(Collectors.groupingBy(StockCategoryMasterEntity::getParentCategoryPoid));
        
        // Fetch all stock items
        Specification<StockMasterEntity> stockSpec = (root, query, cb) -> 
            cb.equal(root.get("groupPoid"), groupPoid);
        
        if (!includeDeleted) {
            stockSpec = stockSpec.and((root, query, cb) -> cb.or(
                cb.isNull(root.get("deleted")),
                cb.notEqual(root.get("deleted"), "Y")));
        }
        
        // Apply filterValue if provided (search by stockCode or stockName)
        if (filterValue != null && !filterValue.trim().isEmpty()) {
            String searchPattern = "%" + filterValue.toLowerCase() + "%";
            Specification<StockMasterEntity> searchSpec = (root, query, cb) -> {
                // Handle null values properly
                return cb.or(
                    cb.and(
                        cb.isNotNull(root.get("stockCode")),
                        cb.like(cb.lower(root.get("stockCode")), searchPattern)
                    ),
                    cb.and(
                        cb.isNotNull(root.get("stockName")),
                        cb.like(cb.lower(root.get("stockName")), searchPattern)
                    )
                );
            };
            stockSpec = stockSpec.and(searchSpec);
        }
        
        List<StockMasterEntity> allStockItems = stockMasterRepository.findAll(stockSpec);
        
        // Group stock items by categoryPoid
        Map<Long, List<StockMasterEntity>> stockItemsByCategory = allStockItems.stream()
                .filter(item -> item.getCategoryPoid() != null)
                .collect(Collectors.groupingBy(StockMasterEntity::getCategoryPoid));
        
        // Get root categories (MAIN_GROUP)
        List<StockCategoryMasterEntity> rootCategories = allCategories.stream()
                .filter(cat -> cat.getParentCategoryPoid() == null)
                .collect(Collectors.toList());
        
        // Build tree starting from root
        List<Map<String, Object>> tree = new ArrayList<>();
        for (StockCategoryMasterEntity rootCategory : rootCategories) {
            Map<String, Object> node = buildTreeNode(rootCategory, categoryMap, categoryChildrenMap, 
                    stockItemsByCategory, groupPoid, filterValue, 0, companyPoid, userPoid);
            
            // Only add if node or its children match the filter
            if (shouldIncludeNode(node, filterValue)) {
                tree.add(node);
            }
        }
        
        return tree;
    }

    /**
     * Recursively build tree node with children
     */
    private Map<String, Object> buildTreeNode(
            StockCategoryMasterEntity category,
            Map<Long, StockCategoryMasterEntity> categoryMap,
            Map<Long, List<StockCategoryMasterEntity>> categoryChildrenMap,
            Map<Long, List<StockMasterEntity>> stockItemsByCategory,
            Long groupPoid,
            String filterValue,
            int level,
            Long companyPoid,
            Long userPoid) {
        
        Map<String, Object> node = new HashMap<>();
        
        // Determine type based on level
        String type = (level == 0) ? "MAIN_GROUP" : "SUB_GROUP";
        
        // Add category fields
        node.put("categoryPoid", category.getCategoryPoid());
        node.put("categoryCode", category.getCategoryCode());
        node.put("categoryName", category.getCategoryName());
        node.put("type", type);
        node.put("level", level);
        node.put("active", true);
        node.put("deleted", false);
        node.put("groupPoid", category.getGroupPoid());
        if (category.getParentCategoryPoid() != null) {
            node.put("parentPoid", category.getParentCategoryPoid());
        }
        
        // Add metadata fields
        node.put("id", "row-" + category.getCategoryPoid());
        node.put("isExpanded", false);
        node.put("isRowGroup", true);
        
        // Add companyPoid and userPoid for tracking (if provided)
        if (companyPoid != null) {
            node.put("companyPoid", companyPoid);
        }
        if (userPoid != null) {
            node.put("userPoid", userPoid);
        }
        
        // Build children array
        List<Map<String, Object>> children = new ArrayList<>();
        
        // Add child categories (SUB_GROUP)
        List<StockCategoryMasterEntity> childCategories = categoryChildrenMap.getOrDefault(
                category.getCategoryPoid(), new ArrayList<>());
        
        for (StockCategoryMasterEntity childCategory : childCategories) {
            Map<String, Object> childNode = buildTreeNode(childCategory, categoryMap, categoryChildrenMap,
                    stockItemsByCategory, groupPoid, filterValue, level + 1, companyPoid, userPoid);
            
            if (shouldIncludeNode(childNode, filterValue)) {
                children.add(childNode);
            }
        }
        
        // Add stock items (LEDGER) for this category
        // If no filter: only add to leaf nodes (categories with no children)
        // If filter exists: add to all matching categories
        boolean shouldAddStockItems = childCategories.isEmpty() || 
                (filterValue != null && !filterValue.trim().isEmpty());
        
        if (shouldAddStockItems) {
            List<StockMasterEntity> stockItems = stockItemsByCategory.getOrDefault(
                    category.getCategoryPoid(), new ArrayList<>());
            
            for (StockMasterEntity stock : stockItems) {
                // Apply filter if provided
                if (filterValue == null || filterValue.trim().isEmpty() || 
                    matchesFilter(stock, filterValue)) {
                    Map<String, Object> stockNode = convertStockToTreeItem(stock, level + 1, companyPoid, userPoid);
                    stockNode.put("parentPoid", category.getCategoryPoid());
                    children.add(stockNode);
                }
            }
        }
        
        node.put("children", children);
        
        return node;
    }

    /**
     * Convert stock entity to tree item format with metadata
     */
    private Map<String, Object> convertStockToTreeItem(StockMasterEntity stock, int level, Long companyPoid, Long userPoid) {
        Map<String, Object> item = convertStockToHierarchicalItem(stock, level);
        
        // Add metadata fields
        item.put("id", "row-" + stock.getStockPoid());
        item.put("isExpanded", false);
        item.put("isRowGroup", true);
        item.put("children", new ArrayList<>()); // LEDGER items have no children
        
        // Add companyPoid and userPoid for tracking (if provided)
        if (companyPoid != null) {
            item.put("companyPoid", companyPoid);
        }
        if (userPoid != null) {
            item.put("userPoid", userPoid);
        }
        
        return item;
    }

    /**
     * Check if stock item matches filter
     */
    private boolean matchesFilter(StockMasterEntity stock, String filterValue) {
        if (filterValue == null || filterValue.trim().isEmpty()) {
            return true;
        }
        
        String filter = filterValue.toLowerCase().trim();
        
        // Check stockCode
        if (stock.getStockCode() != null) {
            String stockCode = stock.getStockCode().toLowerCase().trim();
            if (stockCode.contains(filter)) {
                return true;
            }
        }
        
        // Check stockName
        if (stock.getStockName() != null) {
            String stockName = stock.getStockName().toLowerCase().trim();
            if (stockName.contains(filter)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if node should be included based on filter
     * Include if node matches filter or has matching children
     */
    private boolean shouldIncludeNode(Map<String, Object> node, String filterValue) {
        if (filterValue == null || filterValue.trim().isEmpty()) {
            return true;
        }
        
        String filter = filterValue.toLowerCase().trim();
        
        // Check if node itself matches (for categories)
        String categoryName = (String) node.get("categoryName");
        String categoryCode = (String) node.get("categoryCode");
        
        if (categoryName != null && categoryName.toLowerCase().trim().contains(filter)) {
            return true;
        }
        if (categoryCode != null && categoryCode.toLowerCase().trim().contains(filter)) {
            return true;
        }
        
        // Check if node is a stock item (LEDGER) and matches
        String type = (String) node.get("type");
        if ("LEDGER".equals(type)) {
            String stockName = (String) node.get("stockName");
            String stockCode = (String) node.get("stockCode");
            
            if (stockName != null && stockName.toLowerCase().trim().contains(filter)) {
                return true;
            }
            if (stockCode != null && stockCode.toLowerCase().trim().contains(filter)) {
                return true;
            }
        }
        
        // Check if any children match
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> children = (List<Map<String, Object>>) node.get("children");
        if (children != null && !children.isEmpty()) {
            // Check if any child matches
            for (Map<String, Object> child : children) {
                if (shouldIncludeNode(child, filterValue)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public StockDetailsResponse getStockDetails(Long stockPoid, Long companyPoid) {
        logger.info("getStockDetails started for stockPoid={} companyPoid={}", stockPoid, companyPoid);
        
        // Fetch stock details with category, tax, and unit in a single query
        List<Object[]> results = stockMasterRepository.findStockDetailsWithCategoryAndTax(stockPoid);
        
        if (results.isEmpty()) {
            logger.warn("Stock not found for stockPoid={}", stockPoid);
            throw new ResourceNotFoundException("Stock", "stockPoid", stockPoid);
        }
        
        Object[] row = results.get(0);
        StockDetailsResponse response = populateStockDetailsFromQueryResult(row);
        
        logger.info("getStockDetails completed for stockPoid={}", stockPoid);
        return response;
    }
    
    /**
     * Populate StockDetailsResponse from query result
     * Column order: stock fields (0-16), category fields (17-19), tax fields (20-23), unit fields (24-26)
     */
    private StockDetailsResponse populateStockDetailsFromQueryResult(Object[] row) {
        StockDetailsResponse response = new StockDetailsResponse();
        int index = 0;
        
        // Stock Master fields (indices 0-16)
        response.setStockPoid(getLongValueFromRow(row[index++]));
        response.setStockCode(getStringValueFromRow(row[index++]));
        response.setStockName(getStringValueFromRow(row[index++]));
        response.setStockName2(getStringValueFromRow(row[index++]));
        response.setStockDescription(getStringValueFromRow(row[index++]));
        response.setStockUnitPoid(getLongValueFromRow(row[index++]));
        response.setStockCost(getBigDecimalValueFromRow(row[index++]));
        response.setTagPrice(getBigDecimalValueFromRow(row[index++]));
        response.setRetailPrice(getBigDecimalValueFromRow(row[index++]));
        response.setWholesalePrice(getBigDecimalValueFromRow(row[index++]));
        response.setPrice1(getBigDecimalValueFromRow(row[index++]));
        response.setPrice2(getBigDecimalValueFromRow(row[index++]));
        response.setPrice3(getBigDecimalValueFromRow(row[index++]));
        response.setCurrencyCode(getStringValueFromRow(row[index++]));
        response.setBarcode(getStringValueFromRow(row[index++]));
        response.setActive(getStringValueFromRow(row[index++]));
        response.setDeleted(getStringValueFromRow(row[index++]));
        
        // Category Details (indices 17-19)
        StockDetailsResponse.CategoryDetailDto categoryDetails = new StockDetailsResponse.CategoryDetailDto();
        categoryDetails.setCategoryPoid(getLongValueFromRow(row[index++]));
        categoryDetails.setCategoryCode(getStringValueFromRow(row[index++]));
        categoryDetails.setCategoryName(getStringValueFromRow(row[index++]));
        response.setCategoryDetails(categoryDetails);
        
        // Tax Details (indices 20-23)
        StockDetailsResponse.TaxDetailDto taxDetails = new StockDetailsResponse.TaxDetailDto();
        taxDetails.setTaxPoid(getLongValueFromRow(row[index++]));
        taxDetails.setTaxCode(getStringValueFromRow(row[index++]));
        taxDetails.setTaxName(getStringValueFromRow(row[index++]));
        taxDetails.setTaxPercentage(getBigDecimalValueFromRow(row[index++]));
        response.setTaxDetails(taxDetails);
        
        // Unit Details (indices 24-26)
        StockDetailsResponse.UnitDetailDto unitDetails = new StockDetailsResponse.UnitDetailDto();
        unitDetails.setUnitPoid(getLongValueFromRow(row[index++]));
        unitDetails.setUnitCode(getStringValueFromRow(row[index++]));
        unitDetails.setUnitName(getStringValueFromRow(row[index++]));
        response.setUnitDetails(unitDetails);
        
        return response;
    }
    
    /**
     * Helper method to safely extract Long value from Object[]
     */
    private Long getLongValueFromRow(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return null;
    }
    
    /**
     * Helper method to safely extract String value from Object[]
     */
    private String getStringValueFromRow(Object obj) {
        return obj != null ? obj.toString() : null;
    }
    
    /**
     * Helper method to safely extract BigDecimal value from Object[]
     */
    private BigDecimal getBigDecimalValueFromRow(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        }
        if (obj instanceof Number) {
            return BigDecimal.valueOf(((Number) obj).doubleValue());
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public StockDetailsResponse getStockDetailsByCode(String stockCode, Long companyPoid) {
        logger.info("getStockDetailsByCode started for stockCode={} companyPoid={}", stockCode, companyPoid);

        if (stockCode == null || stockCode.trim().isEmpty()) {
            throw new IllegalArgumentException("stockCode is required");
        }

        // Fetch stock details with category, tax, and unit in a single query by stock code
        List<Object[]> results = stockMasterRepository.findStockDetailsWithCategoryAndTaxByCode(stockCode.trim());

        if (results.isEmpty()) {
            logger.warn("Stock not found for stockCode={}, returning empty response", stockCode);
            return new StockDetailsResponse();
        }

        Object[] row  = results.get(0);
        StockDetailsResponse response = populateStockDetailsFromQueryResult(row);

        logger.info("getStockDetailsByCode completed for stockCode={}", stockCode);
        return response;
    }

}
