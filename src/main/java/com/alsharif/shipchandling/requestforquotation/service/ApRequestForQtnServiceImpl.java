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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

        // Save item details
        /*
         * if (request.getItemDetails() != null && !request.getItemDetails().isEmpty())
         * {
         * saveItemDetails(savedRfq.getTransactionPoid(), request.getItemDetails(),
         * normalizedUserId,
         * groupPoid, companyPoid);
         * }
         */

        // Save supplier details
        if (request.getSupplierDetails() != null && !request.getSupplierDetails().isEmpty()) {
            saveSupplierDetails(savedRfq.getTransactionPoid(), request.getSupplierDetails(), normalizedUserId);
        }

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

        /*
         * // Update detail tables
         * updateItemDetails(transactionPoid, request.getItemDetails(), groupPoid,
         * companyPoid, userId);
         * 
         * updateSupplierDetails(transactionPoid, request.getSupplierDetails(), userId);
         * 
         * // Save
         * ApRequestForQtnHdr savedRfq = rfqHdrRepository.save(rfq);
         */

        // Save header first
        ApRequestForQtnHdr savedRfq = rfqHdrRepository.save(rfq);

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
    public Page<ApRequestForQtnHdrDto> getAllRequestForQuotations(Long groupPoid, Long companyPoid,
            String status, Long divisionPoid,
            Long salesQtnPoid, String search,
            LocalDate fromDate, LocalDate toDate,
            int page, int size) {

        // Convert LocalDate to Timestamp for query
        Timestamp fromDateTimestamp = fromDate != null ? Timestamp.valueOf(fromDate.atStartOfDay()) : null;
        Timestamp toDateTimestamp = toDate != null ? Timestamp.valueOf(toDate.atTime(23, 59, 59)) : null;

        // Normalize status and search
        String normalizedStatus = hasText(status) ? status.trim().toUpperCase(Locale.ROOT) : null;
        String normalizedSearch = hasText(search) ? search.trim() : null;

        // Create Pageable with sorting (already sorted in query, but explicit for
        // clarity)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate"));

        // Execute query with filters at database level
        Page<ApRequestForQtnHdr> rfqPage = rfqHdrRepository.findAllWithFilters(
                groupPoid, companyPoid, normalizedStatus, divisionPoid,
                salesQtnPoid, fromDateTimestamp, toDateTimestamp, normalizedSearch, pageable);

        // Convert to DTO page
        return rfqPage.map(r -> convertToDto(r, false));
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

        if (includeDetails) {
            List<ApRequestForQtnItemDtl> itemDetails = rfqItemDtlRepository
                    .findByTransactionPoid(rfq.getTransactionPoid())
                    .stream()
                    .sorted(Comparator.comparing(ApRequestForQtnItemDtl::getDetRowId))
                    .collect(Collectors.toList());
            dto.setItemDetails(itemDetails.stream()
                    .map(this::convertItemDtlToDto)
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
        return convertItemDtlToDto(savedItemDtl);
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
        return convertItemDtlToDto(savedItemDtl);
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
                .map(this::convertItemDtlToDto)
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
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AP_RFQ_CREATE_PO_NEW(?,?,?,?,?,?); END;";
        // Parameters: P_GROUP_POID, P_USER_POID, P_COMPANY_POID, P_TRANSACTION_POID,
        // P_SUPPLIER_POID, P_RESULT (OUT VARCHAR)
        // Return result string (may contain PO DocRef)
        return null;
    }

    private String callUpdateCostProcedure(Long groupPoid, Long companyPoid, String userId, Long transactionPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AP_RFQ_PRICE_UPDATE(?,?,?,?,?); END;";
        // Parameters: P_GROUP_POID, P_USER_POID, P_COMPANY_POID, P_TRANSACTION_POID,
        // P_RESULT (OUT VARCHAR)
        // Return result string
        return null;
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
}
