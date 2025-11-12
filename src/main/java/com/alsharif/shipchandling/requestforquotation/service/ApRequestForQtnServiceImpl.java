package com.alsharif.shipchandling.requestforquotation.service;

import com.alsharif.shipchandling.exceptions.CustomException;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import com.alsharif.shipchandling.requestforquotation.dto.ItemWithoutSupplierDto;
import com.alsharif.shipchandling.requestforquotation.dto.RfqDependenciesDto;
import com.alsharif.shipchandling.requestforquotation.dto.request.*;
import com.alsharif.shipchandling.requestforquotation.dto.response.*;
import com.alsharif.shipchandling.requestforquotation.entity.*;
import com.alsharif.shipchandling.requestforquotation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
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

    @Autowired
    private DataSource dataSource;

    // Add DataSource for stored procedure calls
    // private final DataSource dataSource;

    @Override
    @Transactional
    public ApRequestForQtnHdrDto createRequestForQuotation(CreateApRequestForQtnRequest request,
                                                           Long groupPoid, Long companyPoid, String userId) {

        // ---------- 1. VALIDATION ----------
        if (request.getTransactionDate() == null) {
            throw new CustomException("Transaction date is required");
        }

//        if (request.getCurrencyCode() == null || !isValidCurrency(request.getCurrencyCode())) {
//            throw new CustomException("Invalid or missing currency code: " + request.getCurrencyCode());
//        }

        // Create header entity
        ApRequestForQtnHdr rfq = new ApRequestForQtnHdr();
        BeanUtils.copyProperties(request, rfq);
        rfq.setGroupPoid(groupPoid);
        rfq.setCompanyPoid(companyPoid);
        rfq.setCreatedBy(userId);
        rfq.setLastmodifiedBy(userId);
        rfq.setStatus("IN PROGRESS");
        rfq.setDeleted("N");

        // Save to get transactionPoid
       /* ApRequestForQtnHdr savedRfq = rfqHdrRepository.save(rfq);
        rfqHdrRepository.flush();*/

        // Save header first to get TRANSACTION_POID (auto-generated)
        ApRequestForQtnHdr savedRfq = rfqHdrRepository.saveAndFlush(rfq);
        log.info("RFQ Header saved with Transaction POID: {}", savedRfq .getTransactionPoid());

        // Save item details
        if (request.getItemDetails() != null && !request.getItemDetails().isEmpty()) {
            saveItemDetails(savedRfq.getTransactionPoid(), request.getItemDetails(), userId);
        }

        // Save supplier details
        if (request.getSupplierDetails() != null && !request.getSupplierDetails().isEmpty()) {
            saveSupplierDetails(savedRfq.getTransactionPoid(), request.getSupplierDetails(), userId);
        }

        // Call stored procedure AFTER SAVE -
        callItemsWithoutSupplierProcedure(groupPoid, companyPoid, userId, savedRfq.getTransactionPoid());

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
    /*private boolean isValidCurrency(String currencyCode) {
        return currencyRateUploadTempRepository.findById(currencyCode).isPresent();
    }*/

    @Override
    @Transactional(readOnly = true)
    public ApRequestForQtnHdrDto getRequestForQuotationByPoid(Long transactionPoid, Long groupPoid, 
                                                              Long companyPoid, Boolean includeDetails) {


        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        // Validate soft delete
        if ("Y".equalsIgnoreCase(rfq.getDeleted())) {
            throw new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid);
        }

        // Convert entity to DTO
        ApRequestForQtnHdrDto dto = new ApRequestForQtnHdrDto();
        BeanUtils.copyProperties(rfq, dto);

        // Include details if requested
        if (Boolean.TRUE.equals(includeDetails)) {
            List<ApRequestForQtnItemDtlDto> itemDtos = rfqItemDtlRepository.findByTransactionPoid(rfq.getTransactionPoid())
                    .stream()
                    .map(this::convertItemDtlToDto)
                    .collect(Collectors.toList());

            List<ApRequestForQtnSupDtlDto> supDtos = rfqSupDtlRepository.findByTransactionPoid(rfq.getTransactionPoid())
                    .stream()
                    .map(this::convertSupDtlToDto)
                    .collect(Collectors.toList());

            dto.setItemDetails(itemDtos);
            dto.setSupplierDetails(supDtos);
        }

        // Return DTO
        return dto;

        // return convertToDto(rfq, includeDetails != null && includeDetails);
    }

    @Override
    @Transactional
    public ApRequestForQtnHdrDto updateRequestForQuotation(Long transactionPoid, UpdateApRequestForQtnRequest request, 
                                                           Long groupPoid, Long companyPoid, String userId) {
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

        // Update fields (excluding read-only fields)
        BeanUtils.copyProperties(request, rfq, "transactionPoid", "docRef", "status", "salesQtnPoid", 
                                "salesInvDocRef", "createdBy", "createdDate");

        //  Audit fields update
        rfq.setLastmodifiedBy(userId);
        rfq.setLastmodifiedDate(new Timestamp(System.currentTimeMillis()));

        /*
        // Update detail tables
        updateItemDetails(transactionPoid, request.getItemDetails(), userId);
        updateSupplierDetails(transactionPoid, request.getSupplierDetails(), userId);

        // Save
        ApRequestForQtnHdr savedRfq = rfqHdrRepository.save(rfq);
        */

        // Save header first
        ApRequestForQtnHdr savedRfq = rfqHdrRepository.save(rfq);

        // Update detail tables (upsert pattern)
        updateItemDetails(transactionPoid, request.getItemDetails(), userId);
        updateSupplierDetails(transactionPoid, request.getSupplierDetails(), userId);

        // Call stored procedure AFTER SAVE
        callItemsWithoutSupplierProcedure(groupPoid, companyPoid, userId, transactionPoid);

        return convertToDto(savedRfq, true);
    }

    @Override
    @Transactional
    public void deleteRequestForQuotation(Long transactionPoid, Long groupPoid, Long companyPoid) {
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

        // TODO: Check dependencies (Purchase Orders, etc.)
        // Dependency check — ensure not used in Purchase Orders or other transactions
        /*
        int poCount = purchaseOrderRepository.countByRfqPoid(transactionPoid);
        if (poCount > 0) {
            throw new CustomException(
                    String.format("Cannot delete RFQ. It has been used to create %d Purchase Orders.", poCount));
        }
        */

        int quotationCount = rfqSupDtlRepository.countByTransactionPoid(transactionPoid);
        if (quotationCount > 0) {
            throw new CustomException(
                    String.format("Cannot delete RFQ. %d supplier quotations are linked to it.", quotationCount));
        }

        // Delete detail tables
        rfqItemDtlRepository.deleteByTransactionPoid(transactionPoid);
        rfqSupDtlRepository.deleteByTransactionPoid(transactionPoid);

        // Soft delete
        rfq.setDeleted("Y");
        rfq.setLastmodifiedDate(new Timestamp(System.currentTimeMillis()));
        rfqHdrRepository.save(rfq);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApRequestForQtnHdrDto> getAllRequestForQuotations(Long groupPoid, Long companyPoid, 
                                                                  String status, Long divisionPoid, 
                                                                  Long salesQtnPoid, String search) {
        List<ApRequestForQtnHdr> rfqs = rfqHdrRepository
                .findByGroupPoidAndCompanyPoidAndDeletedNotOrDeletedIsNull(groupPoid, companyPoid, "Y");

        return rfqs.stream()
                .filter(r -> status == null || status.equals(r.getStatus()))
                .filter(r -> divisionPoid == null || divisionPoid.equals(r.getDivisionPoid()))
                .filter(r -> salesQtnPoid == null || salesQtnPoid.equals(r.getSalesQtnPoid()))
                .filter(r -> {
                    if (search == null || search.trim().isEmpty()) {
                        return true;
                    }
                    String searchLower = search.toLowerCase();
                    return (r.getDocRef() != null && r.getDocRef().toLowerCase().contains(searchLower)) ||
                           (r.getDescription() != null && r.getDescription().toLowerCase().contains(searchLower));
                })
                .sorted((r1, r2) -> r2.getTransactionDate().compareTo(r1.getTransactionDate())) // DESC
                .map(r -> convertToDto(r, false))
                .collect(Collectors.toList());
    }

    private void callItemsWithoutSupplierProcedure(Long groupPoid, Long companyPoid, String userId, Long transactionPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AP_RFQ_ITEMS_WITHOUT_SUP(?,?,?,?,?); END;";
        // Parameters: P_GROUP_POID, P_COMPANY_POID, P_USER_POID, P_TRANSACTION_POID, P_RESULT (OUT VARCHAR)
        // Check result for "ERROR" and log warning if found

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
                log.info("PROC_AP_RFQ_ITEMS_WITHOUT_SUP executed successfully for RFQ POID: {} - Result: {}", transactionPoid, result);
            }

        } catch (SQLException ex) {
            log.error("Failed to execute stored procedure PROC_AP_RFQ_ITEMS_WITHOUT_SUP for RFQ POID: {}", transactionPoid, ex);
            throw new CustomException("Database error while calling stored procedure: " + ex.getMessage());
        }

    }

    private void saveItemDetails(Long transactionPoid, List<CreateApRequestForQtnItemDtlRequest> details, String userId) {
        Long maxDetRowId = rfqItemDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        for (CreateApRequestForQtnItemDtlRequest detail : details) {
            ApRequestForQtnItemDtl itemDtl = new ApRequestForQtnItemDtl();
            itemDtl.setTransactionPoid(transactionPoid);
            itemDtl.setDetRowId(detRowId++);
            itemDtl.setStockPoid(detail.getStockPoid());
            itemDtl.setStockUnitPoid(detail.getStockUnitPoid());
            itemDtl.setQty(detail.getQty());
            itemDtl.setSupplierPoid(detail.getSupplierPoid());
            itemDtl.setPrice(detail.getPrice());
            itemDtl.setTaxPoid(detail.getTaxPoid());
            itemDtl.setRemarks(detail.getRemarks());
            itemDtl.setCreatedBy(userId);
            itemDtl.setLastmodifiedBy(userId);

            // Calculate tax amount if tax percentage is available
            // - TAX HANDLING FROM GLOBAL_TAX_MASTER
            if (detail.getTaxPoid() != null) {
                // TODO: Get tax percentage from GLOBAL_TAX_MASTER and calculate
                // TaxAmount = Qty * Price * TaxPercentage / 100

                GlobalTaxMaster tax = globalTaxMasterRepository
                        .findActiveByTaxPoid(detail.getTaxPoid())
                        .orElseThrow(() -> new CustomException("Invalid or inactive Tax POID: " + detail.getTaxPoid()));

                // Use PERCENTAGE column for tax rate
                BigDecimal taxPercentage = tax.getPercentage() != null ? tax.getPercentage() : BigDecimal.ZERO;
                itemDtl.setTaxPercentage(taxPercentage);

                // Calculate Tax Amount = (Qty * Price * TaxPercentage) / 100
                if (detail.getPrice() != null && detail.getQty() != null) {
                    BigDecimal taxAmount = detail.getQty()
                            .multiply(detail.getPrice())
                            .multiply(taxPercentage)
                            .divide(BigDecimal.valueOf(100));
                    itemDtl.setTaxAmount(taxAmount);
                }
            }

            rfqItemDtlRepository.save(itemDtl);
        }
    }

    private void saveSupplierDetails(Long transactionPoid, List<CreateApRequestForQtnSupDtlRequest> details, String userId) {
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

    private void updateItemDetails(Long transactionPoid, List<CreateApRequestForQtnItemDtlRequest> details, String userId) {
        // Delete existing
        rfqItemDtlRepository.deleteByTransactionPoid(transactionPoid);
        // Save new
        if (details != null && !details.isEmpty()) {
            saveItemDetails(transactionPoid, details, userId);
        }
    }

    private void updateSupplierDetails(Long transactionPoid, List<CreateApRequestForQtnSupDtlRequest> details, String userId) {
        // Delete existing
        rfqSupDtlRepository.deleteByTransactionPoid(transactionPoid);
        // Save new
        if (details != null && !details.isEmpty()) {
            saveSupplierDetails(transactionPoid, details, userId);
        }
    }

    private ApRequestForQtnHdrDto convertToDto(ApRequestForQtnHdr rfq, boolean includeDetails) {
        ApRequestForQtnHdrDto dto = new ApRequestForQtnHdrDto();
        BeanUtils.copyProperties(rfq, dto);
        
        if (includeDetails) {
            List<ApRequestForQtnItemDtl> itemDetails = rfqItemDtlRepository.findByTransactionPoid(rfq.getTransactionPoid());
            dto.setItemDetails(itemDetails.stream()
                    .map(this::convertItemDtlToDto)
                    .collect(Collectors.toList()));
            
            List<ApRequestForQtnSupDtl> supplierDetails = rfqSupDtlRepository.findByTransactionPoid(rfq.getTransactionPoid());
            dto.setSupplierDetails(supplierDetails.stream()
                    .map(this::convertSupDtlToDto)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    private ApRequestForQtnItemDtlDto convertItemDtlToDto(ApRequestForQtnItemDtl itemDtl) {
        ApRequestForQtnItemDtlDto dto = new ApRequestForQtnItemDtlDto();
        BeanUtils.copyProperties(itemDtl, dto);
        return dto;
    }

    private ApRequestForQtnSupDtlDto convertSupDtlToDto(ApRequestForQtnSupDtl supDtl) {
        ApRequestForQtnSupDtlDto dto = new ApRequestForQtnSupDtlDto();
        BeanUtils.copyProperties(supDtl, dto);
        return dto;
    }

    // Detail Table Methods
    @Override
    @Transactional
    public ApRequestForQtnItemDtlDto addItemDetail(Long transactionPoid, CreateApRequestForQtnItemDtlRequest request, 
                                                    Long groupPoid, Long companyPoid, String userId) {
        // Validate RFQ exists and belongs to group/company
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot add items. RFQ is in closed status");
        }

        // Get next DetRowId
        Long maxDetRowId = rfqItemDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        // Create item detail
        ApRequestForQtnItemDtl itemDtl = new ApRequestForQtnItemDtl();
        itemDtl.setTransactionPoid(transactionPoid);
        itemDtl.setDetRowId(detRowId);
        itemDtl.setStockPoid(request.getStockPoid());
        itemDtl.setStockUnitPoid(request.getStockUnitPoid());
        itemDtl.setQty(request.getQty());
        itemDtl.setSupplierPoid(request.getSupplierPoid());
        itemDtl.setPrice(request.getPrice());
        itemDtl.setTaxPoid(request.getTaxPoid());
        itemDtl.setRemarks(request.getRemarks());
        itemDtl.setCreatedBy(userId);
        itemDtl.setLastmodifiedBy(userId);

        // Get last price if stock, unit, and supplier are all set
        if (request.getStockPoid() != null && request.getStockUnitPoid() != null && request.getSupplierPoid() != null) {
            BigDecimal lastPrice = getLastPriceFromProcedure(request.getStockPoid(), request.getStockUnitPoid(), 
                                                            request.getSupplierPoid(), groupPoid, companyPoid, userId);
            itemDtl.setLastRate(lastPrice);
        }

        // Get default unit if stock is set but unit is not
        if (request.getStockPoid() != null && request.getStockUnitPoid() == null) {
            Long defaultUnit = getDefaultUnitFromProcedure(request.getStockPoid());
            itemDtl.setStockUnitPoid(defaultUnit);
        }

        ApRequestForQtnItemDtl savedItemDtl = rfqItemDtlRepository.save(itemDtl);
        return convertItemDtlToDto(savedItemDtl);
    }

    @Override
    @Transactional
    public ApRequestForQtnItemDtlDto updateItemDetail(Long transactionPoid, Long detRowId, 
                                                       CreateApRequestForQtnItemDtlRequest request, 
                                                       Long groupPoid, Long companyPoid, String userId) {
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot update items. RFQ is in closed status");
        }

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
                (request.getSupplierPoid() != null && !request.getSupplierPoid().equals(itemDtl.getSupplierPoid()))) {
                throw new CustomException("Cannot modify stock, unit, or supplier. Item is linked to another document.");
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
        itemDtl.setStockUnitPoid(request.getStockUnitPoid());
        itemDtl.setQty(request.getQty());
        if (itemDtl.getRefDocId() == null || !itemDtl.getRefDocId().contains("400")) {
            itemDtl.setPrice(request.getPrice());
        }
        itemDtl.setSupplierPoid(request.getSupplierPoid());
        itemDtl.setTaxPoid(request.getTaxPoid());
        itemDtl.setRemarks(request.getRemarks());
        itemDtl.setLastmodifiedBy(userId);

        // Update last price if stock, unit, and supplier are all set
        if (request.getStockPoid() != null && request.getStockUnitPoid() != null && request.getSupplierPoid() != null) {
            BigDecimal lastPrice = getLastPriceFromProcedure(request.getStockPoid(), request.getStockUnitPoid(), 
                                                            request.getSupplierPoid(), groupPoid, companyPoid, userId);
            itemDtl.setLastRate(lastPrice);
        }

        ApRequestForQtnItemDtl savedItemDtl = rfqItemDtlRepository.save(itemDtl);
        return convertItemDtlToDto(savedItemDtl);
    }

    @Override
    @Transactional
    public void deleteItemDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid) {
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot delete items. RFQ is in closed status");
        }

        // Delete item detail
        rfqItemDtlRepository.deleteById(new ApRequestForQtnItemDtlId(transactionPoid, detRowId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApRequestForQtnItemDtlDto> getItemDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {
        // Validate RFQ exists
        rfqHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        List<ApRequestForQtnItemDtl> itemDetails = rfqItemDtlRepository.findByTransactionPoid(transactionPoid);
        return itemDetails.stream()
                .map(this::convertItemDtlToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApRequestForQtnSupDtlDto addSupplierDetail(Long transactionPoid, CreateApRequestForQtnSupDtlRequest request, 
                                                       Long groupPoid, Long companyPoid, String userId) {
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot add suppliers. RFQ is in closed status");
        }

        // Get next DetRowId
        Long maxDetRowId = rfqSupDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        // Create supplier detail
        ApRequestForQtnSupDtl supDtl = new ApRequestForQtnSupDtl();
        supDtl.setTransactionPoid(transactionPoid);
        supDtl.setDetRowId(detRowId);
        supDtl.setSupplierPoid(request.getSupplierPoid());
        supDtl.setRemarks(request.getRemarks());
        supDtl.setCreatedBy(userId);
        supDtl.setLastmodifiedBy(userId);

        ApRequestForQtnSupDtl savedSupDtl = rfqSupDtlRepository.save(supDtl);
        return convertSupDtlToDto(savedSupDtl);
    }

    @Override
    @Transactional
    public ApRequestForQtnSupDtlDto updateSupplierDetail(Long transactionPoid, Long detRowId, 
                                                          CreateApRequestForQtnSupDtlRequest request, 
                                                          Long groupPoid, Long companyPoid, String userId) {
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot update suppliers. RFQ is in closed status");
        }

        // Find existing supplier detail
        ApRequestForQtnSupDtl supDtl = rfqSupDtlRepository
                .findById(new ApRequestForQtnSupDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Supplier Detail", "detRowId", detRowId));

        // Update fields
        supDtl.setSupplierPoid(request.getSupplierPoid());
        supDtl.setRemarks(request.getRemarks());
        supDtl.setLastmodifiedBy(userId);

        ApRequestForQtnSupDtl savedSupDtl = rfqSupDtlRepository.save(supDtl);
        return convertSupDtlToDto(savedSupDtl);
    }

    @Override
    @Transactional
    public void deleteSupplierDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid) {
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot delete suppliers. RFQ is in closed status");
        }

        // Delete supplier detail
        rfqSupDtlRepository.deleteById(new ApRequestForQtnSupDtlId(transactionPoid, detRowId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApRequestForQtnSupDtlDto> getSupplierDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {
        // Validate RFQ exists
        rfqHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        List<ApRequestForQtnSupDtl> supplierDetails = rfqSupDtlRepository.findByTransactionPoid(transactionPoid);
        return supplierDetails.stream()
                .map(this::convertSupDtlToDto)
                .collect(Collectors.toList());
    }

    // Business Logic Methods
    @Override
    @Transactional
    public AddSuppliersResponse addRelatedSuppliers(Long transactionPoid, Long groupPoid, Long companyPoid, String userId) {
        // Validate RFQ exists and is in edit mode
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        if ("CLOSED".equalsIgnoreCase(rfq.getStatus())) {
            throw new CustomException("Cannot add suppliers. RFQ is in closed status");
        }

        // Call stored procedure
        String result = callAddSuppliersProcedure(groupPoid, companyPoid, userId, transactionPoid);

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
        return response;
    }

    @Override
    @Transactional
    public SendMailResponse sendMailToSuppliers(Long transactionPoid, Long groupPoid, Long companyPoid, String userId) {
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
        String result = callSendMailProcedure(groupPoid, companyPoid, userId, transactionPoid);

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
        String result = callCreatePurchaseOrderProcedure(groupPoid, companyPoid, userId, transactionPoid, supplierPoid);

        if (result == null || result.contains("ERROR")) {
            throw new CustomException("Error creating Purchase Order: " + result);
        }

        // Parse result to extract PO DocRef (if available in result message)
        CreatePurchaseOrderResponse response = new CreatePurchaseOrderResponse();
        response.setSuccess(true);
        response.setMessage(result);
        // TODO: Parse PO DocRef and Poid from result message or return from stored procedure
        return response;
    }

    @Override
    @Transactional
    public UpdateCostResponse updateCost(Long transactionPoid, Boolean confirm,
                                         Long groupPoid, Long companyPoid, String userId) {
        if (confirm == null || !confirm) {
            throw new CustomException("Confirmation required for cost update operation");
        }

        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        // Call stored procedure
        String result = callUpdateCostProcedure(groupPoid, companyPoid, userId, transactionPoid);

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
        BigDecimal lastPrice = getLastPriceFromProcedure(stockPoid, stockUnitPoid, supplierPoid, 
                                                         groupPoid, companyPoid, userId);
        LastPriceResponse response = new LastPriceResponse();
        response.setLastPrice(lastPrice);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public DefaultUnitResponse getDefaultStockUnit(Long stockPoid) {
        Long stockUnitPoid = getDefaultUnitFromProcedure(stockPoid);
        DefaultUnitResponse response = new DefaultUnitResponse();
        response.setStockUnitPoid(stockUnitPoid);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemsWithoutSuppliersResponse getItemsWithoutSuppliers(Long transactionPoid, 
                                                                  Long groupPoid, Long companyPoid, String userId) {
        // Validate RFQ exists
        rfqHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        // Call stored procedure
        callItemsWithoutSupplierProcedure(groupPoid, companyPoid, userId, transactionPoid);

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
        // Validate RFQ exists
        ApRequestForQtnHdr rfq = rfqHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("RFQ", "transactionPoid", transactionPoid));

        // TODO: Check for Purchase Orders created from this RFQ
        // Query: SELECT COUNT(*) FROM AP_PURCHASE_ORDER_HDR WHERE RFQ_POID = ? OR similar
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
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AP_RFQ_ADD_SUPPLIERS(?,?,?,?,?); END;";
        // Parameters: P_GROUP_POID, P_USER_POID, P_COMPANY_POID, P_TRANSACTION_POID, P_RESULT (OUT VARCHAR)
        // Return result string
        return null;
    }

    private String callSendMailProcedure(Long groupPoid, Long companyPoid, String userId, Long transactionPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AP_RFQ_CREATE_SEND_MAIL(?,?,?,?,?); END;";
        // Parameters: P_GROUP_POID, P_USER_POID, P_COMPANY_POID, P_TRANSACTION_POID, P_RESULT (OUT VARCHAR)
        // Return result string
        return null;
    }

    private String callCreatePurchaseOrderProcedure(Long groupPoid, Long companyPoid, String userId, 
                                                    Long transactionPoid, Long supplierPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AP_RFQ_CREATE_PO_NEW(?,?,?,?,?,?); END;";
        // Parameters: P_GROUP_POID, P_USER_POID, P_COMPANY_POID, P_TRANSACTION_POID, P_SUPPLIER_POID, P_RESULT (OUT VARCHAR)
        // Return result string (may contain PO DocRef)
        return null;
    }

    private String callUpdateCostProcedure(Long groupPoid, Long companyPoid, String userId, Long transactionPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AP_RFQ_PRICE_UPDATE(?,?,?,?,?); END;";
        // Parameters: P_GROUP_POID, P_USER_POID, P_COMPANY_POID, P_TRANSACTION_POID, P_RESULT (OUT VARCHAR)
        // Return result string
        return null;
    }

    private BigDecimal getLastPriceFromProcedure(Long stockPoid, Long stockUnitPoid, Long supplierPoid, 
                                                 Long groupPoid, Long companyPoid, String userId) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AP_RFQ_CREATE_LAST_PRICE(?,?,?,?,?,?,?); END;";
        // Parameters: P_GROUP_POID, P_COMPANY_POID, P_USER_POID, P_STOCK_POID, P_STOCK_UNIT_POID, P_SUPPLIER_POID, P_LAST_PRICE (OUT NUMERIC)
        // Return last price
        return null;
    }

    private Long getDefaultUnitFromProcedure(Long stockPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AP_RFQ_SET_DFLT_DTL(?,?); END;";
        // Parameters: P_STOCK_POID, P_STOCK_UNIT_POID (OUT NUMERIC)
        // Return stock unit POID
        return null;
    }
}