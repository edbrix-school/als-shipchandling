package com.asg.shipchandling.salesquotationsch.service;

import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.shipchandling.salesquotationsch.dto.*;
import com.asg.shipchandling.salesquotationsch.dto.request.*;
import com.asg.shipchandling.salesquotationsch.dto.response.CustomerDetailsResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.SalesQuotationSchListResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.StoredProcedureResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.ValidationResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.ExcelImportResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.AddressDetailsResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.CurrencyRateResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.TempAddressProcedureResponse;
import com.asg.shipchandling.salesquotationsch.dto.TempNewAddressRow;
import com.asg.shipchandling.StockMaster.service.StockMasterService;
import com.asg.shipchandling.StockMaster.dto.StockDetailsResponse;
import com.asg.shipchandling.common.repository.GlobalCurrencyMasterRepository;
import com.asg.shipchandling.common.repository.GlobalCurrencyRatesRepository;
import com.asg.shipchandling.common.entity.GlobalCurrencyMaster;
import com.asg.shipchandling.common.entity.GlobalCurrencyRates;
import com.asg.shipchandling.commonlov.dto.LovItem;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.salesquotationsch.entity.SalesQuotationSchHdr;
import com.asg.shipchandling.salesquotationsch.entity.SalesQuotationSchItemDtl;
import com.asg.shipchandling.salesquotationsch.repository.SalesQuotationSchHdrRepository;
import com.asg.shipchandling.salesquotationsch.repository.SalesQuotationSchItemDtlRepository;
import com.asg.shipchandling.salesquotationsch.repository.SalesQuotationSchStoredProcRepository;
import com.asg.shipchandling.salesquotationsch.spec.SalesQuotationSchSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sql.DataSource;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesQuotationSchServiceImpl implements SalesQuotationSchService {

    // Legacy (ADF) constants for Sales Quotation (SCH)
    private static final String LEGACY_DOC_ID_SALES_QUOTATION = "350-101";
    private static final String LEGACY_DOC_FIELD_NAME_CUSTOMER_POID = "CustomerPoid";

    // Item detail action types (PUT / update contract)
    private static final String ACTION_NO_CHANGE = "noChange";
    private static final String ACTION_NO_CHANGES = "noChanges";
    private static final String ACTION_IS_CREATED = "isCreated";
    private static final String ACTION_IS_UPDATED = "isUpdated";
    private static final String ACTION_IS_DELETED = "isDeleted";

    private final SalesQuotationSchHdrRepository quotationSchHdrRepository;
    private final SalesQuotationSchItemDtlRepository itemDtlRepository;
    private final SalesQuotationSchStoredProcRepository quotationSchStoredProcRepository;
    private final StockMasterService stockMasterService;
    private final GlobalCurrencyMasterRepository globalCurrencyMasterRepository;
    private final GlobalCurrencyRatesRepository globalCurrencyRatesRepository;
    private final DocumentSearchService documentService;

    @Autowired
    private DataSource dataSource;

    @Override
    @Transactional
    public SalesQuotationSchHdrDto createSalesQuotationSch(CreateSalesQuotationSchRequest request, Long groupPoid,
                                                           Long companyPoid, Long userPoid, String userId) {
        log.info("createSalesQuotationSch service started for groupPoid={} userId={}", groupPoid, userId);
        // Validate required fields
        validateQuotationSchRequest(request);

        // Set transactionDate to current date if not provided
        if (request.getTransactionDate() == null) {
            request.setTransactionDate(new Timestamp(System.currentTimeMillis()));
            log.info("createSalesQuotationSch set transactionDate to current timestamp");
        }

        // Keep a copy of the incoming value for any lookups before we overwrite customerPoid (legacy temp address flow)
        Long requestCustomerPoid = request.getCustomerPoid();

        // Create entity
        SalesQuotationSchHdr quotationSch = new SalesQuotationSchHdr();
        BeanUtils.copyProperties(request, quotationSch);
        quotationSch.setCompanyPoid(companyPoid);
        quotationSch.setCreatedBy(userId);
        quotationSch.setLastmodifiedBy(userId);
        quotationSch.setDeleted("N");
        // Keep existing behavior for addressPoid (not part of legacy temp address implementation)
        if (request.getAddressPoid() != null) {
            quotationSch.setAddressPoid(request.getAddressPoid());
        }

        // Save to get transactionPoid
        SalesQuotationSchHdr savedQuotationSch = quotationSchHdrRepository.save(quotationSch);
        quotationSchHdrRepository.flush();
        log.info("createSalesQuotationSch persisted groupPoid={} userId={} transactionPoid={} ", groupPoid, userId,
                savedQuotationSch.getTransactionPoid());

        // Legacy-compatible: create/update temp address in GLOBAL_NEW_ADDRESS_DETAILS, keyed by DocId+DocKeyPoid+DocFieldName
        if (request.isNewAddressYN() && request.getAddressDetails() != null) {
            String addressName = getCustomerName(requestCustomerPoid);
            if (addressName == null || addressName.isBlank()) {
                addressName = "Customer Address";
            }

            // Legacy UI requires Tel; for REST we map best-effort.
            String offTel1 = request.getAddressDetails().getContactPerson();
            if (offTel1 == null || offTel1.isBlank()) {
                offTel1 = request.getAddressDetails().getMobile();
            }

            Long generatedNewAddressPoid = System.currentTimeMillis();

            TempAddressProcedureResponse tempAddrResp = quotationSchStoredProcRepository.callNewTempAddressCreateUpdateProc(
                    groupPoid,
                    userPoid,
                    LEGACY_DOC_ID_SALES_QUOTATION,
                    savedQuotationSch.getTransactionPoid(),
                    LEGACY_DOC_FIELD_NAME_CUSTOMER_POID,
                    addressName,
                    generatedNewAddressPoid,
                    offTel1,
                    null,
                    request.getAddressDetails().getContactPerson(),
                    null,
                    request.getAddressDetails().getMobile(),
                    null,
                    request.getAddressDetails().getEmail1(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "CREATE"
            );

            if (!tempAddrResp.isSuccess() || tempAddrResp.getNewAddressPoid() == null) {
                throw new CustomException(tempAddrResp.getErrorMessage() != null ? tempAddrResp.getErrorMessage()
                        : "Temp address save failed");
            }

            // Per agreed REST contract: store returned temp id into customerPoid (ADF binding-style)
            savedQuotationSch.setCustomerPoid(tempAddrResp.getNewAddressPoid());
            savedQuotationSch.setLastmodifiedBy(userId);
            savedQuotationSch = quotationSchHdrRepository.save(savedQuotationSch);
            quotationSchHdrRepository.flush();
            log.info("createSalesQuotationSch updated customerPoid to temp newAddressPoid={} for transactionPoid={}",
                    tempAddrResp.getNewAddressPoid(), savedQuotationSch.getTransactionPoid());
        }

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
        // reflect request flag in response
        dto.setNewAddressYN(request.isNewAddressYN());
        log.info("createSalesQuotationSch completed for transactionPoid={} docRef={}",
                dto.getTransactionPoid(), dto.getDocRef());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesQuotationSchHdrDto getSalesQuotationSchByPoid(Long transactionPoid, Long groupPoid,
            Long companyPoid, Long userPoid, Boolean includeDetails) {
        log.info(
                "getSalesQuotationSchByPoid called for transactionPoid={} groupPoid={} companyPoid={} includeDetails={}",
                transactionPoid, groupPoid, companyPoid, includeDetails);

        // First check if quotation exists and is not deleted
        SalesQuotationSchHdr quotationSch = quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

        if ("Y".equals(quotationSch.getDeleted())) {
            log.warn("getSalesQuotationSchByPoid found companyPoid={} transactionPoid={} marked as deleted",
                    companyPoid,
                    transactionPoid);
            throw new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid);
        }

        // Fetch quotation with all LOV details in a single query
        List<Object[]> results = quotationSchHdrRepository.findSalesQuotationSchWithDetails(transactionPoid,
                companyPoid);
        SalesQuotationSchHdrDto dto;

        if (!results.isEmpty()) {
            Object[] row = results.get(0);
            dto = populateQuotationSchFromQueryResult(row, includeDetails != null && includeDetails);
        } else {
            // Fallback: use entity if query fails
            dto = convertToDto(quotationSch, includeDetails != null && includeDetails);
            setEmptyLovDetails(dto);
        }

        // Legacy "reopen" behavior: load temp addresses via PROC_NEW_ADDRESS_LOADLIST and patch addressDetails
        boolean tempNewAddressFound = false;
        try {
            List<TempNewAddressRow> newTempAddresses = quotationSchStoredProcRepository.callNewTempAddressLoadListProc(
                    groupPoid,
                    companyPoid,
                    userPoid,
                    LEGACY_DOC_ID_SALES_QUOTATION,
                    transactionPoid
            );

            if (newTempAddresses != null) {
                for (TempNewAddressRow row : newTempAddresses) {
                    if (row != null && row.getDocFieldName() != null &&
                            row.getDocFieldName().equalsIgnoreCase(LEGACY_DOC_FIELD_NAME_CUSTOMER_POID)) {
                        AddressDetailsResponse addr = new AddressDetailsResponse();
                        addr.setAddressPoid(row.getNewAddressPoid());
                        addr.setContactPerson(row.getContactPerson());
                        addr.setEmail1(row.getEmail1());
                        addr.setMobile(row.getMobile());
                        dto.setAddressDetails(addr);
                        tempNewAddressFound = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // Do not fail GET if temp address cannot be loaded; keep existing dto as-is.
            log.warn("Temp address loadlist failed for transactionPoid={}: {}", transactionPoid, e.getMessage());
        }

        // Expose legacy temp-address status in response (true if temp address exists for CustomerPoid)
        dto.setNewAddressYN(tempNewAddressFound);

        log.info("getSalesQuotationSchByPoid completed for transactionPoid={} companyPoid={}",
                transactionPoid, companyPoid);
        return dto;
    }

    @Override
    @Transactional
    public SalesQuotationSchHdrDto updateSalesQuotationSch(Long groupPoid, Long transactionPoid,
            UpdateSalesQuotationSchRequest request,
            Long companyPoid, Long userPoid, String userId) {
        log.info("updateSalesQuotationSch service started for transactionPoid={} groupPoid={}", transactionPoid,
                groupPoid);
        SalesQuotationSchHdr quotationSch = quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

        if ("Y".equals(quotationSch.getDeleted())) {
            log.warn("updateSalesQuotationSch found companyPoid={} transactionPoid={} marked as deleted", companyPoid,
                    transactionPoid);
            throw new CustomException("Cannot update deleted sales quotation sch");
        }

        // Validate required fields
        validateQuotationSchRequest(request);

        TempAddressProcedureResponse tempAddrResp = null;
        if (request.isNewAddressYN() && request.getAddressDetails() != null) {
            // For update, assume request.customerPoid holds the temp id (legacy binding-style). If missing, create a new one.
            Long existingOrNewTempId = request.getCustomerPoid() != null ? request.getCustomerPoid() : System.currentTimeMillis();
            String action = request.getCustomerPoid() != null ? "UPDATE" : "CREATE";

            String addressName = "Customer Address";
            String offTel1 = request.getAddressDetails().getContactPerson();
            if (offTel1 == null || offTel1.isBlank()) {
                offTel1 = request.getAddressDetails().getMobile();
            }

            tempAddrResp = quotationSchStoredProcRepository.callNewTempAddressCreateUpdateProc(
                    groupPoid,
                    userPoid,
                    LEGACY_DOC_ID_SALES_QUOTATION,
                    transactionPoid,
                    LEGACY_DOC_FIELD_NAME_CUSTOMER_POID,
                    addressName,
                    existingOrNewTempId,
                    offTel1,
                    null,
                    request.getAddressDetails().getContactPerson(),
                    null,
                    request.getAddressDetails().getMobile(),
                    null,
                    request.getAddressDetails().getEmail1(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    action
            );

            if (!tempAddrResp.isSuccess() || tempAddrResp.getNewAddressPoid() == null) {
                throw new CustomException(tempAddrResp.getErrorMessage() != null ? tempAddrResp.getErrorMessage()
                        : "Temp address save failed");
            }
        }

        // Update fields (excluding read-only fields)
        BeanUtils.copyProperties(request, quotationSch, "transactionPoid", "docRef", "createdBy",
                "createdDate", "transactionDate");
        quotationSch.setLastmodifiedBy(userId);

        // Per agreed REST contract: store returned temp id into customerPoid (ADF binding-style)
        if (tempAddrResp != null && tempAddrResp.getNewAddressPoid() != null) {
            quotationSch.setCustomerPoid(tempAddrResp.getNewAddressPoid());
        }

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
        // reflect request flag in response
        dto.setNewAddressYN(request.isNewAddressYN());
        log.info("updateSalesQuotationSch completed for transactionPoid={} companyPoid={}",
                quotationSch.getTransactionPoid(), quotationSch.getCompanyPoid());
        return dto;
    }

    @Override
    @Transactional
    public void deleteSalesQuotationSch(Long groupPoid, Long transactionPoid, Long companyPoid) {
        SalesQuotationSchHdr quotationSch = quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

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
        specification = andIfPresent(specification,
                SalesQuotationSchSpecifications.customerIs(filter.getCustomerPoid()));
        specification = andIfPresent(specification,
                SalesQuotationSchSpecifications.salesmanIs(filter.getSalesmanPoid()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.lineIs(filter.getLinePoid()));
        specification = andIfPresent(specification,
                SalesQuotationSchSpecifications.statusIs(filter.getQuotationStatus()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.docRefLike(filter.getDocRef()));
        specification = andIfPresent(specification, SalesQuotationSchSpecifications.searchText(filter.getSearch()));
        specification = andIfPresent(specification,
                SalesQuotationSchSpecifications.transactionDateFrom(filter.getFromDate()));
        specification = andIfPresent(specification,
                SalesQuotationSchSpecifications.transactionDateTo(filter.getToDate()));
        specification = andIfPresent(specification,
                SalesQuotationSchSpecifications.validityFromDate(filter.getValidityFromDate()));
        specification = andIfPresent(specification,
                SalesQuotationSchSpecifications.validityToDate(filter.getValidityToDate()));

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

        // Build display fields map
        Map<String, String> displayFields = new HashMap<>();
        displayFields.put("docRef", "text");
        displayFields.put("quotationStatus", "text");
        displayFields.put("transactionDate", "date");
        displayFields.put("customerRef", "text");
        displayFields.put("vesselName", "text");

        // Build response
        SalesQuotationSchListResponse response = new SalesQuotationSchListResponse();
        response.setContent(content);
        response.setLast(pageResult.isLast());
        response.setTotalPages(pageResult.getTotalPages());
        response.setTotalElements(pageResult.getTotalElements());
        response.setPageSize(size);
        response.setDisplayFields(displayFields);
        response.setPageNumber(page);

        log.info("search sales quotation sch completed for companyPoid={} totalElements={} totalPages={}",
                filter.getCompanyPoid(), response.getTotalElements(), response.getTotalPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listSalesQuotationSch(String docId, FilterRequestDto request, LocalDate startDateValue, LocalDate endDateValue, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveDateFilters(request, "TRANSACTION_DATE", startDateValue, endDateValue);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "DOC_REF",   // label
                "TRANSACTION_POID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
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
        // Note: companyPoid should be passed from controller, but for now we'll check
        // without it
        if (transactionPoid != null) {
            Long count = quotationSchHdrRepository.countByCompanyPoidAndDocRefExcluding(
                    null, docRef, transactionPoid);
            exists = count != null && count > 0;
        } else {
            // For validation without transactionPoid, we need companyPoid - this should be
            // passed from controller
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
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

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
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

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
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

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
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

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
    }

    private void validateQuotationSchRequest(UpdateSalesQuotationSchRequest request) {
        if (request.getCustomerPoid() == null) {
            log.warn("Validation failed : Customer is required");
            throw new CustomException("Customer is required");
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
            String actionTypeRaw = item.getActionType();
            String actionType = actionTypeRaw == null ? "" : actionTypeRaw.trim();

            // noChange/noChanges (or blank): skip processing on PUT
            if (actionType.isEmpty()
                    || ACTION_NO_CHANGE.equalsIgnoreCase(actionType)
                    || ACTION_NO_CHANGES.equalsIgnoreCase(actionType)) {
                continue;
            }

            // DELETE
            if (ACTION_IS_DELETED.equalsIgnoreCase(actionType) || "DELETE".equalsIgnoreCase(actionType)) {
                if (item.getDetRowId() == null) {
                    throw new CustomException("actionType '" + actionType + "' requires detRowId");
                }

                SalesQuotationSchItemDtlId id = new SalesQuotationSchItemDtlId(transactionPoid, item.getDetRowId());
                SalesQuotationSchItemDtl existingItem = itemDtlRepository.findById(id).orElse(null);
                if (existingItem == null) {
                    throw new CustomException("Item detail not found for delete. detRowId=" + item.getDetRowId());
                }
                itemDtlRepository.deleteById(id);
                log.debug("Deleted item detail transactionPoid={} detRowId={}", transactionPoid, item.getDetRowId());
                continue;
            }

            // UPDATE
            if (ACTION_IS_UPDATED.equalsIgnoreCase(actionType) || "UPDATE".equalsIgnoreCase(actionType)) {
                if (item.getDetRowId() == null) {
                    throw new CustomException("actionType '" + actionType + "' requires detRowId");
                }

                SalesQuotationSchItemDtl existingItem = itemDtlRepository
                        .findById(new SalesQuotationSchItemDtlId(transactionPoid, item.getDetRowId()))
                        .orElse(null);

                if (existingItem == null) {
                    // strict: do not create on update, to avoid accidental duplicates
                    throw new CustomException("Item detail not found for update. detRowId=" + item.getDetRowId());
                }

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
                continue;
            }

            // CREATE
            if (ACTION_IS_CREATED.equalsIgnoreCase(actionType) || "CREATE".equalsIgnoreCase(actionType)) {
                itemsToCreate.add(item);
                continue;
            }

            // Unknown actionType => reject (prevents silent creates)
            throw new CustomException("Invalid actionType '" + actionType
                    + "'. Allowed: " + ACTION_NO_CHANGE + ", " + ACTION_IS_CREATED + ", "
                    + ACTION_IS_UPDATED + ", " + ACTION_IS_DELETED);
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
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));
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

    /**
     * Gets customer name from SALES_CUSTOMER_MASTER table
     *
     * @param customerPoid Customer POID
     * @return Customer name or null if not found
     */
    private String getCustomerName(Long customerPoid) {
        if (customerPoid == null) {
            return null;
        }
        try {
            String customerName = quotationSchHdrRepository.findCustomerNameByPoid(customerPoid);
            return customerName;
        } catch (Exception e) {
            log.warn("Failed to get customer name for customerPoid={}: {}", customerPoid, e.getMessage());
        }
        return null;
    }

    private SalesQuotationSchHdrDto convertToDto(SalesQuotationSchHdr quotationSch, boolean includeDetails) {
        SalesQuotationSchHdrDto dto = new SalesQuotationSchHdrDto();
        BeanUtils.copyProperties(quotationSch, dto);

        if (includeDetails) {
            Map<Long, LovItem> stockCache = new HashMap<>();
            Map<Long, LovItem> stockUnitCache = new HashMap<>();
            Map<Long, LovItem> taxCache = new HashMap<>();
            Map<Long, StockDetailsResponse.CategoryDetailDto> categoryCache = new HashMap<>();

            List<SalesQuotationSchItemDtl> itemDetails = itemDtlRepository
                    .findByTransactionPoid(quotationSch.getTransactionPoid());
            dto.setItemDetails(itemDetails.stream()
                    .map(item -> convertItemDtlToDto(
                            item,
                            quotationSch.getCompanyPoid(),
                            stockCache,
                            stockUnitCache,
                            taxCache,
                            categoryCache))
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private SalesQuotationSchItemDtlDto convertItemDtlToDto(SalesQuotationSchItemDtl itemDtl) {
        SalesQuotationSchItemDtlDto dto = new SalesQuotationSchItemDtlDto();
        BeanUtils.copyProperties(itemDtl, dto);
        return dto;
    }

    private SalesQuotationSchItemDtlDto convertItemDtlToDto(
            SalesQuotationSchItemDtl itemDtl,
            Long companyPoid,
            Map<Long, LovItem> stockCache,
            Map<Long, LovItem> stockUnitCache,
            Map<Long, LovItem> taxCache,
            Map<Long, StockDetailsResponse.CategoryDetailDto> categoryCache) {

        SalesQuotationSchItemDtlDto dto = new SalesQuotationSchItemDtlDto();
        BeanUtils.copyProperties(itemDtl, dto);

        Long stockPoid = itemDtl.getStockPoid();
        if (stockPoid != null) {
            dto.setStockPoidDetails(stockCache.computeIfAbsent(stockPoid, this::getStockPoidDetails));
            dto.setCategoryDetails(categoryCache.computeIfAbsent(stockPoid,
                    sp -> getStockCategoryDetails(sp, companyPoid)));
        }

        Long stockUnitPoid = itemDtl.getStockUnitPoid();
        if (stockUnitPoid != null) {
            dto.setStockUnitDetails(stockUnitCache.computeIfAbsent(stockUnitPoid, this::getStockUnitDetails));
        }

        Long taxPoid = itemDtl.getTaxPoid();
        if (taxPoid != null) {
            dto.setTaxPoidDetails(taxCache.computeIfAbsent(taxPoid, this::getTaxPoidDetails));
        }

        return dto;
    }

    private StockDetailsResponse.CategoryDetailDto getStockCategoryDetails(Long stockPoid, Long companyPoid) {
        if (stockPoid == null) {
            return null;
        }
        try {
            StockDetailsResponse stockDetails = stockMasterService.getStockDetails(stockPoid, companyPoid);
            return stockDetails != null ? stockDetails.getCategoryDetails() : null;
        } catch (Exception e) {
            // Do not fail quotation GET if stock details cannot be loaded
            log.warn("Failed to load stock category details for stockPoid={}: {}", stockPoid, e.getMessage());
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
                "WHERE TAX_POID = ? " +
                "AND NVL(ACTIVE, 'Y') = 'Y' " +
                "AND NVL(DELETED, 'N') = 'N' " +
                "AND TAX_TYPE = 'OUTPUT_VAT'";
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
        index += 3;

        // Address Details (addr: index 75-78)
        // ADDRESS_POID, CONTACT_PERSON, EMAIL1, MOBILE
        AddressDetailsResponse addressDetails = new AddressDetailsResponse();
        if (row.length > index && row[index] != null) {
            addressDetails.setAddressPoid(getLongValue(row[index]));
        }
        if (row.length > index + 1 && row[index + 1] != null) {
            addressDetails.setContactPerson(getStringValue(row[index + 1]));
        }
        if (row.length > index + 2 && row[index + 2] != null) {
            addressDetails.setEmail1(getStringValue(row[index + 2]));
        }
        if (row.length > index + 3 && row[index + 3] != null) {
            addressDetails.setMobile(getStringValue(row[index + 3]));
        }
        // Only set addressDetails if at least addressPoid is present
        if (addressDetails.getAddressPoid() != null) {
            dto.setAddressDetails(addressDetails);
        }

        // Fetch item details if requested
        if (includeDetails) {
            Map<Long, LovItem> stockCache = new HashMap<>();
            Map<Long, LovItem> stockUnitCache = new HashMap<>();
            Map<Long, LovItem> taxCache = new HashMap<>();
            Map<Long, StockDetailsResponse.CategoryDetailDto> categoryCache = new HashMap<>();

            List<SalesQuotationSchItemDtl> itemDetails = itemDtlRepository
                    .findByTransactionPoid(dto.getTransactionPoid());
            dto.setItemDetails(itemDetails.stream()
                    .map(item -> convertItemDtlToDto(
                            item,
                            dto.getCompanyPoid(),
                            stockCache,
                            stockUnitCache,
                            taxCache,
                            categoryCache))
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
        if (obj == null)
            return null;
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return null;
    }

    private String getStringValue(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private Timestamp getTimestampValue(Object obj) {
        if (obj == null)
            return null;
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
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

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

    @Override
    @Transactional
    public ExcelImportResponse importItemsFromExcel(Long transactionPoid, Long companyPoid, String userId,
            MultipartFile file) {
        log.info("importItemsFromExcel started for transactionPoid={} companyPoid={} fileName={}",
                transactionPoid, companyPoid, file != null ? file.getOriginalFilename() : "null");

        ExcelImportResponse response = new ExcelImportResponse();
        List<String> errors = new ArrayList<>();
        int successfulRows = 0;
        int failedRows = 0;

        // Validate file
        if (file == null || file.isEmpty()) {
            response.setSuccess(false);
            response.setMessage("Excel file is required");
            response.setTotalRows(0);
            response.setSuccessfulRows(0);
            response.setFailedRows(0);
            response.setErrors(List.of("Excel file is required"));
            return response;
        }

        // Validate quotation exists
        SalesQuotationSchHdr quotationSch = quotationSchHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Sales Quotation SCH", "transactionPoid", transactionPoid));

        if ("Y".equals(quotationSch.getDeleted())) {
            throw new CustomException("Cannot import items. Sales quotation sch is deleted");
        }

        // Get next detRowId starting point
        Long maxDetRowId = itemDtlRepository.getMaxDetRowIdByTransactionPoid(transactionPoid);
        Long nextDetRowId = (maxDetRowId == null) ? 1L : maxDetRowId + 1L;

        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook;
            String fileName = file.getOriginalFilename();

            // Determine workbook type based on file extension
            if (fileName != null && fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else if (fileName != null && fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                response.setSuccess(false);
                response.setMessage("Invalid file format. Only .xls and .xlsx files are supported");
                response.setErrors(List.of("Invalid file format. Only .xls and .xlsx files are supported"));
                return response;
            }

            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            int totalRows = sheet.getLastRowNum(); // 0-based index

            // Skip header rows (row 0, 1) and process data rows
            for (int rowIndex = 2; rowIndex <= totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue; // Skip empty rows
                }

                try {
                    SalesQuotationSchItemDtl itemDtl = parseExcelRowToItemDtl(row, transactionPoid, nextDetRowId++,
                            userId, companyPoid);

                    // Validate required fields
                    if (itemDtl.getStockPoid() == null) {
                        errors.add("Row " + (rowIndex + 1) + ": Stock POID is required");
                        failedRows++;
                        continue;
                    }

                    // Save item detail
                    itemDtlRepository.save(itemDtl);
                    successfulRows++;

                } catch (Exception e) {
                    String errorMsg = "Row " + (rowIndex + 1) + ": " + e.getMessage();
                    errors.add(errorMsg);
                    log.warn("Error processing row {}: {}", rowIndex + 1, e.getMessage());
                    failedRows++;
                }
            }

            workbook.close();

            // Calculate totals after import
            if (successfulRows > 0) {
                calculateTotals(transactionPoid);
            }

            response.setSuccess(successfulRows > 0);
            response.setMessage(String.format("Import completed. %d rows succeeded, %d rows failed",
                    successfulRows, failedRows));
            response.setTotalRows(totalRows);
            response.setSuccessfulRows(successfulRows);
            response.setFailedRows(failedRows);

            // Limit errors list to prevent serialization issues with very large lists
            // Keep first 100 errors and add a summary if there are more
            if (errors.size() > 100) {
                List<String> limitedErrors = new ArrayList<>(errors.subList(0, 100));
                limitedErrors.add(String.format("... and %d more errors (showing first 100)", errors.size() - 100));
                response.setErrors(limitedErrors);
            } else {
                response.setErrors(errors);
            }

            log.info(
                    "importItemsFromExcel completed for transactionPoid={} successfulRows={} failedRows={} totalErrors={}",
                    transactionPoid, successfulRows, failedRows, errors.size());

        } catch (Exception e) {
            log.error("Error importing Excel file for transactionPoid={}", transactionPoid, e);
            response.setSuccess(false);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            response.setMessage("Error processing Excel file: " + errorMessage);
            response.setTotalRows(0);
            response.setSuccessfulRows(0);
            response.setFailedRows(0);
            response.setErrors(List.of("Error processing Excel file: " + errorMessage));
        }

        return response;
    }

    /**
     * Parse Excel row to SalesQuotationSchItemDtl entity
     * Expected columns (0-based index):
     * Column 5 (F): Stock Code (required)
     * Column 6 (G): Stock Remarks (optional)
     * Column 8 (I): Quantity (required)
     */
    private SalesQuotationSchItemDtl parseExcelRowToItemDtl(Row row, Long transactionPoid, Long detRowId, String userId,
            Long companyPoid) {
        SalesQuotationSchItemDtl itemDtl = new SalesQuotationSchItemDtl();
        itemDtl.setTransactionPoid(transactionPoid);
        itemDtl.setDetRowId(detRowId);
        itemDtl.setCreatedBy(userId);
        itemDtl.setLastmodifiedBy(userId);

        // Stock Code (required) - Column 4 (F)
        Cell stockCodeCell = row.getCell(4);
        if (stockCodeCell != null) {
            String stockCode = getStringValueFromCell(stockCodeCell);
            if (stockCode != null && !stockCode.trim().isEmpty()) {
                try {
                    StockDetailsResponse stockDetails = stockMasterService.getStockDetailsByCode(stockCode.trim(),
                            companyPoid);
                    // Check if stock was found (stockPoid is not null)
                    if (stockDetails != null && stockDetails.getStockPoid() != null) {
                        BeanUtils.copyProperties(stockDetails, itemDtl);
                    } else {
                        // Stock not found - return empty itemDtl
                        log.warn("Stock not found for stockCode={}, returning empty itemDtl", stockCode);
                        return itemDtl; // Return empty itemDtl with only basic fields set
                    }
                } catch (ResourceNotFoundException e) {
                    // Stock not found - return empty itemDtl instead of throwing exception
                    log.warn("Stock not found for stockCode={}, returning empty itemDtl: {}", stockCode,
                            e.getMessage());
                    return itemDtl; // Return empty itemDtl with only basic fields set
                } catch (Exception e) {
                    log.warn("Error fetching stock details for stockCode=" + stockCode + ": " + e.getMessage());
                    return itemDtl; // Return empty itemDtl with only basic fields set
                }
            }
        }

        // Stock Remarks - Column 5(G)
        Cell stockRemarksCell = row.getCell(5);
        if (stockRemarksCell != null) {
            String remarks = getStringValueFromCell(stockRemarksCell);
            if (remarks != null && !remarks.trim().isEmpty()) {
                itemDtl.setRemarks(remarks.trim());
            }
        }

        // Quantity - Column 8 (I)
        Cell quantityCell = row.getCell(8);
        if (quantityCell != null) {
            Long quantity = getLongValueFromCell(quantityCell);
            if (quantity != null && quantity > 0) {
                itemDtl.setQuantity(quantity);
            }
        }

        return itemDtl;
    }

    /**
     * Extract Long value from Excel cell
     */
    private Long getLongValueFromCell(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return null; // Date cells are not converted to Long
                }
                return (long) cell.getNumericCellValue();
            case STRING:
                String stringValue = cell.getStringCellValue().trim();
                if (stringValue.isEmpty()) {
                    return null;
                }
                try {
                    // Try to parse as double first, then convert to long
                    double doubleValue = Double.parseDouble(stringValue);
                    return (long) doubleValue;
                } catch (NumberFormatException e) {
                    return null;
                }
            case FORMULA:
                try {
                    return (long) cell.getNumericCellValue();
                } catch (Exception e) {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * Extract String value from Excel cell
     */
    private String getStringValueFromCell(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                // Convert numeric to string without decimal if it's a whole number
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf((long) cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return null;
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressDetailsResponse> getCustomerAddress(Long userPoid, Long customerPoid, String addressType) {
        log.info("getCustomerAddress called for userPoid={} customerPoid={} addressType={}", userPoid, customerPoid,
                addressType);
        CustomerDetailsResponse response = quotationSchStoredProcRepository.callGetCustomerAddressProc(userPoid,
                customerPoid, addressType);
        if (response.isSuccess()) {
            return response.getAddressDetails();
        } else {
            return new ArrayList<>();
        }

    }

    @Override
    @Transactional(readOnly = true)
    public CurrencyRateResponse getLatestCurrencyRate(Long currencyPoid) {
        log.info("getLatestCurrencyRate called for currencyPoid={}", currencyPoid);

        if (currencyPoid == null) {
            throw new CustomException("Currency POID is required");
        }

        // Find currency by POID
        GlobalCurrencyMaster currency = globalCurrencyMasterRepository
                .findByCurrencyPoid(java.math.BigDecimal.valueOf(currencyPoid))
                .orElseThrow(() -> new ResourceNotFoundException("Currency", "currencyPoid", currencyPoid));

        // Check if currency is active and not deleted
        if (!"Y".equalsIgnoreCase(currency.getActive()) || "Y".equalsIgnoreCase(currency.getDeleted())) {
            throw new CustomException("Currency is not active or has been deleted");
        }

        // Get latest rate by currency code
        GlobalCurrencyRates latestRate = globalCurrencyRatesRepository
                .findLatestByCurrencyCode(currency.getCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Currency Rate", "currencyCode", currency.getCurrencyCode()));

        // Map to response DTO
        CurrencyRateResponse response = new CurrencyRateResponse();
        response.setCurrencyPoid(currencyPoid);
        response.setCurrencyCode(currency.getCurrencyCode());
        response.setCurrencyName(currency.getCurrencyName());
        response.setBuyRate(latestRate.getBuyRate());
        response.setSellRate(latestRate.getSellRate());
        response.setRateDate(latestRate.getRateDate());

        log.info("getLatestCurrencyRate completed for currencyPoid={} currencyCode={} buyRate={} sellRate={}",
                currencyPoid, currency.getCurrencyCode(), latestRate.getBuyRate(), latestRate.getSellRate());

        return response;
    }
}
