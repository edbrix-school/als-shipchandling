package com.asg.shipchandling.requestforquotation.service;

import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.shipchandling.commonlov.dto.LovItem;
import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.requestforquotation.dto.ItemWithoutSupplierDto;
import com.asg.shipchandling.requestforquotation.dto.RfqDependenciesDto;
import com.asg.shipchandling.requestforquotation.dto.request.*;
import com.asg.shipchandling.requestforquotation.dto.response.*;
import com.asg.shipchandling.requestforquotation.entity.*;
import com.asg.shipchandling.requestforquotation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApRequestForQtnServiceImpl implements ApRequestForQtnService {

    private final ApRequestForQtnHdrRepository rfqHdrRepository;
    private final ApRequestForQtnItemDtlRepository rfqItemDtlRepository;
    private final ApRequestForQtnSupDtlRepository rfqSupDtlRepository;

    private final GlobalTaxMasterRepository globalTaxMasterRepository;
    private final CurrencyRateUploadTempRepository currencyRateUploadTempRepository;
    private final DocumentSearchService documentService;

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    // Add DataSource for stored procedure calls
    // private final DataSource dataSource;

    @Override
    @Transactional
    public ApRequestForQtnHdrDto createRequestForQuotation(CreateApRequestForQtnRequest request,
                                                           Long groupPoid, Long companyPoid, String userId) {

        if (request == null) {
            throw new CustomException("Request body cannot be empty");
        }
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }

        String normalizedUserId = normalizeUserId(userId);

        Timestamp resolvedTransactionDate = request.getTransactionDate() != null
                ? request.getTransactionDate()
                : Timestamp.from(Instant.now());

        ApRequestForQtnHdr rfq = new ApRequestForQtnHdr();
        BeanUtils.copyProperties(request, rfq);
        rfq.setTransactionDate(resolvedTransactionDate);
        rfq.setGroupPoid(groupPoid);
        rfq.setCompanyPoid(companyPoid);
        rfq.setCreatedBy(normalizedUserId);
        rfq.setLastmodifiedBy(normalizedUserId);
        rfq.setStatus("IN PROGRESS");
        rfq.setDeleted("N");

        if (!hasText(rfq.getDescriptionPrintYn())) {
            rfq.setDescriptionPrintYn("N");
        } else {
            rfq.setDescriptionPrintYn(rfq.getDescriptionPrintYn().trim().toUpperCase(Locale.ROOT));
        }

        if (hasText(rfq.getCurrencyCode())) {
            String normalizedCurrency = rfq.getCurrencyCode().trim().toUpperCase(Locale.ROOT);
            validateCurrencyCode(normalizedCurrency, groupPoid, companyPoid);
            rfq.setCurrencyCode(normalizedCurrency);
            if (rfq.getCurrencyRate() == null) {
                rfq.setCurrencyRate(BigDecimal.ONE);
            }
            if (rfq.getCurrencyRate().compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomException("Currency rate must be greater than zero");
            }
        } else {
            rfq.setCurrencyCode(null);
            rfq.setCurrencyRate(null);
        }

        // Save header first to get TRANSACTION_POID (auto-generated)
        ApRequestForQtnHdr savedRfq = rfqHdrRepository.saveAndFlush(rfq);
        log.info("RFQ Header saved with Transaction POID: {}", savedRfq.getTransactionPoid());

        // Process item details based on action field
        processItemDetailsWithAction(savedRfq.getTransactionPoid(), request.getItemDetails(),
                normalizedUserId, groupPoid, companyPoid, true);

        // Process supplier details based on action field
        processSupplierDetailsWithAction(savedRfq.getTransactionPoid(), request.getSupplierDetails(),
                normalizedUserId, true);

        // Call stored procedure AFTER SAVE -
        callItemsWithoutSupplierProcedure(groupPoid, companyPoid, normalizedUserId, savedRfq.getTransactionPoid());

        // Refresh to get auto-generated DocRef
        rfqHdrRepository.flush();
        ApRequestForQtnHdr refreshedRfq = rfqHdrRepository.findByTransactionPoid(
                savedRfq.getTransactionPoid()).orElse(savedRfq);

        // Convert to DTO
        ApRequestForQtnHdrDto dto = convertToDto(refreshedRfq, true);
        log.info("RFQ created successfully with DOC_REF: {}", dto.getDocRef());
        return dto;
    }

    // HELPER METHODS
    /*
     * private boolean isValidCurrency(String currencyCode) {
     * return currencyRateUploadTempRepository.findById(currencyCode).isPresent();
     * }
     */

    @Override
    @Transactional(readOnly = true)
    public ApRequestForQtnHdrDto getRequestForQuotationByPoid(Long transactionPoid, Long groupPoid,
                                                              Long companyPoid, Boolean includeDetails) {

        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("Y".equalsIgnoreCase(rfq.getDeleted())) {
            throw new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid);
        }

        boolean loadDetails = Boolean.TRUE.equals(includeDetails);
        ApRequestForQtnHdrDto dto = convertToDto(rfq, loadDetails);

        if (!loadDetails) {
            dto.setItemDetails(Collections.emptyList());
            dto.setSupplierDetails(Collections.emptyList());
        }

        return dto;
    }

    @Override
    @Transactional
    public ApRequestForQtnHdrDto updateRequestForQuotation(Long transactionPoid, UpdateApRequestForQtnRequest request,
                                                           Long groupPoid, Long companyPoid, String userId) {
        if (request == null) {
            throw new CustomException("Request body cannot be empty");
        }
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }

        String normalizedUserId = normalizeUserId(userId);

        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        // Validate Deletion Flag
        if ("Y".equalsIgnoreCase(rfq.getDeleted())) {
            throw new CustomException("Cannot update deleted RFQ");
        }

        // Check status - CLOSED status prevents editing
        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Current document is in closed status");
        }

        // Validate group/company match
        if (!rfq.getGroupPoid().equals(groupPoid) || !rfq.getCompanyPoid().equals(companyPoid)) {
            throw new CustomException("Group or Company mismatch for RFQ");
        }

        if (request.getTransactionDate() == null) {
            throw new CustomException("Transaction date is required");
        }

        // Update fields (excluding read-only fields)
        BeanUtils.copyProperties(request, rfq, "transactionPoid", "docRef", "status", "salesQtnPoid",
                "salesInvDocRef", "createdBy", "createdDate");

        rfq.setTransactionDate(request.getTransactionDate());

        if (!hasText(rfq.getDescriptionPrintYn())) {
            rfq.setDescriptionPrintYn("N");
        } else {
            rfq.setDescriptionPrintYn(rfq.getDescriptionPrintYn().trim().toUpperCase(Locale.ROOT));
        }

        if (hasText(rfq.getCurrencyCode())) {
            String normalizedCurrency = rfq.getCurrencyCode().trim().toUpperCase(Locale.ROOT);
            validateCurrencyCode(normalizedCurrency, groupPoid, companyPoid);
            rfq.setCurrencyCode(normalizedCurrency);
            if (rfq.getCurrencyRate() == null) {
                rfq.setCurrencyRate(BigDecimal.ONE);
            }
            if (rfq.getCurrencyRate().compareTo(BigDecimal.ZERO) <= 0) {
                throw new CustomException("Currency rate must be greater than zero");
            }
        } else {
            rfq.setCurrencyCode(null);
            rfq.setCurrencyRate(null);
        }

        // Audit fields update
        rfq.setLastmodifiedBy(normalizedUserId);
        rfq.setLastmodifiedDate(new Timestamp(System.currentTimeMillis()));

        // Save header first
        ApRequestForQtnHdr savedRfq = rfqHdrRepository.save(rfq);

        // Process item details based on action field
        processItemDetailsWithAction(transactionPoid, request.getItemDetails(),
                normalizedUserId, groupPoid, companyPoid, false);

        // Process supplier details based on action field
        processSupplierDetailsWithAction(transactionPoid, request.getSupplierDetails(),
                normalizedUserId, false);

        // Call stored procedure AFTER SAVE
        callItemsWithoutSupplierProcedure(groupPoid, companyPoid, normalizedUserId, transactionPoid);

        return convertToDto(savedRfq, true);
    }

    @Override
    @Transactional
    public void deleteRequestForQuotation(Long transactionPoid, Long groupPoid, Long companyPoid) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }

        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        // Validate if already deleted
        if ("Y".equalsIgnoreCase(rfq.getDeleted())) {
            throw new CustomException("RFQ is already deleted");
        }

        // Check status - CLOSED status may prevent deletion
        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot delete RFQ. Current document is in closed status");
        }

        if (rfqSupDtlRepository.countByTransactionPoid(transactionPoid) > 0) {
            throw new CustomException("Cannot delete RFQ. Supplier quotations are linked to this document.");
        }

        if (existsPurchaseOrderForRfq(transactionPoid)) {
            throw new CustomException("Cannot delete RFQ. Purchase Orders are linked to this document.");
        }

        performSoftDelete(rfq);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listRequestForQuotations(String docId, FilterRequestDto request, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "DOC_REF",   // label
                "TRANSACTION_POID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    private void callItemsWithoutSupplierProcedure(Long groupPoid, Long companyPoid, String userId,
                                                   Long transactionPoid) {
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping call to PROC_AP_RFQ_ITEMS_WITHOUT_SUP");
            return;
        }
        String sql = "{ call PROC_AP_RFQ_ITEMS_WITHOUT_SUP(?, ?, ?, ?, ?) }";

        try (Connection connection = dataSource.getConnection();
             CallableStatement callableStatement = connection.prepareCall(sql)) {

            // --- Set IN parameters ---
            callableStatement.setLong(1, groupPoid);
            callableStatement.setString(2, userId);
            callableStatement.setLong(3, companyPoid);
            callableStatement.setLong(4, transactionPoid);

            // --- Register OUT parameter ---
            callableStatement.registerOutParameter(5, Types.VARCHAR);

            // --- Execute the stored procedure ---
            callableStatement.execute();

            // --- Retrieve the OUT parameter ---
            String result = callableStatement.getString(5);

            // --- Log and handle any "ERROR" response from procedure ---
            if (result != null && result.toUpperCase().contains("ERROR")) {
                log.warn("PROC_AP_RFQ_ITEMS_WITHOUT_SUP returned error: {}", result);
                // Optionally, throw a custom warning exception or just log it
                // throw new CustomException("Error executing stored procedure: " + result);
            } else {
                log.info("PROC_AP_RFQ_ITEMS_WITHOUT_SUP executed successfully for RFQ POID: {} - Result: {}",
                        transactionPoid, result);
            }

        } catch (SQLException ex) {
            log.error("Failed to execute stored procedure PROC_AP_RFQ_ITEMS_WITHOUT_SUP for RFQ POID: {}",
                    transactionPoid, ex);
            throw new CustomException("Database error while calling stored procedure: " + ex.getMessage());
        }

    }

    private void saveItemDetails(Long transactionPoid,
                                 List<CreateApRequestForQtnItemDtlRequest> details,
                                 String userId,
                                 Long groupPoid,
                                 Long companyPoid) {
        if (details == null || details.isEmpty()) {
            return;
        }

        Long maxDetRowId = rfqItemDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        for (CreateApRequestForQtnItemDtlRequest detail : details) {
            validateItemDetail(detail);

            ApRequestForQtnItemDtl itemDtl = new ApRequestForQtnItemDtl();
            itemDtl.setTransactionPoid(transactionPoid);
            itemDtl.setDetRowId(detRowId++);
            itemDtl.setStockPoid(detail.getStockPoid());

            Long stockUnitPoid = detail.getStockUnitPoid();
            if (stockUnitPoid == null) {
                stockUnitPoid = getDefaultUnitFromProcedure(detail.getStockPoid());
                if (stockUnitPoid == null) {
                    throw new CustomException(
                            "Unable to determine default stock unit for stock POID: " + detail.getStockPoid());
                }
            }
            itemDtl.setStockUnitPoid(stockUnitPoid);

            itemDtl.setQty(detail.getQty());
            itemDtl.setSupplierPoid(detail.getSupplierPoid());
            itemDtl.setPrice(detail.getPrice());

            if (detail.getSupplierPoid() != null) {
                BigDecimal lastPrice = getLastPriceFromProcedure(detail.getStockPoid(), stockUnitPoid,
                        detail.getSupplierPoid(), groupPoid, companyPoid, userId);
                if (lastPrice != null) {
                    itemDtl.setLastRate(lastPrice);
                }
            }

            populateTaxDetails(itemDtl, detail.getTaxPoid(), detail.getQty(), detail.getPrice());

            itemDtl.setRemarks(detail.getRemarks());
            itemDtl.setCreatedBy(userId);
            itemDtl.setLastmodifiedBy(userId);

            rfqItemDtlRepository.save(itemDtl);
        }
    }

    private void saveSupplierDetails(Long transactionPoid, List<CreateApRequestForQtnSupDtlRequest> details,
                                     String userId) {
        Long maxDetRowId = rfqSupDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        for (CreateApRequestForQtnSupDtlRequest detail : details) {
            ApRequestForQtnSupDtl supDtl = new ApRequestForQtnSupDtl();
            supDtl.setTransactionPoid(transactionPoid);
            supDtl.setDetRowId(detRowId++);
            supDtl.setSupplierPoid(detail.getSupplierPoid());
            supDtl.setRemarks(detail.getRemarks());
            supDtl.setCreatedBy(userId);
            supDtl.setLastmodifiedBy(userId);
            rfqSupDtlRepository.save(supDtl);
        }
    }

    private void updateItemDetails(Long transactionPoid,
                                   List<CreateApRequestForQtnItemDtlRequest> details,
                                   Long groupPoid,
                                   Long companyPoid,
                                   String userId) {
        // Delete existing
        rfqItemDtlRepository.deleteByTransactionPoid(transactionPoid);
        // Save new
        if (details != null && !details.isEmpty()) {
            saveItemDetails(transactionPoid, details, userId, groupPoid, companyPoid);
        }
    }

    private void updateSupplierDetails(Long transactionPoid, List<CreateApRequestForQtnSupDtlRequest> details,
                                       String userId) {
        // Delete existing
        rfqSupDtlRepository.deleteByTransactionPoid(transactionPoid);
        // Save new
        if (details != null && !details.isEmpty()) {
            saveSupplierDetails(transactionPoid, details, userId);
        }
    }

    /**
     * Process item details based on action field (isCreated, isUpdated, isDeleted, noChange)
     */
    private void processItemDetailsWithAction(Long transactionPoid,
                                              List<CreateApRequestForQtnItemDtlRequest> itemDetails,
                                              String userId,
                                              Long groupPoid,
                                              Long companyPoid,
                                              boolean isCreateOperation) {
        if (itemDetails == null || itemDetails.isEmpty()) {
            return;
        }

        // Validate RFQ exists and is not closed (for update operations)
        if (!isCreateOperation) {
            ApRequestForQtnHdr rfq = rfqHdrRepository
                    .findByTransactionPoid(transactionPoid)
                    .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));
            if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
                throw new CustomException("Cannot modify items. RFQ is in closed status");
            }
        }

        Long maxDetRowId = rfqItemDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long nextDetRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        for (CreateApRequestForQtnItemDtlRequest detail : itemDetails) {
            String action = detail.getAction();
            if (action == null || action.trim().isEmpty()) {
                // Default action based on operation type
                // NOTE: isCreated is disabled for itemDetails, so default to noChange
                action = "noChange";
            } else {
                action = action.trim();
            }

            // Validate action value - reject disabled actions
            if ("isCreated".equalsIgnoreCase(action) || "isDeleted".equalsIgnoreCase(action)) {
                throw new CustomException("Action '" + action + "' is currently disabled for itemDetails. Only 'isUpdated' and 'noChange' are supported.");
            }

            // Validate action value
            if (!isValidAction(action)) {
                throw new CustomException("Invalid action value: " + action + ". Valid values are: isUpdated, noChange");
            }

            // For create operation, skip itemDetails processing (isCreated is disabled)
            if (isCreateOperation) {
                continue;
            }

            switch (action.toLowerCase()) {
                // COMMENTED OUT: isCreated action for itemDetails is temporarily disabled
                // case "iscreated":
                //     // Validate detRowId is null for new items
                //     if (detail.getDetRowId() != null) {
                //         throw new CustomException("detRowId must be null for isCreated action");
                //     }
                //     validateItemDetail(detail);
                //     createItemDetail(transactionPoid, detail, userId, groupPoid, companyPoid, nextDetRowId++);
                //     break;

                case "isupdated":
                    // Validate detRowId is provided
                    if (detail.getDetRowId() == null) {
                        throw new CustomException("detRowId is required for isUpdated action");
                    }
                    validateItemDetail(detail);
                    updateItemDetailByAction(transactionPoid, detail, userId, groupPoid, companyPoid);
                    break;

                // COMMENTED OUT: isDeleted action for itemDetails is temporarily disabled
                // case "isdeleted":
                //     // Validate detRowId is provided
                //     if (detail.getDetRowId() == null) {
                //         throw new CustomException("detRowId is required for isDeleted action");
                //     }
                //     deleteItemDetailByAction(transactionPoid, detail.getDetRowId(), groupPoid, companyPoid);
                //     break;

                case "nochange":
                    // Skip processing
                    break;

                default:
                    // Check if it's a disabled action
                    if ("iscreated".equalsIgnoreCase(action) || "isdeleted".equalsIgnoreCase(action)) {
                        throw new CustomException("Action '" + action + "' is currently disabled for itemDetails. Only 'isUpdated' and 'noChange' are supported.");
                    }
                    throw new CustomException("Unsupported action: " + action);
            }
        }
    }

    /**
     * Process supplier details based on action field (isCreated, isUpdated, isDeleted, noChange)
     */
    private void processSupplierDetailsWithAction(Long transactionPoid,
                                                  List<CreateApRequestForQtnSupDtlRequest> supplierDetails,
                                                  String userId,
                                                  boolean isCreateOperation) {
        if (supplierDetails == null || supplierDetails.isEmpty()) {
            return;
        }

        // Validate RFQ exists and is not closed (for update operations)
        if (!isCreateOperation) {
            ApRequestForQtnHdr rfq = rfqHdrRepository
                    .findByTransactionPoid(transactionPoid)
                    .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));
            if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
                throw new CustomException("Cannot modify suppliers. RFQ is in closed status");
            }
        }

        Long maxDetRowId = rfqSupDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long nextDetRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        for (CreateApRequestForQtnSupDtlRequest detail : supplierDetails) {
            String action = detail.getAction();
            if (action == null || action.trim().isEmpty()) {
                // Default action based on operation type
                action = isCreateOperation ? "isCreated" : "noChange";
            } else {
                action = action.trim();
            }

            // Validate action value
            if (!isValidAction(action)) {
                throw new CustomException("Invalid action value: " + action + ". Valid values are: isCreated, isUpdated, isDeleted, noChange");
            }

            // For create operation, only process isCreated items
            if (isCreateOperation && !"isCreated".equalsIgnoreCase(action)) {
                continue;
            }

            switch (action.toLowerCase()) {
                case "iscreated":
                    // Validate detRowId is null for new items
                    if (detail.getDetRowId() != null) {
                        throw new CustomException("detRowId must be null for isCreated action");
                    }
                    if (detail.getSupplierPoid() == null && !hasText(detail.getRemarks())) {
                        throw new CustomException("Supplier or remarks must be provided when adding supplier detail");
                    }
                    createSupplierDetail(transactionPoid, detail, userId, nextDetRowId++);
                    break;

                case "isupdated":
                    // Validate detRowId is provided
                    if (detail.getDetRowId() == null) {
                        throw new CustomException("detRowId is required for isUpdated action");
                    }
                    if (detail.getSupplierPoid() == null && !hasText(detail.getRemarks())) {
                        throw new CustomException("Supplier or remarks must be provided when updating supplier detail");
                    }
                    updateSupplierDetailByAction(transactionPoid, detail, userId);
                    break;

                case "isdeleted":
                    // Validate detRowId is provided
                    if (detail.getDetRowId() == null) {
                        throw new CustomException("detRowId is required for isDeleted action");
                    }
                    deleteSupplierDetailByAction(transactionPoid, detail.getDetRowId());
                    break;

                case "nochange":
                    // Skip processing
                    break;

                default:
                    throw new CustomException("Unsupported action: " + action);
            }
        }
    }

    /**
     * Validate action value
     */
    private boolean isValidAction(String action) {
        if (action == null) {
            return false;
        }
        String normalized = action.trim().toLowerCase();
        return "iscreated".equals(normalized) || "isupdated".equals(normalized)
                || "isdeleted".equals(normalized) || "nochange".equals(normalized);
    }

    /**
     * Create a new item detail
     */
    private void createItemDetail(Long transactionPoid, CreateApRequestForQtnItemDtlRequest request,
                                  String userId, Long groupPoid, Long companyPoid, Long detRowId) {
        ApRequestForQtnItemDtl itemDtl = new ApRequestForQtnItemDtl();
        itemDtl.setTransactionPoid(transactionPoid);
        itemDtl.setDetRowId(detRowId);
        itemDtl.setStockPoid(request.getStockPoid());
        itemDtl.setQty(request.getQty());
        itemDtl.setPrice(request.getPrice());

        Long stockUnitPoid = request.getStockUnitPoid();
        if (stockUnitPoid == null) {
            stockUnitPoid = getDefaultUnitFromProcedure(request.getStockPoid());
            if (stockUnitPoid == null) {
                throw new CustomException(
                        "Unable to determine default stock unit for stock POID: " + request.getStockPoid());
            }
        }
        itemDtl.setStockUnitPoid(stockUnitPoid);
        itemDtl.setSupplierPoid(request.getSupplierPoid());

        // Get last price if stock, unit, and supplier are all set
        if (request.getSupplierPoid() != null) {
            BigDecimal lastPrice = getLastPriceFromProcedure(request.getStockPoid(), stockUnitPoid,
                    request.getSupplierPoid(), groupPoid, companyPoid, userId);
            if (lastPrice != null) {
                itemDtl.setLastRate(lastPrice);
            }
        }

        populateTaxDetails(itemDtl, request.getTaxPoid(), request.getQty(), request.getPrice());
        itemDtl.setRemarks(request.getRemarks());
        itemDtl.setCreatedBy(userId);
        itemDtl.setLastmodifiedBy(userId);

        rfqItemDtlRepository.save(itemDtl);
    }

    /**
     * Update an existing item detail by action
     */
    private void updateItemDetailByAction(Long transactionPoid, CreateApRequestForQtnItemDtlRequest request,
                                          String userId, Long groupPoid, Long companyPoid) {
        // Find existing item detail
        ApRequestForQtnItemDtl itemDtl = rfqItemDtlRepository
                .findById(new ApRequestForQtnItemDtlId(transactionPoid, request.getDetRowId()))
                .orElseThrow(() -> new ResourceNotFoundException("Item Detail", "detRowId", request.getDetRowId()));

        // Check conditional read-only: If RefPoid > 0, some fields become read-only
        if (itemDtl.getRefPoid() != null && !itemDtl.getRefPoid().isEmpty() &&
                Long.parseLong(itemDtl.getRefPoid()) > 0) {
            // StockPoid, StockUnitPoid, SupplierPoid become read-only
            if (!request.getStockPoid().equals(itemDtl.getStockPoid()) ||
                    !request.getStockUnitPoid().equals(itemDtl.getStockUnitPoid()) ||
                    (request.getSupplierPoid() != null
                            && !request.getSupplierPoid().equals(itemDtl.getSupplierPoid()))) {
                throw new CustomException(
                        "Cannot modify stock, unit, or supplier. Item is linked to another document.");
            }
        }

        // Check if RefDocId contains "400" (Purchase Order) - Price becomes read-only
        if (itemDtl.getRefDocId() != null && itemDtl.getRefDocId().contains("400")) {
            if (request.getPrice() != null && !request.getPrice().equals(itemDtl.getPrice())) {
                throw new CustomException("Cannot modify price. Item is linked to Purchase Order.");
            }
        }

        // Update fields
        itemDtl.setStockPoid(request.getStockPoid());
        Long stockUnitPoid = request.getStockUnitPoid();
        if (stockUnitPoid == null) {
            stockUnitPoid = getDefaultUnitFromProcedure(request.getStockPoid());
            if (stockUnitPoid == null) {
                throw new CustomException(
                        "Unable to determine default stock unit for stock POID: " + request.getStockPoid());
            }
        }
        itemDtl.setStockUnitPoid(stockUnitPoid);
        itemDtl.setQty(request.getQty());
        if (itemDtl.getRefDocId() == null || !itemDtl.getRefDocId().contains("400")) {
            itemDtl.setPrice(request.getPrice());
        }
        itemDtl.setSupplierPoid(request.getSupplierPoid());
        itemDtl.setRemarks(request.getRemarks());

        // Update last price if stock, unit, and supplier are all set
        if (request.getSupplierPoid() != null) {
            BigDecimal lastPrice = getLastPriceFromProcedure(request.getStockPoid(), stockUnitPoid,
                    request.getSupplierPoid(), groupPoid, companyPoid, userId);
            if (lastPrice != null) {
                itemDtl.setLastRate(lastPrice);
            }
        }

        populateTaxDetails(itemDtl, request.getTaxPoid(), itemDtl.getQty(), itemDtl.getPrice());
        itemDtl.setLastmodifiedBy(userId);

        rfqItemDtlRepository.save(itemDtl);
    }

    /**
     * Delete an existing item detail by action
     */
    private void deleteItemDetailByAction(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid) {
        ApRequestForQtnItemDtl itemDtl = rfqItemDtlRepository
                .findById(new ApRequestForQtnItemDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Item Detail", "detRowId", detRowId));

        if (itemDtl.getRefPoid() != null && !itemDtl.getRefPoid().isEmpty() &&
                Long.parseLong(itemDtl.getRefPoid()) > 0) {
            throw new CustomException("Cannot delete item detail. It is linked to another document.");
        }
        if (itemDtl.getRefDocId() != null && itemDtl.getRefDocId().contains("400")) {
            throw new CustomException("Cannot delete item detail linked to Purchase Order.");
        }

        rfqItemDtlRepository.delete(itemDtl);
    }

    /**
     * Create a new supplier detail
     */
    private void createSupplierDetail(Long transactionPoid, CreateApRequestForQtnSupDtlRequest request,
                                      String userId, Long detRowId) {
        ApRequestForQtnSupDtl supDtl = new ApRequestForQtnSupDtl();
        supDtl.setTransactionPoid(transactionPoid);
        supDtl.setDetRowId(detRowId);
        supDtl.setSupplierPoid(request.getSupplierPoid());
        supDtl.setRemarks(hasText(request.getRemarks()) ? request.getRemarks().trim() : null);
        supDtl.setCreatedBy(userId);
        supDtl.setLastmodifiedBy(userId);

        rfqSupDtlRepository.save(supDtl);
    }

    /**
     * Update an existing supplier detail by action
     */
    private void updateSupplierDetailByAction(Long transactionPoid, CreateApRequestForQtnSupDtlRequest request,
                                              String userId) {
        // Find existing supplier detail
        ApRequestForQtnSupDtl supDtl = rfqSupDtlRepository
                .findById(new ApRequestForQtnSupDtlId(transactionPoid, request.getDetRowId()))
                .orElseThrow(() -> new ResourceNotFoundException("Supplier Detail", "detRowId", request.getDetRowId()));

        // Update fields
        supDtl.setSupplierPoid(request.getSupplierPoid());
        supDtl.setRemarks(hasText(request.getRemarks()) ? request.getRemarks().trim() : null);
        supDtl.setLastmodifiedBy(userId);

        rfqSupDtlRepository.save(supDtl);
    }

    /**
     * Delete an existing supplier detail by action
     */
    private void deleteSupplierDetailByAction(Long transactionPoid, Long detRowId) {
        ApRequestForQtnSupDtl supDtl = rfqSupDtlRepository
                .findById(new ApRequestForQtnSupDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Supplier Detail", "detRowId", detRowId));

        rfqSupDtlRepository.delete(supDtl);
    }

    private void validateItemDetail(CreateApRequestForQtnItemDtlRequest detail) {
        if (detail == null) {
            throw new CustomException("Item detail cannot be null");
        }
        if (detail.getStockPoid() == null) {
            throw new CustomException("Stock POID is required for RFQ item detail");
        }
        if (detail.getQty() == null) {
            throw new CustomException("Quantity is required for RFQ item detail");
        }
        if (detail.getQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Quantity must be greater than zero");
        }
        if (detail.getPrice() != null && detail.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Price cannot be negative for RFQ item detail");
        }
    }

    private void populateTaxDetails(ApRequestForQtnItemDtl itemDtl,
                                    Long taxPoid,
                                    BigDecimal qty,
                                    BigDecimal price) {
        if (taxPoid == null) {
            itemDtl.setTaxPoid(null);
            itemDtl.setTaxPercentage(null);
            itemDtl.setTaxAmount(null);
            return;
        }

        GlobalTaxMaster tax = globalTaxMasterRepository
                .findActiveByTaxPoid(BigDecimal.valueOf(taxPoid))
                .orElseThrow(() -> new CustomException("Invalid or inactive Tax POID: " + taxPoid));

        BigDecimal taxPercentage = tax.getPercentage();
        if (taxPercentage == null) {
            throw new CustomException("Tax percentage is not configured for Tax POID: " + taxPoid);
        }

        itemDtl.setTaxPoid(taxPoid);
        itemDtl.setTaxPercentage(taxPercentage);
        itemDtl.setTaxAmount(calculateTaxAmount(qty, price, taxPercentage));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTaxPercentage(Long groupPoid, Long companyPoid, Long taxPoid) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        if (taxPoid == null) {
            throw new CustomException("Tax POID is required");
        }

        GlobalTaxMaster tax = globalTaxMasterRepository
                .findActiveByTaxPoid(BigDecimal.valueOf(taxPoid))
                .orElseThrow(() -> new CustomException("Invalid or inactive Tax POID: " + taxPoid));

        if (tax.getGroupPoid() != null
                && BigDecimal.valueOf(groupPoid).compareTo(tax.getGroupPoid()) != 0) {
            throw new CustomException("Tax POID does not belong to the provided group");
        }

        BigDecimal taxPercentage = tax.getPercentage();
        if (taxPercentage == null) {
            throw new CustomException("Tax percentage is not configured for Tax POID: " + taxPoid);
        }
        return taxPercentage;
    }

    private BigDecimal calculateTaxAmount(BigDecimal qty, BigDecimal price, BigDecimal taxPercentage) {
        if (qty == null || price == null || taxPercentage == null) {
            return null;
        }
        if (taxPercentage.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return qty.multiply(price)
                .multiply(taxPercentage)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    private LocalDate toLocalDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().toLocalDate();
    }

    private void performSoftDelete(ApRequestForQtnHdr rfq) {
        Long transactionPoid = rfq.getTransactionPoid();
        rfqItemDtlRepository.deleteByTransactionPoid(transactionPoid);
        rfqSupDtlRepository.deleteByTransactionPoid(transactionPoid);
        rfq.setDeleted("Y");
        rfq.setLastmodifiedDate(new Timestamp(System.currentTimeMillis()));
        rfqHdrRepository.save(rfq);
    }

    private boolean existsPurchaseOrderForRfq(Long transactionPoid) {
        // TODO: Implement actual dependency check against purchase order
        // repository/table
        return false;
    }

    private String normalizeUserId(String userId) {
        if (!hasText(userId)) {
            throw new CustomException("User Id header is required");
        }
        return userId.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void validateCurrencyCode(String currencyCode, Long groupPoid, Long companyPoid) {
        if (!hasText(currencyCode)) {
            return;
        }
        String normalizedCurrency = currencyCode.trim().toUpperCase(Locale.ROOT);

        if (currencyExistsInMaster(normalizedCurrency)) {
            return;
        }

        boolean existsInRateUpload = false;

        if (groupPoid != null && companyPoid != null) {
            existsInRateUpload = currencyRateUploadTempRepository.existsByCurrencyCodeAndContext(
                    normalizedCurrency,
                    BigDecimal.valueOf(groupPoid),
                    BigDecimal.valueOf(companyPoid));
        }

        if (!existsInRateUpload) {
            existsInRateUpload = currencyRateUploadTempRepository.existsByCurrencyCodeIgnoreCase(normalizedCurrency);
        }

        if (!existsInRateUpload) {
            throw new CustomException("Invalid or inactive currency code: " + normalizedCurrency);
        }
    }

    private boolean currencyExistsInMaster(String currencyCode) {
        if (dataSource == null) {
            return false;
        }

        final String sql = "SELECT COUNT(1) FROM CURRENCY_MASTER WHERE UPPER(CURRENCY_CODE) = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, currencyCode.toUpperCase(Locale.ROOT));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1) > 0;
                }
            }
        } catch (SQLException ex) {
            log.warn("Currency validation against CURRENCY_MASTER failed for code {}: {}", currencyCode,
                    ex.getMessage());
        }

        return false;
    }

    private ApRequestForQtnHdrDto convertToDto(ApRequestForQtnHdr rfq, boolean includeDetails) {
        ApRequestForQtnHdrDto dto = new ApRequestForQtnHdrDto();
        BeanUtils.copyProperties(rfq, dto);

        // Populate LOV details for header
        dto.setDivisionPoidDetail(getDivisionPoidDetail(rfq.getDivisionPoid()));
        dto.setSalesQtnPoidDetails(getSalesQtnPoidDetails(rfq.getSalesQtnPoid()));

        if (includeDetails) {
            List<ApRequestForQtnItemDtl> itemDetails = rfqItemDtlRepository
                    .findByTransactionPoid(rfq.getTransactionPoid())
                    .stream()
                    .sorted(Comparator.comparing(ApRequestForQtnItemDtl::getDetRowId))
                    .collect(Collectors.toList());
            dto.setItemDetails(itemDetails.stream()
                    .map(item -> convertItemDtlToDto(item, rfq.getGroupPoid()))
                    .collect(Collectors.toList()));

            List<ApRequestForQtnSupDtl> supplierDetails = rfqSupDtlRepository
                    .findByTransactionPoid(rfq.getTransactionPoid())
                    .stream()
                    .sorted(Comparator.comparing(ApRequestForQtnSupDtl::getDetRowId))
                    .collect(Collectors.toList());
            dto.setSupplierDetails(supplierDetails.stream()
                    .map(this::convertSupDtlToDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private ApRequestForQtnItemDtlDto convertItemDtlToDto(ApRequestForQtnItemDtl itemDtl) {
        return convertItemDtlToDto(itemDtl, null);
    }

    private ApRequestForQtnItemDtlDto convertItemDtlToDto(ApRequestForQtnItemDtl itemDtl, Long groupPoid) {
        ApRequestForQtnItemDtlDto dto = new ApRequestForQtnItemDtlDto();
        BeanUtils.copyProperties(itemDtl, dto);

        // Populate LOV details for item
        dto.setStockPoidDetails(getStockPoidDetails(itemDtl.getStockPoid()));
        dto.setStockUnitDetails(getStockUnitDetails(itemDtl.getStockUnitPoid()));
        dto.setTaxPoidDetails(getTaxPoidDetails(itemDtl.getTaxPoid()));

        // For supplier in item details, use the special method that checks transaction and excludes cash/cheque suppliers
        if (groupPoid != null) {
            dto.setSupplierPoidDetails(getSupplierPoidDetailsForItem(
                    itemDtl.getSupplierPoid(),
                    itemDtl.getTransactionPoid(),
                    groupPoid));
        } else {
            // Fallback: use simple supplier lookup if groupPoid is not available
            dto.setSupplierPoidDetails(getSupplierPoidDetailsForSupplier(itemDtl.getSupplierPoid()));
        }

        return dto;
    }

    private ApRequestForQtnSupDtlDto convertSupDtlToDto(ApRequestForQtnSupDtl supDtl) {
        ApRequestForQtnSupDtlDto dto = new ApRequestForQtnSupDtlDto();
        BeanUtils.copyProperties(supDtl, dto);

        // Populate LOV details for supplier
        dto.setSupplierPoidDetails(getSupplierPoidDetailsForSupplier(supDtl.getSupplierPoid()));

        return dto;
    }

    // Detail Table Methods
    @Override
    @Transactional
    public ApRequestForQtnItemDtlDto addItemDetail(Long transactionPoid, CreateApRequestForQtnItemDtlRequest request,
                                                   Long groupPoid, Long companyPoid, String userId) {
        if (request == null) {
            throw new CustomException("Request body cannot be empty");
        }
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }

        // Validate RFQ exists and belongs to group/company
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot add items. RFQ is in closed status");
        }

        String normalizedUserId = normalizeUserId(userId);

        validateItemDetail(request);

        // Get next DetRowId
        Long maxDetRowId = rfqItemDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        // Create item detail
        ApRequestForQtnItemDtl itemDtl = new ApRequestForQtnItemDtl();
        itemDtl.setTransactionPoid(transactionPoid);
        itemDtl.setDetRowId(detRowId);
        itemDtl.setStockPoid(request.getStockPoid());
        itemDtl.setQty(request.getQty());
        itemDtl.setPrice(request.getPrice());

        Long stockUnitPoid = request.getStockUnitPoid();
        if (stockUnitPoid == null) {
            stockUnitPoid = getDefaultUnitFromProcedure(request.getStockPoid());
            if (stockUnitPoid == null) {
                throw new CustomException(
                        "Unable to determine default stock unit for stock POID: " + request.getStockPoid());
            }
        }
        itemDtl.setStockUnitPoid(stockUnitPoid);
        itemDtl.setSupplierPoid(request.getSupplierPoid());

        // Get last price if stock, unit, and supplier are all set
        if (request.getSupplierPoid() != null) {
            BigDecimal lastPrice = getLastPriceFromProcedure(request.getStockPoid(), stockUnitPoid,
                    request.getSupplierPoid(), groupPoid, companyPoid, normalizedUserId);
            if (lastPrice != null) {
                itemDtl.setLastRate(lastPrice);
            }
        }

        populateTaxDetails(itemDtl, request.getTaxPoid(), request.getQty(), request.getPrice());

        itemDtl.setRemarks(request.getRemarks());
        itemDtl.setCreatedBy(normalizedUserId);
        itemDtl.setLastmodifiedBy(normalizedUserId);

        ApRequestForQtnItemDtl savedItemDtl = rfqItemDtlRepository.save(itemDtl);
        return convertItemDtlToDto(savedItemDtl, groupPoid);
    }

    @Override
    @Transactional
    public ApRequestForQtnItemDtlDto updateItemDetail(Long transactionPoid, Long detRowId,
                                                      CreateApRequestForQtnItemDtlRequest request,
                                                      Long groupPoid, Long companyPoid, String userId) {
        if (request == null) {
            throw new CustomException("Request body cannot be empty");
        }
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot update items. RFQ is in closed status");
        }

        String normalizedUserId = normalizeUserId(userId);

        validateItemDetail(request);

        // Find existing item detail
        ApRequestForQtnItemDtl itemDtl = rfqItemDtlRepository
                .findById(new ApRequestForQtnItemDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Item Detail", "detRowId", detRowId));

        // Check conditional read-only: If RefPoid > 0, some fields become read-only
        if (itemDtl.getRefPoid() != null && !itemDtl.getRefPoid().isEmpty() &&
                Long.parseLong(itemDtl.getRefPoid()) > 0) {
            // StockPoid, StockUnitPoid, SupplierPoid become read-only
            if (!request.getStockPoid().equals(itemDtl.getStockPoid()) ||
                    !request.getStockUnitPoid().equals(itemDtl.getStockUnitPoid()) ||
                    (request.getSupplierPoid() != null
                            && !request.getSupplierPoid().equals(itemDtl.getSupplierPoid()))) {
                throw new CustomException(
                        "Cannot modify stock, unit, or supplier. Item is linked to another document.");
            }
        }

        // Check if RefDocId contains "400" (Purchase Order) - Price becomes read-only
        if (itemDtl.getRefDocId() != null && itemDtl.getRefDocId().contains("400")) {
            if (request.getPrice() != null && !request.getPrice().equals(itemDtl.getPrice())) {
                throw new CustomException("Cannot modify price. Item is linked to Purchase Order.");
            }
        }

        // Update fields
        itemDtl.setStockPoid(request.getStockPoid());
        Long stockUnitPoid = request.getStockUnitPoid();
        if (stockUnitPoid == null) {
            stockUnitPoid = getDefaultUnitFromProcedure(request.getStockPoid());
            if (stockUnitPoid == null) {
                throw new CustomException(
                        "Unable to determine default stock unit for stock POID: " + request.getStockPoid());
            }
        }
        itemDtl.setStockUnitPoid(stockUnitPoid);
        itemDtl.setQty(request.getQty());
        if (itemDtl.getRefDocId() == null || !itemDtl.getRefDocId().contains("400")) {
            itemDtl.setPrice(request.getPrice());
        }
        itemDtl.setSupplierPoid(request.getSupplierPoid());
        itemDtl.setRemarks(request.getRemarks());
        itemDtl.setLastmodifiedBy(userId);

        // Update last price if stock, unit, and supplier are all set
        if (request.getSupplierPoid() != null) {
            BigDecimal lastPrice = getLastPriceFromProcedure(request.getStockPoid(), stockUnitPoid,
                    request.getSupplierPoid(), groupPoid, companyPoid, normalizedUserId);
            if (lastPrice != null) {
                itemDtl.setLastRate(lastPrice);
            }
        }

        populateTaxDetails(itemDtl, request.getTaxPoid(), itemDtl.getQty(), itemDtl.getPrice());
        itemDtl.setLastmodifiedBy(normalizedUserId);

        ApRequestForQtnItemDtl savedItemDtl = rfqItemDtlRepository.save(itemDtl);
        return convertItemDtlToDto(savedItemDtl, groupPoid);
    }

    @Override
    @Transactional
    public void deleteItemDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot delete items. RFQ is in closed status");
        }

        ApRequestForQtnItemDtl itemDtl = rfqItemDtlRepository
                .findById(new ApRequestForQtnItemDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Item Detail", "detRowId", detRowId));

        if (itemDtl.getRefPoid() != null && !itemDtl.getRefPoid().isEmpty() &&
                Long.parseLong(itemDtl.getRefPoid()) > 0) {
            throw new CustomException("Cannot delete item detail. It is linked to another document.");
        }
        if (itemDtl.getRefDocId() != null && itemDtl.getRefDocId().contains("400")) {
            throw new CustomException("Cannot delete item detail linked to Purchase Order.");
        }

        rfqItemDtlRepository.delete(itemDtl);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApRequestForQtnItemDtlDto> getItemDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        // Validate RFQ exists
        rfqHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        List<ApRequestForQtnItemDtl> itemDetails = rfqItemDtlRepository.findByTransactionPoid(transactionPoid)
                .stream()
                .sorted(Comparator.comparing(ApRequestForQtnItemDtl::getDetRowId))
                .collect(Collectors.toList());
        return itemDetails.stream()
                .map(item -> convertItemDtlToDto(item, groupPoid))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApRequestForQtnSupDtlDto addSupplierDetail(Long transactionPoid, CreateApRequestForQtnSupDtlRequest request,
                                                      Long groupPoid, Long companyPoid, String userId) {
        if (request == null) {
            throw new CustomException("Request body cannot be empty");
        }
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot add suppliers. RFQ is in closed status");
        }

        String normalizedUserId = normalizeUserId(userId);

        if (request.getSupplierPoid() == null && !hasText(request.getRemarks())) {
            throw new CustomException("Supplier or remarks must be provided when adding supplier detail");
        }

        // Get next DetRowId
        Long maxDetRowId = rfqSupDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        // Create supplier detail
        ApRequestForQtnSupDtl supDtl = new ApRequestForQtnSupDtl();
        supDtl.setTransactionPoid(transactionPoid);
        supDtl.setDetRowId(detRowId);
        supDtl.setSupplierPoid(request.getSupplierPoid());
        supDtl.setRemarks(hasText(request.getRemarks()) ? request.getRemarks().trim() : null);
        supDtl.setCreatedBy(normalizedUserId);
        supDtl.setLastmodifiedBy(normalizedUserId);

        ApRequestForQtnSupDtl savedSupDtl = rfqSupDtlRepository.save(supDtl);
        return convertSupDtlToDto(savedSupDtl);
    }

    @Override
    @Transactional
    public ApRequestForQtnSupDtlDto updateSupplierDetail(Long transactionPoid, Long detRowId,
                                                         CreateApRequestForQtnSupDtlRequest request,
                                                         Long groupPoid, Long companyPoid, String userId) {
        if (request == null) {
            throw new CustomException("Request body cannot be empty");
        }
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot update suppliers. RFQ is in closed status");
        }

        String normalizedUserId = normalizeUserId(userId);

        if (request.getSupplierPoid() == null && !hasText(request.getRemarks())) {
            throw new CustomException("Supplier or remarks must be provided when updating supplier detail");
        }

        // Find existing supplier detail
        ApRequestForQtnSupDtl supDtl = rfqSupDtlRepository
                .findById(new ApRequestForQtnSupDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Supplier Detail", "detRowId", detRowId));

        // Update fields
        supDtl.setSupplierPoid(request.getSupplierPoid());
        supDtl.setRemarks(hasText(request.getRemarks()) ? request.getRemarks().trim() : null);
        supDtl.setLastmodifiedBy(normalizedUserId);

        ApRequestForQtnSupDtl savedSupDtl = rfqSupDtlRepository.save(supDtl);
        return convertSupDtlToDto(savedSupDtl);
    }

    @Override
    @Transactional
    public void deleteSupplierDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot delete suppliers. RFQ is in closed status");
        }

        ApRequestForQtnSupDtl supDtl = rfqSupDtlRepository
                .findById(new ApRequestForQtnSupDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Supplier Detail", "detRowId", detRowId));

        rfqSupDtlRepository.delete(supDtl);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApRequestForQtnSupDtlDto> getSupplierDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        // Validate RFQ exists
        rfqHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        List<ApRequestForQtnSupDtl> supplierDetails = rfqSupDtlRepository.findByTransactionPoid(transactionPoid)
                .stream()
                .sorted(Comparator.comparing(ApRequestForQtnSupDtl::getDetRowId))
                .collect(Collectors.toList());
        return supplierDetails.stream()
                .map(this::convertSupDtlToDto)
                .collect(Collectors.toList());
    }

    // Business Logic Methods
    @Override
    @Transactional
    public AddSuppliersResponse addRelatedSuppliers(Long transactionPoid, Long groupPoid, Long companyPoid,
                                                    String userId) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        String normalizedUserId = normalizeUserId(userId);

        // Validate RFQ exists and is in edit mode
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot add suppliers. RFQ is in closed status");
        }

        // Call stored procedure
        String result = callAddSuppliersProcedure(groupPoid, companyPoid, normalizedUserId, transactionPoid);

        if (result != null && result.contains("ERROR")) {
            throw new CustomException("Error adding suppliers: " + result);
        }

        // Count suppliers added (or get from result message)
        List<ApRequestForQtnSupDtl> suppliers = rfqSupDtlRepository.findByTransactionPoid(transactionPoid);
        int suppliersAdded = suppliers.size();

        AddSuppliersResponse response = new AddSuppliersResponse();
        response.setSuccess(true);
        response.setMessage(result != null ? result : "Suppliers added successfully");
        response.setSuppliersAdded(suppliersAdded);
        response.setAddedSupplierPoidList(
                suppliers.stream()
                        .map(ApRequestForQtnSupDtl::getSupplierPoid)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional
    public SendMailResponse sendMailToSuppliers(Long transactionPoid, Long groupPoid, Long companyPoid, String userId) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        String normalizedUserId = normalizeUserId(userId);

        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        // Check if RFQ has suppliers
        List<ApRequestForQtnSupDtl> suppliers = rfqSupDtlRepository.findByTransactionPoid(transactionPoid);
        if (suppliers.isEmpty()) {
            throw new CustomException("RFQ has no suppliers. Please add suppliers before sending mail.");
        }

        // Call stored procedure
        String result = callSendMailProcedure(groupPoid, companyPoid, normalizedUserId, transactionPoid);

        if (result != null && result.contains("ERROR")) {
            throw new CustomException("Error sending mail: " + result);
        }

        SendMailResponse response = new SendMailResponse();
        response.setSuccess(true);
        response.setMessage(result != null ? result : "RFQ sent to suppliers successfully");
        response.setEmailsSent(suppliers.size());
        return response;
    }

    @Override
    @Transactional
    public CreatePurchaseOrderResponse createPurchaseOrder(Long transactionPoid, Long supplierPoid,
                                                           Long groupPoid, Long companyPoid, String userId) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        if (supplierPoid == null) {
            throw new CustomException("Supplier POID is required to create Purchase Order");
        }
        String normalizedUserId = normalizeUserId(userId);

        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot create PO. RFQ is in closed status");
        }

        // Check if RFQ has items
        List<ApRequestForQtnItemDtl> items = rfqItemDtlRepository.findByTransactionPoid(transactionPoid);
        if (items.isEmpty()) {
            throw new CustomException("RFQ has no items. Please add items before creating Purchase Order.");
        }

        // Check if supplier has quoted prices for items
        boolean hasSupplierItems = items.stream()
                .anyMatch(item -> supplierPoid.equals(item.getSupplierPoid()) && item.getPrice() != null);
        if (!hasSupplierItems) {
            throw new CustomException("Selected supplier has not quoted prices for any items in this RFQ.");
        }

        // Call stored procedure
        String result = callCreatePurchaseOrderProcedure(groupPoid, companyPoid, normalizedUserId, transactionPoid,
                supplierPoid);

        if (result == null || result.contains("ERROR")) {
            throw new CustomException("Error creating Purchase Order: " + result);
        }

        // Parse result to extract PO DocRef (if available in result message)
        CreatePurchaseOrderResponse response = new CreatePurchaseOrderResponse();
        response.setSuccess(true);
        response.setMessage(result);
        // TODO: Parse PO DocRef and Poid from result message or return from stored
        // procedure
        return response;
    }

    @Override
    @Transactional
    public UpdateCostResponse updateCost(Long transactionPoid, Boolean confirm,
                                         Long groupPoid, Long companyPoid, String userId) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        if (confirm == null || !confirm) {
            throw new CustomException("Confirmation required for cost update operation");
        }

        String normalizedUserId = normalizeUserId(userId);

        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        // Call stored procedure
        String result = callUpdateCostProcedure(groupPoid, companyPoid, normalizedUserId, transactionPoid);

        if (result != null && result.contains("ERROR")) {
            throw new CustomException("Error updating cost: " + result);
        }

        UpdateCostResponse response = new UpdateCostResponse();
        response.setSuccess(true);
        response.setMessage(result != null ? result : "Cost updated successfully");
        // TODO: Get count of documents updated from result or query
        response.setDocumentsUpdated(0);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LastPriceResponse getLastPrice(Long stockPoid, Long stockUnitPoid, Long supplierPoid,
                                          Long groupPoid, Long companyPoid, String userId) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        if (stockPoid == null || stockUnitPoid == null || supplierPoid == null) {
            throw new CustomException("Stock, unit, and supplier POIDs are required to fetch last price");
        }
        String normalizedUserId = normalizeUserId(userId);

        BigDecimal lastPrice = getLastPriceFromProcedure(stockPoid, stockUnitPoid, supplierPoid,
                groupPoid, companyPoid, normalizedUserId);
        LastPriceResponse response = new LastPriceResponse();
        response.setLastPrice(lastPrice);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public DefaultUnitResponse getDefaultStockUnit(Long stockPoid) {
        if (stockPoid == null) {
            throw new CustomException("Stock POID is required to fetch default unit");
        }
        Long stockUnitPoid = getDefaultUnitFromProcedure(stockPoid);
        DefaultUnitResponse response = new DefaultUnitResponse();
        response.setStockUnitPoid(stockUnitPoid);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemsWithoutSuppliersResponse getItemsWithoutSuppliers(Long transactionPoid,
                                                                  Long groupPoid, Long companyPoid, String userId) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        String normalizedUserId = normalizeUserId(userId);
        // Validate RFQ exists
        rfqHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        // Call stored procedure
        callItemsWithoutSupplierProcedure(groupPoid, companyPoid, normalizedUserId, transactionPoid);

        // Query items without suppliers
        List<ApRequestForQtnItemDtl> items = rfqItemDtlRepository.findByTransactionPoid(transactionPoid);
        List<ItemWithoutSupplierDto> itemsWithoutSuppliers = items.stream()
                .filter(item -> item.getSupplierPoid() == null)
                .map(item -> {
                    ItemWithoutSupplierDto dto = new ItemWithoutSupplierDto();
                    dto.setTransactionPoid(item.getTransactionPoid());
                    dto.setDetRowId(item.getDetRowId());
                    dto.setStockPoid(item.getStockPoid());
                    dto.setQty(item.getQty());
                    // TODO: Get stock name from StockMaster
                    return dto;
                })
                .collect(Collectors.toList());

        ItemsWithoutSuppliersResponse response = new ItemsWithoutSuppliersResponse();
        response.setItemsWithoutSuppliers(itemsWithoutSuppliers);
        response.setCount(itemsWithoutSuppliers.size());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public RfqDependenciesDto checkRfqDependencies(Long transactionPoid, Long groupPoid, Long companyPoid) {
        if (groupPoid == null) {
            throw new CustomException("Group POID header is required");
        }
        if (companyPoid == null) {
            throw new CustomException("Company POID header is required");
        }
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        // TODO: Check for Purchase Orders created from this RFQ
        // Query: SELECT COUNT(*) FROM AP_PURCHASE_ORDER_HDR WHERE RFQ_POID = ? OR
        // similar
        Long purchaseOrderCount = 0L; // TODO: Implement dependency check

        RfqDependenciesDto dto = new RfqDependenciesDto();
        dto.setTransactionPoid(transactionPoid);
        dto.setPurchaseOrderCount(purchaseOrderCount);
        dto.setCanDelete(purchaseOrderCount == 0);

        if (dto.getCanDelete()) {
            dto.setReason("No dependencies");
            dto.setMessage("RFQ can be deleted. No dependencies found.");
        } else {
            dto.setReason("RFQ has dependencies");
            dto.setMessage(String.format("Cannot delete RFQ. It has been used to create %d Purchase Orders.",
                    purchaseOrderCount));
        }

        return dto;
    }

    // Helper methods for stored procedures
    private String callAddSuppliersProcedure(Long groupPoid, Long companyPoid, String userId, Long transactionPoid) {
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping call to PROC_AP_RFQ_ADD_SUPPLIERS");
            return null;
        }
        String sql = "{ call PROC_AP_RFQ_ADD_SUPPLIERS(?, ?, ?, ?, ?) }";

        try (Connection connection = dataSource.getConnection();
             CallableStatement callableStatement = connection.prepareCall(sql)) {

            // --- Set IN parameters ---
            callableStatement.setLong(1, groupPoid);
            callableStatement.setString(2, userId);
            callableStatement.setLong(3, companyPoid);
            callableStatement.setLong(4, transactionPoid);

            // --- Register OUT parameter ---
            callableStatement.registerOutParameter(5, Types.VARCHAR);

            // --- Execute the stored procedure ---
            callableStatement.execute();

            // --- Retrieve the OUT parameter ---
            String result = callableStatement.getString(5);

            // --- Log and handle any "ERROR" response from procedure ---
            if (result != null && result.toUpperCase().contains("ERROR")) {
                log.warn("PROC_AP_RFQ_ADD_SUPPLIERS returned error: {}", result);
            } else {
                log.info("PROC_AP_RFQ_ADD_SUPPLIERS executed successfully for RFQ POID: {} - Result: {}",
                        transactionPoid, result);
            }

            return result;

        } catch (SQLException ex) {
            log.error("Failed to execute stored procedure PROC_AP_RFQ_ADD_SUPPLIERS for RFQ POID: {}", transactionPoid,
                    ex);
            throw new CustomException("Database error while calling stored procedure: " + ex.getMessage());
        }
    }

    private String callSendMailProcedure(Long groupPoid, Long companyPoid, String userId, Long transactionPoid) {
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping call to PROC_AP_RFQ_CREATE_SEND_MAIL");
            return null;
        }
        String sql = "{ call PROC_AP_RFQ_CREATE_SEND_MAIL(?, ?, ?, ?, ?) }";

        try (Connection connection = dataSource.getConnection();
             CallableStatement callableStatement = connection.prepareCall(sql)) {

            // --- Set IN parameters ---
            callableStatement.setLong(1, groupPoid);
            callableStatement.setString(2, userId);
            callableStatement.setLong(3, companyPoid);
            callableStatement.setLong(4, transactionPoid);

            // --- Register OUT parameter ---
            callableStatement.registerOutParameter(5, Types.VARCHAR);

            // --- Execute the stored procedure ---
            callableStatement.execute();

            // --- Retrieve the OUT parameter ---
            String result = callableStatement.getString(5);

            // --- Log and handle any "ERROR" response from procedure ---
            if (result != null && result.toUpperCase().contains("ERROR")) {
                log.warn("PROC_AP_RFQ_CREATE_SEND_MAIL returned error: {}", result);
            } else {
                log.info("PROC_AP_RFQ_CREATE_SEND_MAIL executed successfully for RFQ POID: {} - Result: {}",
                        transactionPoid, result);
            }

            return result;

        } catch (SQLException ex) {
            log.error("Failed to execute stored procedure PROC_AP_RFQ_CREATE_SEND_MAIL for RFQ POID: {}",
                    transactionPoid, ex);
            throw new CustomException("Database error while calling stored procedure: " + ex.getMessage());
        }
    }

    private String callCreatePurchaseOrderProcedure(Long groupPoid, Long companyPoid, String userId,
                                                    Long transactionPoid, Long supplierPoid) {
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping call to PROC_AP_RFQ_CREATE_PO_NEW");
            return null;
        }
        String sql = "{ call PROC_AP_RFQ_CREATE_PO_NEW(?, ?, ?, ?, ?, ?) }";

        try (Connection connection = dataSource.getConnection();
             CallableStatement callableStatement = connection.prepareCall(sql)) {

            // --- Set IN parameters ---
            callableStatement.setLong(1, groupPoid);
            callableStatement.setString(2, userId);
            callableStatement.setLong(3, companyPoid);
            callableStatement.setLong(4, transactionPoid);
            callableStatement.setLong(5, supplierPoid);

            // --- Register OUT parameter ---
            callableStatement.registerOutParameter(6, Types.VARCHAR);

            // --- Execute the stored procedure ---
            callableStatement.execute();

            // --- Retrieve the OUT parameter ---
            String result = callableStatement.getString(6);

            // --- Log and handle any "ERROR" response from procedure ---
            if (result != null && result.toUpperCase().contains("ERROR")) {
                log.warn("PROC_AP_RFQ_CREATE_PO_NEW returned error: {}", result);
            } else {
                log.info("PROC_AP_RFQ_CREATE_PO_NEW executed successfully for RFQ POID: {} Supplier POID: {} - Result: {}",
                        transactionPoid, supplierPoid, result);
            }

            return result;

        } catch (SQLException ex) {
            log.error("Failed to execute stored procedure PROC_AP_RFQ_CREATE_PO_NEW for RFQ POID: {} Supplier POID: {}",
                    transactionPoid, supplierPoid, ex);
            throw new CustomException("Database error while calling stored procedure: " + ex.getMessage());
        }
    }

    private String callUpdateCostProcedure(Long groupPoid, Long companyPoid, String userId, Long transactionPoid) {
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping call to PROC_AP_RFQ_PRICE_UPDATE");
            return null;
        }
        String sql = "{ call PROC_AP_RFQ_PRICE_UPDATE(?, ?, ?, ?, ?) }";

        try (Connection connection = dataSource.getConnection();
             CallableStatement callableStatement = connection.prepareCall(sql)) {

            // --- Set IN parameters ---
            callableStatement.setLong(1, groupPoid);
            callableStatement.setString(2, userId);
            callableStatement.setLong(3, companyPoid);
            callableStatement.setLong(4, transactionPoid);

            // --- Register OUT parameter ---
            callableStatement.registerOutParameter(5, Types.VARCHAR);

            // --- Execute the stored procedure ---
            callableStatement.execute();

            // --- Retrieve the OUT parameter ---
            String result = callableStatement.getString(5);

            // --- Log and handle any "ERROR" response from procedure ---
            if (result != null && result.toUpperCase().contains("ERROR")) {
                log.warn("PROC_AP_RFQ_PRICE_UPDATE returned error: {}", result);
            } else {
                log.info("PROC_AP_RFQ_PRICE_UPDATE executed successfully for RFQ POID: {} - Result: {}",
                        transactionPoid, result);
            }

            return result;

        } catch (SQLException ex) {
            log.error("Failed to execute stored procedure PROC_AP_RFQ_PRICE_UPDATE for RFQ POID: {}", transactionPoid,
                    ex);
            throw new CustomException("Database error while calling stored procedure: " + ex.getMessage());
        }
    }

    private BigDecimal getLastPriceFromProcedure(Long stockPoid, Long stockUnitPoid, Long supplierPoid,
                                                 Long groupPoid, Long companyPoid, String userId) {
        if (stockPoid == null || stockUnitPoid == null || supplierPoid == null) {
            return null;
        }
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping call to PROC_AP_RFQ_CREATE_LAST_PRICE");
            return null;
        }

        final String sql = "{ call PROC_AP_RFQ_CREATE_LAST_PRICE(?, ?, ?, ?, ?, ?, ?) }";
        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setLong(1, groupPoid);
            statement.setLong(2, companyPoid);
            statement.setString(3, userId);
            statement.setLong(4, stockPoid);
            statement.setLong(5, stockUnitPoid);
            statement.setLong(6, supplierPoid);
            statement.registerOutParameter(7, Types.NUMERIC);

            statement.execute();
            BigDecimal lastPrice = statement.getBigDecimal(7);
            if (lastPrice != null) {
                log.debug("PROC_AP_RFQ_CREATE_LAST_PRICE returned {} for stock {} unit {} supplier {}",
                        lastPrice, stockPoid, stockUnitPoid, supplierPoid);
            }
            return lastPrice;
        } catch (SQLException ex) {
            log.error(
                    "Failed to execute stored procedure PROC_AP_RFQ_CREATE_LAST_PRICE for stock {} unit {} supplier {}",
                    stockPoid, stockUnitPoid, supplierPoid, ex);
            throw new CustomException("Database error while fetching last price: " + ex.getMessage());
        }
    }

    private Long getDefaultUnitFromProcedure(Long stockPoid) {
        if (stockPoid == null) {
            return null;
        }
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping call to PROC_AP_RFQ_SET_DFLT_DTL");
            return null;
        }

        final String sql = "{ call PROC_AP_RFQ_SET_DFLT_DTL(?, ?) }";
        try (Connection connection = dataSource.getConnection();
             CallableStatement statement = connection.prepareCall(sql)) {

            statement.setLong(1, stockPoid);
            statement.registerOutParameter(2, Types.NUMERIC);
            statement.execute();

            long value = statement.getLong(2);
            if (statement.wasNull()) {
                log.warn("PROC_AP_RFQ_SET_DFLT_DTL returned null default unit for stock {}", stockPoid);
                return null;
            }
            return value;
        } catch (SQLException ex) {
            log.error("Failed to execute stored procedure PROC_AP_RFQ_SET_DFLT_DTL for stock {}", stockPoid, ex);
            throw new CustomException("Database error while fetching default stock unit: " + ex.getMessage());
        }
    }

    // LOV Fetching Methods
    private LovItem getDivisionPoidDetail(Long divisionPoid) {
        if (divisionPoid == null) {
            return null;
        }
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping division LOV fetch");
            return null;
        }

        final String sql = "SELECT DIVISION_POID AS POID, DIVISION_CODE AS CODE, DIVISION_NAME AS DESCRIPTION " +
                "FROM GLOBAL_DIVISION_MASTER WHERE DIVISION_POID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, divisionPoid);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Long poid = rs.getLong("POID");
                    String code = rs.getString("CODE");
                    String description = rs.getString("DESCRIPTION");
                    return new LovItem(poid, code, description, description, poid, null);
                }
            }
            return null;
        } catch (SQLException ex) {
            log.error("Failed to fetch division LOV for divisionPoid {}", divisionPoid, ex);
            return null;
        }
    }

    private LovItem getSalesQtnPoidDetails(Long salesQtnPoid) {
        if (salesQtnPoid == null) {
            return null;
        }
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping sales quotation LOV fetch");
            return null;
        }

        final String sql = "SELECT SQH.TRANSACTION_POID AS POID, " +
                "SQH.DOC_REF AS CODE, " +
                "GAM.ADDRESS_NAME AS DESCRIPTION " +
                "FROM SALES_QUOTATION_HDR SQH " +
                "INNER JOIN GLOBAL_ADDRESS_DETAILS GAD ON GAD.ADDRESS_POID = SQH.CUSTOMER_POID " +
                "INNER JOIN GLOBAL_ADDRESS_MASTER GAM ON GAM.ADDRESS_MASTER_POID = GAD.ADDRESS_MASTER_POID " +
                "WHERE SQH.TRANSACTION_POID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, salesQtnPoid);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Long poid = rs.getLong("POID");
                    String code = rs.getString("CODE");
                    String description = rs.getString("DESCRIPTION");
                    return new LovItem(poid, code, description, description, poid, null);
                }
            }
            return null;
        } catch (SQLException ex) {
            log.error("Failed to fetch sales quotation LOV for salesQtnPoid {}", salesQtnPoid, ex);
            return null;
        }
    }

    private LovItem getStockPoidDetails(Long stockPoid) {
        if (stockPoid == null) {
            return null;
        }
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping stock LOV fetch");
            return null;
        }

        final String sql = "SELECT STOCK_POID AS POID, STOCK_CODE AS CODE, STOCK_NAME AS DESCRIPTION " +
                "FROM STOCK_MASTER WHERE STOCK_POID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, stockPoid);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Long poid = rs.getLong("POID");
                    String code = rs.getString("CODE");
                    String description = rs.getString("DESCRIPTION");
                    return new LovItem(poid, code, description, description, poid, null);
                }
            }
            return null;
        } catch (SQLException ex) {
            log.error("Failed to fetch stock LOV for stockPoid {}", stockPoid, ex);
            return null;
        }
    }

    private LovItem getStockUnitDetails(Long stockUnitPoid) {
        if (stockUnitPoid == null) {
            return null;
        }
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping stock unit LOV fetch");
            return null;
        }

        final String sql = "SELECT STOCK_UNIT_POID AS POID, STOCK_UNIT_CODE AS CODE, STOCK_UNIT_CODE AS DESCRIPTION " +
                "FROM STOCK_UNIT_MASTER WHERE STOCK_UNIT_POID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, stockUnitPoid);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Long poid = rs.getLong("POID");
                    String code = rs.getString("CODE");
                    String description = rs.getString("DESCRIPTION");
                    return new LovItem(poid, code, description, description, poid, null);
                }
            }
            return null;
        } catch (SQLException ex) {
            log.error("Failed to fetch stock unit LOV for stockUnitPoid {}", stockUnitPoid, ex);
            return null;
        }
    }

    private LovItem getSupplierPoidDetailsForItem(Long supplierPoid, Long transactionPoid, Long groupPoid) {
        if (supplierPoid == null || transactionPoid == null) {
            return null;
        }
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping supplier LOV fetch for item");
            return null;
        }

        try (Connection connection = dataSource.getConnection()) {
            // First, get Cash and Cheque supplier POIDs
            String paramSql = "SELECT RTN_GLOBAL_PARAMETER(1, 'Cash Suppliers', 'GROUP', '1', NULL) AS CASH_SUPPLIER, " +
                    "RTN_GLOBAL_PARAMETER(1, 'Cheque Suppliers', 'GROUP', '1', NULL) AS CHEQUE_SUPPLIER FROM DUAL";
            Long cashSupplier = null;
            Long chequeSupplier = null;

            try (PreparedStatement paramStatement = connection.prepareStatement(paramSql);
                 ResultSet paramRs = paramStatement.executeQuery()) {
                if (paramRs.next()) {
                    Object cashObj = paramRs.getObject("CASH_SUPPLIER");
                    Object chequeObj = paramRs.getObject("CHEQUE_SUPPLIER");
                    if (cashObj != null) {
                        cashSupplier = paramRs.getLong("CASH_SUPPLIER");
                    }
                    if (chequeObj != null) {
                        chequeSupplier = paramRs.getLong("CHEQUE_SUPPLIER");
                    }
                }
            }

            // Check if supplier is cash or cheque supplier - if so, return null
            if ((cashSupplier != null && supplierPoid.equals(cashSupplier)) ||
                    (chequeSupplier != null && supplierPoid.equals(chequeSupplier))) {
                return null;
            }

            final String sql = "SELECT DISTINCT ASM.SUPPLIER_POID AS POID, " +
                    "ASM.SUPPLIER_CODE AS CODE, " +
                    "ASM.SUPPLIER_NAME AS DESCRIPTION " +
                    "FROM AP_REQUEST_FOR_QTN_ITEM_DTL RFQI " +
                    "INNER JOIN AP_REQUEST_FOR_QTN_HDR RFQH ON RFQH.TRANSACTION_POID = RFQI.TRANSACTION_POID " +
                    "INNER JOIN AP_SUPPLIER_MASTER ASM ON ASM.SUPPLIER_POID = RFQI.SUPPLIER_POID " +
                    "WHERE RFQI.TRANSACTION_POID = ? AND RFQI.SUPPLIER_POID = ? " +
                    "AND RFQH.STATUS != 'CLOSED' " +
                    "AND RFQI.SUPPLIER_POID != NVL(?, -1) " +
                    "AND RFQI.SUPPLIER_POID != NVL(?, -1)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, transactionPoid);
                statement.setLong(2, supplierPoid);
                statement.setObject(3, cashSupplier);
                statement.setObject(4, chequeSupplier);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        Long poid = rs.getLong("POID");
                        String code = rs.getString("CODE");
                        String description = rs.getString("DESCRIPTION");
                        return new LovItem(poid, code, description, description, poid, null);
                    }
                }
            }
            return null;
        } catch (SQLException ex) {
            log.error("Failed to fetch supplier LOV for item supplierPoid {} transactionPoid {}", supplierPoid, transactionPoid, ex);
            return null;
        }
    }

    private LovItem getTaxPoidDetails(Long taxPoid) {
        if (taxPoid == null) {
            return null;
        }
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping tax LOV fetch");
            return null;
        }

        final String sql = "SELECT TAX_POID AS POID, TAX_CODE AS CODE, TAX_NAME AS DESCRIPTION " +
                "FROM GLOBAL_TAX_MASTER " +
                "WHERE TAX_POID = ? AND NVL(ACTIVE, 'Y') = 'Y' AND NVL(DELETED, 'N') = 'N' AND TAX_TYPE = 'INPUT_VAT'";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, taxPoid);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Long poid = rs.getLong("POID");
                    String code = rs.getString("CODE");
                    String description = rs.getString("DESCRIPTION");
                    return new LovItem(poid, code, description, description, poid, null);
                }
            }
            return null;
        } catch (SQLException ex) {
            log.error("Failed to fetch tax LOV for taxPoid {}", taxPoid, ex);
            return null;
        }
    }

    private LovItem getSupplierPoidDetailsForSupplier(Long supplierPoid) {
        if (supplierPoid == null) {
            return null;
        }
        if (dataSource == null) {
            log.warn("DataSource is not configured; skipping supplier LOV fetch");
            return null;
        }

        final String sql = "SELECT SUPPLIER_POID AS POID, SUPPLIER_CODE AS CODE, SUPPLIER_NAME AS DESCRIPTION " +
                "FROM AP_SUPPLIER_MASTER " +
                "WHERE SUPPLIER_POID = ? AND NVL(ACTIVE, 'Y') = 'Y'";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, supplierPoid);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Long poid = rs.getLong("POID");
                    String code = rs.getString("CODE");
                    String description = rs.getString("DESCRIPTION");
                    return new LovItem(poid, code, description, description, poid, null);
                }
            }
            return null;
        } catch (SQLException ex) {
            log.error("Failed to fetch supplier LOV for supplierPoid {}", supplierPoid, ex);
            return null;
        }
    }
}
