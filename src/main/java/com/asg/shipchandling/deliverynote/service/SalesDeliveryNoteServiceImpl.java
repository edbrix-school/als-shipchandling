package com.asg.shipchandling.deliverynote.service;

import com.asg.shipchandling.deliverynote.dto.*;
import com.asg.shipchandling.deliverynote.entity.*;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.deliverynote.repository.*;
import com.asg.shipchandling.deliverynote.repository.SalesDeliveryNoteHdrRepositoryImpl;
import com.asg.shipchandling.salesinvoice.repository.SalesDnDtlRepository;
import com.asg.shipchandling.StockMaster.entity.StockMasterEntity;
import com.asg.shipchandling.StockMaster.repository.StockMasterRepository;
import com.asg.shipchandling.stockunitmaster.entity.StockUnitMaster;
import com.asg.shipchandling.stockunitmaster.repository.StockUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesDeliveryNoteServiceImpl implements SalesDeliveryNoteService {

    private final SalesDeliveryNoteHdrRepository deliveryNoteHdrRepository;
    private final SalesDeliveryNoteItemDtlRepository itemDtlRepository;
    private final SalesDeliveryNoteRepository salesDeliveryNoteRepository;
    private final SalesDeliveryNoteHdrRepositoryImpl deliveryNoteHdrRepositoryImpl;
    private final SalesDnDtlRepository salesDnDtlRepository;
    private final StockMasterRepository stockMasterRepository;
    private final StockUnitRepository stockUnitRepository;

    // Add OracleDataSource or DataSource injection for stored procedure calls
    private final DataSource dataSource;

    @Override
    @Transactional
    public SalesDeliveryNoteHdrDto createDeliveryNote(CreateSalesDeliveryNoteRequest request, Long groupPoid,
            Long companyPoid, String userId) {
        log.info("createDeliveryNote service started for  groupPoid={} userId={}",
                groupPoid, userId);
        // Validate required fields
        validateDeliveryNoteRequest(request);

        // Create entity
        SalesDeliveryNoteHdr deliveryNote = new SalesDeliveryNoteHdr();
        BeanUtils.copyProperties(request, deliveryNote);
        deliveryNote.setCompanyPoid(companyPoid);
        deliveryNote.setCreatedBy(userId);
        deliveryNote.setLastmodifiedBy(userId);
        deliveryNote.setDeleted("N");
        deliveryNote
                .setDescriptionPrintYn(request.getDescriptionPrintYn() != null ? request.getDescriptionPrintYn() : "Y");

        // Save to get transactionPoid
        SalesDeliveryNoteHdr savedDeliveryNote = deliveryNoteHdrRepository.save(deliveryNote);
        deliveryNoteHdrRepository.flush();
        log.info("createDeliveryNote persisted groupPoid={} userId={} transactionPoid={} ", groupPoid, userId,
                savedDeliveryNote.getTransactionPoid());

        // Save item details (only items with CheckAll = "Y")
        if (request.getItemDetails() != null && !request.getItemDetails().isEmpty()) {
            saveItemDetails(savedDeliveryNote.getTransactionPoid(),
                    request.getItemDetails().stream()
                            .filter(item -> "Y".equals(item.getCheckAll()))
                            .collect(Collectors.toList()),
                    userId);
        }

        // Calculate totals
        calculateTotals(savedDeliveryNote.getTransactionPoid());

        // Refresh to get auto-generated DocRef
        deliveryNoteHdrRepository.flush();
        SalesDeliveryNoteHdr refreshedDeliveryNote = deliveryNoteHdrRepository.findByTransactionPoid(
                savedDeliveryNote.getTransactionPoid()).orElse(savedDeliveryNote);

        // Convert to DTO
        SalesDeliveryNoteHdrDto dto = convertToDto(refreshedDeliveryNote, true);
        log.info("createDeliveryNote completed for groupPoid={} userId={} transactionPoid={}", groupPoid, userId,
                dto.getTransactionPoid());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesDeliveryNoteHdrDto getDeliveryNoteByPoid(Long groupPoid, Long transactionPoid,
            Long companyPoid, Boolean includeDetails) {
        log.info("getDeliveryNoteByPoid service started for transactionPoid={} groupPoid={}", transactionPoid,
                groupPoid);
        
        // Validate delivery note exists
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        if ("Y".equals(deliveryNote.getDeleted())) {
            log.warn("getDeliveryNoteByPoid found companyPoid={} transactionPoid={} marked as deleted", companyPoid,
                    transactionPoid);
            throw new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid);
        }
        
        // Fetch delivery note with all LOV details in a single query
        List<Object[]> results = deliveryNoteHdrRepository.findDeliveryNoteWithDetails(transactionPoid, companyPoid);
        SalesDeliveryNoteHdrDto dto;
        
        if (!results.isEmpty()) {
            Object[] row = results.get(0);
            dto = populateDeliveryNoteFromQueryResult(row, includeDetails != null && includeDetails);
        } else {
            // Fallback: use entity if query fails
            dto = convertToDto(deliveryNote, includeDetails != null && includeDetails);
            setEmptyLovDetails(dto);
        }
        
        log.info("getDeliveryNoteByPoid completed for transactionPoid={} companyPoid={}",
                transactionPoid, companyPoid);
        return dto;
    }

    @Override
    @Transactional
    public SalesDeliveryNoteHdrDto updateDeliveryNote(Long groupPoid, Long transactionPoid,
            CreateSalesDeliveryNoteRequest request,
            Long companyPoid, String userId) {
        log.info("updateDeliveryNote service started for transactionPoid={} groupPoid={}", transactionPoid, groupPoid);
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        if ("Y".equals(deliveryNote.getDeleted())) {
            log.warn("updateDeliveryNote found companyPoid={} transactionPoid={} marked as deleted", companyPoid,
                    transactionPoid);
            throw new CustomException("Cannot update deleted delivery note");
        }

        // Check if status allows editing
        if ("CLOSED".equals(deliveryNote.getDeliveryStatus())) {
            log.warn("updateDeliveryNote found companyPoid={} transactionPoid={} in closed status", companyPoid,
                    transactionPoid);
            throw new CustomException("Cannot update delivery note. Current document is in closed status");
        }

        // Validate customer change if customer is being changed
        if (!deliveryNote.getCustomerPoid().equals(request.getCustomerPoid())) {
            ValidateCustomerChangeResponse validation = validateCustomerChange(
                    transactionPoid, companyPoid);
            if (!validation.getCanChange()) {
                throw new CustomException(validation.getMessage());
            }
        }

        // Validate required fields
        validateDeliveryNoteRequest(request);

        // Update fields (excluding read-only fields)
        BeanUtils.copyProperties(request, deliveryNote, "transactionPoid", "docRef", "createdBy",
                "createdDate", "qtnRefNo", "deliveryStatus");
        deliveryNote.setLastmodifiedBy(userId);

        // Remove items with CheckAll = "N" before updating
        // itemDtlRepository.deleteByTransactionPoidAndCheckAllN(transactionPoid);

        // Process item details based on actionType (UPDATE, DELETE, or CREATE)
        if (request.getItemDetails() != null && !request.getItemDetails().isEmpty()) {
            processItemDetailsByActionType(transactionPoid, 
                    request.getItemDetails(),
                    // .stream()
                    // .filter(item -> item.getActionType() != null)
                    //         .filter(item -> "Y".equals(item.getCheckAll()))
                    //         .collect(Collectors.toList()),
                    userId);
        }

        // Save
        SalesDeliveryNoteHdr savedDeliveryNote = deliveryNoteHdrRepository.save(deliveryNote);
        deliveryNoteHdrRepository.flush();
        // Calculate totals
        calculateTotals(transactionPoid);

        // Call stored procedure to update quotation header with deleted details
        // callUpdateDeletedDetailsProcedure(groupPoid, companyPoid, userId,
        // transactionPoid);

        SalesDeliveryNoteHdrDto dto = convertToDto(savedDeliveryNote, true);
        log.info("updateDeliveryNote completed for transactionPoid={} companyPoid={}",
                deliveryNote.getTransactionPoid(), deliveryNote.getCompanyPoid());
        return dto;
    }

    @Override
    @Transactional
    public void deleteDeliveryNote(Long groupPoid, Long transactionPoid, Long companyPoid) {
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        if ("Y".equals(deliveryNote.getDeleted())) {
            log.warn("deleteDeliveryNote found companyPoid={} transactionPoid={} already deleted", companyPoid,
                    transactionPoid);
            throw new CustomException("Cannot delete delivery note. It is already deleted.");
        }
        if ("CLOSED".equals(deliveryNote.getDeliveryStatus())) {
            log.warn("deleteDeliveryNote found companyPoid={} transactionPoid={} in closed status", companyPoid,
                    transactionPoid);
            throw new CustomException("Cannot delete delivery note. It is closed.");
        }

        // Delete item details
        itemDtlRepository.deleteByTransactionPoid(transactionPoid);

        // Soft delete
        deliveryNote.setDeleted("Y");
        deliveryNoteHdrRepository.save(deliveryNote);
        log.info("deleteDeliveryNote completed for transactionPoid={} companyPoid={}",
                deliveryNote.getTransactionPoid(), deliveryNote.getCompanyPoid());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<SalesDeliveryNoteHdrDto> getAllDeliveryNotes(Long groupPoid, Long companyPoid,
            String deliveryStatus, Long customerPoid, Long salesmanPoid, String qtnRefNo, Timestamp fromDate,
            Timestamp toDate, String search, Integer page, Integer size) {
        log.info("getAllDeliveryNotes service started for groupPoid={} companyPoid={} page={} size={}", 
                groupPoid, companyPoid, page, size);
        
        // Set default values for pagination
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 10; // Default page size is 10
        
        // Create Pageable with sorting by transaction date descending
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("transactionDate").descending());
        
        // Use the repository implementation method with filters and customer name
        Page<Object[]> deliveryNotesPage = deliveryNoteHdrRepositoryImpl.findAllWithFiltersAndCustomerName(
                companyPoid, deliveryStatus, customerPoid, salesmanPoid, qtnRefNo, fromDate, toDate, search, pageable);
        
        // Convert to DTOs - Object[] contains [SalesDeliveryNoteHdr, customerName]
        List<SalesDeliveryNoteHdrDto> data = deliveryNotesPage.getContent().stream()
                .map(result -> {
                    SalesDeliveryNoteHdr entity = (SalesDeliveryNoteHdr) result[0];
                    String customerName = (String) result[1];
                    SalesDeliveryNoteHdrDto dto = convertToDto(entity, false);
                    dto.setCustomerName(customerName);
                    return dto;
                })
                .collect(Collectors.toList());
        
        // Create paginated response
        PaginatedResponse<SalesDeliveryNoteHdrDto> response = new PaginatedResponse<>();
        response.setData(data);
        response.setPage(deliveryNotesPage.getNumber());
        response.setSize(deliveryNotesPage.getSize());
        response.setTotalElements(deliveryNotesPage.getTotalElements());
        response.setTotalPages(deliveryNotesPage.getTotalPages());
        response.setFirst(deliveryNotesPage.isFirst());
        response.setLast(deliveryNotesPage.isLast());
        
        log.info("getAllDeliveryNotes completed for groupPoid={} companyPoid={} totalElements={}", 
                groupPoid, companyPoid, response.getTotalElements());
        return response;
    }

    // Validation Methods
    @Override
    @Transactional(readOnly = true)
    public ValidationResponse validateDocRef(String docRef, Long transactionPoid) {
        if (docRef == null || docRef.trim().isEmpty()) {
            log.warn("validateDocRef called with empty docRef");
            return new ValidationResponse(false, "Document reference cannot be empty");
        }

        boolean exists;
        if (transactionPoid != null) {
            exists = deliveryNoteHdrRepository.existsByDocRefIgnoreCaseAndTransactionPoidNot(
                    docRef, transactionPoid);
        } else {
            exists = deliveryNoteHdrRepository.existsByDocRefIgnoreCase(docRef);
        }

        ValidationResponse response = new ValidationResponse();
        response.setIsUnique(!exists);
        response.setMessage(exists ? "Document reference already exists" : "Document reference is available");
        log.info("validateDocRef completed for docRef={} transactionPoid={}", docRef, transactionPoid);
        return response;
    }

    // Item Details Methods
    @Override
    @Transactional
    public SalesDeliveryNoteItemDtlDto addItemDetail(Long transactionPoid,
            CreateSalesDeliveryNoteItemDtlRequest request,
            Long companyPoid, String userId) {
        log.info("addItemDetail called for transactionPoid={} companyPoid={}", transactionPoid, companyPoid);
        // Validate delivery note exists
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        if ("Y".equals(deliveryNote.getDeleted())) {
            log.warn("addItemDetail called for deleted delivery note transactionPoid={} companyPoid={}",
                    transactionPoid, companyPoid);
            throw new CustomException("Cannot add item details. Delivery note is deleted");
        }

        if ("CLOSED".equals(deliveryNote.getDeliveryStatus())) {
            log.warn("addItemDetail called for closed delivery note transactionPoid={} companyPoid={}", transactionPoid,
                    companyPoid);
            throw new CustomException("Cannot add item details. Delivery note is closed");
        }

        // Get next DetRowId
        Long maxDetRowId = itemDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        // Create item detail
        SalesDeliveryNoteItemDtl itemDtl = new SalesDeliveryNoteItemDtl();
        itemDtl.setTransactionPoid(transactionPoid);
        itemDtl.setDetRowId(detRowId);
        itemDtl.setStockPoid(request.getStockPoid());
        itemDtl.setStockUnitPoid(request.getStockUnitPoid());
        itemDtl.setQuantity(request.getQuantity());
        itemDtl.setPrice(request.getPrice());
        itemDtl.setDiscount(request.getDiscount() != null ? request.getDiscount() : 0L);
        itemDtl.setAmount(request.getAmount());
        itemDtl.setRemarks(request.getRemarks());
        itemDtl.setCheckAll(request.getCheckAll() != null ? request.getCheckAll() : "Y");
        itemDtl.setQtnDetRowId(request.getQtnDetRowId());
        itemDtl.setTotCost(request.getTotCost());
        itemDtl.setItemType(request.getItemType());
        itemDtl.setCreatedBy(userId);
        itemDtl.setLastmodifiedBy(userId);

        SalesDeliveryNoteItemDtl savedItemDtl = itemDtlRepository.save(itemDtl);

        // Recalculate totals
        calculateTotals(transactionPoid);

        SalesDeliveryNoteItemDtlDto dto = convertItemDtlToDto(savedItemDtl);
        log.info("addItemDetail completed for companyPoid={} transactionPoid={} detRowId={}", companyPoid,
                transactionPoid, detRowId);
        return dto;
    }

    @Override
    @Transactional
    public SalesDeliveryNoteItemDtlDto updateItemDetail(Long transactionPoid, Long detRowId,
            CreateSalesDeliveryNoteItemDtlRequest request,
            Long companyPoid, String userId) {
        log.info("updateItemDetail called for transactionPoid={} detRowId={} companyPoid={}", transactionPoid, detRowId,
                companyPoid);
        // Validate delivery note exists
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        if ("Y".equals(deliveryNote.getDeleted())) {
            log.warn("updateItemDetail called for deleted delivery note transactionPoid={} detRowId={} companyPoid={}",
                    transactionPoid, detRowId, companyPoid);
            throw new CustomException("Cannot update item details. Delivery note is deleted");
        }

        if ("CLOSED".equals(deliveryNote.getDeliveryStatus())) {
            log.warn("updateItemDetail called for closed delivery note transactionPoid={} detRowId={} companyPoid={}",
                    transactionPoid, detRowId, companyPoid);
            throw new CustomException("Cannot update item details. Delivery note is closed");
        }

        // Find existing item detail
        SalesDeliveryNoteItemDtl itemDtl = itemDtlRepository
                .findById(new SalesDeliveryNoteItemDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Item Detail", "detRowId", detRowId));

        // Check if item is from quotation (read-only fields)
        if (itemDtl.getQtnDetRowId() != null && itemDtl.getQtnDetRowId() > 0) {
            // StockPoid and Quantity are read-only if loaded from quotation
            if (request.getStockPoid() != null && !request.getStockPoid().equals(itemDtl.getStockPoid())) {
                log.warn(
                        "Attempt to change stockPoid for item loaded from quotation. transactionPoid={} detRowId={} companyPoid={}",
                        transactionPoid, detRowId, companyPoid);
                throw new CustomException("Cannot change stock. Item is loaded from quotation.");
            }
            if (request.getQuantity() != null && !request.getQuantity().equals(itemDtl.getQuantity())) {
                log.warn(
                        "Attempt to change quantity for item loaded from quotation. transactionPoid={} detRowId={} companyPoid={}",
                        transactionPoid, detRowId, companyPoid);
                throw new CustomException("Cannot change quantity. Item is loaded from quotation.");
            }
        }

        // Update fields
        if (itemDtl.getQtnDetRowId() == null || itemDtl.getQtnDetRowId() == 0) {
            itemDtl.setStockPoid(request.getStockPoid());
            itemDtl.setQuantity(request.getQuantity());
        }
        itemDtl.setStockUnitPoid(request.getStockUnitPoid());
        itemDtl.setPrice(request.getPrice());
        itemDtl.setDiscount(request.getDiscount() != null ? request.getDiscount() : 0L);
        itemDtl.setAmount(request.getAmount());
        itemDtl.setRemarks(request.getRemarks());
        itemDtl.setCheckAll(request.getCheckAll() != null ? request.getCheckAll() : "Y");
        itemDtl.setTotCost(request.getTotCost());
        itemDtl.setItemType(request.getItemType());
        itemDtl.setLastmodifiedBy(userId);

        SalesDeliveryNoteItemDtl savedItemDtl = itemDtlRepository.save(itemDtl);

        // Recalculate totals
        calculateTotals(transactionPoid);
        SalesDeliveryNoteItemDtlDto dto = convertItemDtlToDto(savedItemDtl);
        log.info("updateItemDetail completed for transactionPoid={} detRowId={} companyPoid={}", transactionPoid,
                detRowId, companyPoid);
        return dto;
    }

    @Override
    @Transactional
    public void deleteItemDetail(Long transactionPoid, Long detRowId, Long companyPoid) {
        log.info("deleteItemDetail called for transactionPoid={} detRowId={} companyPoid={}", transactionPoid, detRowId,
                companyPoid);
        // Validate delivery note exists
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        if ("Y".equals(deliveryNote.getDeleted())) {
            log.warn("deleteItemDetail called for deleted delivery note transactionPoid={} detRowId={} companyPoid={}",
                    transactionPoid, detRowId, companyPoid);
            throw new CustomException("Cannot delete item details. Delivery note is deleted");
        }

        if ("CLOSED".equals(deliveryNote.getDeliveryStatus())) {
            log.warn("deleteItemDetail called for closed delivery note transactionPoid={} detRowId={} companyPoid={}",
                    transactionPoid, detRowId, companyPoid);
            throw new CustomException("Cannot delete item details. Delivery note is closed");
        }

        // Delete item detail
        itemDtlRepository.deleteById(new SalesDeliveryNoteItemDtlId(transactionPoid, detRowId));
        log.info("deleteItemDetail completed for transactionPoid={} detRowId={} companyPoid={}", transactionPoid,
                detRowId, companyPoid);
        // Recalculate totals
        calculateTotals(transactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesDeliveryNoteItemDtlDto> getItemDetails(Long transactionPoid, Long companyPoid) {
        log.info("getItemDetails called for transactionPoid={} companyPoid={}", transactionPoid, companyPoid);
        // Validate delivery note exists
        deliveryNoteHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        List<SalesDeliveryNoteItemDtl> itemDetails = itemDtlRepository.findByTransactionPoid(transactionPoid);
        return itemDetails.stream()
                .map(this::convertItemDtlToDto)
                .collect(Collectors.toList());
    }

    // Helper methods
    private void validateDeliveryNoteRequest(CreateSalesDeliveryNoteRequest request) {
        if (request.getCustomerPoid() == null) {
            log.warn("Validation failed : Customer is required, for customerPoid={}", request.getCustomerPoid());
            throw new CustomException("Customer is required");
        }
        if (request.getTransactionDate() == null) {
            log.warn("Validation failed : Transaction date is required, for customerPoid={}",
                    request.getCustomerPoid());
            throw new CustomException("Transaction date is required");
        }
    }

    private void saveItemDetails(Long transactionPoid, List<CreateSalesDeliveryNoteItemDtlRequest> details,
            String userId) {
        Long detRowId = 1L;
        for (CreateSalesDeliveryNoteItemDtlRequest detail : details) {
            SalesDeliveryNoteItemDtl itemDtl = new SalesDeliveryNoteItemDtl();
            itemDtl.setTransactionPoid(transactionPoid);
            itemDtl.setDetRowId(detRowId++);
            itemDtl.setStockPoid(detail.getStockPoid());
            itemDtl.setStockUnitPoid(detail.getStockUnitPoid());
            itemDtl.setQuantity(detail.getQuantity());
            itemDtl.setPrice(detail.getPrice());
            itemDtl.setDiscount(detail.getDiscount() != null ? detail.getDiscount() : 0L);
            itemDtl.setAmount(detail.getAmount());
            itemDtl.setRemarks(detail.getRemarks());
            itemDtl.setCheckAll(detail.getCheckAll() != null ? detail.getCheckAll() : "Y");
            itemDtl.setQtnDetRowId(detail.getQtnDetRowId());
            itemDtl.setTotCost(detail.getTotCost());
            itemDtl.setItemType(detail.getItemType());
            itemDtl.setCreatedBy(userId);
            itemDtl.setLastmodifiedBy(userId);
            itemDtlRepository.save(itemDtl);
        }
    }

    private void updateItemDetails(Long transactionPoid, List<CreateSalesDeliveryNoteItemDtlRequest> details,
            String userId) {
        // Delete existing
        itemDtlRepository.deleteByTransactionPoid(transactionPoid);
        // Save new
        if (details != null && !details.isEmpty()) {
            saveItemDetails(transactionPoid, details, userId);
        }
    }

    /**
     * Process item details based on actionType:
     * - DELETE: Delete the item by detRowId
     * - UPDATE: Update the existing item by detRowId
     * - CREATE/null: Add as new item
     */
    private void processItemDetailsByActionType(Long transactionPoid, 
            List<CreateSalesDeliveryNoteItemDtlRequest> details, String userId) {
        if (details == null || details.isEmpty()) {
            return;
        }

        List<CreateSalesDeliveryNoteItemDtlRequest> itemsToCreate = new ArrayList<>();
        
        for (CreateSalesDeliveryNoteItemDtlRequest item : details) {
            String actionType = item.getActionType();
            
            if ("DELETE".equalsIgnoreCase(actionType)) {
                // Delete item by detRowId
                if (item.getDetRowId() != null) {
                    try {
                        itemDtlRepository.deleteById(new SalesDeliveryNoteItemDtlId(transactionPoid, item.getDetRowId()));
                        log.debug("Deleted item detail transactionPoid={} detRowId={}", transactionPoid, item.getDetRowId());
                    } catch (Exception ex) {
                        log.warn("Failed to delete item detail transactionPoid={} detRowId={}: {}", 
                                transactionPoid, item.getDetRowId(), ex.getMessage());
                    }
                } else {
                    log.warn("DELETE action requires detRowId, skipping item transactionPoid={}", transactionPoid);
                }
            } else if ("UPDATE".equalsIgnoreCase(actionType)) {
                // Update existing item by detRowId
                if (item.getDetRowId() != null) {
                    try {
                        SalesDeliveryNoteItemDtl existingItem = itemDtlRepository
                                .findById(new SalesDeliveryNoteItemDtlId(transactionPoid, item.getDetRowId()))
                                .orElse(null);
                        
                        if (existingItem != null) {
                            // Check if item is from quotation (read-only fields)
                           /*  if (existingItem.getQtnDetRowId() != null && existingItem.getQtnDetRowId() > 0) {
                                // StockPoid and Quantity are read-only if loaded from quotation
                                if (item.getStockPoid() != null && !item.getStockPoid().equals(existingItem.getStockPoid())) {
                                    log.warn("Attempt to change stockPoid for item loaded from quotation. transactionPoid={} detRowId={}",
                                            transactionPoid, item.getDetRowId());
                                    throw new CustomException("Cannot change stock. Item is loaded from quotation.");
                                }
                                if (item.getQuantity() != null && !item.getQuantity().equals(existingItem.getQuantity())) {
                                    log.warn("Attempt to change quantity for item loaded from quotation. transactionPoid={} detRowId={}",
                                            transactionPoid, item.getDetRowId());
                                    throw new CustomException("Cannot change quantity. Item is loaded from quotation.");
                                }
                            }
                             */
                            // Update fields
                            if (existingItem.getQtnDetRowId() == null || existingItem.getQtnDetRowId() == 0) {
                                existingItem.setStockPoid(item.getStockPoid());
                                existingItem.setQuantity(item.getQuantity());
                            }
                            existingItem.setStockUnitPoid(item.getStockUnitPoid());
                            existingItem.setPrice(item.getPrice());
                            existingItem.setDiscount(item.getDiscount() != null ? item.getDiscount() : 0L);
                            existingItem.setAmount(item.getAmount());
                            existingItem.setRemarks(item.getRemarks());
                            existingItem.setCheckAll(item.getCheckAll() != null ? item.getCheckAll() : "Y");
                            existingItem.setTotCost(item.getTotCost());
                            existingItem.setItemType(item.getItemType());
                            existingItem.setLastmodifiedBy(userId);
                            
                            itemDtlRepository.save(existingItem);
                            log.debug("Updated item detail transactionPoid={} detRowId={}", transactionPoid, item.getDetRowId());
                        } else {
                            log.warn("Item not found for UPDATE action transactionPoid={} detRowId={}, treating as CREATE",
                                    transactionPoid, item.getDetRowId());
                            itemsToCreate.add(item);
                        }
                    } catch (CustomException ex) {
                        throw ex; // Re-throw custom exceptions
                    } catch (Exception ex) {
                        log.warn("Failed to update item detail transactionPoid={} detRowId={}: {}", 
                                transactionPoid, item.getDetRowId(), ex.getMessage());
                    }
                } else {
                    log.warn("UPDATE action requires detRowId, treating as CREATE transactionPoid={}", transactionPoid);
                    itemsToCreate.add(item);
                }
            } else {
                // CREATE or null actionType - add as new item
                itemsToCreate.add(item);
            }
        }
        
        // Save new items
        if (!itemsToCreate.isEmpty()) {
            saveItemDetails(transactionPoid, itemsToCreate, userId);
        }
    }

    private void calculateTotals(Long transactionPoid) {
        // Calculate total amount from item details (only items with CheckAll = "Y")
        Long totalAmount = itemDtlRepository.sumAmountByTransactionPoid(transactionPoid);
        if (totalAmount == null) {
            totalAmount = 0L;
        }

        // Calculate total discount
        Long totalDiscount = itemDtlRepository.sumDiscountByTransactionPoid(transactionPoid);
        if (totalDiscount == null) {
            totalDiscount = 0L;
        }

        // Update delivery note header
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository.findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));
        deliveryNote.setTotalAmount(totalAmount);
        deliveryNote.setTotalDiscount(totalDiscount);
        deliveryNoteHdrRepository.save(deliveryNote);
    }

    private SalesDeliveryNoteHdrDto convertToDto(SalesDeliveryNoteHdr deliveryNote, boolean includeDetails) {
        SalesDeliveryNoteHdrDto dto = new SalesDeliveryNoteHdrDto();
        BeanUtils.copyProperties(deliveryNote, dto);

        if (includeDetails) {
            List<SalesDeliveryNoteItemDtl> itemDetails = itemDtlRepository
                    .findByTransactionPoid(deliveryNote.getTransactionPoid());
            dto.setItemDetails(convertItemDetailsWithLov(itemDetails));
        }

        return dto;
    }

    private SalesDeliveryNoteItemDtlDto convertItemDtlToDto(SalesDeliveryNoteItemDtl itemDtl) {
        SalesDeliveryNoteItemDtlDto dto = new SalesDeliveryNoteItemDtlDto();
        BeanUtils.copyProperties(itemDtl, dto);
        return dto;
    }

    /**
     * Convert item details and enrich with stock & stock unit LOV details
     */
    private List<SalesDeliveryNoteItemDtlDto> convertItemDetailsWithLov(List<SalesDeliveryNoteItemDtl> itemDetails) {
        if (itemDetails == null || itemDetails.isEmpty()) {
            return new ArrayList<>();
        }

        List<SalesDeliveryNoteItemDtlDto> dtos = itemDetails.stream()
                .map(this::convertItemDtlToDto)
                .collect(Collectors.toList());

        Set<Long> stockPoids = itemDetails.stream()
                .map(SalesDeliveryNoteItemDtl::getStockPoid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, StockMasterEntity> stockMap = stockPoids.isEmpty()
                ? Collections.emptyMap()
                : stockMasterRepository.findAllById(stockPoids).stream()
                        .collect(Collectors.toMap(StockMasterEntity::getStockPoid, entity -> entity));

        Set<Long> stockUnitPoids = itemDetails.stream()
                .map(SalesDeliveryNoteItemDtl::getStockUnitPoid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, StockUnitMaster> stockUnitMap = stockUnitPoids.isEmpty()
                ? Collections.emptyMap()
                : stockUnitRepository.findAllById(stockUnitPoids).stream()
                        .collect(Collectors.toMap(StockUnitMaster::getStockUnitPoid, entity -> entity));

        for (int i = 0; i < itemDetails.size(); i++) {
            SalesDeliveryNoteItemDtl entity = itemDetails.get(i);
            SalesDeliveryNoteItemDtlDto dto = dtos.get(i);

            StockMasterEntity stock = stockMap.get(entity.getStockPoid());
            if (stock != null) {
                dto.setStockDetails(new SalesDeliveryNoteItemDtlDto.LovDetailDto(
                        stock.getStockPoid(),
                        stock.getStockCode(),
                        stock.getStockName()));
            }

            StockUnitMaster stockUnit = stockUnitMap.get(entity.getStockUnitPoid());
            if (stockUnit != null) {
                dto.setStockUnitDetails(new SalesDeliveryNoteItemDtlDto.LovDetailDto(
                        stockUnit.getStockUnitPoid(),
                        stockUnit.getStockUnitCode(),
                        stockUnit.getStockUnitName()));
            }
        }

        return dtos;
    }

    /**
     * Populate delivery note DTO from native query result with LOV details
     * Column order: delivery note fields (28) + LOV details (7 objects * 3 fields = 21) = 49 columns
     * LOV order: Customer, Salesman, Line, Port, Vessel, Print Division, Principal
     */
    private SalesDeliveryNoteHdrDto populateDeliveryNoteFromQueryResult(Object[] row, boolean includeDetails) {
        SalesDeliveryNoteHdrDto dto = new SalesDeliveryNoteHdrDto();
        
        // Delivery Note Header fields (indices 0-27)
        int index = 0;
        dto.setTransactionPoid(getLongValue(row[index++]));
        dto.setDocRef(getStringValue(row[index++]));
        dto.setTransactionDate(getTimestampValue(row[index++]));
        dto.setCompanyPoid(getLongValue(row[index++]));
        dto.setCustomerPoid(getLongValue(row[index++]));
        dto.setCurrencyCode(getStringValue(row[index++]));
        dto.setCurrencyRate(getLongValue(row[index++]));
        dto.setDeliveryStatus(getStringValue(row[index++]));
        dto.setSalesmanPoid(getLongValue(row[index++]));
        dto.setPaymentMode(getStringValue(row[index++]));
        dto.setDeliveryTerms(getStringValue(row[index++]));
        dto.setLinePoid(getLongValue(row[index++]));
        dto.setVesselPoid(getStringValue(row[index++]));
        dto.setVesselName(getStringValue(row[index++]));
        dto.setVoyageRef(getStringValue(row[index++]));
        dto.setPortPoid(getLongValue(row[index++]));
        dto.setPortDescription(getStringValue(row[index++]));
        dto.setQtnRefNo(getStringValue(row[index++]));
        dto.setVesselAgent(getStringValue(row[index++]));
        dto.setDeliveryToAddress(getStringValue(row[index++]));
        dto.setDescriptionPrintYn(getStringValue(row[index++]));
        dto.setPartyAddressDetails(getStringValue(row[index++]));
        dto.setPrintDivisionPoid(getLongValue(row[index++]));
        dto.setPartyType(getStringValue(row[index++]));
        dto.setPrincipalPoid(getLongValue(row[index++]));
        dto.setTotalDiscount(getLongValue(row[index++]));
        dto.setTotalAmount(getLongValue(row[index++]));
        dto.setRemarks(getStringValue(row[index++]));
        dto.setDeleted(getStringValue(row[index++]));
        dto.setCreatedBy(getStringValue(row[index++]));
        dto.setCreatedDate(getTimestampValue(row[index++]));
        dto.setLastmodifiedBy(getStringValue(row[index++]));
        dto.setLastmodifiedDate(getTimestampValue(row[index++]));
        
        // LOV Details start at index 28
        // Customer Details (cust: index 28-30)
        dto.setCustomerDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Salesman Details (sm: index 31-33)
        dto.setSalesmanDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Line Details (lm: index 34-36)
        dto.setLineDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Port Details (pm: index 37-39)
        dto.setPortDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Vessel Details (vm: index 40-42)
        dto.setVesselDetails(createLovDetailFromRow(row, index));
        index += 3;

        // Print Division Details (div: index 43-45)
        dto.setPrintDivisionDetails(createLovDetailFromRow(row, index));
        index += 3;

        // Principal Details (pr: index 46-48)
        dto.setPrincipalDetails(createLovDetailFromRow(row, index));
        
        // Set customerName from customerDetails if available
        if (dto.getCustomerDetails() != null && dto.getCustomerDetails().getDescription() != null) {
            dto.setCustomerName(dto.getCustomerDetails().getDescription());
        }
        
        // Fetch item details if requested
        if (includeDetails) {
            List<SalesDeliveryNoteItemDtl> itemDetails = itemDtlRepository
                    .findByTransactionPoid(dto.getTransactionPoid());
            dto.setItemDetails(convertItemDetailsWithLov(itemDetails));
        }
        
        return dto;
    }

    /**
     * Create LOV detail from row array starting at given index
     * Expects: [poid, code, description] at indices [index, index+1, index+2]
     */
    private SalesDeliveryNoteHdrDto.LovDetailDto createLovDetailFromRow(Object[] row, int index) {
        SalesDeliveryNoteHdrDto.LovDetailDto detail = new SalesDeliveryNoteHdrDto.LovDetailDto();
        
        if (row.length > index) {
            // Poid (may be BigDecimal or Long)
            if (row[index] != null) {
                if (row[index] instanceof java.math.BigDecimal) {
                    detail.setPoid(((java.math.BigDecimal) row[index]).longValue());
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
     * Create empty LOV detail object
     */
    private SalesDeliveryNoteHdrDto.LovDetailDto createEmptyLovDetail() {
        SalesDeliveryNoteHdrDto.LovDetailDto detail = new SalesDeliveryNoteHdrDto.LovDetailDto();
        detail.setPoid(null);
        detail.setCode(null);
        detail.setDescription(null);
        return detail;
    }

    /**
     * Set empty LOV details for fallback
     */
    private void setEmptyLovDetails(SalesDeliveryNoteHdrDto dto) {
        dto.setCustomerDetails(createEmptyLovDetail());
        dto.setSalesmanDetails(createEmptyLovDetail());
        dto.setLineDetails(createEmptyLovDetail());
        dto.setPortDetails(createEmptyLovDetail());
        dto.setVesselDetails(createEmptyLovDetail());
        dto.setPrintDivisionDetails(createEmptyLovDetail());
        dto.setPrincipalDetails(createEmptyLovDetail());
    }

    /**
     * Helper methods to safely extract values from Object[]
     */
    private Long getLongValue(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return null;
    }

    private String getStringValue(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private Timestamp getTimestampValue(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Timestamp) {
            return (Timestamp) obj;
        }
        return null;
    }

    // Placeholder for stored procedure call (implemented in Part 2)
    // private void callUpdateDeletedDetailsProcedure1(Long groupPoid, Long
    // companyPoid,
    // String userId, Long transactionPoid) {
    // salesDeliveryNoteRepository.callUpdateDeletedDetailsProc(groupPoid,
    // companyPoid, userId, transactionPoid);
    // }

    @Override
    public ValidateCustomerChangeResponse validateCustomerChange(Long transactionPoid,
            Long customerPoid) {
        ValidateCustomerChangeResponse response = new ValidateCustomerChangeResponse();
        boolean valid = salesDeliveryNoteRepository.callSalesSCDNCustomerValidateProc(customerPoid, transactionPoid);
        response.setCanChange(valid);
        response.setMessage(valid ? "Customer can be changed" : "Customer cannot be changed");
        response.setSuccess(true);
        return response;
    }

    // call this from transactional methods AFTER you've saved/flushed changes
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void callUpdateDeletedDetailsProcedure(Long groupPoid, Long companyPoid, String userId,
            Long transactionPoid) {
        log.info(
                "callUpdateDeletedDetailsProcedure: PROC_DN_UPDATE_DELETED_DTLSQH called for groupPoid={} companyPoid={} transactionPoid={}",
                groupPoid, companyPoid, transactionPoid);
        String proc = "{call PROC_DN_UPDATE_DELETED_DTLSQH(?, ?, ?, ?, ?)}";
        try (Connection conn = dataSource.getConnection();
                CallableStatement cs = conn.prepareCall(proc)) {

            // ensure connection not participating in suspended tx
            conn.setAutoCommit(true);

            cs.setLong(1, groupPoid);
            cs.setLong(2, companyPoid);
            cs.setString(3, userId);
            cs.setLong(4, transactionPoid);
            cs.registerOutParameter(5, Types.VARCHAR);

            cs.execute();

            String procResult = cs.getString(5);
            if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                throw new CustomException("PROC_DN_UPDATE_DELETED_DTLSQH failed: " + procResult);
            }
            log.info(
                    "PROC_DN_UPDATE_DELETED_DTLSQH completed successfully for groupPoid={} companyPoid={} transactionPoid={}",
                    groupPoid, companyPoid, transactionPoid);
        } catch (SQLException ex) {
            throw new CustomException("Error calling PROC_DN_UPDATE_DELETED_DTLSQH: " + ex.getMessage());
        }
    }

    @Override
    public LoadQuotationItemsResponse loadQuotationItems(Long groupPoid, Long transactionPoid, Long companyPoid,
            String userId) {
                log.info(
                "loadQuotationItems: PROC_DN_LOAD_QUOTATION_DETAIL called for groupPoid={} companyPoid={} transactionPoid={}",
                groupPoid, companyPoid, transactionPoid);
        String proc = "{call PROC_DN_LOAD_QUOTATION_DETAIL(?, ?, ?, ?, ?, ?)}";
        try (Connection conn = dataSource.getConnection();
                CallableStatement cs = conn.prepareCall(proc)) {

            // ensure connection not participating in suspended tx
            conn.setAutoCommit(true);

            cs.setLong(1, groupPoid);
            cs.setLong(2, companyPoid);
            cs.setLong(3, Long.parseLong(userId));
            cs.setString(4, transactionPoid.toString());
            cs.registerOutParameter(5, Types.VARCHAR);
            cs.registerOutParameter(6, Types.REF_CURSOR);

            cs.execute();

            String procResult = cs.getString(5);
            try (ResultSet rs = (ResultSet) cs.getObject(6)) {
                if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                    throw new CustomException("PROC_DN_LOAD_QUOTATION_DETAIL failed: " + procResult);
                }

                List<QuotationItemDto> items = new ArrayList<>();
                if (rs != null) {
                    while (rs.next()) {
                        QuotationItemDto dto = new QuotationItemDto();

                        Object stockPoidObj = rs.getObject("STOCK_POID");
                        dto.setStockPoid(stockPoidObj != null ? ((Number) stockPoidObj).longValue() : null);

                        Object stockUnitPoidObj = rs.getObject("STOCK_UNIT_POID");
                        dto.setStockUnitPoid(stockUnitPoidObj != null ? ((Number) stockUnitPoidObj).longValue() : null);

                        Long qty = rs.getLong("QUANTITY");
                        dto.setQuantity(qty);

                        Long price = rs.getLong("PRICE");
                        dto.setPrice(price);

                        Long discount = rs.getLong("DISCOUNT");
                        dto.setDiscount(discount);

                        Long amount = rs.getLong("AMOUNT");
                        dto.setAmount(amount);

                        dto.setRemarks(rs.getString("REMARKS"));

                        Object qtnDetRowIdObj = rs.getObject("QTN_DET_ROW_ID");
                        dto.setQtnDetRowId(qtnDetRowIdObj != null ? ((Number) qtnDetRowIdObj).longValue() : null);

                        Long totCost = rs.getLong("TOT_COST");
                        dto.setTotCost(totCost);

                        dto.setItemType(rs.getString("ITEM_TYPE"));
                        dto.setCheckAll(rs.getString("CHECK_ALL"));

                        items.add(dto);
                    }
                }
                LoadQuotationItemsResponse response = new LoadQuotationItemsResponse();
                response.setMessage("Quotation items loaded successfully");
                response.setItems(items);
                log.info("loadQuotationItems: PROC_DN_LOAD_QUOTATION_DETAIL completed successfully for groupPoid={} companyPoid={} transactionPoid={}",
                        groupPoid, companyPoid, transactionPoid);
                return response;
            }
        } catch (SQLException ex) {
            throw new CustomException("Error calling PROC_DN_LOAD_QUOTATION_DETAIL: " + ex.getMessage());
        }
    }

    /**
     * Check dependencies for a delivery note:
     * - whether it is linked to a sales quotation (QtnRefNo)
     * - count of sales invoices referencing this delivery note
     *
     * Returns DeliveryNoteDependenciesResponse indicating whether deletion is
     * allowed and reasons.
     */
    @Override
    @Transactional(readOnly = true)
    public SalesDeliveryNoteDependenciesDto checkDeliveryNoteDependencies(Long transactionPoid, Long companyPoid) {
        SalesDeliveryNoteDependenciesDto resp = new SalesDeliveryNoteDependenciesDto();
        log.info("checkDeliveryNoteDependencies called for transactionPoid={} companyPoid={}", transactionPoid, companyPoid);
        // load delivery note and validate company
        SalesDeliveryNoteHdr dn = deliveryNoteHdrRepository.findById(transactionPoid)
                .orElseThrow(() -> new CustomException("Delivery Note not found: " + transactionPoid));

        if (dn.getCompanyPoid() == null || !dn.getCompanyPoid().equals(companyPoid)) {
            throw new CustomException("Delivery Note does not belong to company " + companyPoid);
        }

        // check quotation link
        boolean linkedToQuotation = dn.getQtnRefNo() != null && !dn.getQtnRefNo().trim().isEmpty();
        resp.setLinkedToQuotation(linkedToQuotation);

        // Count sales invoice references using repository
        // This uses JPQL which handles deleted check: (deleted IS NULL OR UPPER(deleted) <> 'Y')
        // Equivalent to Oracle's NVL(DELETED,'N') <> 'Y'
        Long invoiceCountLong = salesDnDtlRepository.countByDnPoidFkAndCompanyPoidAndInvoiceNotDeleted(
                transactionPoid, companyPoid);
        int invoiceCount = invoiceCountLong != null ? invoiceCountLong.intValue() : 0;
        
        log.debug("Found {} sales invoice(s) referencing delivery note {} for companyPoid={}", 
                invoiceCount, transactionPoid, companyPoid);

        resp.setSalesInvoiceCount(invoiceCount);

        boolean canDelete = invoiceCount == 0 && !linkedToQuotation;
        resp.setCanDelete(canDelete);

        if (!canDelete) {
            StringBuilder reason = new StringBuilder();
            if (invoiceCount > 0) {
                reason.append("Delivery Note is used in ").append(invoiceCount).append(" sales invoice(s).");
            }
            if (linkedToQuotation) {
                if (reason.length() > 0)
                    reason.append(" ");
                reason.append("Delivery Note is linked to a Sales Quotation (QtnRefNo=").append(dn.getQtnRefNo())
                        .append(").");
            }
            resp.setReason(reason.toString());
            resp.setMessage("Cannot delete delivery note due to dependencies.");
        } else {
            resp.setReason(null);
            resp.setMessage("No dependencies found. Delivery note can be deleted.");
        }
        log.info("checkDeliveryNoteDependencies completed for transactionPoid={} companyPoid={}", transactionPoid, companyPoid);
        return resp;
    }
}
