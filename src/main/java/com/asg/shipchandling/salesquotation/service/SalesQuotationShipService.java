package com.asg.shipchandling.salesquotation.service;

import com.asg.shipchandling.salesquotation.dto.*;
import com.asg.shipchandling.salesquotation.entity.*;
import com.asg.shipchandling.salesquotation.dto.*;
import com.asg.shipchandling.salesquotation.entity.*;
import com.asg.shipchandling.salesquotation.repository.SalesQuotationShipHeaderRepository;
import com.asg.shipchandling.salesquotation.repository.SalesQuotationShipChargeDetailRepository;
import com.asg.shipchandling.salesquotation.repository.SalesQuotationShipEquipmentDetailRepository;
import com.asg.shipchandling.salesquotation.spec.SalesQuotationShipSpecifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import oracle.jdbc.OracleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class SalesQuotationShipService {

    private static final Logger log = LoggerFactory.getLogger(SalesQuotationShipService.class);

    private final SalesQuotationShipHeaderRepository repository;
    private final SalesQuotationShipChargeDetailRepository chargeDetailRepository;
    private final SalesQuotationShipEquipmentDetailRepository equipmentDetailRepository;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    // Default currency code - can be retrieved from parameter SALES_QUOTATION_SH_DEF_CURRENCY
    private static final String DEFAULT_CURRENCY_CODE = "USD";
    private static final BigDecimal DEFAULT_CURRENCY_RATE = BigDecimal.ONE;
    private static final BigDecimal DEFAULT_QTN_DAYS = BigDecimal.ONE;

    public SalesQuotationShipService(SalesQuotationShipHeaderRepository repository,
                                     SalesQuotationShipChargeDetailRepository chargeDetailRepository,
                                     SalesQuotationShipEquipmentDetailRepository equipmentDetailRepository,
                                     JdbcTemplate jdbcTemplate,
                                     EntityManager entityManager) {
        this.repository = repository;
        this.chargeDetailRepository = chargeDetailRepository;
        this.equipmentDetailRepository = equipmentDetailRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
    }

    public SalesQuotationShipListResponse search(SalesQuotationShipFilter filter, BigDecimal userId) {
        Objects.requireNonNull(filter, "filter is required");
        Objects.requireNonNull(filter.getCompanyPoid(), "companyPoid is required in filter");
        
        // Build specification
        Specification<SalesQuotationShipHeader> specification = Specification.where(SalesQuotationShipSpecifications.notDeleted());
        
        // Company filtering (mandatory for data isolation)
        specification = andIfPresent(specification, SalesQuotationShipSpecifications.companyIs(filter.getCompanyPoid()));
        
        // Line access control - get user's accessible lines
        if (userId != null) {
            List<BigDecimal> accessibleLinePoids = getAccessibleLinePoids(userId);
            if (accessibleLinePoids != null && !accessibleLinePoids.isEmpty()) {
                // User has limited line access - filter by accessible lines
                specification = andIfPresent(specification, SalesQuotationShipSpecifications.lineIn(accessibleLinePoids));
            }
            // If accessibleLinePoids is empty or null, it means "ALL_LINE_USER" - no line filtering needed
        }
        
        // Apply filters
        specification = andIfPresent(specification, SalesQuotationShipSpecifications.customerIs(filter.getCustomerPoid()));
        specification = andIfPresent(specification, SalesQuotationShipSpecifications.salesmanIs(filter.getSalesmanPoid()));
        
        // Line filtering (if specific line is requested, it should be in the accessible lines list)
        if (filter.getLinePoid() != null) {
            specification = andIfPresent(specification, SalesQuotationShipSpecifications.lineIs(filter.getLinePoid()));
        }
        
        specification = andIfPresent(specification, SalesQuotationShipSpecifications.statusIs(filter.getQuotationStatus()));
        specification = andIfPresent(specification, SalesQuotationShipSpecifications.quotationTypeIs(filter.getQuotationType()));
        specification = andIfPresent(specification, SalesQuotationShipSpecifications.docRefLike(filter.getDocRef()));
        specification = andIfPresent(specification, SalesQuotationShipSpecifications.searchText(filter.getSearch()));
        specification = andIfPresent(specification, SalesQuotationShipSpecifications.transactionDateFrom(filter.getFromDate()));
        specification = andIfPresent(specification, SalesQuotationShipSpecifications.transactionDateTo(filter.getToDate()));
        specification = andIfPresent(specification, SalesQuotationShipSpecifications.validityFromDate(filter.getValidityFromDate()));
        specification = andIfPresent(specification, SalesQuotationShipSpecifications.validityToDate(filter.getValidityToDate()));
        
        // Build sort
        Sort sort = buildSort(filter.getSortBy(), filter.getSortOrder());
        
        // Pagination
        int page = filter.getPage() != null && filter.getPage() >= 0 ? filter.getPage() : 0;
        int size = filter.getSize() != null && filter.getSize() > 0 ? filter.getSize() : 20;
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        org.springframework.data.domain.Page<SalesQuotationShipHeader> pageResult = repository.findAll(specification, pageable);
        
        // Convert to DTOs
        List<SalesQuotationShipSummaryDto> content = pageResult.getContent().stream()
                .map(this::toSummaryDto)
                .toList();
        
        // Build response
        return new SalesQuotationShipListResponse(
                content,
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                page,
                size
        );
    }
    
    /**
     * Gets accessible line POIDs for a user.
     * Calls PROC_GLOB_USER_LINE_LIST_SHQN stored procedure.
     * Returns list of line POIDs, or empty list if user has "ALL_LINE_USER" access.
     */
    private List<BigDecimal> getAccessibleLinePoids(BigDecimal userId) {
        try {
            String lineList = jdbcTemplate.execute((Connection connection) -> {
                try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GLOB_USER_LINE_LIST_SHQN(?,?); END;")) {
                    cs.setBigDecimal(1, userId);
                    cs.registerOutParameter(2, Types.VARCHAR);
                    cs.execute();
                    return cs.getString(2);
                }
            });
            
            if (lineList == null || lineList.isBlank()) {
                log.debug("User {} has no line access restrictions (empty line list)", userId);
                return null; // No restrictions - all lines accessible
            }
            
            // Check if user has "ALL_LINE_USER" access
            if ("ALL_LINE_USER".equalsIgnoreCase(lineList.trim())) {
                log.debug("User {} has ALL_LINE_USER access", userId);
                return null; // No restrictions - all lines accessible
            }
            
            // Parse comma-separated line POIDs
            List<BigDecimal> linePoids = new ArrayList<>();
            String[] parts = lineList.split(",");
            for (String part : parts) {
                try {
                    String trimmed = part.trim();
                    if (!trimmed.isBlank()) {
                        linePoids.add(new BigDecimal(trimmed));
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid line POID in line list for user {}: {}", userId, part);
                }
            }
            
            log.debug("User {} has access to {} lines: {}", userId, linePoids.size(), linePoids);
            return linePoids.isEmpty() ? null : linePoids;
        } catch (Exception e) {
            log.error("Error getting accessible lines for user {}: {}", userId, e.getMessage(), e);
            // On error, return null to allow all lines (conservative approach)
            // Alternatively, return empty list to block all lines (more restrictive)
            // For now, we'll return null to allow access
            return null;
        }
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
            "validityToDate", "customerName", "totalSellingAmountLocal", "totalBuyingAmountLocal"
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

    public SalesQuotationShipDetailDto getDetail(BigDecimal transactionPoid, BigDecimal companyPoid, boolean includeDetails) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");
        
        // Find quotation with company filtering for data isolation
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid + 
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));
        
        // Access collections to trigger @Fetch(FetchMode.SUBSELECT) loading
        // The entity has @Fetch(FetchMode.SUBSELECT) configured, which will automatically
        // load collections when accessed within the transaction
        // Always load charges (needed for totals calculation)
        header.getCharges().size();
        
        // Load equipment only if details are requested
        if (includeDetails) {
            header.getEquipment().size();
        }
        
        // Calculate totals from charge details
        recalculateTotalsIfNeeded(header);
        
        // Convert to DTO
        return toDetailDto(header, includeDetails);
    }
    
    /**
     * Recalculate totals from charge details if they are null or need recalculation.
     * This ensures totals are always accurate based on current charge details.
     */
    private void recalculateTotalsIfNeeded(SalesQuotationShipHeader header) {
        if (header.getCharges() == null || header.getCharges().isEmpty()) {
            // No charges, totals should be zero
            header.setTotalBuyingAmountLocal(BigDecimal.ZERO);
            header.setTotalSellingAmountLocal(BigDecimal.ZERO);
            header.setTotalTax(BigDecimal.ZERO);
            return;
        }
        
        // Calculate totals from charge details
        BigDecimal buyingTotal = header.getCharges().stream()
                .filter(c -> c.getBuyingChargeLocal() != null)
                .map(SalesQuotationShipChargeDetail::getBuyingChargeLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal sellingTotal = header.getCharges().stream()
                .filter(c -> c.getTotalSellingChargeLocal() != null)
                .map(SalesQuotationShipChargeDetail::getTotalSellingChargeLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal taxTotal = header.getCharges().stream()
                .filter(c -> c.getTaxAmountLocal() != null)
                .map(SalesQuotationShipChargeDetail::getTaxAmountLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Update totals in header (always recalculate to ensure accuracy)
        header.setTotalBuyingAmountLocal(buyingTotal);
        header.setTotalSellingAmountLocal(sellingTotal);
        header.setTotalTax(taxTotal);
    }

    @Transactional
    public SalesQuotationShipDetailDto createQuotation(SalesQuotationShipCommand command) {
        Objects.requireNonNull(command, "command is required");
        
        // Validate required request values
        if (command.getUserPoid() == null && (command.getUserId() == null || command.getUserId().isBlank())) {
            throw new IllegalArgumentException("userPoid or userId is required");
        }
        
        // Validate business rules before creating
        validateCreateBusinessRules(command);
        
        SalesQuotationShipHeader header = new SalesQuotationShipHeader();


        // Apply default values and populate header fields
        applyHeaderFields(header, command, true);
        
        // Auto-populate SalesmanPoid if not provided
        if (header.getSalesmanPoid() == null && command.getUserPoid() != null) {
            try {
                SalesmanDefaultResponse salesmanResponse = getDefaultSalesman(command.getUserPoid());
                if (salesmanResponse.salesmanPoid() != null && !salesmanResponse.salesmanPoid().isBlank()) {
                    header.setSalesmanPoid(new BigDecimal(salesmanResponse.salesmanPoid()));
                }
            } catch (Exception e) {
                log.warn("Unable to get default salesman for userPoid {}: {}", command.getUserPoid(), e.getMessage());
            }
        }
        
        // Validate SalesmanPoid is required
        if (header.getSalesmanPoid() == null) {
            throw new IllegalArgumentException("salesmanPoid is required");
        }
        
        // Handle customer validation
        if ("Y".equalsIgnoreCase(command.getNewCustomer())) {
            // New Customer: CustomerName is required
            if (command.getCustomerName() == null || command.getCustomerName().isBlank()) {
                throw new IllegalArgumentException("customerName is required when newCustomer = 'Y'");
            }
            // Clear CustomerPoid and AddressPoid for new customers
            header.setCustomerPoid(null);
            header.setAddressPoid(null);
        } else {
            // Existing Customer: CustomerPoid or AddressPoid is required
            if (command.getAddressPoid() == null && command.getCustomerPoid() == null) {
                throw new IllegalArgumentException("customerPoid or addressPoid is required when newCustomer = 'N'");
            }
            if (command.getAddressPoid() != null) {
                validateCustomerBeforeSave(command.getAddressPoid());
            }
        }
        
        // Set audit fields
        LocalDateTime now = LocalDateTime.now();
        // Use userId string if provided, otherwise use userPoid as string
        String auditUserId = command.getUserId();
        if (auditUserId == null || auditUserId.isBlank()) {
            auditUserId = command.getUserPoid() != null ? command.getUserPoid().toString() : null;
        }
        if (auditUserId == null || auditUserId.isBlank()) {
            throw new IllegalArgumentException("userId or userPoid is required for audit fields");
        }
        header.setCreatedBy(auditUserId);
        header.setCreatedDate(now);
        header.setLastModifiedBy(auditUserId);
        header.setLastModifiedDate(now);
        
        // Set Active default to "Y"
        header.setDeleted(command.getDeleted() != null ? command.getDeleted() : "N");
        
        // Save header first to get transactionPoid
        SalesQuotationShipHeader persistedHeader = repository.save(header);
        entityManager.flush();
        
        // Set transactionPoid in command for detail mapping
        command.setTransactionPoid(persistedHeader.getTransactionPoid());
        
        // Map charges and equipment
        mapCharges(persistedHeader, command.getCharges());
        mapEquipment(persistedHeader, command.getEquipment());
        
        // Save again with details
        SalesQuotationShipHeader saved = repository.save(persistedHeader);
        entityManager.flush();
        
        // Validate customer after save (if addressPoid was provided)
        if (saved.getAddressPoid() != null) {
            validateCustomerAfterSave(saved.getTransactionPoid(), saved.getAddressPoid());
        }
        
        // Calculate totals and handle Company/Division assignment
        calculateTotalsAndAssignCompanyDivision(saved, command);
        
        // Save again after calculating totals and assigning company/division
        SalesQuotationShipHeader savedWithTotals = repository.save(saved);
        entityManager.flush();
        
        // Trigger post-save calculation
        triggerPostSaveCalculation(savedWithTotals, command);
        
        // Reload to get auto-generated DocRef
        SalesQuotationShipHeader refreshed = reloadHeader(savedWithTotals);
        
        return toDetailDto(refreshed);
    }

    @Transactional
    public SalesQuotationShipDetailDto updateQuotation(BigDecimal transactionPoid, SalesQuotationShipCommand command) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(command, "command is required");
        
        // Validate required request values
        if (command.getUserPoid() == null && (command.getUserId() == null || command.getUserId().isBlank())) {
            throw new IllegalArgumentException("userPoid or userId is required");
        }
        
        // Find quotation - use companyPoid from command if provided, otherwise load from header
        SalesQuotationShipHeader header;
        BigDecimal companyPoid;
        
        if (command.getCompanyPoid() != null) {
            // CompanyPoid provided: find with company filtering
            companyPoid = command.getCompanyPoid();
            header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                    .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid + 
                            " with companyPoid " + companyPoid + 
                            ". The quotation may not exist, be soft-deleted, or belong to a different company."));
        } else {
            // CompanyPoid not provided: find header first, then use its companyPoid
            header = repository.findActiveWithDetails(transactionPoid)
                    .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid));
            
            // Use header's companyPoid
            companyPoid = header.getCompanyPoid();
            if (companyPoid == null) {
                throw new IllegalStateException("Quotation does not have a companyPoid assigned");
            }
            
            // Set companyPoid in command for consistency
            command.setCompanyPoid(companyPoid);
            
            // Access collections to trigger loading
            header.getCharges().size();
            header.getEquipment().size();
        }
        
        // Validate status-based editing restrictions
        validateStatusForUpdate(header.getQuotationStatus());
        
        // Store original DocRef to prevent update
        String originalDocRef = header.getDocRef();
        
        // Store original audit fields (should not be updated)
        String originalCreatedBy = header.getCreatedBy();
        LocalDateTime originalCreatedDate = header.getCreatedDate();
        
        // Validate business rules before updating
        validateUpdateBusinessRules(command, header);
        
        // Apply header fields (excluding read-only fields)
        applyHeaderFields(header, command, false);
        
        // Restore read-only fields (DocRef, CreatedBy, CreatedDate)
        header.setDocRef(originalDocRef); // DocRef is read-only
        header.setCreatedBy(originalCreatedBy); // CreatedBy should NOT be updated
        header.setCreatedDate(originalCreatedDate); // CreatedDate should NOT be updated
        
        // Handle customer validation
        if ("Y".equalsIgnoreCase(command.getNewCustomer())) {
            // New Customer: CustomerName is required
            if (command.getCustomerName() == null || command.getCustomerName().isBlank()) {
                throw new IllegalArgumentException("customerName is required when newCustomer = 'Y'");
            }
            // Clear CustomerPoid and AddressPoid for new customers
            header.setCustomerPoid(null);
            header.setAddressPoid(null);
        } else {
            // Existing Customer: CustomerPoid or AddressPoid is required
            if (command.getAddressPoid() == null && command.getCustomerPoid() == null && header.getAddressPoid() == null && header.getCustomerPoid() == null) {
                throw new IllegalArgumentException("customerPoid or addressPoid is required when newCustomer = 'N'");
            }
            BigDecimal addressPoid = command.getAddressPoid() != null ? command.getAddressPoid() : header.getAddressPoid();
            if (addressPoid != null) {
                validateCustomerBeforeSave(addressPoid);
            }
        }
        
        // Update audit fields (LastModifiedBy, LastModifiedDate)
        LocalDateTime now = LocalDateTime.now();
        String auditUserId = command.getUserId();
        if (auditUserId == null || auditUserId.isBlank()) {
            auditUserId = command.getUserPoid() != null ? command.getUserPoid().toString() : null;
        }
        if (auditUserId == null || auditUserId.isBlank()) {
            throw new IllegalArgumentException("userId or userPoid is required for audit fields");
        }
        header.setLastModifiedBy(auditUserId);
        header.setLastModifiedDate(now);
        
        // Map charges and equipment (upsert pattern)
        mapCharges(header, command.getCharges());
        mapEquipment(header, command.getEquipment());
        
        // Save header with details
        SalesQuotationShipHeader saved = repository.save(header);
        entityManager.flush();
        
        // Validate customer after save (if addressPoid was provided)
        BigDecimal addressPoid = saved.getAddressPoid();
        if (addressPoid != null) {
            validateCustomerAfterSave(saved.getTransactionPoid(), addressPoid);
        }
        
        // Calculate totals and handle Company/Division assignment
        calculateTotalsAndAssignCompanyDivision(saved, command);
        
        // Save again after calculating totals
        SalesQuotationShipHeader savedWithTotals = repository.save(saved);
        entityManager.flush();
        
        // Trigger post-save calculation
        triggerPostSaveCalculation(savedWithTotals, command);
        
        // Reload to get any database-generated values
        SalesQuotationShipHeader refreshed = reloadHeader(savedWithTotals);
        
        return toDetailDto(refreshed);
    }
    
    /**
     * Validates if quotation status allows editing.
     * Quotations in "PROCESSING" status can be edited.
     * Quotations in "CONFIRMED" or "LOST" status may have restricted editing.
     */
    private void validateStatusForUpdate(String quotationStatus) {
        if (quotationStatus == null || quotationStatus.isBlank()) {
            throw new IllegalStateException("Quotation status is required");
        }
        
        String status = quotationStatus.toUpperCase();
        
        // For now, allow editing of PROCESSING status
        // CONFIRMED and LOST statuses may have restrictions - implement based on business requirements
        if ("CONFIRMED".equals(status) || "LOST".equals(status)) {
            // Log warning but allow update for now
            // TODO: Implement business rules for restricted editing based on status
            log.warn("Updating quotation with status {} - may have restricted editing", status);
        }
        
        // Reject if status is invalid
        if (!isValidQuotationStatus(status)) {
            throw new IllegalStateException("Invalid quotation status: " + quotationStatus);
        }
    }
    
    /**
     * Validates business rules for update operation.
     */
    private void validateUpdateBusinessRules(SalesQuotationShipCommand command, SalesQuotationShipHeader existingHeader) {
        String quotationType = command.getQuotationType() != null ? command.getQuotationType() : existingHeader.getQuotationType();
        boolean isGeneral = quotationType != null && "GENERAL".equalsIgnoreCase(quotationType);
        
        // For shipping quotations (not GENERAL), validate shipping-specific fields
        if (!isGeneral) {
            // LinePoid is required for shipping quotations
            BigDecimal linePoid = command.getLinePoid() != null ? command.getLinePoid() : existingHeader.getLinePoid();
            if (linePoid == null) {
                throw new IllegalArgumentException("linePoid is required for shipping quotations");
            }
            
            // CommodityType is required for shipping quotations
            String commodityType = command.getCommodityType() != null ? command.getCommodityType() : existingHeader.getCommodityType();
            if (commodityType == null || commodityType.isBlank()) {
                throw new IllegalArgumentException("commodityType is required for shipping quotations");
            }
            
            // TermsOfFreight is required for shipping quotations
            String termsOfFreight = command.getTermsOfFreight() != null ? command.getTermsOfFreight() : existingHeader.getTermsOfFreight();
            if (termsOfFreight == null || termsOfFreight.isBlank()) {
                throw new IllegalArgumentException("termsOfFreight is required for shipping quotations");
            }
            
            // CargoType is required for shipping quotations
            String cargoType = command.getCargoType() != null ? command.getCargoType() : existingHeader.getCargoType();
            if (cargoType == null || cargoType.isBlank()) {
                throw new IllegalArgumentException("cargoType is required for shipping quotations");
            }
            
            // LoadingPortPoid and DischargePortPoid are required if MultiPort = 'N'
            String multiPort = command.getMultiPort() != null ? command.getMultiPort() : existingHeader.getMultiPort();
            if (multiPort == null || "N".equalsIgnoreCase(multiPort)) {
                BigDecimal loadingPort = command.getLoadingPortPoid() != null ? command.getLoadingPortPoid() : existingHeader.getLoadingPortPoid();
                BigDecimal dischargePort = command.getDischargePortPoid() != null ? command.getDischargePortPoid() : existingHeader.getDischargePortPoid();
                if (loadingPort == null) {
                    throw new IllegalArgumentException("loadingPortPoid is required when multiPort = 'N'");
                }
                if (dischargePort == null) {
                    throw new IllegalArgumentException("dischargePortPoid is required when multiPort = 'N'");
                }
            }
        }
        
        // SalesmanPoid is required
        BigDecimal salesmanPoid = command.getSalesmanPoid() != null ? command.getSalesmanPoid() : existingHeader.getSalesmanPoid();
        if (salesmanPoid == null) {
            throw new IllegalArgumentException("salesmanPoid is required");
        }
    }
    
    /**
     * Checks if quotation status is valid.
     */
    private boolean isValidQuotationStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        // Valid statuses: PROCESSING, CONFIRMED, LOST, etc.
        // Add more statuses as needed
        return status.matches("^(PROCESSING|CONFIRMED|LOST|CANCELLED|APPROVED|REJECTED)$");
    }

    @Transactional
    public SalesQuotationShipDeleteResponse deleteQuotation(BigDecimal transactionPoid, BigDecimal companyPoid, String userId) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");
        Objects.requireNonNull(userId, "userId is required");
        
        // Find quotation with company filtering for data isolation
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid + 
                        " with companyPoid " + companyPoid + 
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));
        
        // Check for dependencies first (before any status validation)
        DependencyCheckResult dependencyCheck = checkDependencies(transactionPoid, header.getQuotationStatus());
        
        // If dependencies exist, return response indicating deletion is not allowed
        if (!dependencyCheck.canDelete()) {
            return SalesQuotationShipDeleteResponse.blockedWithDependencies(
                    dependencyCheck.getReason(),
                    dependencyCheck.getMessage(),
                    dependencyCheck.getSalesInvoiceCount()
            );
        }
        
        // Validate status before allowing delete
        validateStatusForDelete(header.getQuotationStatus());
        
        // Perform soft delete (set Deleted = 'Y')
        header.setDeleted("Y");
        
        // Update audit fields
        LocalDateTime now = LocalDateTime.now();
        header.setLastModifiedBy(userId);
        header.setLastModifiedDate(now);
        
        // Save the soft-deleted quotation
        repository.save(header);
        entityManager.flush();
        
        log.info("Soft deleted sales quotation transactionPoid={} companyPoid={} userId={}", 
                transactionPoid, companyPoid, userId);
        
        // Return success response
        return SalesQuotationShipDeleteResponse.success();
    }
    
    /**
     * Checks for dependencies that prevent deletion of a quotation.
     * Returns a result indicating whether deletion is allowed and details about any dependencies.
     */
    private DependencyCheckResult checkDependencies(BigDecimal transactionPoid, String quotationStatus) {
        List<String> reasons = new ArrayList<>();
        Integer salesInvoiceCount = 0;
        Integer dependencyCount = 0;
        
        // Check for Sales Invoices referencing this quotation
        try {
            salesInvoiceCount = countSalesInvoicesByQuotation(transactionPoid);
            if (salesInvoiceCount > 0) {
                reasons.add("Sales Invoice");
                dependencyCount += salesInvoiceCount;
            }
        } catch (Exception e) {
            log.warn("Error checking Sales Invoice dependencies for quotation {}: {}", transactionPoid, e.getMessage());
            // If we can't check, we should be conservative and block deletion
            // Or we can allow deletion if the table doesn't exist yet
            // For now, we'll log the error but not block deletion
        }
        
        // Check status-based restrictions
        String status = quotationStatus != null ? quotationStatus.toUpperCase() : null;
        if ("CONFIRMED".equals(status) && salesInvoiceCount == 0) {
            // CONFIRMED status may have restrictions, but allow deletion if no dependencies
            // This can be made more restrictive based on business rules
            log.warn("Deleting quotation with CONFIRMED status - may have restrictions");
        }
        
        // If there are dependencies, return blocked response
        if (dependencyCount > 0) {
            String reason = "Quotation has dependencies";
            String message = String.format("Cannot delete quotation. It has been used to create %d Sales Invoice%s.", 
                    salesInvoiceCount, salesInvoiceCount > 1 ? "s" : "");
            return new DependencyCheckResult(false, reason, message, salesInvoiceCount, dependencyCount);
        }
        
        // No dependencies found, deletion is allowed
        return new DependencyCheckResult(true, "No dependencies found", "Quotation can be deleted", 0, 0);
    }
    
    /**
     * Counts the number of Sales Invoices that reference this quotation.
     * This method queries the Sales Invoice table to check if any invoices reference the quotation.
     * 
     * Note: The table name and column names may need to be adjusted based on actual database schema.
     * Common patterns:
     * - Table: SALES_INVOICE, SALES_INVOICE_HDR, INVOICE_HDR
     * - Column: QUOTATION_POID, QUOTATION_ID, REF_QUOTATION_POID, SOURCE_QUOTATION_POID
     */
    private Integer countSalesInvoicesByQuotation(BigDecimal transactionPoid) {
        try {
            // Try common table/column name patterns
            // Adjust these based on actual database schema
            String[] tableNames = {
                "SALES_INVOICE_HDR",
                "SALES_INVOICE",
                "INVOICE_HDR"
            };
            String[] columnNames = {
                "QUOTATION_POID",
                "QUOTATION_ID",
                "REF_QUOTATION_POID",
                "SOURCE_QUOTATION_POID",
                "SALES_QUOTATION_POID"
            };
            
            for (String tableName : tableNames) {
                for (String columnName : columnNames) {
                    try {
                        String sql = String.format(
                            "SELECT COUNT(*) FROM %s WHERE %s = ? AND (DELETED IS NULL OR UPPER(DELETED) <> 'Y')",
                            tableName, columnName
                        );
                        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, transactionPoid);
                        if (count != null && count > 0) {
                            log.debug("Found {} Sales Invoice(s) referencing quotation {} in table {}.{}", 
                                    count, transactionPoid, tableName, columnName);
                            return count;
                        }
                    } catch (Exception e) {
                        // Table or column doesn't exist, try next combination
                        log.trace("Table {}.{} does not exist or query failed: {}", tableName, columnName, e.getMessage());
                    }
                }
            }
            
            // If no table/column combination worked, return 0 (no dependencies found)
            log.debug("Could not find Sales Invoice table/column for quotation {}, assuming no dependencies", transactionPoid);
            return 0;
        } catch (Exception e) {
            log.error("Error counting Sales Invoices for quotation {}: {}", transactionPoid, e.getMessage(), e);
            // Return 0 to allow deletion if we can't check
            // Alternatively, return 1 to block deletion if we're unsure
            // For now, we'll return 0 to allow deletion if check fails
            return 0;
        }
    }
    
    /**
     * Validates if quotation status allows deletion.
     * Quotations that have been converted to Sales Invoice or confirmed may not be deletable.
     */
    private void validateStatusForDelete(String quotationStatus) {
        if (quotationStatus == null || quotationStatus.isBlank()) {
            throw new IllegalStateException("Quotation status is required");
        }
        
        String status = quotationStatus.toUpperCase();
        
        // Reject if status is invalid
        if (!isValidQuotationStatus(status)) {
            throw new IllegalStateException("Invalid quotation status: " + quotationStatus);
        }
    }
    
    /**
     * Internal class to hold dependency check results.
     */
    private static class DependencyCheckResult {
        private final boolean canDelete;
        private final String reason;
        private final String message;
        private final Integer salesInvoiceCount;
        private final Integer dependencyCount;
        
        public DependencyCheckResult(boolean canDelete, String reason, String message, 
                                    Integer salesInvoiceCount, Integer dependencyCount) {
            this.canDelete = canDelete;
            this.reason = reason;
            this.message = message;
            this.salesInvoiceCount = salesInvoiceCount;
            this.dependencyCount = dependencyCount;
        }
        
        public boolean canDelete() {
            return canDelete;
        }
        
        public String getReason() {
            return reason;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Integer getSalesInvoiceCount() {
            return salesInvoiceCount;
        }
        
        public Integer getDependencyCount() {
            return dependencyCount;
        }
    }

    @Transactional
    public String applyLocalCharges(BigDecimal transactionPoid, String loginUser) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(loginUser, "loginUser is required");

        SalesQuotationShipHeader header = getRequiredHeader(transactionPoid);
        BigDecimal groupId = firstNonNull(header.getQuotationCompany(), header.getCompanyPoid());
        BigDecimal companyId = header.getCompanyPoid();

        if (groupId == null) {
            throw new IllegalStateException("Unable to determine group id for quotation " + transactionPoid);
        }
        if (companyId == null) {
            throw new IllegalStateException("Unable to determine company id for quotation " + transactionPoid);
        }

        String result = jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SHQTN_LOCAL_CHARGES(?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, groupId);
                cs.setBigDecimal(2, companyId);
                cs.setBigDecimal(3, transactionPoid);
                cs.setString(4, loginUser);
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(5));
            }
        });

        SalesQuotationShipCalculateRequest calcRequest = new SalesQuotationShipCalculateRequest(
                groupId, companyId, loginUser, transactionPoid
        );
        String calcMessage = calculateAfterSave(calcRequest);
        if (!calcMessage.isBlank()) {
            log.debug("Post local charge calculation message for {}: {}", transactionPoid, calcMessage);
        }
        return result;
    }

    /**
     * Gets default salesman for a user (overload for BigDecimal).
     * Converts BigDecimal to String and calls the main method.
     */
    public SalesmanDefaultResponse getDefaultSalesman(BigDecimal userPoid) {
        Objects.requireNonNull(userPoid, "userPoid is required");
        return getDefaultSalesman(userPoid.toString());
    }

    /**
     * Gets default salesman for a user.
     * Calls stored procedure PROC_SALES_QTN_SALESMAN.
     */
    public SalesmanDefaultResponse getDefaultSalesman(String userId) {
        Objects.requireNonNull(userId, "userId is required");

        // Convert userId string to BigDecimal for stored procedure
        // Note: The stored procedure expects P_USER_POID as NUMBER
        BigDecimal userPoid;
        try {
            userPoid = new BigDecimal(userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid userId format. Expected numeric value, got: " + userId);
        }

        log.info("getDefaultSalesman started for userId={} userPoid={}", userId, userPoid);

        // Call stored procedure PROC_SALES_QTN_SALESMAN
        // Parameters: P_USER_POID, P_SALESMAN_POID (OUT VARCHAR)
        String salesmanPoidStr = jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_QTN_SALESMAN(?,?); END;")) {
                cs.setBigDecimal(1, userPoid);
                cs.registerOutParameter(2, Types.VARCHAR);
                cs.execute();
                return cs.getString(2);
            }
        });

        // Convert salesman POID string to BigDecimal (may be null if not found)
        BigDecimal salesmanPoid = null;
        if (salesmanPoidStr != null && !salesmanPoidStr.trim().isEmpty()) {
            try {
                salesmanPoid = new BigDecimal(salesmanPoidStr.trim());
            } catch (NumberFormatException e) {
                log.warn("Invalid salesman POID format returned from stored procedure: {}", salesmanPoidStr);
                // Return null if conversion fails
            }
        }

        log.info("getDefaultSalesman completed for userId={} salesmanPoid={}", userId, salesmanPoid);

        // Return salesmanPoid as String (as per existing record definition)
        // The response will contain the salesman POID as a string
        return new SalesmanDefaultResponse(salesmanPoid != null ? salesmanPoid.toString() : null);
    }

    /**
     * Gets accessible lines for a user (overload for BigDecimal).
     * Converts BigDecimal to String and calls the main method.
     */
    public LineAccessResponse getAccessibleLines(BigDecimal userPoid) {
        Objects.requireNonNull(userPoid, "userPoid is required");
        return getAccessibleLines(userPoid.toString());
    }

    /**
     * Gets accessible lines for a user.
     * Calls stored procedure PROC_GLOB_USER_LINE_LIST_SHQN.
     * Returns comma-separated list of line POIDs or "ALL_LINE_USER" if user has access to all lines.
     */
    public LineAccessResponse getAccessibleLines(String userId) {
        Objects.requireNonNull(userId, "userId is required");

        // Convert userId string to BigDecimal for stored procedure
        // Note: The stored procedure expects P_USER_POID as NUMBER
        BigDecimal userPoid;
        try {
            userPoid = new BigDecimal(userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid userId format. Expected numeric value, got: " + userId);
        }

        log.info("getAccessibleLines started for userId={} userPoid={}", userId, userPoid);

        // Call stored procedure PROC_GLOB_USER_LINE_LIST_SHQN
        // Parameters: P_USER_POID, P_LINE_LIST (OUT VARCHAR)
        // Returns: Comma-separated list of line POIDs, or "ALL_LINE_USER" if user has access to all lines
        String lineList = jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GLOB_USER_LINE_LIST_SHQN(?,?); END;")) {
                cs.setBigDecimal(1, userPoid);
                cs.registerOutParameter(2, Types.VARCHAR);
                cs.execute();
                return cs.getString(2);
            }
        });

        log.info("getAccessibleLines completed for userId={} lineList={}", userId, lineList);

        return new LineAccessResponse(lineList != null ? lineList : "");
    }

    /**
     * Gets quotation totals (buying total, selling total, tax total).
     * Calculates totals from charge details for the specified quotation.
     * Validates quotation ownership for data isolation.
     */
    @Transactional(readOnly = true)
    public QuotationTotalsResponse getQuotationTotals(BigDecimal transactionPoid, BigDecimal companyPoid) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");

        log.info("getQuotationTotals started for transactionPoid={} companyPoid={}", transactionPoid, companyPoid);

        // Find quotation with charges loaded (for data isolation and totals calculation)
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Ensure charges are loaded
        if (header.getCharges() == null) {
            header.setCharges(new ArrayList<>());
        }
        header.getCharges().size(); // Force load if lazy

        // Calculate totals from charge details
        // BuyingTotal: Sum of BuyingChargeLocal from all charge details
        BigDecimal buyingTotal = header.getCharges().stream()
                .filter(c -> c.getBuyingChargeLocal() != null)
                .map(SalesQuotationShipChargeDetail::getBuyingChargeLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // SellingTotal: Sum of TotalSellingChargeLocal (selling charge + tax) from all charge details
        BigDecimal sellingTotal = header.getCharges().stream()
                .filter(c -> c.getTotalSellingChargeLocal() != null)
                .map(SalesQuotationShipChargeDetail::getTotalSellingChargeLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // TotalTax: Sum of TaxAmountLocal from all charge details
        BigDecimal totalTax = header.getCharges().stream()
                .filter(c -> c.getTaxAmountLocal() != null)
                .map(SalesQuotationShipChargeDetail::getTaxAmountLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("getQuotationTotals completed for transactionPoid={} buyingTotal={} sellingTotal={} totalTax={}",
                transactionPoid, buyingTotal, sellingTotal, totalTax);

        return new QuotationTotalsResponse(buyingTotal, sellingTotal, totalTax);
    }

    /**
     * Checks quotation dependencies to determine if it can be deleted.
     * Validates quotation ownership and checks for dependencies like Sales Invoices.
     * Returns detailed information about dependencies and whether deletion is allowed.
     */
    @Transactional(readOnly = true)
    public QuotationDependenciesResponse checkQuotationDependencies(BigDecimal transactionPoid, BigDecimal companyPoid) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");

        log.info("checkQuotationDependencies started for transactionPoid={} companyPoid={}", transactionPoid, companyPoid);

        // Find quotation with company filtering for data isolation
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Check for dependencies using existing private method
        DependencyCheckResult dependencyCheck = checkDependencies(transactionPoid, header.getQuotationStatus());

        log.info("checkQuotationDependencies completed for transactionPoid={} canDelete={} salesInvoiceCount={}",
                transactionPoid, dependencyCheck.canDelete(), dependencyCheck.getSalesInvoiceCount());

        // Convert to response format
        return new QuotationDependenciesResponse(
                dependencyCheck.canDelete(),
                dependencyCheck.getReason(),
                dependencyCheck.getSalesInvoiceCount(),
                dependencyCheck.getMessage()
        );
    }

    public CustomerContactResponse getCustomerContactDetails(BigDecimal loginUserPoid, BigDecimal customerPoid) {
        Objects.requireNonNull(loginUserPoid, "loginUserPoid is required");
        Objects.requireNonNull(customerPoid, "customerPoid is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GET_QTN_CUST_ADDRESS(?,?,?); END;")) {
                cs.setBigDecimal(1, loginUserPoid);
                cs.setBigDecimal(2, customerPoid);
                cs.registerOutParameter(3, OracleTypes.CURSOR);
                cs.execute();
                try (ResultSet rs = (ResultSet) cs.getObject(3)) {
                    if (rs != null && rs.next()) {
                        return new CustomerContactResponse(rs.getString("CONTACT_PERSON"), rs.getString("EMAIL1"));
                    }
                }
                return new CustomerContactResponse(null, null);
            }
        });
    }

    public ChargeTaxResponse getChargeTaxDetails(BigDecimal companyPoid, BigDecimal customerPoid, BigDecimal chargePoid) {
        Objects.requireNonNull(companyPoid, "companyPoid is required");
        Objects.requireNonNull(customerPoid, "customerPoid is required");
        Objects.requireNonNull(chargePoid, "chargePoid is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GET_CHARGE_TAX_PER_V2(?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, companyPoid);
                cs.setString(2, "CUSTOMER");
                cs.setBigDecimal(3, customerPoid);
                cs.setBigDecimal(4, chargePoid);
                cs.registerOutParameter(5, OracleTypes.CURSOR);
                cs.execute();
                try (ResultSet rs = (ResultSet) cs.getObject(5)) {
                    if (rs != null && rs.next()) {
                        return new ChargeTaxResponse(
                                rs.getBigDecimal("PERCENTAGE"),
                                rs.getBigDecimal("TAX_POID")
                        );
                    }
                }
                return new ChargeTaxResponse(BigDecimal.ZERO, null);
            }
        });
    }

    /**
     * Adds a charge detail to a sales quotation.
     * Implements business logic including default values, tax auto-population, and charge calculations.
     */
    @Transactional
    public SalesQuotationShipItemDto addChargeDetail(BigDecimal transactionPoid, BigDecimal companyPoid, 
                                                     String userId, SalesQuotationShipChargeRequest request) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(request, "request is required");

        log.info("addChargeDetail started for transactionPoid={} companyPoid={} userId={}", 
                transactionPoid, companyPoid, userId);

        // Find quotation with company filtering for data isolation
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Access charges collection to trigger loading
        header.getCharges().size();

        // Generate DetRowId (max existing DetRowId + 1)
        BigDecimal nextDetRowId = chargeDetailRepository.findMaxDetailRowIdByTransactionPoid(transactionPoid)
                .map(maxId -> maxId.add(BigDecimal.ONE))
                .orElse(BigDecimal.ONE);

        log.debug("Generated DetRowId={} for transactionPoid={}", nextDetRowId, transactionPoid);

        // Create new charge detail entity
        SalesQuotationShipChargeDetail chargeDetail = new SalesQuotationShipChargeDetail();
        SalesQuotationShipChargeDetailId chargeDetailId = new SalesQuotationShipChargeDetailId(transactionPoid, nextDetRowId);
        chargeDetail.setId(chargeDetailId);
        chargeDetail.setHeader(header);

        // Apply default values
        String currencyCode = request.getCurrencyCode();
        if (currencyCode == null || currencyCode.isBlank()) {
            currencyCode = DEFAULT_CURRENCY_CODE; // Default from parameter SALES_QUOTATION_SH_DEF_CURRENCY
        }
        chargeDetail.setCurrencyCode(currencyCode);

        BigDecimal currencyRate = request.getCurrencyRate();
        if (currencyRate == null) {
            currencyRate = DEFAULT_CURRENCY_RATE; // Default to 1.0
        }
        chargeDetail.setCurrencyRate(currencyRate);

        BigDecimal qtnDays = request.getQuotationDays();
        if (qtnDays == null || qtnDays.compareTo(BigDecimal.ZERO) == 0) {
            qtnDays = DEFAULT_QTN_DAYS; // Default to 1
        }
        chargeDetail.setQuotationDays(qtnDays);

        // Set input fields
        chargeDetail.setChargePoid(request.getChargePoid());
        chargeDetail.setEquipmentPoid(request.getEquipmentPoid());
        chargeDetail.setLoadingPortPoid(request.getLoadingPortPoid());
        chargeDetail.setDischargePortPoid(request.getDischargePortPoid());
        chargeDetail.setQuantity(request.getQuantity());
        chargeDetail.setPerBuy(request.getPerBuy());
        chargeDetail.setPerQty(request.getPerQty());
        chargeDetail.setUnit(request.getUnit());
        chargeDetail.setRemarks(request.getRemarks());

        // Tax auto-population: Call PROC_GET_CHARGE_TAX_PER_V2 if chargePoid is provided
        if (request.getChargePoid() != null && header.getCustomerPoid() != null) {
            try {
                ChargeTaxResponse taxResponse = getChargeTaxDetails(companyPoid, header.getCustomerPoid(), request.getChargePoid());
                if (taxResponse != null && taxResponse.taxPercentage() != null && taxResponse.taxPercentage().compareTo(BigDecimal.ZERO) > 0) {
                    chargeDetail.setTaxPoid(taxResponse.taxPoid());
                    chargeDetail.setTaxPercentage(taxResponse.taxPercentage());
                    log.debug("Auto-populated tax for charge {}: taxPoid={} taxPercentage={}", 
                            request.getChargePoid(), taxResponse.taxPoid(), taxResponse.taxPercentage());
                }
            } catch (Exception e) {
                log.warn("Error retrieving tax details for charge {}: {}", request.getChargePoid(), e.getMessage());
                // Continue without tax if error occurs
            }
        }

        // Calculate read-only fields
        calculateChargeAmounts(chargeDetail, header.getQuotationType());

        // Set audit fields
        LocalDateTime now = LocalDateTime.now();
        chargeDetail.setCreatedBy(userId);
        chargeDetail.setCreatedDate(now);
        chargeDetail.setLastModifiedBy(userId);
        chargeDetail.setLastModifiedDate(now);

        // Add charge detail to header collection (for bidirectional relationship)
        header.addCharge(chargeDetail);

        // Save header (cascade will save the charge detail)
        header = repository.save(header);
        entityManager.flush();

        // Recalculate header totals from all charges (including the new one)
        recalculateTotalsIfNeeded(header);

        // Save header with updated totals
        header = repository.save(header);
        entityManager.flush();

        // Reload charge detail to get the latest state
        chargeDetail = chargeDetailRepository.findById(chargeDetailId)
                .orElseThrow(() -> new IllegalStateException("Charge detail not found after save"));

        log.info("addChargeDetail completed for transactionPoid={} detRowId={}", 
                transactionPoid, nextDetRowId);

        // Convert to DTO and return
        return toChargeDetailDto(chargeDetail);
    }

    /**
     * Calculates read-only charge amounts based on input fields.
     * Implements the business logic for charge calculations.
     */
    private void calculateChargeAmounts(SalesQuotationShipChargeDetail chargeDetail, String quotationType) {
        BigDecimal qty = chargeDetail.getQuantity();
        BigDecimal perBuy = chargeDetail.getPerBuy();
        BigDecimal perQty = chargeDetail.getPerQty();
        BigDecimal qtnDays = chargeDetail.getQuotationDays();
        BigDecimal currencyRate = chargeDetail.getCurrencyRate();
        BigDecimal taxPercentage = chargeDetail.getTaxPercentage();

        // Ensure qtnDays is at least 1 for calculation
        if (qtnDays == null || qtnDays.compareTo(BigDecimal.ZERO) == 0) {
            qtnDays = DEFAULT_QTN_DAYS;
            chargeDetail.setQuotationDays(qtnDays);
        }

        // Ensure currencyRate is set
        if (currencyRate == null) {
            currencyRate = DEFAULT_CURRENCY_RATE;
            chargeDetail.setCurrencyRate(currencyRate);
        }

        // Calculate BuyingCharge = Qty * PerBuy (only if not GENERAL quotation type)
        if (qty != null && perBuy != null && !"GENERAL".equalsIgnoreCase(quotationType)) {
            BigDecimal buyingCharge = qty.multiply(perBuy);
            chargeDetail.setBuyingCharge(buyingCharge);
            // Calculate BuyingChargeLocal = BuyingCharge * CurrencyRate
            BigDecimal buyingChargeLocal = buyingCharge.multiply(currencyRate);
            chargeDetail.setBuyingChargeLocal(buyingChargeLocal);
        } else {
            // For GENERAL quotation type, BuyingCharge is not shown/calculated
            chargeDetail.setBuyingCharge(null);
            chargeDetail.setBuyingChargeLocal(null);
        }

        // Calculate SellingCharge = Qty * PerQty * QtnDays
        if (qty != null && perQty != null) {
            BigDecimal sellingCharge = qty.multiply(perQty).multiply(qtnDays);
            chargeDetail.setSellingCharge(sellingCharge);
            // Calculate SellingChargeLocal = SellingCharge * CurrencyRate
            BigDecimal sellingChargeLocal = sellingCharge.multiply(currencyRate);
            chargeDetail.setSellingChargeLocal(sellingChargeLocal);

            // Calculate TaxAmount = SellingCharge * TaxPercentage / 100
            if (taxPercentage != null && taxPercentage.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal taxAmount = sellingCharge.multiply(taxPercentage).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                chargeDetail.setTaxAmount(taxAmount);
                // Calculate TaxAmountLocal = TaxAmount * CurrencyRate
                BigDecimal taxAmountLocal = taxAmount.multiply(currencyRate);
                chargeDetail.setTaxAmountLocal(taxAmountLocal);
                // Calculate TotalSellingChargeLocal = SellingChargeLocal + TaxAmountLocal
                BigDecimal totalSellingChargeLocal = sellingChargeLocal.add(taxAmountLocal);
                chargeDetail.setTotalSellingChargeLocal(totalSellingChargeLocal);
            } else {
                // No tax: TaxAmount = 0, TotalSellingChargeLocal = SellingChargeLocal
                chargeDetail.setTaxAmount(BigDecimal.ZERO);
                chargeDetail.setTaxAmountLocal(BigDecimal.ZERO);
                chargeDetail.setTotalSellingChargeLocal(sellingChargeLocal);
            }
        } else {
            // If qty or perQty is null, set calculated fields to null
            chargeDetail.setSellingCharge(null);
            chargeDetail.setSellingChargeLocal(null);
            chargeDetail.setTaxAmount(null);
            chargeDetail.setTaxAmountLocal(null);
            chargeDetail.setTotalSellingChargeLocal(null);
        }
    }

    /**
     * Updates an existing charge detail in a sales quotation.
     * Implements business logic including default values, tax auto-population, and charge calculations.
     * DetRowId cannot be changed (part of primary key).
     */
    @Transactional
    public SalesQuotationShipItemDto updateChargeDetail(BigDecimal transactionPoid, BigDecimal detRowId,
                                                         BigDecimal companyPoid, String userId,
                                                         SalesQuotationShipChargeRequest request) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(detRowId, "detRowId is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(request, "request is required");

        log.info("updateChargeDetail started for transactionPoid={} detRowId={} companyPoid={} userId={}",
                transactionPoid, detRowId, companyPoid, userId);

        // Find quotation with company filtering for data isolation
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Access charges collection to trigger loading
        header.getCharges().size();

        // Find existing charge detail by transactionPoid and detRowId
        SalesQuotationShipChargeDetailId chargeDetailId = new SalesQuotationShipChargeDetailId(transactionPoid, detRowId);
        SalesQuotationShipChargeDetail chargeDetail = chargeDetailRepository.findById(chargeDetailId)
                .orElseThrow(() -> new EntityNotFoundException("Charge detail not found for transactionPoid " + transactionPoid +
                        " and detRowId " + detRowId));

        // Validate that charge detail belongs to the quotation
        if (!chargeDetail.getHeader().getTransactionPoid().equals(transactionPoid)) {
            throw new IllegalStateException("Charge detail does not belong to the specified quotation");
        }

        // Store original audit fields (preserve createdBy and createdDate)
        String originalCreatedBy = chargeDetail.getCreatedBy();
        LocalDateTime originalCreatedDate = chargeDetail.getCreatedDate();

        // Check if chargePoid changed (for tax re-population)
        // Compare before updating to determine if tax should be re-fetched
        BigDecimal oldChargePoid = chargeDetail.getChargePoid();
        BigDecimal newChargePoid = request.getChargePoid();
        boolean chargePoidChanged = !Objects.equals(oldChargePoid, newChargePoid);

        // Apply default values (only if not provided in request)
        String currencyCode = request.getCurrencyCode();
        if (currencyCode == null || currencyCode.isBlank()) {
            currencyCode = chargeDetail.getCurrencyCode(); // Keep existing if not provided
            if (currencyCode == null || currencyCode.isBlank()) {
                currencyCode = DEFAULT_CURRENCY_CODE; // Default from parameter SALES_QUOTATION_SH_DEF_CURRENCY
            }
        }
        chargeDetail.setCurrencyCode(currencyCode);

        BigDecimal currencyRate = request.getCurrencyRate();
        if (currencyRate == null) {
            currencyRate = chargeDetail.getCurrencyRate(); // Keep existing if not provided
            if (currencyRate == null) {
                currencyRate = DEFAULT_CURRENCY_RATE; // Default to 1.0
            }
        }
        chargeDetail.setCurrencyRate(currencyRate);

        BigDecimal qtnDays = request.getQuotationDays();
        if (qtnDays == null || qtnDays.compareTo(BigDecimal.ZERO) == 0) {
            qtnDays = chargeDetail.getQuotationDays(); // Keep existing if not provided
            if (qtnDays == null || qtnDays.compareTo(BigDecimal.ZERO) == 0) {
                qtnDays = DEFAULT_QTN_DAYS; // Default to 1
            }
        }
        chargeDetail.setQuotationDays(qtnDays);

        // Update input fields (DetRowId cannot be changed - part of primary key)
        // Update all fields from request (full update - all fields from request are applied)
        chargeDetail.setChargePoid(newChargePoid);
        chargeDetail.setEquipmentPoid(request.getEquipmentPoid());
        chargeDetail.setLoadingPortPoid(request.getLoadingPortPoid());
        chargeDetail.setDischargePortPoid(request.getDischargePortPoid());
        chargeDetail.setQuantity(request.getQuantity());
        chargeDetail.setPerBuy(request.getPerBuy());
        chargeDetail.setPerQty(request.getPerQty());
        chargeDetail.setUnit(request.getUnit());
        chargeDetail.setRemarks(request.getRemarks());

        // Tax auto-population: Re-call PROC_GET_CHARGE_TAX_PER_V2 when chargePoid changes
        // Per documentation: "When ChargePoid changes: Call PROC_GET_CHARGE_TAX_PER_V2 to get tax percentage and tax POID"
        if (chargePoidChanged && newChargePoid != null && header.getCustomerPoid() != null) {
            try {
                ChargeTaxResponse taxResponse = getChargeTaxDetails(companyPoid, header.getCustomerPoid(), newChargePoid);
                if (taxResponse != null && taxResponse.taxPercentage() != null && taxResponse.taxPercentage().compareTo(BigDecimal.ZERO) > 0) {
                    chargeDetail.setTaxPoid(taxResponse.taxPoid());
                    chargeDetail.setTaxPercentage(taxResponse.taxPercentage());
                    log.debug("Auto-populated tax for charge {} (changed from {}): taxPoid={} taxPercentage={}",
                            newChargePoid, oldChargePoid, taxResponse.taxPoid(), taxResponse.taxPercentage());
                } else {
                    // If no tax found, clear tax fields
                    chargeDetail.setTaxPoid(null);
                    chargeDetail.setTaxPercentage(null);
                    log.debug("No tax found for charge {} (changed from {}), cleared tax fields", newChargePoid, oldChargePoid);
                }
            } catch (Exception e) {
                log.warn("Error retrieving tax details for charge {}: {}", newChargePoid, e.getMessage());
                // Continue without tax if error occurs - preserve existing tax if available
            }
        } else if (chargePoidChanged && newChargePoid == null) {
            // ChargePoid was cleared (set to null) - clear tax fields
            chargeDetail.setTaxPoid(null);
            chargeDetail.setTaxPercentage(null);
            log.debug("ChargePoid cleared (was {}), cleared tax fields", oldChargePoid);
        }
        // Note: Recalculation of charges and local amounts happens in calculateChargeAmounts
        // It recalculates based on current values of Qty, PerBuy, PerQty, QtnDays, CurrencyRate, and TaxPercentage
        // Per documentation:
        // - When Qty, PerBuy, PerQty, QtnDays, or CurrencyCode changes: Recalculate buying/selling charges and local amounts
        // - When TaxPercentage changes: Recalculate tax amount
        // - When CurrencyRate changes: Recalculate local currency amounts

        // Recalculate all read-only fields (all calculated fields should be recalculated)
        calculateChargeAmounts(chargeDetail, header.getQuotationType());

        // Update audit fields (preserve createdBy and createdDate)
        chargeDetail.setCreatedBy(originalCreatedBy);
        chargeDetail.setCreatedDate(originalCreatedDate);
        LocalDateTime now = LocalDateTime.now();
        chargeDetail.setLastModifiedBy(userId);
        chargeDetail.setLastModifiedDate(now);

        // Save charge detail (it's already in the header's collection)
        chargeDetail = chargeDetailRepository.save(chargeDetail);
        entityManager.flush();

        // Recalculate header totals from all charges (including the updated one)
        recalculateTotalsIfNeeded(header);

        // Save header with updated totals
        header = repository.save(header);
        entityManager.flush();

        // Reload charge detail to get the latest state
        chargeDetail = chargeDetailRepository.findById(chargeDetailId)
                .orElseThrow(() -> new IllegalStateException("Charge detail not found after update"));

        log.info("updateChargeDetail completed for transactionPoid={} detRowId={}",
                transactionPoid, detRowId);

        // Convert to DTO and return
        return toChargeDetailDto(chargeDetail);
    }

    /**
     * Deletes a charge detail from a sales quotation.
     * Validates company ownership, deletes the charge detail, and recalculates header totals.
     */
    @Transactional
    public void deleteChargeDetail(BigDecimal transactionPoid, BigDecimal detRowId, BigDecimal companyPoid) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(detRowId, "detRowId is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");

        log.info("deleteChargeDetail started for transactionPoid={} detRowId={} companyPoid={}",
                transactionPoid, detRowId, companyPoid);

        // Find quotation with company filtering for data isolation
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Access charges collection to trigger loading
        header.getCharges().size();

        // Find existing charge detail by transactionPoid and detRowId
        SalesQuotationShipChargeDetailId chargeDetailId = new SalesQuotationShipChargeDetailId(transactionPoid, detRowId);
        SalesQuotationShipChargeDetail chargeDetail = chargeDetailRepository.findById(chargeDetailId)
                .orElseThrow(() -> new EntityNotFoundException("Charge detail not found for transactionPoid " + transactionPoid +
                        " and detRowId " + detRowId));

        // Validate that charge detail belongs to the quotation
        if (!chargeDetail.getHeader().getTransactionPoid().equals(transactionPoid)) {
            throw new IllegalStateException("Charge detail does not belong to the specified quotation");
        }

        // Validate that charge detail belongs to the company (extra validation)
        if (!chargeDetail.getHeader().getCompanyPoid().equals(companyPoid)) {
            throw new IllegalStateException("Charge detail does not belong to the specified company");
        }

        // Remove charge detail from header collection
        // Since the relationship has orphanRemoval=true, removing from collection and saving header will delete the charge detail
        boolean removed = header.getCharges().removeIf(c -> c.getId().equals(chargeDetailId));
        if (!removed) {
            // If not found in collection, delete directly via repository
            // This can happen if the collection wasn't loaded or was already modified
            log.debug("Charge detail {} not found in header collection, deleting directly via repository", chargeDetailId);
            chargeDetailRepository.delete(chargeDetail);
        }
        // If removed from collection, saving header will trigger orphanRemoval delete

        // Recalculate header totals from remaining charges
        recalculateTotalsIfNeeded(header);

        // Save header (this will trigger orphanRemoval delete if charge was removed from collection)
        repository.save(header);
        entityManager.flush();

        log.info("deleteChargeDetail completed for transactionPoid={} detRowId={}",
                transactionPoid, detRowId);
    }

    /**
     * Retrieves all charge details for a sales quotation.
     * Validates company ownership and returns charge details ordered by detRowId.
     */
    @Transactional(readOnly = true)
    public List<SalesQuotationShipItemDto> getChargeDetails(BigDecimal transactionPoid, BigDecimal companyPoid) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");

        log.info("getChargeDetails started for transactionPoid={} companyPoid={}",
                transactionPoid, companyPoid);

        // Find quotation with company filtering for data isolation
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Access charges collection to trigger loading
        // The @Fetch(FetchMode.SUBSELECT) will automatically load all charges
        List<SalesQuotationShipChargeDetail> charges = header.getCharges();

        // Convert to DTOs and return
        List<SalesQuotationShipItemDto> dtos = charges.stream()
                .map(this::toChargeDetailDto)
                .toList();

        log.info("getChargeDetails completed for transactionPoid={} found {} charge details",
                transactionPoid, dtos.size());

        return dtos;
    }

    /**
     * Adds an equipment detail to a sales quotation.
     * Implements business logic including default values and DetRowId generation.
     * Equipment details are only shown for FCL cargo type.
     */
    @Transactional
    public SalesQuotationShipEquipmentDto addEquipmentDetail(BigDecimal transactionPoid, BigDecimal companyPoid,
                                                              String userId, SalesQuotationShipEquipmentRequest request) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(request, "request is required");

        log.info("addEquipmentDetail started for transactionPoid={} companyPoid={} userId={}",
                transactionPoid, companyPoid, userId);

        // Find quotation with company filtering for data isolation
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Optional: Validate that cargo type is FCL (equipment details are only for FCL)
        // This is a business rule - equipment details tab is only shown for FCL
        // We can add this validation if needed, but the documentation says it's a UI concern
        // For now, we'll allow adding equipment details regardless of cargo type

        // Generate DetRowId as next sequence number for this quotation
        BigDecimal nextDetRowId = equipmentDetailRepository.findMaxDetailRowIdByTransactionPoid(transactionPoid)
                .map(max -> max.add(BigDecimal.ONE))
                .orElse(BigDecimal.ONE);

        // Create new equipment detail
        SalesQuotationShipEquipmentDetail equipmentDetail = new SalesQuotationShipEquipmentDetail();
        SalesQuotationShipEquipmentDetailId equipmentDetailId = new SalesQuotationShipEquipmentDetailId(transactionPoid, nextDetRowId);
        equipmentDetail.setId(equipmentDetailId);
        equipmentDetail.setHeader(header);

        // Set default values
        BigDecimal quantity = request.getQuantity();
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            quantity = BigDecimal.ONE; // Default to 1
        }
        equipmentDetail.setQuantity(quantity);

        // Set input fields
        equipmentDetail.setEquipmentPoid(request.getEquipmentPoid());
        equipmentDetail.setOog(request.getOog());
        equipmentDetail.setOogDetails(request.getOogDetails());
        equipmentDetail.setDangerousGoods(request.getDangerousGoods());
        equipmentDetail.setDangerousGoodsClass(request.getDangerousGoodsClass());
        equipmentDetail.setUnoNumber(request.getUnoNumber());
        equipmentDetail.setTemperature(request.getTemperature());
        equipmentDetail.setRemarks(request.getRemarks());

        // Set audit fields
        LocalDateTime now = LocalDateTime.now();
        equipmentDetail.setCreatedBy(userId);
        equipmentDetail.setCreatedDate(now);
        equipmentDetail.setLastModifiedBy(userId);
        equipmentDetail.setLastModifiedDate(now);

        // Add equipment detail to header collection (for bidirectional relationship)
        header.addEquipment(equipmentDetail);

        // Save header (cascade will save the equipment detail)
        header = repository.save(header);
        entityManager.flush();

        // Reload equipment detail to get the latest state
        equipmentDetail = equipmentDetailRepository.findById(equipmentDetailId)
                .orElseThrow(() -> new IllegalStateException("Equipment detail not found after save"));

        log.info("addEquipmentDetail completed for transactionPoid={} detRowId={}",
                transactionPoid, nextDetRowId);

        // Convert to DTO and return
        return toEquipmentDto(equipmentDetail);
    }

    /**
     * Updates an existing equipment detail in a sales quotation.
     * Implements business logic including default values.
     * DetRowId cannot be changed (part of primary key).
     */
    @Transactional
    public SalesQuotationShipEquipmentDto updateEquipmentDetail(BigDecimal transactionPoid, BigDecimal detRowId,
                                                                 BigDecimal companyPoid, String userId,
                                                                 SalesQuotationShipEquipmentRequest request) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(detRowId, "detRowId is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(request, "request is required");

        log.info("updateEquipmentDetail started for transactionPoid={} detRowId={} companyPoid={} userId={}",
                transactionPoid, detRowId, companyPoid, userId);

        // Find quotation with company filtering for data isolation
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Access equipment collection to trigger loading
        header.getEquipment().size();

        // Find existing equipment detail by transactionPoid and detRowId
        SalesQuotationShipEquipmentDetailId equipmentDetailId = new SalesQuotationShipEquipmentDetailId(transactionPoid, detRowId);
        SalesQuotationShipEquipmentDetail equipmentDetail = equipmentDetailRepository.findById(equipmentDetailId)
                .orElseThrow(() -> new EntityNotFoundException("Equipment detail not found for transactionPoid " + transactionPoid +
                        " and detRowId " + detRowId));

        // Validate that equipment detail belongs to the quotation
        if (!equipmentDetail.getHeader().getTransactionPoid().equals(transactionPoid)) {
            throw new IllegalStateException("Equipment detail does not belong to the specified quotation");
        }

        // Store original audit fields (preserve createdBy and createdDate)
        String originalCreatedBy = equipmentDetail.getCreatedBy();
        LocalDateTime originalCreatedDate = equipmentDetail.getCreatedDate();

        // Apply default values (keep existing if not provided in request)
        BigDecimal quantity = request.getQuantity();
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
            quantity = equipmentDetail.getQuantity(); // Keep existing if not provided
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) == 0) {
                quantity = BigDecimal.ONE; // Default to 1
            }
        }
        equipmentDetail.setQuantity(quantity);

        // Update input fields (DetRowId cannot be changed - part of primary key)
        equipmentDetail.setEquipmentPoid(request.getEquipmentPoid());
        equipmentDetail.setOog(request.getOog());
        equipmentDetail.setOogDetails(request.getOogDetails());
        equipmentDetail.setDangerousGoods(request.getDangerousGoods());
        equipmentDetail.setDangerousGoodsClass(request.getDangerousGoodsClass());
        equipmentDetail.setUnoNumber(request.getUnoNumber());
        equipmentDetail.setTemperature(request.getTemperature());
        equipmentDetail.setRemarks(request.getRemarks());

        // Update audit fields (preserve createdBy and createdDate)
        equipmentDetail.setCreatedBy(originalCreatedBy);
        equipmentDetail.setCreatedDate(originalCreatedDate);
        LocalDateTime now = LocalDateTime.now();
        equipmentDetail.setLastModifiedBy(userId);
        equipmentDetail.setLastModifiedDate(now);

        // Save equipment detail (it's already in the header's collection)
        equipmentDetail = equipmentDetailRepository.save(equipmentDetail);
        entityManager.flush();

        // Reload equipment detail to get the latest state
        equipmentDetail = equipmentDetailRepository.findById(equipmentDetailId)
                .orElseThrow(() -> new IllegalStateException("Equipment detail not found after update"));

        log.info("updateEquipmentDetail completed for transactionPoid={} detRowId={}",
                transactionPoid, detRowId);

        // Convert to DTO and return
        return toEquipmentDto(equipmentDetail);
    }

    /**
     * Retrieves all equipment details for a sales quotation.
     * Validates company ownership and returns equipment details ordered by detRowId.
     */
    @Transactional(readOnly = true)
    public List<SalesQuotationShipEquipmentDto> getEquipmentDetails(BigDecimal transactionPoid, BigDecimal companyPoid) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");

        log.info("getEquipmentDetails started for transactionPoid={} companyPoid={}",
                transactionPoid, companyPoid);

        // Find quotation with company filtering for data isolation
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Access equipment collection to trigger loading
        // The @Fetch(FetchMode.SUBSELECT) will automatically load all equipment
        List<SalesQuotationShipEquipmentDetail> equipment = header.getEquipment();

        // Convert to DTOs and return
        List<SalesQuotationShipEquipmentDto> dtos = equipment.stream()
                .map(this::toEquipmentDto)
                .toList();

        log.info("getEquipmentDetails completed for transactionPoid={} found {} equipment details",
                transactionPoid, dtos.size());

        return dtos;
    }

    /**
     * Response DTO for add local charges operation.
     */
    public record AddLocalChargesResponse(boolean success, String message, Integer chargesAdded) {}

    /**
     * Adds default local charges to a sales quotation.
     * Calls stored procedure PROC_SALES_SHQTN_LOCAL_CHARGES.
     * Validates quotation status is "PROCESSING" and requires user confirmation.
     */
    @Transactional
    public AddLocalChargesResponse addLocalCharges(BigDecimal transactionPoid, BigDecimal companyPoid,
                                                    String userId, boolean confirm) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");
        Objects.requireNonNull(userId, "userId is required");

        if (!confirm) {
            throw new IllegalStateException("Confirmation is required to add local charges. This operation cannot be undone.");
        }

        log.info("addLocalCharges started for transactionPoid={} companyPoid={} userId={}",
                transactionPoid, companyPoid, userId);

        // Find quotation with company filtering for data isolation
        SalesQuotationShipHeader header = repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Validate quotation status is "PROCESSING"
        String quotationStatus = header.getQuotationStatus();
        if (quotationStatus == null || !"PROCESSING".equalsIgnoreCase(quotationStatus)) {
            throw new IllegalStateException("Local charges can only be added when quotation status is 'PROCESSING'. Current status: " + quotationStatus);
        }

        // Count existing charges before adding local charges
        int chargesBefore = chargeDetailRepository.findByTransactionPoid(transactionPoid).size();

        // Call stored procedure PROC_SALES_SHQTN_LOCAL_CHARGES
        // Procedure signature: PROC_SALES_SHQTN_LOCAL_CHARGES(P_TRANSACTION_POID NUMBER, P_LOGIN_USER VARCHAR2, P_STATUS OUT VARCHAR2)
        String result = jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SHQTN_LOCAL_CHARGES(?,?,?); END;")) {
                cs.setBigDecimal(1, transactionPoid);
                cs.setString(2, userId);
                cs.registerOutParameter(3, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(3));
            }
        });

        // Count charges after adding local charges
        int chargesAfter = chargeDetailRepository.findByTransactionPoid(transactionPoid).size();
        int chargesAdded = chargesAfter - chargesBefore;

        // Handle error messages from stored procedure
        // Check for "ERROR" or "INFO:" prefixes as per documentation
        boolean success = true;
        String message = result;

        if (result != null && !result.isEmpty()) {
            String upperResult = result.toUpperCase();
            if (upperResult.startsWith("ERROR")) {
                success = false;
                message = result;
                log.error("Error adding local charges for transactionPoid={}: {}", transactionPoid, result);
                throw new IllegalStateException("Failed to add local charges: " + result);
            } else if (upperResult.startsWith("INFO:")) {
                // INFO messages are typically informational, not errors
                message = result.substring(5).trim(); // Remove "INFO:" prefix
                log.info("Info from local charges procedure for transactionPoid={}: {}", transactionPoid, message);
            }
        }

        // If no message from stored procedure, use default success message
        if (message == null || message.isEmpty()) {
            message = "Default local charges added successfully";
        }

        log.info("addLocalCharges completed for transactionPoid={} chargesAdded={} success={}",
                transactionPoid, chargesAdded, success);

        return new AddLocalChargesResponse(success, message, chargesAdded);
    }

    /**
     * Gets customer address details (contact person and email) for a sales quotation.
     * Validates quotation ownership and calls stored procedure PROC_GET_QTN_CUST_ADDRESS.
     */
    @Transactional(readOnly = true)
    public CustomerContactResponse getCustomerAddressDetails(BigDecimal transactionPoid, BigDecimal companyPoid,
                                                              String userId, BigDecimal customerPoid) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(customerPoid, "customerPoid is required");

        log.info("getCustomerAddressDetails started for transactionPoid={} companyPoid={} userId={} customerPoid={}",
                transactionPoid, companyPoid, userId, customerPoid);

        // Validate quotation exists and belongs to company (for data isolation)
        // Even though the stored procedure doesn't need transactionPoid, we validate it for security
        repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Convert userId string to BigDecimal for stored procedure
        // Note: The stored procedure expects P_USER_POID as NUMBER, so we need to parse userId
        BigDecimal userPoid;
        try {
            userPoid = new BigDecimal(userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid userId format. Expected numeric value, got: " + userId);
        }

        // Call stored procedure PROC_GET_QTN_CUST_ADDRESS
        // Parameters: P_USER_POID, P_CUSTOMER_POID, P_CURSOR (OUT CURSOR)
        CustomerContactResponse response = jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GET_QTN_CUST_ADDRESS(?,?,?); END;")) {
                cs.setBigDecimal(1, userPoid);
                cs.setBigDecimal(2, customerPoid);
                cs.registerOutParameter(3, OracleTypes.CURSOR);
                cs.execute();
                try (ResultSet rs = (ResultSet) cs.getObject(3)) {
                    if (rs != null && rs.next()) {
                        String contactPerson = rs.getString("CONTACT_PERSON");
                        String email = rs.getString("EMAIL1");
                        log.debug("Retrieved customer contact details: contactPerson={} email={}", contactPerson, email);
                        return new CustomerContactResponse(contactPerson, email);
                    }
                }
                // No data found - return null values
                log.warn("No customer contact details found for customerPoid={}", customerPoid);
                return new CustomerContactResponse(null, null);
            }
        });

        log.info("getCustomerAddressDetails completed for transactionPoid={} customerPoid={}",
                transactionPoid, customerPoid);

        return response;
    }

    /**
     * Gets charge tax percentage and tax POID for a sales quotation.
     * Validates quotation ownership and calls stored procedure PROC_GET_CHARGE_TAX_PER_V2.
     */
    @Transactional(readOnly = true)
    public ChargeTaxResponse getChargeTaxForQuotation(BigDecimal transactionPoid, BigDecimal companyPoid,
                                                       BigDecimal customerPoid, BigDecimal chargePoid) {
        Objects.requireNonNull(transactionPoid, "transactionPoid is required");
        Objects.requireNonNull(companyPoid, "companyPoid is required");
        Objects.requireNonNull(customerPoid, "customerPoid is required");
        Objects.requireNonNull(chargePoid, "chargePoid is required");

        log.info("getChargeTaxForQuotation started for transactionPoid={} companyPoid={} customerPoid={} chargePoid={}",
                transactionPoid, companyPoid, customerPoid, chargePoid);

        // Validate quotation exists and belongs to company (for data isolation)
        // Even though the stored procedure doesn't need transactionPoid, we validate it for security
        repository.findActiveWithDetailsByCompany(transactionPoid, companyPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid +
                        " with companyPoid " + companyPoid +
                        ". The quotation may not exist, be soft-deleted, or belong to a different company."));

        // Call stored procedure PROC_GET_CHARGE_TAX_PER_V2
        // Parameters: P_COMPANY_POID, P_PARTY_TYPE ("CUSTOMER"), P_CUSTOMER_POID, P_CHARGE_POID, P_CURSOR (OUT CURSOR)
        ChargeTaxResponse response = getChargeTaxDetails(companyPoid, customerPoid, chargePoid);

        log.info("getChargeTaxForQuotation completed for transactionPoid={} chargePoid={} taxPercentage={} taxPoid={}",
                transactionPoid, chargePoid,
                response != null ? response.taxPercentage() : null,
                response != null ? response.taxPoid() : null);

        return response;
    }

    /**
     * Converts charge detail entity to DTO.
     */
    private SalesQuotationShipItemDto toChargeDetailDto(SalesQuotationShipChargeDetail chargeDetail) {
        SalesQuotationShipItemDto dto = new SalesQuotationShipItemDto();
        dto.setTransactionPoid(chargeDetail.getId().getTransactionPoid());
        dto.setDetailRowId(chargeDetail.getId().getDetailRowId());
        dto.setChargePoid(chargeDetail.getChargePoid());
        dto.setCurrencyCode(chargeDetail.getCurrencyCode());
        dto.setCurrencyRate(chargeDetail.getCurrencyRate());
        dto.setBuyingCharge(chargeDetail.getBuyingCharge());
        dto.setBuyingChargeLocal(chargeDetail.getBuyingChargeLocal());
        dto.setSellingCharge(chargeDetail.getSellingCharge());
        dto.setSellingChargeLocal(chargeDetail.getSellingChargeLocal());
        dto.setTotalSellingChargeLocal(chargeDetail.getTotalSellingChargeLocal());
        dto.setRemarks(chargeDetail.getRemarks());
        dto.setEquipmentPoid(chargeDetail.getEquipmentPoid());
        dto.setDischargePortPoid(chargeDetail.getDischargePortPoid());
        dto.setLoadingPortPoid(chargeDetail.getLoadingPortPoid());
        dto.setQuantity(chargeDetail.getQuantity());
        dto.setPerBuy(chargeDetail.getPerBuy());
        dto.setPerQty(chargeDetail.getPerQty());
        dto.setQuotationDays(chargeDetail.getQuotationDays());
        dto.setUnit(chargeDetail.getUnit());
        dto.setTaxPoid(chargeDetail.getTaxPoid());
        dto.setTaxPercentage(chargeDetail.getTaxPercentage());
        dto.setTaxAmount(chargeDetail.getTaxAmount());
        dto.setTaxAmountLocal(chargeDetail.getTaxAmountLocal());
        dto.setCreatedBy(chargeDetail.getCreatedBy());
        dto.setCreatedDate(chargeDetail.getCreatedDate());
        return dto;
    }

    public SalesQuotationShipCustomerDataResponse loadCustomerData(SalesQuotationShipCustomerDataRequest request) {
        Objects.requireNonNull(request, "request is required");
        Objects.requireNonNull(request.groupId(), "groupId is required");
        Objects.requireNonNull(request.companyId(), "companyId is required");
        Objects.requireNonNull(request.customerAddressId(), "customerAddressId is required");
        Objects.requireNonNull(request.transactionPoid(), "transactionPoid is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_GET_CUST_DATA(?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.customerAddressId());
                cs.setBigDecimal(4, request.transactionPoid());
                cs.registerOutParameter(5, OracleTypes.CURSOR);
                cs.execute();
                try (ResultSet rs = (ResultSet) cs.getObject(5)) {
                    return new SalesQuotationShipCustomerDataResponse(mapResultSet(rs));
                }
            }
        });
    }

    public String refreshDetailCharges(SalesQuotationShipRefreshDetailRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_REFRESH_DTL(?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.transactionPoid());
                cs.setString(4, request.quotedRate());
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(5));
            }
        });
    }

    public String createRfq(SalesQuotationShipRfQRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_RFQ_CREATE(?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.transactionPoid());
                cs.setString(4, request.loginUser());
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(5));
            }
        });
    }

    public String updateLinkedQuantities(SalesQuotationShipQuantityUpdateRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_QTY_UPDATE(?,?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.userPoid());
                cs.setString(4, request.loginUser());
                cs.setBigDecimal(5, request.transactionPoid());
                cs.registerOutParameter(6, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(6));
            }
        });
    }

    public String importItems(SalesQuotationShipImportRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_IMPORT_ITEMS(?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.transactionPoid());
                cs.setString(4, request.loginUser());
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(5));
            }
        });
    }

    public String clearItems(SalesQuotationShipClearItemsRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_ITEMS_CLEAR(?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.transactionPoid());
                cs.registerOutParameter(4, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(4));
            }
        });
    }

    public String createDeliveryNote(SalesQuotationShipDeliveryNoteRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_DN_CREATE(?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.transactionPoid());
                cs.setString(4, request.loginUser());
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(5));
            }
        });
    }

    public String selectAllDetails(SalesQuotationShipSelectAllRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_SELECT_ALL(?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.transactionPoid());
                cs.setString(4, request.selectionFlag());
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(5));
            }
        });
    }

    public SalesQuotationShipCustomerValidationResponse validateCustomer(SalesQuotationShipValidationRequest request) {
        Objects.requireNonNull(request, "request is required");
        String customerStatus = runCustomerPreValidation(request.customerAddressId());
        String validationStatus = runCustomerChangeValidation(request.transactionPoid(), request.customerAddressId());

        return new SalesQuotationShipCustomerValidationResponse(customerStatus, validationStatus);
    }

    public SalesQuotationShipDefaultDetailResponse setDefaultDetailValues(SalesQuotationShipDefaultDetailRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_SET_DFLT_DTL2(?,?,?,?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.customerAddressId());
                cs.setBigDecimal(2, request.stockPoid());
                cs.setBigDecimal(3, request.transactionPoid());
                cs.setBigDecimal(4, request.stockUnitPoid());
                setOptionalDate(cs, 5, request.documentDate());
                cs.registerOutParameter(6, OracleTypes.NUMBER);
                cs.registerOutParameter(7, OracleTypes.NUMBER);
                cs.registerOutParameter(8, OracleTypes.NUMBER);
                cs.execute();
                return new SalesQuotationShipDefaultDetailResponse(
                        cs.getBigDecimal(6),
                        cs.getBigDecimal(7),
                        cs.getBigDecimal(8)
                );
            }
        });
    }

    public String validateDeliveryOption(SalesQuotationShipDeliveryOptionValidateRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_CBOX_VALIDATE(?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.transactionPoid());
                cs.setBigDecimal(2, request.detailRowId());
                cs.setBigDecimal(3, request.stockPoid());
                cs.setString(4, request.selectionFlag());
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(5));
            }
        });
    }

    public String calculateAfterSave(SalesQuotationShipCalculateRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_DO_CALC(?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setString(3, request.loginUser());
                cs.setBigDecimal(4, request.transactionPoid());
                cs.registerOutParameter(5, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(5));
            }
        });
    }

    public String markDocumentAsDeleted(SalesQuotationShipGlobalDeleteRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GLOB_DOC_DELETE(?,?,?,?, ?,?,?,?, ?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.userPoid());
                cs.setString(4, request.docId());
                cs.setString(5, request.masterVoSqlName());
                cs.setBigDecimal(6, request.docKeyPoid());
                cs.setString(7, request.tableName());
                cs.setString(8, request.operationMode());
                setOptionalDate(cs, 9, request.docDate());
                cs.setString(10, request.docType());
                cs.registerOutParameter(11, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(11));
            }
        });
    }

    public String acquireRecordLock(SalesQuotationShipRecordLockRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN ? := RTN_GLOBAL_USER_RECORD_LOCK(?,?,?,?,?,?); END;")) {
                cs.registerOutParameter(1, Types.VARCHAR);
                cs.setString(2, request.userId());
                cs.setString(3, request.sessionDetail());
                cs.setString(4, request.docId());
                cs.setString(5, request.docName());
                cs.setBigDecimal(6, request.docKeyPoid());
                cs.setString(7, request.requestType());
                cs.execute();
                return nullToEmpty(cs.getString(1));
            }
        });
    }

    public String releaseRecordLock(SalesQuotationShipReleaseLockRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GLOB_DOC_RELEASE_LOCK(?,?,?,?, ?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.userPoid());
                cs.setString(4, request.docId());
                cs.setBigDecimal(5, request.docKeyPoid());
                cs.setString(6, request.requestMetadata());
                cs.registerOutParameter(7, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(7));
            }
        });
    }

    public SalesQuotationShipTreeResponse loadDeletedDocuments(SalesQuotationShipDeletedDocsRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GLOB_DOC_TREEVIEW_LOAD(?,?,?,?, ?,?,?,?, ?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.userPoid());
                cs.setString(4, request.docId());
                cs.setString(5, request.filterField1());
                cs.setString(6, request.filterValue1());
                cs.setString(7, request.filterField2());
                cs.setString(8, request.filterValue2());
                cs.registerOutParameter(9, OracleTypes.CURSOR);
                cs.execute();
                try (ResultSet rs = (ResultSet) cs.getObject(9)) {
                    return new SalesQuotationShipTreeResponse(mapResultSet(rs));
                }
            }
        });
    }

    public String resetSequence(SalesQuotationShipResetSequenceRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_UPDATE_SEQNO_SORTING(?,?,?,?, ?,?,?,? ,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.userPoid());
                cs.setString(4, request.tableName());
                cs.setString(5, "SEQNO");
                cs.setNull(6, Types.NUMERIC);
                cs.setString(7, request.masterVoSqlName());
                cs.setNull(8, Types.NUMERIC);
                cs.setString(9, "RESETSEQNO");
                cs.registerOutParameter(10, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(10));
            }
        });
    }

    public String updateSequence(SalesQuotationShipUpdateSequenceRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_UPDATE_SEQNO_SORTING(?,?,?,?, ?,?,?,? ,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.userPoid());
                cs.setString(4, request.tableName());
                cs.setString(5, "SEQNO");
                cs.setBigDecimal(6, request.currentSeqNo());
                cs.setString(7, request.masterVoSqlName());
                cs.setBigDecimal(8, request.docKeyPoid());
                cs.setString(9, "");
                cs.registerOutParameter(10, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(10));
            }
        });
    }

    public void updateUserProfile(SalesQuotationShipUserProfileRequest request) {
        Objects.requireNonNull(request, "request is required");
        jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GLOB_USR_PROFILE_UPDATE(?,?,?); END;")) {
                cs.setBigDecimal(1, request.userPoid());
                cs.setString(2, request.settingName());
                cs.setString(3, request.settingValue());
                cs.execute();
                return null;
            }
        });
    }

    public String grantEditPermission(SalesQuotationShipGrantEditRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GLOBAL_DOC_EDIT_RIGHT_SET(?,?,?,?, ?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.userPoid());
                cs.setString(3, request.docId());
                cs.setBigDecimal(4, request.docKeyPoid());
                cs.registerOutParameter(5, OracleTypes.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(5));
            }
        });
    }

    public SalesQuotationShipApprovalResponse approvalAction(SalesQuotationShipApprovalActionRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GLOB_APPROVAL_ACTION(?,?,?,?, ?,?,?,?, ?,?,?,?, ?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.userPoid());
                cs.setString(4, request.docId());
                cs.setBigDecimal(5, request.docKeyPoid());
                cs.setString(6, request.action());
                cs.setString(7, request.comments());
                cs.setString(8, request.docInfo());
                cs.setString(9, request.docRef());
                setOptionalDate(cs, 10, request.docDate());
                cs.setObject(11, request.targetUserPoid());
                cs.registerOutParameter(12, Types.VARCHAR);
                cs.registerOutParameter(13, Types.VARCHAR);
                cs.registerOutParameter(14, Types.NUMERIC);
                cs.execute();
                return new SalesQuotationShipApprovalResponse(
                        nullToEmpty(cs.getString(12)),
                        nullToEmpty(cs.getString(13)),
                        cs.getBigDecimal(14)
                );
            }
        });
    }

    public String updateDocumentConfidentiality(SalesQuotationShipConfidentialRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GLOB_DOC_RIGHT_UPDATE(?,?,?,?, ?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.userPoid());
                cs.setString(3, request.docId());
                cs.setBigDecimal(4, request.docKeyPoid());
                cs.setString(5, request.rightCode());
                cs.setString(6, request.actionFlag());
                cs.registerOutParameter(7, OracleTypes.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(7));
            }
        });
    }

    public SalesQuotationShipGlPostingResponse loadGlPosting(SalesQuotationShipGlViewRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GL_POSTING_VIEW_LOAD_V2(?,?,?,?, ?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setString(3, request.docId());
                cs.setBigDecimal(4, request.docKeyPoid());
                cs.registerOutParameter(5, OracleTypes.CURSOR);
                cs.registerOutParameter(6, OracleTypes.CURSOR);
                cs.registerOutParameter(7, OracleTypes.CURSOR);
                cs.registerOutParameter(8, OracleTypes.CURSOR);
                cs.execute();
                try (ResultSet rs1 = (ResultSet) cs.getObject(5);
                     ResultSet rs2 = (ResultSet) cs.getObject(6);
                     ResultSet rs3 = (ResultSet) cs.getObject(7);
                     ResultSet rs4 = (ResultSet) cs.getObject(8)) {
                    return new SalesQuotationShipGlPostingResponse(
                            mapResultSet(rs1),
                            mapResultSet(rs2),
                            mapResultSet(rs3),
                            mapResultSet(rs4)
                    );
                }
            }
        });
    }

    public String repostToGl(SalesQuotationShipGlRepostRequest request) {
        Objects.requireNonNull(request, "request is required");
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_GL_LEDGER_POSTING_MAIN(?,?,?,?,?,?,?); END;")) {
                cs.setBigDecimal(1, request.groupId());
                cs.setBigDecimal(2, request.companyId());
                cs.setBigDecimal(3, request.userPoid());
                cs.setString(4, request.docId());
                cs.setBigDecimal(5, request.docKeyPoid());
                cs.setString(6, request.docRef());
                cs.registerOutParameter(7, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(7));
            }
        });
    }

    private void validateCustomerBeforeSave(BigDecimal customerAddressId) {
        if (customerAddressId == null) {
            throw new IllegalArgumentException("Customer address is required");
        }
        String status = runCustomerPreValidation(customerAddressId);
        if (status.isEmpty()) {
            return;
        }
        String normalized = status.toUpperCase(Locale.ROOT);
        if (normalized.contains("NO_DATA") || normalized.contains("ERROR")) {
            throw new IllegalStateException(status);
        }
        if (normalized.contains("WARNING")) {
            log.warn("Customer validation returned warning: {}", status);
        }
    }

    private void validateCustomerAfterSave(BigDecimal transactionPoid, BigDecimal customerAddressId) {
        if (transactionPoid == null || customerAddressId == null) {
            log.debug("Skipping post-save customer validation due to missing identifiers (transaction={}, address={})",
                    transactionPoid, customerAddressId);
            return;
        }
        String status = runCustomerChangeValidation(transactionPoid, customerAddressId);
        if (status.isEmpty()) {
            return;
        }
        if ("false".equalsIgnoreCase(status.trim())) {
            throw new IllegalStateException("Not allowed to change customer after dependent documents exist");
        }
        String normalized = status.toUpperCase(Locale.ROOT);
        if (normalized.contains("ERROR")) {
            throw new IllegalStateException(status);
        }
        if (normalized.contains("WARNING")) {
            log.warn("Post-save customer validation warning: {}", status);
        }
    }

    private void triggerPostSaveCalculation(SalesQuotationShipHeader header, SalesQuotationShipCommand command) {
        if (header == null) {
            return;
        }
        BigDecimal groupId = firstNonNull(command.getQuotationCompany(), header.getQuotationCompany());
        BigDecimal companyId = firstNonNull(command.getCompanyPoid(), header.getCompanyPoid());
        String loginUser = firstNonBlank(command.getLastModifiedBy(), command.getCreatedBy(),
                header.getLastModifiedBy(), header.getCreatedBy());
        BigDecimal transactionPoid = header.getTransactionPoid();

        if (groupId == null || companyId == null || transactionPoid == null || loginUser == null || loginUser.isBlank()) {
            log.debug("Skipping calculation routine for quotation {}. Missing context - group:{}, company:{}, user:{}",
                    transactionPoid, groupId, companyId, loginUser);
            return;
        }

        SalesQuotationShipCalculateRequest request = new SalesQuotationShipCalculateRequest(
                groupId, companyId, loginUser, transactionPoid
        );
        String result = calculateAfterSave(request);
        if (!result.isBlank()) {
            String normalized = result.toUpperCase(Locale.ROOT);
            if (normalized.contains("ERROR")) {
                log.warn("Calculation routine returned message for quotation {}: {}", transactionPoid, result);
            } else {
                log.info("Calculation routine response for quotation {}: {}", transactionPoid, result);
            }
        }
    }

    private SalesQuotationShipHeader reloadHeader(SalesQuotationShipHeader header) {
        if (header == null) {
            return null;
        }
        BigDecimal transactionPoid = header.getTransactionPoid();
        if (transactionPoid == null) {
            return header;
        }
        try {
            entityManager.detach(header);
        } catch (IllegalArgumentException ex) {
            log.debug("Unable to detach quotation {} before reload: {}", transactionPoid, ex.getMessage());
        }
        return repository.findActiveWithDetails(transactionPoid).orElse(header);
    }

    private SalesQuotationShipHeader getRequiredHeader(BigDecimal transactionPoid) {
        return repository.findActiveWithDetails(transactionPoid)
                .orElseThrow(() -> new EntityNotFoundException("Sales shipping quotation not found for id " + transactionPoid));
    }

    private String runCustomerPreValidation(BigDecimal customerAddressId) {
        if (customerAddressId == null) {
            return "";
        }
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SCH_QTN_VALIDATE_CUSTOMER(?,?); END;")) {
                cs.setBigDecimal(1, customerAddressId);
                cs.registerOutParameter(2, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(2));
            }
        });
    }

    private String runCustomerChangeValidation(BigDecimal transactionPoid, BigDecimal customerAddressId) {
        if (transactionPoid == null || customerAddressId == null) {
            return "";
        }
        return jdbcTemplate.execute((Connection connection) -> {
            try (CallableStatement cs = connection.prepareCall("BEGIN PROC_SALES_SCQTN_CUST_VALIDATE(?,?,?); END;")) {
                cs.setBigDecimal(1, transactionPoid);
                cs.setBigDecimal(2, customerAddressId);
                cs.registerOutParameter(3, Types.VARCHAR);
                cs.execute();
                return nullToEmpty(cs.getString(3));
            }
        });
    }

    private BigDecimal firstNonNull(BigDecimal... values) {
        if (values == null) {
            return null;
        }
        for (BigDecimal value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private Specification<SalesQuotationShipHeader> andIfPresent(Specification<SalesQuotationShipHeader> base,
                                                                Specification<SalesQuotationShipHeader> addition) {
        if (addition == null) {
            return base;
        }
        return base == null ? addition : base.and(addition);
    }

    private List<Map<String, Object>> mapResultSet(ResultSet rs) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        if (rs == null) {
            return results;
        }
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }
            results.add(row);
        }
        return results;
    }

    private void setOptionalDate(CallableStatement cs, int parameterIndex, LocalDate date) throws SQLException {
        if (date == null) {
            cs.setNull(parameterIndex, Types.DATE);
        } else {
            cs.setDate(parameterIndex, java.sql.Date.valueOf(date));
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private void validateCreateBusinessRules(SalesQuotationShipCommand command) {
        String quotationType = command.getQuotationType();
        // If quotationType is null, assume it's a shipping quotation (default behavior)
        boolean isGeneral = quotationType != null && "GENERAL".equalsIgnoreCase(quotationType);
        
        // For shipping quotations (not GENERAL), validate shipping-specific fields
        if (!isGeneral) {
            // LinePoid is required for shipping quotations
            // if (command.getLinePoid() == null) {
            //     throw new IllegalArgumentException("linePoid is required for shipping quotations");
            // }
            
            // CommodityType is required for shipping quotations
            // if (command.getCommodityType() == null || command.getCommodityType().isBlank()) {
            //     throw new IllegalArgumentException("commodityType is required for shipping quotations");
            // }
            
            // TermsOfFreight is required for shipping quotations
            // if (command.getTermsOfFreight() == null || command.getTermsOfFreight().isBlank()) {
            //     throw new IllegalArgumentException("termsOfFreight is required for shipping quotations");
            // }
            
            // CargoType is required for shipping quotations
            // if (command.getCargoType() == null || command.getCargoType().isBlank()) {
            //     throw new IllegalArgumentException("cargoType is required for shipping quotations");
            // }
            
            // LoadingPortPoid and DischargePortPoid are required if MultiPort = 'N' or null
            // String multiPort = command.getMultiPort();
            // if (multiPort == null || "N".equalsIgnoreCase(multiPort)) {
            //     if (command.getLoadingPortPoid() == null) {
            //         throw new IllegalArgumentException("loadingPortPoid is required when multiPort = 'N'");
            //     }
            //     if (command.getDischargePortPoid() == null) {
            //         throw new IllegalArgumentException("dischargePortPoid is required when multiPort = 'N'");
            //     }
            // }
        }
    }
    
    private void calculateTotalsAndAssignCompanyDivision(SalesQuotationShipHeader header, SalesQuotationShipCommand command) {
        // Calculate buying total and selling total from charge details
        BigDecimal buyingTotal = header.getCharges().stream()
                .filter(c -> c.getBuyingChargeLocal() != null)
                .map(SalesQuotationShipChargeDetail::getBuyingChargeLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal sellingTotal = header.getCharges().stream()
                .filter(c -> c.getTotalSellingChargeLocal() != null)
                .map(SalesQuotationShipChargeDetail::getTotalSellingChargeLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Update totals in header
        header.setTotalBuyingAmountLocal(buyingTotal);
        header.setTotalSellingAmountLocal(sellingTotal);
        
        // Calculate tax total
        BigDecimal taxTotal = header.getCharges().stream()
                .filter(c -> c.getTaxAmountLocal() != null)
                .map(SalesQuotationShipChargeDetail::getTaxAmountLocal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        header.setTotalTax(taxTotal);
        
        // Company/Division assignment logic based on buying vs selling total comparison
        // Use company from command as login company (may have been set in applyHeaderFields)
        BigDecimal loginCompanyPoid = command.getCompanyPoid();
        
        if (loginCompanyPoid != null) {
            if (buyingTotal.compareTo(sellingTotal) > 0) {
                // Buying total > Selling total: Auto-assign default division and company from parameters
                // Use login company and default division from parameters
                // If division was provided in command, use it; otherwise use default from parameter
                header.setCompanyPoid(loginCompanyPoid);
                if (header.getQuotationDivision() == null) {
                    // TODO: Get default division from parameter SALES_QUOTATION_SH_DEF_DIV
                    // For now, use division from command if provided, otherwise keep null
                    if (command.getQuotationDivision() != null) {
                        header.setQuotationDivision(command.getQuotationDivision());
                    }
                }
            } else {
                // Selling total >= Buying total: Assign login company and division = 1
                header.setCompanyPoid(loginCompanyPoid);
                // Set division to 1 if not already set
                if (header.getQuotationDivision() == null) {
                    header.setQuotationDivision(BigDecimal.ONE);
                }
            }
        } else {
            // If company was not provided, log warning
            log.warn("CompanyPoid is null for quotation {} - company should be provided in request", 
                    header.getTransactionPoid());
        }
        
        // Warning: If selling amount < buying amount, show warning but allow save
        if (sellingTotal.compareTo(buyingTotal) < 0) {
            log.warn("Warning: Selling total ({}) is less than buying total ({}) for quotation {}", 
                    sellingTotal, buyingTotal, header.getTransactionPoid());
        }
    }
    
    private void applyHeaderFields(SalesQuotationShipHeader header, SalesQuotationShipCommand command, boolean isCreate) {
        // Transaction Date
        if (command.getTransactionDate() != null) {
            header.setTransactionDate(command.getTransactionDate());
        } else if (isCreate && header.getTransactionDate() == null) {
            header.setTransactionDate(LocalDate.now());
        }
        
        // Company Poid (may be auto-assigned later based on totals)
        header.setCompanyPoid(command.getCompanyPoid());
        
        // DocRef handling
        if (isCreate) {
            // For create: DocRef is auto-generated, will be retrieved after insert
            // Don't set it here, let database generate it
        } else {
            // For update: DocRef is read-only, should NOT be updated
            // It will be restored from original value after applyHeaderFields
        }
        
        // Customer fields
        header.setCustomerPoid(command.getCustomerPoid());
        header.setAddressPoid(command.getAddressPoid());
        header.setCustomerName(command.getCustomerName());
        header.setCustomerContact(command.getCustomerContact());
        header.setCustomerEmail(command.getCustomerEmail());
        header.setNewCustomer(command.getNewCustomer());
        
        // Line
        header.setLinePoid(command.getLinePoid());
        
        // Quotation Status - default to "PROCESSING" if not provided
        header.setQuotationStatus(Optional.ofNullable(command.getQuotationStatus())
                .orElseGet(() -> isCreate ? "PROCESSING" : header.getQuotationStatus()));
        
        // Salesman - will be auto-populated if not provided
        header.setSalesmanPoid(command.getSalesmanPoid());
        
        // Validity Dates - default to current date and +30 days
        if (command.getValidityFromDate() != null) {
            header.setValidityFromDate(command.getValidityFromDate());
        } else if (isCreate && header.getValidityFromDate() == null) {
            header.setValidityFromDate(LocalDate.now());
        }
        
        if (command.getValidityToDate() != null) {
            header.setValidityToDate(command.getValidityToDate());
        } else if (isCreate && header.getValidityToDate() == null) {
            LocalDate fromDate = header.getValidityFromDate() != null ? header.getValidityFromDate() : LocalDate.now();
            header.setValidityToDate(fromDate.plusDays(30));
        }
        
        // Ports
        header.setLoadingPortPoid(command.getLoadingPortPoid());
        header.setDischargePortPoid(command.getDischargePortPoid());
        header.setElsewherePoid(command.getElsewherePoid());
        header.setPlaceOfReceipt(command.getPlaceOfReceipt());
        header.setPlaceOfDelivery(command.getPlaceOfDelivery());
        
        // Commodity and Description
        header.setCommodityType(command.getCommodityType());
        header.setDescription(command.getDescription());
        
        // Shipping Mode - default to "BY_SEA" if not provided
        header.setShippingMode(Optional.ofNullable(command.getShippingMode())
                .orElseGet(() -> isCreate ? "BY_SEA" : header.getShippingMode()));
        
        // Terms
        header.setTermsOfShipment(command.getTermsOfShipment());
        header.setShippingTerms(command.getShippingTerms());
        header.setTermsOfFreight(command.getTermsOfFreight());
        header.setIncoTerms(command.getIncoTerms());
        
        // Vessel Frequency - default to "Weekly" if not provided
        if (command.getVesselFrequency() != null) {
            header.setVesselFrequency(command.getVesselFrequency());
        } else if (isCreate && header.getVesselFrequency() == null) {
            header.setVesselFrequency("Weekly");
        }
        
        // MultiPort - default to "N" if not provided
        header.setMultiPort(Optional.ofNullable(command.getMultiPort())
                .orElseGet(() -> isCreate ? "N" : header.getMultiPort()));
        
        // Terms Condition Poid - default from parameter TERMS_CONDITION_SHIP_POID (default "42")
        if (command.getTermsConditionPoid() != null) {
            header.setTermsConditionPoid(command.getTermsConditionPoid());
        } else if (isCreate && header.getTermsConditionPoid() == null) {
            // Default to 42 if not provided (from parameter TERMS_CONDITION_SHIP_POID)
            header.setTermsConditionPoid(new BigDecimal("42"));
        }
        
        // Other fields
        header.setSurcharges(command.getSurcharges());
        header.setRemarks(command.getRemarks());
        header.setPrintRemarks(command.getPrintRemarks());
        header.setActionStatus(command.getActionStatus());
        header.setActionDueDate(command.getActionDueDate());
        header.setLostReason(command.getLostReason());
        header.setCargoType(command.getCargoType());
        header.setFreeDaysPol(command.getFreeDaysPol());
        header.setFreeDaysPod(command.getFreeDaysPod());
        header.setTransitDays(command.getTransitDays());
        header.setImportExport(command.getImportExport());
        header.setLinePrintYn(command.getLinePrintYn());
        header.setRouting(command.getRouting());
        header.setQuotationCompany(command.getQuotationCompany());
        header.setQuotationDivision(command.getQuotationDivision());
        header.setQuotationSubject(command.getQuotationSubject());
        header.setQuotationType(command.getQuotationType());
        header.setVesselPoid(command.getVesselPoid());
        header.setEnquiryRefPoid(command.getEnquiryRefPoid());
        
        // Totals will be calculated later
        // Don't set them from command, they should be calculated
    }

    private void mapCharges(SalesQuotationShipHeader header, List<SalesQuotationShipChargeRequest> requests) {
        List<SalesQuotationShipChargeRequest> safeRequests = requests == null ? new ArrayList<>() : requests;

        Map<BigDecimal, SalesQuotationShipChargeDetail> existing = new HashMap<>();
        header.getCharges().forEach(charge -> existing.put(charge.getId().getDetailRowId(), charge));

        BigDecimal nextId = existing.keySet().stream()
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO)
                .add(BigDecimal.ONE);

        List<SalesQuotationShipChargeDetail> updated = new ArrayList<>();
        for (SalesQuotationShipChargeRequest request : safeRequests) {
            BigDecimal rowId = request.getDetailRowId();
            SalesQuotationShipChargeDetail entity = null;
            if (rowId != null) {
                entity = existing.remove(rowId);
            }
            if (entity == null) {
                BigDecimal assignedId = rowId != null ? rowId : nextId;
                if (rowId == null) {
                    nextId = nextId.add(BigDecimal.ONE);
                }
                entity = new SalesQuotationShipChargeDetail();
                entity.setId(new SalesQuotationShipChargeDetailId(header.getTransactionPoid(), assignedId));
            }
            populateChargeEntity(entity, request, header);
            entity.setHeader(header);
            updated.add(entity);
        }

        header.clearCharges();
        updated.forEach(header::addCharge);
    }

    private void populateChargeEntity(SalesQuotationShipChargeDetail entity,
                                      SalesQuotationShipChargeRequest request,
                                      SalesQuotationShipHeader header) {
        entity.setChargePoid(request.getChargePoid());
        entity.setCurrencyCode(request.getCurrencyCode());
        entity.setCurrencyRate(request.getCurrencyRate());
        entity.setBuyingCharge(request.getBuyingCharge());
        entity.setBuyingChargeLocal(request.getBuyingChargeLocal());
        entity.setSellingCharge(request.getSellingCharge());
        entity.setSellingChargeLocal(request.getSellingChargeLocal());
        entity.setTotalSellingChargeLocal(request.getTotalSellingChargeLocal());
        entity.setRemarks(request.getRemarks());
        entity.setEquipmentPoid(request.getEquipmentPoid());
        entity.setDischargePortPoid(request.getDischargePortPoid());
        entity.setLoadingPortPoid(request.getLoadingPortPoid());
        entity.setQuantity(request.getQuantity());
        entity.setPerBuy(request.getPerBuy());
        entity.setPerQty(request.getPerQty());
        entity.setQuotationDays(request.getQuotationDays());
        entity.setUnit(request.getUnit());
        entity.setTaxPoid(request.getTaxPoid());
        entity.setTaxPercentage(request.getTaxPercentage());
        entity.setTaxAmount(request.getTaxAmount());
        entity.setTaxAmountLocal(request.getTaxAmountLocal());

        String auditUser = firstNonBlank(header != null ? header.getLastModifiedBy() : null,
                header != null ? header.getCreatedBy() : null);
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy(auditUser);
            entity.setCreatedDate(now);
        }
        entity.setLastModifiedBy(auditUser);
        entity.setLastModifiedDate(now);
    }

    private void mapEquipment(SalesQuotationShipHeader header, List<SalesQuotationShipEquipmentRequest> requests) {
        List<SalesQuotationShipEquipmentRequest> safeRequests = requests == null ? new ArrayList<>() : requests;

        Map<BigDecimal, SalesQuotationShipEquipmentDetail> existing = new HashMap<>();
        header.getEquipment().forEach(equip -> existing.put(equip.getId().getDetailRowId(), equip));

        BigDecimal nextId = existing.keySet().stream()
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO)
                .add(BigDecimal.ONE);

        List<SalesQuotationShipEquipmentDetail> updated = new ArrayList<>();
        for (SalesQuotationShipEquipmentRequest request : safeRequests) {
            BigDecimal rowId = request.getDetailRowId();
            SalesQuotationShipEquipmentDetail entity = null;
            if (rowId != null) {
                entity = existing.remove(rowId);
            }
            if (entity == null) {
                BigDecimal assignedId = rowId != null ? rowId : nextId;
                if (rowId == null) {
                    nextId = nextId.add(BigDecimal.ONE);
                }
                entity = new SalesQuotationShipEquipmentDetail();
                entity.setId(new SalesQuotationShipEquipmentDetailId(header.getTransactionPoid(), assignedId));
            }
            populateEquipmentEntity(entity, request, header);
            entity.setHeader(header);
            updated.add(entity);
        }

        header.clearEquipment();
        updated.forEach(header::addEquipment);
    }

    private void populateEquipmentEntity(SalesQuotationShipEquipmentDetail entity,
                                         SalesQuotationShipEquipmentRequest request,
                                         SalesQuotationShipHeader header) {
        entity.setEquipmentPoid(request.getEquipmentPoid());
        entity.setQuantity(request.getQuantity());
        entity.setOog(request.getOog());
        entity.setOogDetails(request.getOogDetails());
        entity.setDangerousGoods(request.getDangerousGoods());
        entity.setDangerousGoodsClass(request.getDangerousGoodsClass());
        entity.setUnoNumber(request.getUnoNumber());
        entity.setTemperature(request.getTemperature());
        entity.setRemarks(request.getRemarks());

        String auditUser = firstNonBlank(header != null ? header.getLastModifiedBy() : null,
                header != null ? header.getCreatedBy() : null);
        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy(auditUser);
            entity.setCreatedDate(now);
        }
        entity.setLastModifiedBy(auditUser);
        entity.setLastModifiedDate(now);
    }

    private SalesQuotationShipSummaryDto toSummaryDto(SalesQuotationShipHeader header) {
        SalesQuotationShipSummaryDto dto = new SalesQuotationShipSummaryDto();
        dto.setTransactionPoid(header.getTransactionPoid());
        dto.setTransactionDate(header.getTransactionDate());
        dto.setDocRef(header.getDocRef());
        dto.setCompanyPoid(header.getCompanyPoid());
        dto.setCustomerPoid(header.getCustomerPoid());
        dto.setCustomerName(header.getCustomerName());
        dto.setQuotationStatus(header.getQuotationStatus());
        dto.setValidityToDate(header.getValidityToDate());
        dto.setTotalSellingAmountLocal(header.getTotalSellingAmountLocal());
        dto.setTotalBuyingAmountLocal(header.getTotalBuyingAmountLocal());
        dto.setTotalTax(header.getTotalTax());
        // TODO: Populate salesmanName if needed - may require joining with salesman table
        // For now, leaving it null as it's not stored in the header entity
        dto.setSalesmanName(null);
        return dto;
    }

    private SalesQuotationShipDetailDto toDetailDto(SalesQuotationShipHeader header) {
        return toDetailDto(header, true);
    }
    
    private SalesQuotationShipDetailDto toDetailDto(SalesQuotationShipHeader header, boolean includeDetails) {
        SalesQuotationShipDetailDto dto = new SalesQuotationShipDetailDto();
        dto.setTransactionPoid(header.getTransactionPoid());
        dto.setTransactionDate(header.getTransactionDate());
        dto.setDocRef(header.getDocRef());
        dto.setCompanyPoid(header.getCompanyPoid());
        dto.setCustomerPoid(header.getCustomerPoid());
        dto.setAddressPoid(header.getAddressPoid());
        dto.setLinePoid(header.getLinePoid());
        dto.setCustomerName(header.getCustomerName());
        dto.setCustomerContact(header.getCustomerContact());
        dto.setCustomerEmail(header.getCustomerEmail());
        dto.setQuotationStatus(header.getQuotationStatus());
        dto.setQuotationSubject(header.getQuotationSubject());
        dto.setQuotationType(header.getQuotationType());
        dto.setPaymentMode(header.getTermsOfFreight());
        dto.setValidityFromDate(header.getValidityFromDate());
        dto.setValidityToDate(header.getValidityToDate());
        dto.setRemarks(header.getRemarks());
        dto.setShippingMode(header.getShippingMode());
        dto.setTermsOfShipment(header.getTermsOfShipment());
        dto.setLoadingPortPoid(header.getLoadingPortPoid());
        dto.setDischargePortPoid(header.getDischargePortPoid());
        dto.setPlaceOfReceipt(header.getPlaceOfReceipt());
        dto.setPlaceOfDelivery(header.getPlaceOfDelivery());
        dto.setCommodityType(header.getCommodityType());
        dto.setDescription(header.getDescription());
        dto.setSalesmanPoid(header.getSalesmanPoid());
        dto.setTotalSellingAmountLocal(header.getTotalSellingAmountLocal());
        dto.setTotalBuyingAmountLocal(header.getTotalBuyingAmountLocal());
        dto.setTotalTax(header.getTotalTax());
        dto.setTotalTaxAmount(header.getTotalTax());
        dto.setActionStatus(header.getActionStatus());
        dto.setActionDueDate(header.getActionDueDate());
        dto.setEnquiryRefPoid(header.getEnquiryRefPoid());
        dto.setSurcharges(header.getSurcharges());
        dto.setRouting(header.getRouting());
        dto.setMultiPort(header.getMultiPort());
        dto.setNewCustomer(header.getNewCustomer());
        dto.setCreatedBy(header.getCreatedBy());
        dto.setCreatedDate(header.getCreatedDate());
        dto.setLastModifiedBy(header.getLastModifiedBy());
        dto.setLastModifiedDate(header.getLastModifiedDate());

        // Include details only if requested
        if (includeDetails) {
            List<SalesQuotationShipItemDto> items = header.getCharges().stream()
                    .map(this::toItemDto)
                    .toList();
            dto.setCharges(items);

            List<SalesQuotationShipEquipmentDto> equipment = header.getEquipment().stream()
                    .map(this::toEquipmentDto)
                    .toList();
            dto.setEquipment(equipment);
        } else {
            // Set empty lists if details are not requested
            dto.setCharges(new ArrayList<>());
            dto.setEquipment(new ArrayList<>());
        }

        return dto;
    }

    private SalesQuotationShipItemDto toItemDto(SalesQuotationShipChargeDetail entity) {
        SalesQuotationShipItemDto dto = new SalesQuotationShipItemDto();
        if (entity.getId() != null) {
            dto.setDetailRowId(entity.getId().getDetailRowId());
        }
        dto.setChargePoid(entity.getChargePoid());
        dto.setCurrencyCode(entity.getCurrencyCode());
        dto.setCurrencyRate(entity.getCurrencyRate());
        dto.setBuyingCharge(entity.getBuyingCharge());
        dto.setBuyingChargeLocal(entity.getBuyingChargeLocal());
        dto.setSellingCharge(entity.getSellingCharge());
        dto.setSellingChargeLocal(entity.getSellingChargeLocal());
        dto.setTotalSellingChargeLocal(entity.getTotalSellingChargeLocal());
        dto.setRemarks(entity.getRemarks());
        dto.setEquipmentPoid(entity.getEquipmentPoid());
        dto.setDischargePortPoid(entity.getDischargePortPoid());
        dto.setLoadingPortPoid(entity.getLoadingPortPoid());
        dto.setQuantity(entity.getQuantity());
        dto.setPerBuy(entity.getPerBuy());
        dto.setPerQty(entity.getPerQty());
        dto.setQuotationDays(entity.getQuotationDays());
        dto.setUnit(entity.getUnit());
        dto.setTaxPoid(entity.getTaxPoid());
        dto.setTaxPercentage(entity.getTaxPercentage());
        dto.setTaxAmount(entity.getTaxAmount());
        dto.setTaxAmountLocal(entity.getTaxAmountLocal());
        return dto;
    }

    private SalesQuotationShipEquipmentDto toEquipmentDto(SalesQuotationShipEquipmentDetail entity) {
        SalesQuotationShipEquipmentDto dto = new SalesQuotationShipEquipmentDto();
        if (entity.getId() != null) {
            dto.setTransactionPoid(entity.getId().getTransactionPoid());
            dto.setDetailRowId(entity.getId().getDetailRowId());
        }
        dto.setEquipmentPoid(entity.getEquipmentPoid());
        dto.setQuantity(entity.getQuantity());
        dto.setOog(entity.getOog());
        dto.setOogDetails(entity.getOogDetails());
        dto.setDangerousGoods(entity.getDangerousGoods());
        dto.setDangerousGoodsClass(entity.getDangerousGoodsClass());
        dto.setUnoNumber(entity.getUnoNumber());
        dto.setTemperature(entity.getTemperature());
        dto.setRemarks(entity.getRemarks());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedDate(entity.getCreatedDate());
        return dto;
    }

    public record SalesmanDefaultResponse(String salesmanPoid) { }

    public record LineAccessResponse(String lineList) { }

    public record CustomerContactResponse(String contactPerson, String email) { }

    public record ChargeTaxResponse(BigDecimal taxPercentage, BigDecimal taxPoid) { }

    public record QuotationTotalsResponse(BigDecimal buyingTotal, BigDecimal sellingTotal, BigDecimal totalTax) { }

    public record QuotationDependenciesResponse(
            Boolean canDelete,
            String reason,
            Integer salesInvoiceCount,
            String message
    ) { }
}
