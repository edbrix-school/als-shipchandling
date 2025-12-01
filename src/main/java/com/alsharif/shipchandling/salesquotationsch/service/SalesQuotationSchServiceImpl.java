package com.alsharif.shipchandling.salesquotationsch.service;

import com.alsharif.shipchandling.salesquotationsch.dto.*;
import com.alsharif.shipchandling.salesquotationsch.dto.request.*;
import com.alsharif.shipchandling.salesquotationsch.dto.response.CustomerDetailsResponse;
import com.alsharif.shipchandling.salesquotationsch.dto.response.SalesQuotationSchListResponse;
import com.alsharif.shipchandling.salesquotationsch.dto.response.StoredProcedureResponse;
import com.alsharif.shipchandling.salesquotationsch.dto.response.ValidationResponse;
import com.alsharif.shipchandling.salesquotationsch.entity.*;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import com.alsharif.shipchandling.exceptions.CustomException;
import com.alsharif.shipchandling.salesquotationsch.repository.*;
import com.alsharif.shipchandling.salesquotationsch.spec.SalesQuotationSchSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesQuotationSchServiceImpl implements SalesQuotationSchService {

    private final SalesQuotationSchHdrRepository quotationSchHdrRepository;
    private final SalesQuotationSchItemDtlRepository itemDtlRepository;
    private final SalesQuotationSchStoredProcRepository quotationSchStoredProcRepository;

    @Override
    @Transactional
    public SalesQuotationSchHdrDto createSalesQuotationSch(CreateSalesQuotationSchRequest request, Long groupPoid,
            Long companyPoid, String userId) {
        log.info("createSalesQuotationSch service started for groupPoid={} userId={}", groupPoid, userId);
        // Validate required fields
        validateQuotationSchRequest(request);

        // Create entity
        SalesQuotationSchHdr quotationSch = new SalesQuotationSchHdr();
        BeanUtils.copyProperties(request, quotationSch);
        quotationSch.setCompanyPoid(companyPoid);
        quotationSch.setCreatedBy(userId);
        quotationSch.setLastmodifiedBy(userId);
        quotationSch.setDeleted("N");

        // Save to get transactionPoid
        SalesQuotationSchHdr savedQuotationSch = quotationSchHdrRepository.save(quotationSch);
        quotationSchHdrRepository.flush();
        log.info("createSalesQuotationSch persisted groupPoid={} userId={} transactionPoid={} ", groupPoid, userId,
                savedQuotationSch.getTransactionPoid());

        // Save item details
        if (request.getItemDetails() != null && !request.getItemDetails().isEmpty()) {
            saveItemDetails(savedQuotationSch.getTransactionPoid(), request.getItemDetails(), userId);
        }

        // Calculate totals
        calculateTotals(savedQuotationSch.getTransactionPoid());

        // Refresh to get auto-generated DocRef
        quotationSchHdrRepository.flush();
        SalesQuotationSchHdr refreshedQuotationSch = quotationSchHdrRepository.findByTransactionPoid(
                savedQuotationSch.getTransactionPoid()).orElse(savedQuotationSch);

        // Convert to DTO
        SalesQuotationSchHdrDto dto = convertToDto(refreshedQuotationSch, true);
        log.info("createSalesQuotationSch completed for transactionPoid={} docRef={}",
                dto.getTransactionPoid(), dto.getDocRef());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesQuotationSchHdrDto getSalesQuotationSchByPoid(Long transactionPoid, Long groupPoid,
            Long companyPoid, Boolean includeDetails) {
        log.info("getSalesQuotationSchByPoid called for transactionPoid={} groupPoid={} companyPoid={} includeDetails={}",
                transactionPoid, groupPoid, companyPoid, includeDetails);
        
        // First check if quotation exists and is not deleted
        SalesQuotationSchHdr quotationSch = quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

        if ("Y".equals(quotationSch.getDeleted())) {
            log.warn("getSalesQuotationSchByPoid found companyPoid={} transactionPoid={} marked as deleted", companyPoid,
                    transactionPoid);
            throw new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid);
        }
        
        // Fetch quotation with all LOV details in a single query
        List<Object[]> results = quotationSchHdrRepository.findSalesQuotationSchWithDetails(transactionPoid, companyPoid);
        SalesQuotationSchHdrDto dto;
        
        if (!results.isEmpty()) {
            Object[] row = results.get(0);
            dto = populateQuotationSchFromQueryResult(row, includeDetails != null && includeDetails);
        } else {
            // Fallback: use entity if query fails
            dto = convertToDto(quotationSch, includeDetails != null && includeDetails);
            setEmptyLovDetails(dto);
        }
        
        log.info("getSalesQuotationSchByPoid completed for transactionPoid={} companyPoid={}",
                transactionPoid, companyPoid);
        return dto;
    }

    @Override
    @Transactional
    public SalesQuotationSchHdrDto updateSalesQuotationSch(Long groupPoid, Long transactionPoid,
            UpdateSalesQuotationSchRequest request,
            Long companyPoid, String userId) {
        log.info("updateSalesQuotationSch service started for transactionPoid={} groupPoid={}", transactionPoid, groupPoid);
        SalesQuotationSchHdr quotationSch = quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

        if ("Y".equals(quotationSch.getDeleted())) {
            log.warn("updateSalesQuotationSch found companyPoid={} transactionPoid={} marked as deleted", companyPoid,
                    transactionPoid);
            throw new CustomException("Cannot update deleted sales quotation sch");
        }

        // Validate required fields
        validateQuotationSchRequest(request);

        // Update fields (excluding read-only fields)
        BeanUtils.copyProperties(request, quotationSch, "transactionPoid", "docRef", "createdBy",
                "createdDate");
        quotationSch.setLastmodifiedBy(userId);

        // Process item details based on actionType (UPDATE, DELETE, or CREATE)
        if (request.getItemDetails() != null && !request.getItemDetails().isEmpty()) {
            processItemDetailsByActionType(transactionPoid, request.getItemDetails(), userId);
        }

        // Save
        SalesQuotationSchHdr savedQuotationSch = quotationSchHdrRepository.save(quotationSch);
        quotationSchHdrRepository.flush();
        // Calculate totals
        calculateTotals(transactionPoid);

        SalesQuotationSchHdrDto dto = convertToDto(savedQuotationSch, true);
        log.info("updateSalesQuotationSch completed for transactionPoid={} companyPoid={}",
                quotationSch.getTransactionPoid(), quotationSch.getCompanyPoid());
        return dto;
    }

    @Override
    @Transactional
    public void deleteSalesQuotationSch(Long groupPoid, Long transactionPoid, Long companyPoid) {
        SalesQuotationSchHdr quotationSch = quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

        if ("Y".equals(quotationSch.getDeleted())) {
            log.warn("deleteSalesQuotationSch found companyPoid={} transactionPoid={} already deleted", companyPoid,
                    transactionPoid);
            throw new CustomException("Cannot delete sales quotation sch. It is already deleted.");
        }

        // Delete item details
        itemDtlRepository.deleteByTransactionPoid(transactionPoid);

        // Soft delete
        quotationSch.setDeleted("Y");
        quotationSchHdrRepository.save(quotationSch);
        log.info("deleteSalesQuotationSch completed for transactionPoid={} companyPoid={}",
                quotationSch.getTransactionPoid(), quotationSch.getCompanyPoid());
    }

    @Override
    @Transactional(readOnly = true)
    public SalesQuotationSchListResponse search(SalesQuotationSchFilter filter, String userId) {
        Objects.requireNonNull(filter, "filter is required");
        Objects.requireNonNull(filter.getCompanyPoid(), "companyPoid is required in filter");
        
        log.info("search sales quotation sch started for companyPoid={} userId={} page={} size={}", 
                filter.getCompanyPoid(), userId, filter.getPage(), filter.getSize());
        
        // Build specification
        Specification<SalesQuotationSchHdr> specification = SalesQuotationSchSpecifications.notDeleted();
        
        // Company filtering (mandatory for data isolation)
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.companyIs(filter.getCompanyPoid()));
        
        // Apply filters
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.customerIs(filter.getCustomerPoid()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.salesmanIs(filter.getSalesmanPoid()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.lineIs(filter.getLinePoid()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.statusIs(filter.getQuotationStatus()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.docRefLike(filter.getDocRef()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.searchText(filter.getSearch()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.transactionDateFrom(filter.getFromDate()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.transactionDateTo(filter.getToDate()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.validityFromDate(filter.getValidityFromDate()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.validityToDate(filter.getValidityToDate()));
        
        // Build sort
        Sort sort = buildSort(filter.getSortBy(), filter.getSortOrder());
        
        // Pagination
        int page = filter.getPage() != null && filter.getPage() >= 0 ? filter.getPage() : 0;
        int size = filter.getSize() != null && filter.getSize() > 0 ? filter.getSize() : 20;
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SalesQuotationSchHdr> pageResult = quotationSchHdrRepository.findAll(specification, pageable);
        
        // Convert to DTOs
        List<SalesQuotationSchSummaryDto> content = pageResult.getContent().stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
        
        // Build response
        SalesQuotationSchListResponse response = new SalesQuotationSchListResponse(
                content,
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                page,
                size
        );
        
        log.info("search sales quotation sch completed for companyPoid={} totalElements={} totalPages={}", 
                filter.getCompanyPoid(), response.getTotalElements(), response.getTotalPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationResponse validateDocRef(String docRef, Long transactionPoid) {
        if (docRef == null || docRef.trim().isEmpty()) {
            log.warn("validateDocRef called with empty docRef");
            ValidationResponse response = new ValidationResponse();
            response.setIsUnique(false);
            response.setIsValid(false);
            response.setMessage("Document reference cannot be empty");
            return response;
        }

        boolean exists;
        // Note: companyPoid should be passed from controller, but for now we'll check without it
        if (transactionPoid != null) {
            Long count = quotationSchHdrRepository.countByCompanyPoidAndDocRefExcluding(
                    null, docRef, transactionPoid);
            exists = count != null && count > 0;
        } else {
            // For validation without transactionPoid, we need companyPoid - this should be passed from controller
            // For now, we'll use a simple check
            exists = false; // This should be enhanced to check with companyPoid
        }

        ValidationResponse response = new ValidationResponse();
        if (exists) {
            response.setIsUnique(false);
            response.setIsValid(false);
            response.setMessage("Document reference already exists");
        } else {
            response.setIsUnique(true);
            response.setIsValid(true);
            response.setMessage("Document reference is available");
        }
        return response;
    }

    @Override
    @Transactional
    public SalesQuotationSchItemDtlDto addItemDetail(Long transactionPoid, CreateSalesQuotationSchItemDtlRequest request,
            Long companyPoid, String userId) {
        log.info("addItemDetail called for transactionPoid={} companyPoid={}", transactionPoid, companyPoid);
        // Validate quotation exists
        SalesQuotationSchHdr quotationSch = quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

        if ("Y".equals(quotationSch.getDeleted())) {
            throw new CustomException("Cannot add item details. Sales quotation sch is deleted");
        }

        // Get next detRowId
        Long maxDetRowId = itemDtlRepository.getMaxDetRowIdByTransactionPoid(transactionPoid);
        Long nextDetRowId = (maxDetRowId == null) ? 1L : maxDetRowId + 1L;

        // Create item detail
        SalesQuotationSchItemDtl itemDtl = new SalesQuotationSchItemDtl();
        itemDtl.setTransactionPoid(transactionPoid);
        itemDtl.setDetRowId(nextDetRowId);
        itemDtl.setStockPoid(request.getStockPoid());
        itemDtl.setQuantity(request.getQuantity());
        itemDtl.setPrice(request.getPrice());
        itemDtl.setDiscount(request.getDiscount());
        itemDtl.setAmount(request.getAmount());
        itemDtl.setRemarks(request.getRemarks());
        itemDtl.setStockUnitPoid(request.getStockUnitPoid());
        itemDtl.setAdjQuantity(request.getAdjQuantity());
        itemDtl.setCost(request.getCost());
        itemDtl.setLastRate1(request.getLastRate1());
        itemDtl.setLastRate2(request.getLastRate2());
        itemDtl.setDeliverySelect(request.getDeliverySelect());
        itemDtl.setDnRefNo(request.getDnRefNo());
        itemDtl.setGpAmount(request.getGpAmount());
        itemDtl.setGpPercentage(request.getGpPercentage());
        itemDtl.setTotCost(request.getTotCost());
        itemDtl.setPurchasePrice(request.getPurchasePrice());
        itemDtl.setPurchaseQty(request.getPurchaseQty());
        itemDtl.setItemType(request.getItemType());
        itemDtl.setRefDocId(request.getRefDocId());
        itemDtl.setRefPoid(request.getRefPoid());
        itemDtl.setTaxPoid(request.getTaxPoid());
        itemDtl.setTaxAmount(request.getTaxAmount());
        itemDtl.setTaxPercentage(request.getTaxPercentage());
        itemDtl.setVatModified(request.getVatModified());
        itemDtl.setCreatedBy(userId);
        itemDtl.setLastmodifiedBy(userId);

        SalesQuotationSchItemDtl savedItemDtl = itemDtlRepository.save(itemDtl);

        // Recalculate totals
        calculateTotals(transactionPoid);
        SalesQuotationSchItemDtlDto dto = convertItemDtlToDto(savedItemDtl);
        log.info("addItemDetail completed for transactionPoid={} detRowId={} companyPoid={}", transactionPoid,
                nextDetRowId, companyPoid);
        return dto;
    }

    @Override
    @Transactional
    public SalesQuotationSchItemDtlDto updateItemDetail(Long transactionPoid, Long detRowId,
            CreateSalesQuotationSchItemDtlRequest request,
            Long companyPoid, String userId) {
        log.info("updateItemDetail called for transactionPoid={} detRowId={} companyPoid={}", transactionPoid, detRowId,
                companyPoid);
        // Validate quotation exists
        SalesQuotationSchHdr quotationSch = quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

        if ("Y".equals(quotationSch.getDeleted())) {
            throw new CustomException("Cannot update item details. Sales quotation sch is deleted");
        }

        // Get item detail
        SalesQuotationSchItemDtl itemDtl = itemDtlRepository
                .findById(new SalesQuotationSchItemDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Item Detail", "detRowId", detRowId));

        // Update fields
        itemDtl.setStockPoid(request.getStockPoid());
        itemDtl.setQuantity(request.getQuantity());
        itemDtl.setPrice(request.getPrice());
        itemDtl.setDiscount(request.getDiscount());
        itemDtl.setAmount(request.getAmount());
        itemDtl.setRemarks(request.getRemarks());
        itemDtl.setStockUnitPoid(request.getStockUnitPoid());
        itemDtl.setAdjQuantity(request.getAdjQuantity());
        itemDtl.setCost(request.getCost());
        itemDtl.setLastRate1(request.getLastRate1());
        itemDtl.setLastRate2(request.getLastRate2());
        itemDtl.setDeliverySelect(request.getDeliverySelect());
        itemDtl.setDnRefNo(request.getDnRefNo());
        itemDtl.setGpAmount(request.getGpAmount());
        itemDtl.setGpPercentage(request.getGpPercentage());
        itemDtl.setTotCost(request.getTotCost());
        itemDtl.setPurchasePrice(request.getPurchasePrice());
        itemDtl.setPurchaseQty(request.getPurchaseQty());
        itemDtl.setItemType(request.getItemType());
        itemDtl.setRefDocId(request.getRefDocId());
        itemDtl.setRefPoid(request.getRefPoid());
        itemDtl.setTaxPoid(request.getTaxPoid());
        itemDtl.setTaxAmount(request.getTaxAmount());
        itemDtl.setTaxPercentage(request.getTaxPercentage());
        itemDtl.setVatModified(request.getVatModified());
        itemDtl.setLastmodifiedBy(userId);

        SalesQuotationSchItemDtl savedItemDtl = itemDtlRepository.save(itemDtl);

        // Recalculate totals
        calculateTotals(transactionPoid);
        SalesQuotationSchItemDtlDto dto = convertItemDtlToDto(savedItemDtl);
        log.info("updateItemDetail completed for transactionPoid={} detRowId={} companyPoid={}", transactionPoid,
                detRowId, companyPoid);
        return dto;
    }

    @Override
    @Transactional
    public void deleteItemDetail(Long transactionPoid, Long detRowId, Long companyPoid) {
        log.info("deleteItemDetail called for transactionPoid={} detRowId={} companyPoid={}", transactionPoid, detRowId,
                companyPoid);
        // Validate quotation exists
        SalesQuotationSchHdr quotationSch = quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

        if ("Y".equals(quotationSch.getDeleted())) {
            throw new CustomException("Cannot delete item details. Sales quotation sch is deleted");
        }

        // Delete item detail
        itemDtlRepository.deleteById(new SalesQuotationSchItemDtlId(transactionPoid, detRowId));
        log.info("deleteItemDetail completed for transactionPoid={} detRowId={} companyPoid={}", transactionPoid,
                detRowId, companyPoid);
        // Recalculate totals
        calculateTotals(transactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesQuotationSchItemDtlDto> getItemDetails(Long transactionPoid, Long companyPoid) {
        log.info("getItemDetails called for transactionPoid={} companyPoid={}", transactionPoid, companyPoid);
        // Validate quotation exists
        quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

        List<SalesQuotationSchItemDtl> itemDetails = itemDtlRepository.findByTransactionPoid(transactionPoid);
        return itemDetails.stream()
                .map(this::convertItemDtlToDto)
                .collect(Collectors.toList());
    }

    // Helper methods
    private void validateQuotationSchRequest(CreateSalesQuotationSchRequest request) {
        if (request.getCustomerPoid() == null) {
            log.warn("Validation failed : Customer is required");
            throw new CustomException("Customer is required");
        }
        if (request.getTransactionDate() == null) {
            log.warn("Validation failed : Transaction date is required");
            throw new CustomException("Transaction date is required");
        }
    }

    private void validateQuotationSchRequest(UpdateSalesQuotationSchRequest request) {
        if (request.getCustomerPoid() == null) {
            log.warn("Validation failed : Customer is required");
            throw new CustomException("Customer is required");
        }
        if (request.getTransactionDate() == null) {
            log.warn("Validation failed : Transaction date is required");
            throw new CustomException("Transaction date is required");
        }
    }

    private void saveItemDetails(Long transactionPoid, List<CreateSalesQuotationSchItemDtlRequest> details,
            String userId) {
        Long detRowId = 1L;
        Long maxDetRowId = itemDtlRepository.getMaxDetRowIdByTransactionPoid(transactionPoid);
        if (maxDetRowId != null && maxDetRowId > 0) {
            detRowId = maxDetRowId + 1;
        }
        
        for (CreateSalesQuotationSchItemDtlRequest detail : details) {
            SalesQuotationSchItemDtl itemDtl = new SalesQuotationSchItemDtl();
            itemDtl.setTransactionPoid(transactionPoid);
            itemDtl.setDetRowId(detRowId++);
            itemDtl.setStockPoid(detail.getStockPoid());
            itemDtl.setQuantity(detail.getQuantity());
            itemDtl.setPrice(detail.getPrice());
            itemDtl.setDiscount(detail.getDiscount());
            itemDtl.setAmount(detail.getAmount());
            itemDtl.setRemarks(detail.getRemarks());
            itemDtl.setStockUnitPoid(detail.getStockUnitPoid());
            itemDtl.setAdjQuantity(detail.getAdjQuantity());
            itemDtl.setCost(detail.getCost());
            itemDtl.setLastRate1(detail.getLastRate1());
            itemDtl.setLastRate2(detail.getLastRate2());
            itemDtl.setDeliverySelect(detail.getDeliverySelect());
            itemDtl.setDnRefNo(detail.getDnRefNo());
            itemDtl.setGpAmount(detail.getGpAmount());
            itemDtl.setGpPercentage(detail.getGpPercentage());
            itemDtl.setTotCost(detail.getTotCost());
            itemDtl.setPurchasePrice(detail.getPurchasePrice());
            itemDtl.setPurchaseQty(detail.getPurchaseQty());
            itemDtl.setItemType(detail.getItemType());
            itemDtl.setRefDocId(detail.getRefDocId());
            itemDtl.setRefPoid(detail.getRefPoid());
            itemDtl.setTaxPoid(detail.getTaxPoid());
            itemDtl.setTaxAmount(detail.getTaxAmount());
            itemDtl.setTaxPercentage(detail.getTaxPercentage());
            itemDtl.setVatModified(detail.getVatModified());
            itemDtl.setCreatedBy(userId);
            itemDtl.setLastmodifiedBy(userId);
            itemDtlRepository.save(itemDtl);
        }
    }

    private void processItemDetailsByActionType(Long transactionPoid, 
            List<CreateSalesQuotationSchItemDtlRequest> details, String userId) {
        if (details == null || details.isEmpty()) {
            return;
        }

        List<CreateSalesQuotationSchItemDtlRequest> itemsToCreate = new ArrayList<>();
        
        for (CreateSalesQuotationSchItemDtlRequest item : details) {
            String actionType = item.getActionType();
            
            if ("DELETE".equalsIgnoreCase(actionType)) {
                if (item.getDetRowId() != null) {
                    try {
                        itemDtlRepository.deleteById(new SalesQuotationSchItemDtlId(transactionPoid, item.getDetRowId()));
                        log.debug("Deleted item detail transactionPoid={} detRowId={}", transactionPoid, item.getDetRowId());
                    } catch (Exception ex) {
                        log.warn("Failed to delete item detail transactionPoid={} detRowId={}: {}", 
                                transactionPoid, item.getDetRowId(), ex.getMessage());
                    }
                }
            } else if ("UPDATE".equalsIgnoreCase(actionType)) {
                if (item.getDetRowId() != null) {
                    try {
                        SalesQuotationSchItemDtl existingItem = itemDtlRepository
                                .findById(new SalesQuotationSchItemDtlId(transactionPoid, item.getDetRowId()))
                                .orElse(null);
                        
                        if (existingItem != null) {
                            existingItem.setStockPoid(item.getStockPoid());
                            existingItem.setQuantity(item.getQuantity());
                            existingItem.setPrice(item.getPrice());
                            existingItem.setDiscount(item.getDiscount());
                            existingItem.setAmount(item.getAmount());
                            existingItem.setRemarks(item.getRemarks());
                            existingItem.setStockUnitPoid(item.getStockUnitPoid());
                            existingItem.setAdjQuantity(item.getAdjQuantity());
                            existingItem.setCost(item.getCost());
                            existingItem.setLastRate1(item.getLastRate1());
                            existingItem.setLastRate2(item.getLastRate2());
                            existingItem.setDeliverySelect(item.getDeliverySelect());
                            existingItem.setDnRefNo(item.getDnRefNo());
                            existingItem.setGpAmount(item.getGpAmount());
                            existingItem.setGpPercentage(item.getGpPercentage());
                            existingItem.setTotCost(item.getTotCost());
                            existingItem.setPurchasePrice(item.getPurchasePrice());
                            existingItem.setPurchaseQty(item.getPurchaseQty());
                            existingItem.setItemType(item.getItemType());
                            existingItem.setRefDocId(item.getRefDocId());
                            existingItem.setRefPoid(item.getRefPoid());
                            existingItem.setTaxPoid(item.getTaxPoid());
                            existingItem.setTaxAmount(item.getTaxAmount());
                            existingItem.setTaxPercentage(item.getTaxPercentage());
                            existingItem.setVatModified(item.getVatModified());
                            existingItem.setLastmodifiedBy(userId);
                            
                            itemDtlRepository.save(existingItem);
                            log.debug("Updated item detail transactionPoid={} detRowId={}", transactionPoid, item.getDetRowId());
                        } else {
                            itemsToCreate.add(item);
                        }
                    } catch (Exception ex) {
                        log.warn("Failed to update item detail transactionPoid={} detRowId={}: {}", 
                                transactionPoid, item.getDetRowId(), ex.getMessage());
                    }
                } else {
                    itemsToCreate.add(item);
                }
            } else {
                itemsToCreate.add(item);
            }
        }
        
        if (!itemsToCreate.isEmpty()) {
            saveItemDetails(transactionPoid, itemsToCreate, userId);
        }
    }

    private void calculateTotals(Long transactionPoid) {
        List<SalesQuotationSchItemDtl> itemDetails = itemDtlRepository.findByTransactionPoid(transactionPoid);
        
        Long totalAmount = itemDetails.stream()
                .map(SalesQuotationSchItemDtl::getAmount)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        
        Long totalTax = itemDetails.stream()
                .map(SalesQuotationSchItemDtl::getTaxAmount)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
        
        Long grossProfitAmount = itemDetails.stream()
                .map(SalesQuotationSchItemDtl::getGpAmount)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        // Update quotation header
        SalesQuotationSchHdr quotationSch = quotationSchHdrRepository.findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));
        quotationSch.setTotalAmount(totalAmount);
        quotationSch.setTotalTax(totalTax);
        quotationSch.setTotalGpAmt(grossProfitAmount);
        
        // Calculate gross profit percentage
        if (totalAmount != null && totalAmount > 0 && grossProfitAmount != null) {
            Long grossProfitPercent = (grossProfitAmount * 100) / totalAmount;
            quotationSch.setTotalGpPercentage(grossProfitPercent);
        }
        
        quotationSchHdrRepository.save(quotationSch);
    }

    private SalesQuotationSchHdrDto convertToDto(SalesQuotationSchHdr quotationSch, boolean includeDetails) {
        SalesQuotationSchHdrDto dto = new SalesQuotationSchHdrDto();
        BeanUtils.copyProperties(quotationSch, dto);

        if (includeDetails) {
            List<SalesQuotationSchItemDtl> itemDetails = itemDtlRepository
                    .findByTransactionPoid(quotationSch.getTransactionPoid());
            dto.setItemDetails(itemDetails.stream()
                    .map(this::convertItemDtlToDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private SalesQuotationSchItemDtlDto convertItemDtlToDto(SalesQuotationSchItemDtl itemDtl) {
        SalesQuotationSchItemDtlDto dto = new SalesQuotationSchItemDtlDto();
        BeanUtils.copyProperties(itemDtl, dto);
        return dto;
    }

    /**
     * Populate DTO from query result with LOV details
     */
    private SalesQuotationSchHdrDto populateQuotationSchFromQueryResult(Object[] row, boolean includeDetails) {
        SalesQuotationSchHdrDto dto = new SalesQuotationSchHdrDto();
        
        // Quotation Header fields (indices 0-53)
        int index = 0;
        dto.setTransactionPoid(getLongValue(row[index++]));
        dto.setDocRef(getStringValue(row[index++]));
        dto.setTransactionDate(getTimestampValue(row[index++]));
        dto.setCompanyPoid(getLongValue(row[index++]));
        dto.setCustomerPoid(getLongValue(row[index++]));
        dto.setAddressPoid(getLongValue(row[index++]));
        dto.setCurrencyCode(getStringValue(row[index++]));
        dto.setCurrencyRate(getLongValue(row[index++]));
        dto.setQuotationStatus(getStringValue(row[index++]));
        dto.setSalesmanPoid(getLongValue(row[index++]));
        dto.setValidityFromDate(getTimestampValue(row[index++]));
        dto.setValidityToDate(getTimestampValue(row[index++]));
        dto.setPaymentMode(getStringValue(row[index++]));
        dto.setDeliveryTerms(getStringValue(row[index++]));
        dto.setLinePoid(getLongValue(row[index++]));
        dto.setVesselPoid(getStringValue(row[index++]));
        dto.setVesselName(getStringValue(row[index++]));
        dto.setVoyageRef(getStringValue(row[index++]));
        dto.setPortPoid(getLongValue(row[index++]));
        dto.setPortDescription(getStringValue(row[index++]));
        dto.setRemarks(getStringValue(row[index++]));
        dto.setTotalDiscount(getLongValue(row[index++]));
        dto.setTotalAmount(getLongValue(row[index++]));
        dto.setActionStatus(getStringValue(row[index++]));
        dto.setActionDueDate(getTimestampValue(row[index++]));
        dto.setEnquiryRefNumber(getLongValue(row[index++]));
        dto.setLostReason(getStringValue(row[index++]));
        dto.setBusinessPromotionValue(getLongValue(row[index++]));
        dto.setPercentage(getLongValue(row[index++]));
        dto.setDetails(getStringValue(row[index++]));
        dto.setExpectedDeliveryDate(getTimestampValue(row[index++]));
        dto.setVesselAgent(getStringValue(row[index++]));
        dto.setQuotedRate(getStringValue(row[index++]));
        dto.setRfqRefNo(getStringValue(row[index++]));
        dto.setPercentageDisc(getLongValue(row[index++]));
        dto.setTotalGpAmt(getLongValue(row[index++]));
        dto.setTotalGpPercentage(getLongValue(row[index++]));
        dto.setCustomerRef(getStringValue(row[index++]));
        dto.setDeliveryToAddress(getStringValue(row[index++]));
        dto.setSelectAllDtl(getStringValue(row[index++]));
        dto.setTotAmtPrintYn(getStringValue(row[index++]));
        dto.setDescriptionPrintYn(getStringValue(row[index++]));
        dto.setAdvanceDetail(getStringValue(row[index++]));
        dto.setMtaInvPoid(getLongValue(row[index++]));
        dto.setSalesInvPoid(getLongValue(row[index++]));
        dto.setSalesInvDocRef(getStringValue(row[index++]));
        dto.setTotalTax(getLongValue(row[index++]));
        dto.setPartyAddressDetails(getStringValue(row[index++]));
        dto.setDeleted(getStringValue(row[index++]));
        dto.setCreatedBy(getStringValue(row[index++]));
        dto.setCreatedDate(getTimestampValue(row[index++]));
        dto.setLastmodifiedBy(getStringValue(row[index++]));
        dto.setLastmodifiedDate(getTimestampValue(row[index++]));
        
        // LOV Details start at index 54
        // Customer Details (cust: index 54-56)
        dto.setCustomerDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Salesman Details (sm: index 57-59)
        dto.setSalesmanDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Line Details (lm: index 60-62)
        dto.setLineDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Port Details (pm: index 63-65)
        dto.setPortDetails(createLovDetailFromRow(row, index));
        index += 3;
        
        // Vessel Details (vm: index 66-68)
        dto.setVesselDetails(createLovDetailFromRow(row, index));
        index += 3;

        // Print Division Details (div: index 69-71)
        dto.setPrintDivisionDetails(createLovDetailFromRow(row, index));
        index += 3;

        // Principal Details (pr: index 72-74)
        dto.setPrincipalDetails(createLovDetailFromRow(row, index));
        
        // Fetch item details if requested
        if (includeDetails) {
            List<SalesQuotationSchItemDtl> itemDetails = itemDtlRepository
                    .findByTransactionPoid(dto.getTransactionPoid());
            dto.setItemDetails(itemDetails.stream()
                    .map(this::convertItemDtlToDto)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    /**
     * Create LOV detail from row array starting at given index
     * Expects: [poid, code, description] at indices [index, index+1, index+2]
     */
    private SalesQuotationSchHdrDto.LovDetailDto createLovDetailFromRow(Object[] row, int index) {
        SalesQuotationSchHdrDto.LovDetailDto detail = new SalesQuotationSchHdrDto.LovDetailDto();
        
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
    private SalesQuotationSchHdrDto.LovDetailDto createEmptyLovDetail() {
        SalesQuotationSchHdrDto.LovDetailDto detail = new SalesQuotationSchHdrDto.LovDetailDto();
        detail.setPoid(null);
        detail.setCode(null);
        detail.setDescription(null);
        return detail;
    }

    /**
     * Set empty LOV details for fallback
     */
    private void setEmptyLovDetails(SalesQuotationSchHdrDto dto) {
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

    /**
     * Helper method to combine specifications safely
     */
    private Specification<SalesQuotationSchHdr> andIfPresent(Specification<SalesQuotationSchHdr> base,
                                                              Specification<SalesQuotationSchHdr> addition) {
        if (addition == null) {
            return base;
        }
        return base == null ? addition : base.and(addition);
    }

    /**
     * Builds Sort object from sortBy and sortOrder parameters.
     */
    private Sort buildSort(String sortBy, String sortOrder) {
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "transactionDate";
        }
        
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortOrder != null && "ASC".equalsIgnoreCase(sortOrder.trim())) {
            direction = Sort.Direction.ASC;
        }
        
        // Validate sort field to prevent SQL injection
        // Only allow sorting by known fields
        String[] allowedSortFields = {
            "transactionDate", "transactionPoid", "docRef", "quotationStatus",
            "validityToDate", "totalAmount", "customerRef"
        };
        
        boolean isValidField = false;
        for (String allowedField : allowedSortFields) {
            if (allowedField.equalsIgnoreCase(sortBy.trim())) {
                isValidField = true;
                break;
            }
        }
        
        if (!isValidField) {
            log.warn("Invalid sort field: {}, using default: transactionDate", sortBy);
            sortBy = "transactionDate";
        }
        
        // Always add transactionPoid as secondary sort for consistent ordering
        Sort.Order primaryOrder = direction == Sort.Direction.ASC 
                ? Sort.Order.asc(sortBy.trim())
                : Sort.Order.desc(sortBy.trim());
        Sort.Order secondaryOrder = Sort.Order.desc("transactionPoid");
        return Sort.by(primaryOrder, secondaryOrder);
    }

    /**
     * Converts entity to summary DTO for list views
     */
    private SalesQuotationSchSummaryDto toSummaryDto(SalesQuotationSchHdr header) {
        SalesQuotationSchSummaryDto dto = new SalesQuotationSchSummaryDto();
        dto.setTransactionPoid(header.getTransactionPoid());
        dto.setTransactionDate(header.getTransactionDate());
        dto.setDocRef(header.getDocRef());
        dto.setCompanyPoid(header.getCompanyPoid());
        dto.setCustomerPoid(header.getCustomerPoid());
        dto.setQuotationStatus(header.getQuotationStatus());
        dto.setValidityToDate(header.getValidityToDate());
        dto.setTotalAmount(header.getTotalAmount());
        dto.setTotalTax(header.getTotalTax());
        dto.setTotalGpAmt(header.getTotalGpAmt());
        dto.setCurrencyCode(header.getCurrencyCode());
        dto.setCustomerRef(header.getCustomerRef());
        dto.setVesselName(header.getVesselName());
        dto.setSalesmanPoid(header.getSalesmanPoid());
        return dto;
    }

    @Override
    public List<SalesQuotationSchCustomerDetailsDto> refreshPreviousQuotationData(Long transactionPoid, Long groupPoid,
            Long companyPoid, Long customerPoid) {
        // Validate invoice exists
        SalesQuotationSchHdr quotationSchHdr = quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

        if ("Y".equals(quotationSchHdr.getDeleted())) {
            throw new CustomException("Cannot load quotation. Sales Quotation SCH is deleted");
        }

        // Call stored procedure to load quotation
        CustomerDetailsResponse response = quotationSchStoredProcRepository
                .callrefreshPreviousQuotationDataProc(groupPoid, customerPoid, companyPoid, transactionPoid);

        return response.isSuccess() ? response.getCustomerDetails() : new ArrayList<>();
    }

    // ==================== Stored Procedure Operations ====================

    @Override
    @Transactional
    public StoredProcedureResponse importItems(ImportItemsRequest request) {
        log.info("importItems called for transactionPoid={} companyPoid={}", 
                request.getTransactionPoid(), request.getCompanyPoid());
        return quotationSchStoredProcRepository.callImportItemsProc(request);
    }

    @Override
    @Transactional
    public StoredProcedureResponse clearItems(ClearItemsRequest request) {
        log.info("clearItems called for transactionPoid={} companyPoid={}", 
                request.getTransactionPoid(), request.getCompanyPoid());
        return quotationSchStoredProcRepository.callClearItemsProc(request);
    }

    @Override
    @Transactional
    public StoredProcedureResponse refreshDetail(RefreshDetailRequest request) {
        log.info("refreshDetail called for transactionPoid={} companyPoid={} quotedRate={}", 
                request.getTransactionPoid(), request.getCompanyPoid(), request.getQuotedRate());
        return quotationSchStoredProcRepository.callRefreshDetailProc(request);
    }

    @Override
    @Transactional
    public StoredProcedureResponse createRfq(CreateRfqRequest request) {
        log.info("createRfq called for transactionPoid={} companyPoid={} user={}", 
                request.getTransactionPoid(), request.getCompanyPoid(), request.getLoginUser());
        return quotationSchStoredProcRepository.callCreateRfqProc(request);
    }

    @Override
    @Transactional
    public StoredProcedureResponse createDeliveryNote(CreateDeliveryNoteRequest request) {
        log.info("createDeliveryNote called for transactionPoid={} companyPoid={} user={}", 
                request.getTransactionPoid(), request.getCompanyPoid(), request.getLoginUser());
        return quotationSchStoredProcRepository.callCreateDeliveryNoteProc(request);
    }

    @Override
    @Transactional
    public StoredProcedureResponse selectAll(SelectAllRequest request) {
        log.info("selectAll called for transactionPoid={} companyPoid={}", 
                request.getTransactionPoid(), request.getCompanyPoid());
        return quotationSchStoredProcRepository.callSelectAllProc(request);
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationResponse validateCustomer(ValidateCustomerRequest request) {
        log.info("validateCustomer called for customerPoid={} principalPoid={} companyPoid={}", 
                request.getCustomerPoid(), request.getPrincipalPoid(), request.getCompanyPoid());
        return quotationSchStoredProcRepository.callValidateCustomerProc(request);
    }

    @Override
    @Transactional
    public StoredProcedureResponse updateQuantity(UpdateQuantityRequest request) {
        log.info("updateQuantity called for transactionPoid={} companyPoid={} user={}", 
                request.getTransactionPoid(), request.getCompanyPoid(), request.getLoginUser());
        return quotationSchStoredProcRepository.callUpdateQuantityProc(request);
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationResponse validateCheckbox(ValidateCheckboxRequest request) {
        log.info("validateCheckbox called for transactionPoid={} detRowId={} companyPoid={}", 
                request.getTransactionPoid(), request.getDetRowId(), request.getCompanyPoid());
        return quotationSchStoredProcRepository.callValidateCheckboxProc(request);
    }

    @Override
    @Transactional
    public StoredProcedureResponse calculate(CalculateRequest request) {
        log.info("calculate called for transactionPoid={} companyPoid={} user={}", 
                request.getTransactionPoid(), request.getCompanyPoid(), request.getLoginUser());
        return quotationSchStoredProcRepository.callCalculateProc(request);
    }
}

