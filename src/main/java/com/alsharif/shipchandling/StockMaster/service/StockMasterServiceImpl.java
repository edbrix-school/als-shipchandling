package com.alsharif.shipchandling.StockMaster.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alsharif.shipchandling.StockMaster.dto.CreateStockMasterDtlRequest;
import com.alsharif.shipchandling.StockMaster.dto.CreateStockMasterRequest;
import com.alsharif.shipchandling.StockMaster.dto.CreateStockMasterWarehouseDtlRequest;
import com.alsharif.shipchandling.StockMaster.dto.StockMasterDependenciesDto;
import com.alsharif.shipchandling.StockMaster.dto.StockMasterDtlDto;
import com.alsharif.shipchandling.StockMaster.dto.StockMasterDto;
import com.alsharif.shipchandling.StockMaster.dto.StockMasterViewResponse;
import com.alsharif.shipchandling.StockMaster.dto.StockMasterWarehouseDtlDto;
import com.alsharif.shipchandling.StockMaster.dto.UpdateStockMasterRequest;
import com.alsharif.shipchandling.StockMaster.dto.ValidationResponse;
import com.alsharif.shipchandling.StockMaster.entity.StockMasterDTLEntity;
import com.alsharif.shipchandling.StockMaster.entity.StockMasterDtlId;
import com.alsharif.shipchandling.StockMaster.entity.StockMasterEntity;
import com.alsharif.shipchandling.StockMaster.entity.StockMasterWarehouseDtl;
import com.alsharif.shipchandling.StockMaster.entity.StockMasterWarehouseDtlId;
import com.alsharif.shipchandling.StockMaster.repository.StockMasterDtlRepository;
import com.alsharif.shipchandling.StockMaster.repository.StockMasterRepository;
import com.alsharif.shipchandling.StockMaster.repository.StockMasterWarehouseDtlRepository;
import com.alsharif.shipchandling.StockMaster.repository.StockCategoryMasterRepository;
import com.alsharif.shipchandling.StockMaster.entity.StockCategoryMasterEntity;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;

import org.springframework.transaction.annotation.Transactional;

import oracle.jdbc.OracleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.Optional;
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
        if (entity == null)
            return null;

        if ("Y".equalsIgnoreCase(entity.getDeleted())) {
            return null;
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
     * @param groupPoid The group POID associated with the stock master
     * @param companyPoid The company POID associated with the stock master
     * @param userId The user ID performing the operation
     * @param stockPoid The stock master POID (primary key)
     * @param serviceItem Indicates if the stock item is a service item ("Y" or "N")
     * 
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
            Long detRowId = 1L;
            for (CreateStockMasterDtlRequest dtlRequest : request.getSupplierDetails()) {
                StockMasterDTLEntity dtl = new StockMasterDTLEntity();
                dtl.setStockPoid(stockPoid); // must match parent exactly
                dtl.setDetRowId(detRowId++);
                dtl.setSupplierPoid(dtlRequest.getSupplierPoid());
                dtl.setSupplierStockCode(dtlRequest.getSupplierStockCode());
                dtl.setRemarks(dtlRequest.getRemarks());
                dtl.setCreatedBy(userId);
                dtl.setLastmodifiedBy(userId);
                // System.out.println("Supplier after setting values: " +
                // "StockPoid=" + dtl.getStockPoid() +
                // ", DetRowId=" + dtl.getDetRowId() +
                // ", SupplierPoid=" + dtl.getSupplierPoid() +
                // ", SupplierStockCode=" + dtl.getSupplierStockCode() +
                // ", Remarks=" + dtl.getRemarks());
                dtlRepository.save(dtl);
            }
            dtlRepository.flush(); // commit child rows
        }

        // --- Save Warehouse Details ---
        if (request.getWarehouseDetails() != null && !request.getWarehouseDetails().isEmpty()) {
            Long detRowId = 1L;
            for (CreateStockMasterWarehouseDtlRequest whRequest : request.getWarehouseDetails()) {
                StockMasterWarehouseDtl wh = new StockMasterWarehouseDtl();
                wh.setStockPoid(stockPoid);
                wh.setDetRowId(detRowId++);
                wh.setTransactionDate(whRequest.getTransactionDate());
                wh.setLocationPoid(whRequest.getLocationPoid());
                wh.setAisleNo(whRequest.getAisleNo());
                wh.setBayNo(whRequest.getBayNo());
                wh.setShelfNo(whRequest.getShelfNo());
                wh.setBinNo(whRequest.getBinNo());
                wh.setReorderLevel(whRequest.getReorderLevel());
                wh.setReorderQty(whRequest.getReorderQty());
                wh.setCreatedBy(userId);
                wh.setLastmodifiedBy(userId);
                // System.out.println("warehouse after setting values: " +
                // "StockPoid=" + wh.getStockPoid());
                warehouseRepository.save(wh);
            }
            warehouseRepository.flush();
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
        updateSupplierDetails(stockPoid, request.getSupplierDetails(), userId);
        updateWarehouseDetails(stockPoid, request.getWarehouseDetails(), userId);

        // Save
        StockMasterEntity savedStock = stockMasterRepository.save(stock);

        // just call the function
        callAfterSaveProcedure(stockPoid);

        return convertToDto(savedStock, true);
    }

    private void saveSupplierDetails(Long stockPoid, List<CreateStockMasterDtlRequest> details, String userId) {
        Long detRowId = 1L;
        for (CreateStockMasterDtlRequest detail : details) {
            StockMasterDTLEntity dtl = new StockMasterDTLEntity();
            dtl.setStockPoid(stockPoid);
            dtl.setDetRowId(detRowId++);
            dtl.setSupplierPoid(detail.getSupplierPoid());
            dtl.setSupplierStockCode(detail.getSupplierStockCode());
            dtl.setRemarks(detail.getRemarks());
            dtl.setCreatedBy(userId);
            dtl.setLastmodifiedBy(userId);
            dtlRepository.save(dtl);
        }
    }

    private void saveWarehouseDetails(Long stockPoid, List<CreateStockMasterWarehouseDtlRequest> details,
            String userId) {
        Long detRowId = 1L;
        for (CreateStockMasterWarehouseDtlRequest detail : details) {
            StockMasterWarehouseDtl dtl = new StockMasterWarehouseDtl();
            dtl.setStockPoid(stockPoid);
            dtl.setDetRowId(detRowId++);
            dtl.setTransactionDate(detail.getTransactionDate());
            dtl.setLocationPoid(detail.getLocationPoid());
            dtl.setAisleNo(detail.getAisleNo());
            dtl.setBayNo(detail.getBayNo());
            dtl.setShelfNo(detail.getShelfNo());
            dtl.setBinNo(detail.getBinNo());
            dtl.setReorderLevel(detail.getReorderLevel());
            dtl.setReorderQty(detail.getReorderQty());
            dtl.setCreatedBy(userId);
            dtl.setLastmodifiedBy(userId);
            warehouseRepository.save(dtl);
        }
    }

    private void updateSupplierDetails(Long stockPoid, List<CreateStockMasterDtlRequest> details, String userId) {
        // Delete existing
        dtlRepository.deleteByStockPoid(stockPoid);
        // Save new
        if (details != null && !details.isEmpty()) {
            saveSupplierDetails(stockPoid, details, userId);
        }
    }

    private void updateWarehouseDetails(Long stockPoid, List<CreateStockMasterWarehouseDtlRequest> details,
            String userId) {
        // Delete existing
        warehouseRepository.deleteByStockPoid(stockPoid);
        // Save new
        if (details != null && !details.isEmpty()) {
            saveWarehouseDetails(stockPoid, details, userId);
        }
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

}
