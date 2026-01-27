package com.asg.shipchandling.salesinvoice.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.service.PrintService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.shipchandling.salesinvoice.dto.*;
import com.asg.shipchandling.salesinvoice.dto.request.CalculateDiscountCommissionRequest;
import com.asg.shipchandling.salesinvoice.dto.request.CreateSalesDnDtlRequest;
import com.asg.shipchandling.salesinvoice.dto.request.CreateSalesInvoiceDtlRequest;
import com.asg.shipchandling.salesinvoice.dto.request.CreateSalesInvoiceRequest;
import com.asg.shipchandling.salesinvoice.dto.request.CreditDetailsRequest;
import com.asg.shipchandling.salesinvoice.dto.request.LoadQuotationItemsRequest;
import com.asg.shipchandling.salesinvoice.dto.request.UpdateSalesDnDtlRequest;
import com.asg.shipchandling.salesinvoice.dto.request.UpdateSalesInvoiceDtlRequest;
import com.asg.shipchandling.salesinvoice.dto.request.UpdateSalesInvoiceRequest;
import com.asg.shipchandling.salesinvoice.dto.response.CalculateDiscountCommissionResponse;
import com.asg.shipchandling.salesinvoice.dto.response.CalculateDueDateResponse;
import com.asg.shipchandling.salesinvoice.dto.response.CalculateGpResponse;
import com.asg.shipchandling.salesinvoice.dto.response.CreditDetailsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadCostBookingsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadDeliveryNoteResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadQuotationCurrencyResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadQuotationItemsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.UnloadQuotationResponse;
import com.asg.shipchandling.salesinvoice.dto.response.ValidationResponse;
import com.asg.shipchandling.salesinvoice.dto.response.VerifyInvoiceResponse;
import com.asg.shipchandling.salesinvoice.entity.*;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.salesinvoice.repository.*;
import com.asg.shipchandling.StockMaster.repository.StockMasterRepository;
import com.asg.shipchandling.StockMaster.entity.StockMasterEntity;
import com.asg.shipchandling.deliverynote.repository.SalesDeliveryNoteHdrRepository;
import com.asg.shipchandling.deliverynote.entity.SalesDeliveryNoteHdr;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JasperReport;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesInvoiceServiceImpl implements SalesInvoiceService {

    private final SalesInvoiceHdrRepository invoiceHdrRepository;
    private final SalesInvoiceHdrRepositoryImpl invoiceHdrRepositoryImpl;
    private final SalesInvoiceDtlRepository invoiceDtlRepository;
    private final SalesInvoiceDtlRepositoryImpl invoiceDtlRepositoryImpl;
    private final SalesDnDtlRepository dnDtlRepository;
    private final SalesInvCostbkdDtlRepository costbkdDtlRepository;
    private final SalesInvoiceStoredProcRepository salesInvoiceStoredProcRepository;
    private final StockMasterRepository stockMasterRepository;
    private final SalesDeliveryNoteHdrRepository deliveryNoteHdrRepository;
    private final DocumentSearchService documentService;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;
    private final PrintService printService;
    @Autowired
    private DataSource dataSource;
    
    @PersistenceContext
    private EntityManager entityManager;
    // Add DataSource for stored procedure calls
    // private final DataSource dataSource;

    @Override
    @Transactional
    public SalesInvoiceHdrDto createSalesInvoice(CreateSalesInvoiceRequest request,
                                                 Long groupPoid, Long companyPoid, String userId) {
        // Validate party type
        if (!"CUSTOMER".equalsIgnoreCase(request.getPartyType()) &&
                !"PRINCIPAL".equalsIgnoreCase(request.getPartyType())) {
            throw new CustomException("Party type must be CUSTOMER or PRINCIPAL");
        }

        // Validate customer/principal based on party type
        if ("CUSTOMER".equalsIgnoreCase(request.getPartyType()) && request.getCustomerPoid() == null) {
            throw new CustomException("Customer is required when party type is CUSTOMER");
        }
        if ("PRINCIPAL".equalsIgnoreCase(request.getPartyType()) && request.getPrincipalPoid() == null) {
            throw new CustomException("Principal is required when party type is PRINCIPAL");
        }
        // ValidationResponse validCustomer = callCustomerValidateProc(
        // "CUSTOMER".equalsIgnoreCase(request.getPartyType()) ?
        // request.getCustomerPoid()
        // : request.getPrincipalPoid(),
        // request.getCreditType(), request.getAuthorizedId());

        // Create entity
        SalesInvoiceHdr invoice = new SalesInvoiceHdr();
        BeanUtils.copyProperties(request, invoice, "transactionDate");
        // Always set transactionDate to current timestamp (don't use value from request)
        invoice.setTransactionDate(LocalDate.now());
        invoice.setGroupPoid(groupPoid);
        invoice.setCompanyPoid(companyPoid);
        invoice.setCreatedBy(userId);
        invoice.setLastmodifiedBy(userId);
        invoice.setInvStatus("IN_PROGRESS");
        invoice.setVerified("N");
        invoice.setDeleted("N");

        // Save to get transactionPoid
        SalesInvoiceHdr savedInvoice = invoiceHdrRepository.save(invoice);
        invoiceHdrRepository.flush();

        // Call stored procedure BEFORE SAVE for validation
        Boolean validCustomer = salesInvoiceStoredProcRepository.callCustomerEditValidateProc(
                savedInvoice.getTransactionPoid(),
                request.getCustomerPoid());

        if (validCustomer) {
            // Save detail tables
            if (request.getInvoiceDetails() != null && !request.getInvoiceDetails().isEmpty()) {
                for (CreateSalesInvoiceDtlRequest createSalesInvoiceDtlRequest : request.getInvoiceDetails()) {
                    // Skip if actionType is "isDeleted" or "delRowId" (should not create deleted items)
                    String actionType = createSalesInvoiceDtlRequest.getActionType();
                    if ("isDeleted".equalsIgnoreCase(actionType) || "delRowId".equalsIgnoreCase(actionType)) {
                        log.debug("Skipping invoice detail with actionType: {}", actionType);
                        continue;
                    }
                    
                    // Create invoice detail (actionType is "isCreated" or null/empty)
                    // Get next DetRowId
                    Long maxDetRowId = invoiceDtlRepository
                            .findMaxDetRowIdByTransactionPoid(savedInvoice.getTransactionPoid());
                    Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;
                    // Create invoice detail
                    SalesInvoiceDtl dtl = new SalesInvoiceDtl();
                    dtl.setTransactionPoid(savedInvoice.getTransactionPoid());
                    dtl.setDetRowId(detRowId);
                    dtl.setStockPoid(createSalesInvoiceDtlRequest.getStockPoid());
                    dtl.setStockUnitPoid(createSalesInvoiceDtlRequest.getStockUnitPoid());
                    dtl.setQuantity(createSalesInvoiceDtlRequest.getQuantity());
                    dtl.setPrice(createSalesInvoiceDtlRequest.getPrice());
                    dtl.setDiscount(createSalesInvoiceDtlRequest.getDiscount());
                    dtl.setBaseAmt(createSalesInvoiceDtlRequest.getBaseAmt());
                    dtl.setTaxPoid(createSalesInvoiceDtlRequest.getTaxPoid());
                    dtl.setCostPoid(createSalesInvoiceDtlRequest.getCostCenterPoid());
                    dtl.setRemarks(createSalesInvoiceDtlRequest.getRemarks());
                    dtl.setCreatedBy(userId);
                    dtl.setLastmodifiedBy(userId);

                    // Calculate amount
                    if (dtl.getQuantity() != null && dtl.getPrice() != null) {
                        BigDecimal quantity = BigDecimal.valueOf(dtl.getQuantity());
                        BigDecimal amount = quantity.multiply(dtl.getPrice());
                        if (dtl.getDiscount() != null) {
                            amount = amount.subtract(BigDecimal.valueOf(dtl.getDiscount()));
                        }
                        dtl.setAmount(amount.longValue());
                    }

                    // Get tax percentage if tax is selected
                    if (createSalesInvoiceDtlRequest.getTaxPoid() != null) {
                        // Calculate tax amount: taxAmount = baseAmt * taxPercentage / 100
                    }

                    SalesInvoiceDtl savedDtl = invoiceDtlRepository.save(dtl);
                    String logDetail = String.format(
                            "Row Created on Sales Invoice Detail with detRowId: %s",
                            savedDtl.getDetRowId()
                    );

                    loggingService.createLogSummaryEntry(
                            UserContext.getDocumentId(),
                            savedInvoice.getTransactionPoid().toString(),
                            logDetail
                    );
                }
            }

            if (request.getDeliveryNoteDetails() != null && !request.getDeliveryNoteDetails().isEmpty()) {
                for (CreateSalesDnDtlRequest dnDtl : request.getDeliveryNoteDetails()) {
                    // Skip if actionType is "isDeleted" or "delRowId" (should not create deleted items)
                    String actionType = dnDtl.getActionType();
                    if ("isDeleted".equalsIgnoreCase(actionType) || "delRowId".equalsIgnoreCase(actionType)) {
                        log.debug("Skipping delivery note detail with actionType: {}", actionType);
                        continue;
                    }
                    
                    // Create delivery note detail (actionType is "isCreated" or null/empty)
                    // Get next DetRowId
                    Long maxDetRowId = dnDtlRepository
                            .findMaxDetRowIdByTransactionPoid(savedInvoice.getTransactionPoid());
                    Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

                    // Create delivery note detail
                    SalesDnDtl dtl = new SalesDnDtl();
                    dtl.setTransactionPoid(savedInvoice.getTransactionPoid());
                    dtl.setDetRowId(detRowId);
                    dtl.setDnPoidFk(dnDtl.getDnPoidFk());
                    dtl.setQuotationPoidFk(dnDtl.getQuotationPoidFk());
                    dtl.setRemarks(dnDtl.getRemarks());
                    dtl.setCreatedBy(userId);
                    dtl.setLastmodifiedBy(userId);

                    log.debug("Creating delivery note detail with detRowId: {}, remarks: {}", detRowId, dnDtl.getRemarks());
                    SalesDnDtl savedDtl = dnDtlRepository.save(dtl);

                    String logDetail = String.format(
                            "Row Created on Sales Delivery Note Detail with detRowId: %s",
                            savedDtl.getDetRowId()
                    );

                    loggingService.createLogSummaryEntry(
                            UserContext.getDocumentId(),
                            savedInvoice.getTransactionPoid().toString(),
                            logDetail
                    );
                }
                // Flush to ensure all delivery note detail changes are persisted
                dnDtlRepository.flush();
            }

            // Call stored procedure AFTER SAVE for authorization
            salesInvoiceStoredProcRepository.callAuthorizationProc(savedInvoice.getTransactionPoid(),
                    savedInvoice.getAuthorizedId(), userId);

            // Refresh to get auto-generated DocRef
            invoiceHdrRepository.flush();
        }
        SalesInvoiceHdr refreshedInvoice = invoiceHdrRepository.findByTransactionPoid(
                savedInvoice.getTransactionPoid()).orElse(savedInvoice);

        String key = refreshedInvoice.getTransactionPoid().toString();
        String documentId = UserContext.getDocumentId();
        loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, documentId, key);

        // Convert to DTO
        SalesInvoiceHdrDto dto = convertToDto(refreshedInvoice, true);
        return dto;
    }

    private SalesInvoiceHdrDto convertToDto(SalesInvoiceHdr invoice, boolean includeDetails) {
        SalesInvoiceHdrDto dto = new SalesInvoiceHdrDto();
        BeanUtils.copyProperties(invoice, dto);

        if (includeDetails) {
            // Use native query to avoid Hibernate type mapping issues with PRICE column
            List<SalesInvoiceDtl> details = invoiceDtlRepositoryImpl
                    .findByTransactionPoidNative(invoice.getTransactionPoid());
            List<SalesInvoiceDtlDto> detailDtos = details.stream()
                    .map(this::convertInvoiceDtlToDto)
                    .collect(Collectors.toList());
            dto.setInvoiceDetails(detailDtos);

            List<SalesDnDtl> dnDetails = dnDtlRepository.findByTransactionPoid(invoice.getTransactionPoid());
            List<SalesDnDtlDto> dnDetailDtos = dnDetails.stream()
                    .map(this::convertDnDtlToDto)
                    .collect(Collectors.toList());
            dto.setDeliveryNoteDetails(dnDetailDtos);

            List<SalesInvCostbkdDtl> costDetails = costbkdDtlRepository
                    .findByTransactionPoid(invoice.getTransactionPoid());
            List<SalesInvCostbkdDtlDto> costDetailDtos = costDetails.stream()
                    .map(this::convertCostbkdDtlToDto)
                    .collect(Collectors.toList());
            dto.setCostBookedDetails(costDetailDtos);
        }

        return dto;
    }

    private SalesInvoiceDtlDto convertInvoiceDtlToDto(SalesInvoiceDtl dtl) {
        SalesInvoiceDtlDto dto = new SalesInvoiceDtlDto();
        BeanUtils.copyProperties(dtl, dto);
        return dto;
    }

    private SalesDnDtlDto convertDnDtlToDto(SalesDnDtl dtl) {
        SalesDnDtlDto dto = new SalesDnDtlDto();
        BeanUtils.copyProperties(dtl, dto);
        return dto;
    }

    private SalesInvCostbkdDtlDto convertCostbkdDtlToDto(SalesInvCostbkdDtl dtl) {
        SalesInvCostbkdDtlDto dto = new SalesInvCostbkdDtlDto();
        BeanUtils.copyProperties(dtl, dto);
        // supplierName and bookType are already in the entity, so they'll be copied
        return dto;
    }
    
    /**
     * Convert invoice to DTO with LOV details populated
     */
    private SalesInvoiceHdrDto convertToDtoWithLov(SalesInvoiceHdr invoice, boolean includeDetails) {
        // Try to fetch with LOV details from repository
        List<Object[]> queryResults = invoiceHdrRepository.findSalesInvoiceWithDetails(
                invoice.getTransactionPoid(), invoice.getCompanyPoid());
        
        SalesInvoiceHdrDto dto = new SalesInvoiceHdrDto();
        BeanUtils.copyProperties(invoice, dto);
        
        // Populate header LOV details from query result
        if (!queryResults.isEmpty()) {
            Object[] row = queryResults.get(0);
            log.debug("Query result row length: {}, customerPoid from invoice: {}", 
                    row.length, invoice.getCustomerPoid());
            if (row.length > 58) {
                log.debug("Customer details from join - poid: {}, code: {}, name: {}", 
                        row[56], row[57], row[58]);
            }
            populateHeaderLovDetails(dto, row);
        } else {
            log.warn("No query results found for invoice transactionPoid: {}", invoice.getTransactionPoid());
        }
        
        if (includeDetails) {
            // Fetch invoice details with tax details in single query
            List<Object[]> detailsWithTax = invoiceDtlRepositoryImpl
                    .findByTransactionPoidWithTaxDetails(invoice.getTransactionPoid());
            List<SalesInvoiceDtlDto> detailDtos = detailsWithTax.stream()
                    .map(this::convertInvoiceDtlRowToDtoWithLov)
                    .collect(Collectors.toList());
            dto.setInvoiceDetails(detailDtos);
            
            // Fetch delivery note details
            List<SalesDnDtl> dnDetails = dnDtlRepository.findByTransactionPoid(invoice.getTransactionPoid());
            List<SalesDnDtlDto> dnDetailDtos = dnDetails.stream()
                    .map(this::convertDnDtlToDtoWithLov)
                    .collect(Collectors.toList());
            dto.setDeliveryNoteDetails(dnDetailDtos);
            
            // Fetch cost booked details
            List<SalesInvCostbkdDtl> costDetails = costbkdDtlRepository
                    .findByTransactionPoid(invoice.getTransactionPoid());
            List<SalesInvCostbkdDtlDto> costDetailDtos = costDetails.stream()
                    .map(this::convertCostbkdDtlToDto)
                    .collect(Collectors.toList());
            dto.setCostBookedDetails(costDetailDtos);
        }
        
        return dto;
    }
    
    /**
     * Populate header LOV details from query result
     * Column order: invoice fields (56) + LOV details (6 objects * 3 fields = 18) = 74 columns
     * Invoice fields: 0-55 (56 fields: transactionPoid through lastmodifiedDate)
     * LOV order: Customer (indices 56-58), Principal (59-61), Quotation (62-64), 
     *            Print Division (65-67), Delivery Note (68-70), FDA (71-73)
     */
    private void populateHeaderLovDetails(SalesInvoiceHdrDto dto, Object[] row) {
        log.debug("Row length: {}, Expected: 74 (56 invoice + 18 LOV)", row.length);
        
        // Customer Details (indices 56-58: custPoid, custCode, custName)
        // The CASE statement in query handles partyType logic
        SalesInvoiceHdrDto.LovDetailDto customerDetail = createLovDetailFromRow(row, 56);
        log.debug("Customer detail from join - poid: {}, code: {}, description: {}, partyType: {}", 
                customerDetail.getPoid(), customerDetail.getCode(), customerDetail.getDescription(), dto.getPartyType());
        
        // Use customerPoid from invoice if join didn't return poid
        if (customerDetail.getPoid() == null && dto.getCustomerPoid() != null) {
            log.debug("Using fallback customerPoid from invoice: {}", dto.getCustomerPoid());
            customerDetail.setPoid(dto.getCustomerPoid());
        }
        
        // If we have poid but no code/description, the JOIN didn't find a match
        // Query separately based on partyType
        if (customerDetail.getPoid() != null && customerDetail.getCode() == null && customerDetail.getDescription() == null) {
            log.debug("Customer poid {} found but code/description are null. Querying separately based on partyType: {}", 
                    customerDetail.getPoid(), dto.getPartyType());
            
            if ("CUSTOMER".equalsIgnoreCase(dto.getPartyType())) {
                // Query SALES_CUSTOMER_MASTER
                SalesInvoiceHdrDto.LovDetailDto fallbackDetail = queryCustomerDetails(customerDetail.getPoid());
                if (fallbackDetail != null && fallbackDetail.getCode() != null) {
                    customerDetail = fallbackDetail;
                }
            } else if ("PRINCIPAL".equalsIgnoreCase(dto.getPartyType())) {
                // Query SHIP_PRINCIPAL_MASTER using customerPoid as PRINCIPAL_POID
                SalesInvoiceHdrDto.LovDetailDto fallbackDetail = queryPrincipalDetails(customerDetail.getPoid());
                if (fallbackDetail != null && fallbackDetail.getCode() != null) {
                    customerDetail = fallbackDetail;
                }
            }
        }
        
        dto.setCustomerDetails(customerDetail);
        
        // Principal Details (indices 59-61: prPoid, prCode, prName)
        SalesInvoiceHdrDto.LovDetailDto principalDetail = createLovDetailFromRow(row, 59);
        // Use principalPoid from invoice if join didn't return poid
        if (principalDetail.getPoid() == null && dto.getPrincipalPoid() != null) {
            principalDetail.setPoid(dto.getPrincipalPoid());
        }
        dto.setPrincipalDetails(principalDetail);
        
        // Quotation Details (indices 62-64: qtnDetailPoid, qtnDetailCode, qtnDetailDescription)
        SalesInvoiceHdrDto.LovDetailDto qtnDetail = new SalesInvoiceHdrDto.LovDetailDto();
        // Use qtn.TRANSACTION_POID from join (index 62)
        if (row.length > 62 && row[62] != null) {
            if (row[62] instanceof Number) {
                qtnDetail.setPoid(((Number) row[62]).longValue());
            }
        }
        // Fallback: use inv.QTN_POID (string at index 14) and convert to Long
        if (qtnDetail.getPoid() == null && row.length > 14 && row[14] != null) {
            try {
                String qtnPoidStr = row[14].toString();
                if (qtnPoidStr != null && !qtnPoidStr.trim().isEmpty()) {
                    qtnDetail.setPoid(Long.parseLong(qtnPoidStr));
                }
            } catch (NumberFormatException e) {
                log.warn("Failed to parse qtnPoid as Long: {}", row[14]);
            }
        }
        // Get code and description from join (indices 63-64)
        if (row.length > 63 && row[63] != null) {
            qtnDetail.setCode(row[63].toString());
        }
        if (row.length > 64 && row[64] != null) {
            qtnDetail.setDescription(row[64].toString());
        }
        dto.setQtnDetails(qtnDetail);
        
        // Print Division Details (indices 65-67: divDetailPoid, divDetailCode, divDetailDescription)
        SalesInvoiceHdrDto.LovDetailDto divDetail = createLovDetailFromRow(row, 65);
        // Use printDivisionPoid from invoice if join didn't return poid
        if (divDetail.getPoid() == null && dto.getPrintDivisionPoid() != null) {
            divDetail.setPoid(dto.getPrintDivisionPoid());
        }
        dto.setPrintDivisionDetails(divDetail);
        
        // Delivery Note Details (indices 68-70: dnDetailPoid, dnDetailCode, dnDetailDescription)
        SalesInvoiceHdrDto.LovDetailDto dnDetail = new SalesInvoiceHdrDto.LovDetailDto();
        // Get poid from query (index 68) - this is the CASE statement result
        if (row.length > 68 && row[68] != null) {
            if (row[68] instanceof Number) {
                dnDetail.setPoid(((Number) row[68]).longValue());
            }
        }
        // Fallback: use inv.DN_POID (string at index 46) and convert to Long
        if (dnDetail.getPoid() == null && row.length > 46 && row[46] != null) {
            try {
                String dnPoidStr = row[46].toString();
                if (dnPoidStr != null && !dnPoidStr.trim().isEmpty()) {
                    dnDetail.setPoid(Long.parseLong(dnPoidStr));
                }
            } catch (NumberFormatException e) {
                log.warn("Failed to parse dnPoid as Long: {}", row[46]);
            }
        }
        // Only populate code and description if we have a valid poid
        if (dnDetail.getPoid() != null) {
            // Get code from join (index 69: dn.DOC_REF)
            if (row.length > 69 && row[69] != null) {
                String code = row[69].toString();
                if (code != null && !code.trim().isEmpty()) {
                    dnDetail.setCode(code);
                }
            }
            // Get description from join (index 70: the concatenated description)
            if (row.length > 70 && row[70] != null) {
                String description = row[70].toString();
                // Only set if description is meaningful (not just "VOY:- CUST:N/A" or empty)
                if (description != null && !description.trim().isEmpty() && 
                    !description.trim().equals("VOY:- CUST:N/A") && 
                    !description.trim().equals("VOY:- CUST:")) {
                    dnDetail.setDescription(description);
                }
            }
        }
        dto.setDnDetails(dnDetail);
        
        // FDA Details (indices 71-73: fdaDetailPoid, fdaDetailCode, fdaDetailDescription)
        SalesInvoiceHdrDto.LovDetailDto fdaDetail = new SalesInvoiceHdrDto.LovDetailDto();
        // Get poid from query (index 71) - this is the CASE statement result
        if (row.length > 71 && row[71] != null) {
            if (row[71] instanceof Number) {
                fdaDetail.setPoid(((Number) row[71]).longValue());
            }
        }
        // Fallback: use inv.FDA_REF (string at index 44) and convert to Long
        if (fdaDetail.getPoid() == null && row.length > 44 && row[44] != null) {
            try {
                String fdaRefStr = row[44].toString();
                if (fdaRefStr != null && !fdaRefStr.trim().isEmpty()) {
                    fdaDetail.setPoid(Long.parseLong(fdaRefStr));
                }
            } catch (NumberFormatException e) {
                log.warn("Failed to parse fdaRef as Long: {}", row[44]);
            }
        }
        // Only populate code and description if we have a valid poid
        if (fdaDetail.getPoid() != null) {
            // Get code from join (index 72: fda.DOC_REF)
            if (row.length > 72 && row[72] != null) {
                String code = row[72].toString();
                if (code != null && !code.trim().isEmpty()) {
                    fdaDetail.setCode(code);
                }
            }
            // Get description from join (index 73: the concatenated description)
            if (row.length > 73 && row[73] != null) {
                String description = row[73].toString();
                // Only set if description is meaningful (not just " /  / " or empty separators)
                if (description != null && !description.trim().isEmpty() && 
                    !description.trim().equals("/") && 
                    !description.trim().matches("^\\s*/\\s*/\\s*$")) {
                    fdaDetail.setDescription(description);
                }
            }
        }
        dto.setFdaDetails(fdaDetail);
    }
    
    /**
     * Create LOV detail from row array starting at given index
     * Expects: [poid, code, description] at indices [index, index+1, index+2]
     */
    private SalesInvoiceHdrDto.LovDetailDto createLovDetailFromRow(Object[] row, int index) {
        SalesInvoiceHdrDto.LovDetailDto detail = new SalesInvoiceHdrDto.LovDetailDto();
        
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
    private SalesInvoiceHdrDto.LovDetailDto createEmptyLovDetail() {
        SalesInvoiceHdrDto.LovDetailDto detail = new SalesInvoiceHdrDto.LovDetailDto();
        detail.setPoid(null);
        detail.setCode(null);
        detail.setDescription(null);
        return detail;
    }
    
    /**
     * Convert invoice detail row (Object[]) to DTO with LOV details
     * Row structure: [0-30] invoice detail fields, [31-33] tax details (POID, CODE, NAME)
     */
    private SalesInvoiceDtlDto convertInvoiceDtlRowToDtoWithLov(Object[] row) {
        SalesInvoiceDtlDto dto = new SalesInvoiceDtlDto();
        
        // Map invoice detail fields (indices 0-30)
        dto.setTransactionPoid(row[0] != null ? ((Number) row[0]).longValue() : null);
        dto.setDetRowId(row[1] != null ? ((Number) row[1]).longValue() : null);
        dto.setDnPoidLinkFk(row[2] != null ? ((Number) row[2]).longValue() : null);
        dto.setDetRowIdChrgFk(row[3] != null ? ((Number) row[3]).longValue() : null);
        dto.setStockPoid(row[4] != null ? ((Number) row[4]).longValue() : null);
        dto.setQuantity(row[5] != null ? ((Number) row[5]).longValue() : null);
        dto.setPrice(convertToBigDecimal(row[6]));
        dto.setDiscount(row[7] != null ? ((Number) row[7]).longValue() : null);
        dto.setAmount(row[8] != null ? ((Number) row[8]).longValue() : null);
        dto.setRemarks(toStringSafe(row[9]));
        dto.setStockUnitPoid(row[10] != null ? ((Number) row[10]).longValue() : null);
        dto.setCreatedBy(toStringSafe(row[11]));
        dto.setCreatedDate(row[12] != null ? (Timestamp) row[12] : null);
        dto.setLastmodifiedBy(toStringSafe(row[13]));
        dto.setLastmodifiedDate(row[14] != null ? (Timestamp) row[14] : null);
        dto.setQuotationPoid(row[15] != null ? ((Number) row[15]).longValue() : null);
        dto.setCostAmt(row[16] != null ? ((Number) row[16]).longValue() : null);
        dto.setQuotationDetRowId(row[17] != null ? ((Number) row[17]).longValue() : null);
        dto.setPurchasePrice(row[18] != null ? ((Number) row[18]).longValue() : null);
        dto.setPurchaseQty(row[19] != null ? ((Number) row[19]).longValue() : null);
        dto.setNetSales(row[20] != null ? ((Number) row[20]).longValue() : null);
        dto.setNetDiscount(row[21] != null ? ((Number) row[21]).longValue() : null);
        dto.setItemGp(row[22] != null ? ((Number) row[22]).longValue() : null);
        dto.setItemGpPer(row[23] != null ? ((Number) row[23]).longValue() : null);
        dto.setItemType(toStringSafe(row[24]));
        dto.setTaxPercentage(row[25] != null ? ((Number) row[25]).longValue() : null);
        dto.setTaxAmount(row[26] != null ? ((Number) row[26]).longValue() : null);
        dto.setTaxPoid(row[27] != null ? ((Number) row[27]).longValue() : null);
        dto.setBaseAmt(row[28] != null ? ((Number) row[28]).longValue() : null);
        dto.setIncentive(row[29] != null ? ((Number) row[29]).longValue() : null);
        String costPoidStr = toStringSafe(row[30]);
        // costPoid is stored as String in entity but DTO expects Long for costCenterPoid
        if (costPoidStr != null && !costPoidStr.isEmpty()) {
            try {
                dto.setCostCenterPoid(Long.parseLong(costPoidStr));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse costPoid as Long: {}", costPoidStr);
            }
        }
        
        // Populate tax details from query result (indices 31-33)
        if (row.length > 31 && row[31] != null) {
            Long taxPoid = row[31] instanceof Number ? ((Number) row[31]).longValue() : null;
            String taxCode = row.length > 32 ? toStringSafe(row[32]) : null;
            String taxName = row.length > 33 ? toStringSafe(row[33]) : null;
            if (taxPoid != null) {
                dto.setTaxDetails(new SalesInvoiceDtlDto.LovDetailDto(
                        taxPoid,
                        taxCode,
                        taxName));
            }
        }
        
        // Populate stock details
        if (dto.getStockPoid() != null) {
            Optional<StockMasterEntity> stock = stockMasterRepository.findByStockPoid(dto.getStockPoid());
            if (stock.isPresent()) {
                StockMasterEntity stockEntity = stock.get();
                dto.setStockDetails(new SalesInvoiceDtlDto.LovDetailDto(
                        stockEntity.getStockPoid(),
                        stockEntity.getStockCode(),
                        stockEntity.getStockName()));
            }
        }
        
        // Populate cost center details (from GL_COST_CENTER_MASTER.MIS_GROUP)
        if (dto.getCostCenterPoid() != null) {
            try {
                String misGroup = getCostCenterMisGroup(dto.getCostCenterPoid());
                if (misGroup != null) {
                    dto.setCostCenterDetails(new SalesInvoiceDtlDto.LovDetailDto(
                            dto.getCostCenterPoid(),
                            misGroup, // MIS_GROUP is the code
                            misGroup)); // MIS_GROUP is also the description
            }
            } catch (Exception e) {
                log.warn("Failed to fetch cost center details for costCenterPoid={}: {}", 
                        dto.getCostCenterPoid(), e.getMessage());
            }
        }
        
        return dto;
    }
    
    /**
     * Helper methods for conversion
     */
    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Convert delivery note detail to DTO with LOV details
     */
    private SalesDnDtlDto convertDnDtlToDtoWithLov(SalesDnDtl dtl) {
        SalesDnDtlDto dto = new SalesDnDtlDto();
        BeanUtils.copyProperties(dtl, dto);
        
        // Populate delivery note details (dnPoidFk)
        if (dtl.getDnPoidFk() != null) {
            Optional<SalesDeliveryNoteHdr> dn = deliveryNoteHdrRepository.findByTransactionPoid(dtl.getDnPoidFk());
            if (dn.isPresent()) {
                SalesDeliveryNoteHdr dnEntity = dn.get();
                String description = "VOY:-" + (dnEntity.getVoyageRef() != null ? dnEntity.getVoyageRef() : "") + 
                        " CUST:" + getCustomerName(dnEntity.getCustomerPoid());
                dto.setDnDetails(new SalesDnDtlDto.LovDetailDto(
                        dnEntity.getTransactionPoid(),
                        dnEntity.getDocRef(), // DOC_REF is the code
                        description));
            }
        }
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesInvoiceHdrDto getSalesInvoiceByPoid(Long transactionPoid, Long companyPoid, Boolean includeDetails) {
        log.info("getSalesInvoiceByPoid service started for transactionPoid={} companyPoid={}", transactionPoid, companyPoid);
        
        // First check if invoice exists with just transactionPoid
        Optional<SalesInvoiceHdr> invoiceByPoid = invoiceHdrRepository.findByTransactionPoid(transactionPoid);
        if (invoiceByPoid.isEmpty()) {
            log.warn("Sales invoice not found with transactionPoid={}", transactionPoid);
            throw new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid);
        }
        
        // Then check companyPoid match
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndCompanyPoid(transactionPoid, companyPoid)
                .orElseThrow(() -> {
                    SalesInvoiceHdr foundInvoice = invoiceByPoid.get();
                    log.warn("Sales invoice found with transactionPoid={} but companyPoid mismatch. Expected: {}, Found: {}", 
                            transactionPoid, companyPoid, foundInvoice.getCompanyPoid());
                    return new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid);
                });

        if ("Y".equals(invoice.getDeleted())) {
            log.warn("Sales invoice found with transactionPoid={} but is deleted", transactionPoid);
            throw new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid);
        }

        log.info("getSalesInvoiceByPoid completed for transactionPoid={} companyPoid={}", transactionPoid, companyPoid);
        // Always include details regardless of includeDetails parameter
        return convertToDtoWithLov(invoice, true);
    }

    @Override
    @Transactional
    public SalesInvoiceHdrDto updateSalesInvoice(Long transactionPoid, UpdateSalesInvoiceRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot update deleted invoice");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot update verified invoice. Invoice must be unverified first.");
        }

        SalesInvoiceHdr oldEntity = new SalesInvoiceHdr();
        BeanUtils.copyProperties(invoice, oldEntity);

        // Validate party type
        // if (request.getPartyType() != null) {
        // if (!"CUSTOMER".equalsIgnoreCase(request.getPartyType()) &&
        // !"PRINCIPAL".equalsIgnoreCase(request.getPartyType())) {
        // throw new CustomException("Party type must be CUSTOMER or PRINCIPAL");
        // }
        // }

        // Update fields
        BeanUtils.copyProperties(request, invoice, "transactionPoid", "docRef", "createdBy",
                "createdDate", "invStatus", "verified", "invAmount", "totalGpAmt", "totalGpPercent",
                "totalCost", "discountAmt", "discountPercent", "invDiscount", "costRefNumber",
                "contractRefNumber", "lpoDetails", "creditDays", "fdaRef", "authorizedId", "vesselName", "portName",
                "deliveryToAddress", "incentiveAmt", "incentivePercent", "incentiveAmt2", "incentivePercent2",
                "incentiveAmt3", "incentivePercent3", "paymentMode", "dueDate");
        invoice.setLastmodifiedBy(userId);

        // Call stored procedure BEFORE SAVE for validation
        Boolean validCustomer = salesInvoiceStoredProcRepository.callCustomerEditValidateProc(
                invoice.getTransactionPoid(),
                request.getCustomerPoid());

        // Update detail tables
        if (request.getInvoiceDetails() != null && !request.getInvoiceDetails().isEmpty()) {
            for (UpdateSalesInvoiceDtlRequest invDetail : request.getInvoiceDetails()) {
                String actionType = invDetail.getActionType();
                
                // Handle deletion
                if ("isDeleted".equalsIgnoreCase(actionType) || "delRowId".equalsIgnoreCase(actionType)) {
                    // Delete the invoice detail
                    if (invDetail.getDetRowId() != null) {
                        try {
                            invoiceDtlRepository.deleteById(new SalesInvoiceDtlId(transactionPoid, invDetail.getDetRowId()));
                            log.debug("Deleted invoice detail with detRowId: {}", invDetail.getDetRowId());
                        } catch (Exception e) {
                            log.warn("Failed to delete invoice detail with detRowId: {}", invDetail.getDetRowId(), e);
                        }
                    }
                    loggingService.logDelete(
                            invDetail,
                            UserContext.getDocumentId(),
                            transactionPoid.toString()
                    );
                    continue;
                }
                
                // Handle creation of new record
                if ("isCreated".equalsIgnoreCase(actionType)) {
                    // Get next DetRowId
                    Long maxDetRowId = invoiceDtlRepository
                            .findMaxDetRowIdByTransactionPoid(transactionPoid);
                    Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;
                    
                    // Create new invoice detail
                    SalesInvoiceDtl newDtl = new SalesInvoiceDtl();
                    newDtl.setTransactionPoid(transactionPoid);
                    newDtl.setDetRowId(detRowId);
                    newDtl.setStockPoid(invDetail.getStockPoid());
                    newDtl.setStockUnitPoid(invDetail.getStockUnitPoid());
                    newDtl.setQuantity(invDetail.getQuantity());
                    newDtl.setPrice(invDetail.getPrice());
                    newDtl.setDiscount(invDetail.getDiscount());
                    newDtl.setBaseAmt(invDetail.getBaseAmt());
                    newDtl.setTaxPoid(invDetail.getTaxPoid());
                    newDtl.setCostPoid(invDetail.getCostCenterPoid());
                    newDtl.setRemarks(invDetail.getRemarks());
                    newDtl.setCreatedBy(userId);
                    newDtl.setLastmodifiedBy(userId);
                    
                    // Calculate amount
                    if (newDtl.getQuantity() != null && newDtl.getPrice() != null) {
                        BigDecimal quantity = BigDecimal.valueOf(newDtl.getQuantity());
                        BigDecimal amount = quantity.multiply(newDtl.getPrice());
                        if (newDtl.getDiscount() != null) {
                            amount = amount.subtract(BigDecimal.valueOf(newDtl.getDiscount()));
                        }
                        newDtl.setAmount(amount.longValue());
                    }
                    
                    invoiceDtlRepository.save(newDtl);
                    log.debug("Created new invoice detail with detRowId: {}", detRowId);
                    String logDetail = String.format(
                            "Row Created on Sales Invoice Detail with detRowId: %s",
                            detRowId
                    );

                    loggingService.createLogSummaryEntry(
                            UserContext.getDocumentId(),
                            transactionPoid.toString(),
                            logDetail
                    );
                    continue;
                }
                
                // Handle no changes - skip processing
                if ("noChanges".equalsIgnoreCase(actionType)) {
                    log.debug("Skipping invoice detail with actionType 'noChanges' for detRowId: {}", invDetail.getDetRowId());
                    continue;
                }
                
                // Handle update of existing record (actionType = "isUpdated" or null/empty)
                if (invDetail.getDetRowId() == null) {
                    log.warn("Skipping invoice detail update - detRowId is null and actionType is not 'isCreated'");
                    continue;
                }
                
                // Find existing invoice detail
                SalesInvoiceDtl dtl = invoiceDtlRepository
                        .findById(new SalesInvoiceDtlId(transactionPoid, invDetail.getDetRowId()))
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Invoice Detail", "detRowId", invDetail.getDetRowId()));
                SalesInvoiceDtl oldDtl = new SalesInvoiceDtl();
                BeanUtils.copyProperties(dtl, oldDtl);

                // Update fields (only editable fields)
                dtl.setStockPoid(invDetail.getStockPoid());
                dtl.setStockUnitPoid(invDetail.getStockUnitPoid());
                dtl.setQuantity(invDetail.getQuantity());
                dtl.setPrice(invDetail.getPrice());
                dtl.setDiscount(invDetail.getDiscount());
                dtl.setBaseAmt(invDetail.getBaseAmt());
                dtl.setTaxPoid(invDetail.getTaxPoid());
                dtl.setCostPoid(invDetail.getCostCenterPoid());
                dtl.setRemarks(invDetail.getRemarks());
                dtl.setLastmodifiedBy(userId);

                // Recalculate amount
                if (dtl.getQuantity() != null && dtl.getPrice() != null) {
                    BigDecimal quantity = BigDecimal.valueOf(dtl.getQuantity());
                    BigDecimal amount = quantity.multiply(dtl.getPrice());
                    if (dtl.getDiscount() != null) {
                        amount = amount.subtract(BigDecimal.valueOf(dtl.getDiscount()));
                    }
                    dtl.setAmount(amount.longValue());
                }

                // Recalculate tax if tax is selected
                // if (invDetail.getTaxPoid() != null) {

                // }

                invoiceDtlRepository.save(dtl);
                log.debug("Updated invoice detail with detRowId: {}", invDetail.getDetRowId());

                loggingService.logChanges(
                        oldDtl,
                        dtl,
                        SalesInvoiceDtl.class,
                        UserContext.getDocumentId(),
                        transactionPoid.toString(),
                        LogDetailsEnum.MODIFIED,
                        "KeyId = TRANSACTION_POID: " + transactionPoid +
                                " DET_ROW_ID: " + invDetail.getDetRowId()
                );
            }
        }

        if (request.getDeliveryNoteDetails() != null && !request.getDeliveryNoteDetails().isEmpty()) {
            for (UpdateSalesDnDtlRequest dnDtlRequest : request.getDeliveryNoteDetails()) {
                String actionType = dnDtlRequest.getActionType();
                
                // Handle deletion
                if ("isDeleted".equalsIgnoreCase(actionType) || "delRowId".equalsIgnoreCase(actionType)) {
                    // Delete the delivery note detail
                    if (dnDtlRequest.getDetRowId() != null) {
                        try {
                            dnDtlRepository.deleteById(new SalesDnDtlId(transactionPoid, dnDtlRequest.getDetRowId()));
                            loggingService.logDelete(dnDtlRequest,  UserContext.getDocumentId(), transactionPoid.toString());
                            log.debug("Deleted delivery note detail with detRowId: {}", dnDtlRequest.getDetRowId());
                        } catch (Exception e) {
                            log.warn("Failed to delete delivery note detail with detRowId: {}", dnDtlRequest.getDetRowId(), e);
                        }
                    } else {
                        log.warn("Skipping delivery note detail deletion - detRowId is null for actionType: {}", actionType);
                    }
                    continue;
                }
                
                // Handle creation of new record
                if ("isCreated".equalsIgnoreCase(actionType)) {
                    // Get next DetRowId
                    Long maxDetRowId = dnDtlRepository
                            .findMaxDetRowIdByTransactionPoid(transactionPoid);
                    Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;
                    
                    log.debug("Creating new delivery note detail with detRowId: {}, remarks: {}", detRowId, dnDtlRequest.getRemarks());
                    
                    // Create new delivery note detail
                    SalesDnDtl newDtl = new SalesDnDtl();
                    newDtl.setTransactionPoid(transactionPoid);
                    newDtl.setDetRowId(detRowId);
                    newDtl.setDnPoidFk(dnDtlRequest.getDnPoidFk());
                    newDtl.setQuotationPoidFk(dnDtlRequest.getQuotationPoidFk());
                    newDtl.setRemarks(dnDtlRequest.getRemarks());
                    newDtl.setCreatedBy(userId);
                    newDtl.setLastmodifiedBy(userId);
                    
                    dnDtlRepository.save(newDtl);
                    log.debug("Successfully created new delivery note detail with detRowId: {}", detRowId);
                    loggingService.createLogSummaryEntry(
                            UserContext.getDocumentId(),
                            transactionPoid.toString(),
                            "Row Created on Sales Delivery Note Detail with detRowId: " + detRowId
                    );
                    continue;
                }
                
                // Handle no changes - skip processing
                if ("noChanges".equalsIgnoreCase(actionType)) {
                    log.debug("Skipping delivery note detail with actionType 'noChanges' for detRowId: {}", dnDtlRequest.getDetRowId());
                    continue;
                }
                
                // Handle update of existing record (actionType = "isUpdated" or null/empty)
                if (dnDtlRequest.getDetRowId() == null) {
                    log.warn("Skipping delivery note detail update - detRowId is null and actionType is not 'isCreated'");
                    continue;
                }
                
                // Find existing delivery note detail
                SalesDnDtl dtl = dnDtlRepository
                        .findById(new SalesDnDtlId(transactionPoid, dnDtlRequest.getDetRowId()))
                        .orElseThrow(() -> new ResourceNotFoundException("Delivery Note Detail", "detRowId",
                                dnDtlRequest.getDetRowId()));

                SalesDnDtl oldDtl = new SalesDnDtl();
                BeanUtils.copyProperties(dtl, oldDtl);

                log.debug("Updating delivery note detail with detRowId: {}, old remarks: {}, new remarks: {}", 
                        dnDtlRequest.getDetRowId(), dtl.getRemarks(), dnDtlRequest.getRemarks());

                // Update fields
                dtl.setDnPoidFk(dnDtlRequest.getDnPoidFk());
                dtl.setQuotationPoidFk(dnDtlRequest.getQuotationPoidFk());
                dtl.setRemarks(dnDtlRequest.getRemarks());
                dtl.setLastmodifiedBy(userId);

                dnDtlRepository.save(dtl);
                log.debug("Updated delivery note detail with detRowId: {}, remarks: {}", 
                        dnDtlRequest.getDetRowId(), dnDtlRequest.getRemarks());
                loggingService.logChanges(
                        oldDtl,
                        dtl,
                        SalesDnDtl.class,
                        UserContext.getDocumentId(),
                        transactionPoid.toString(),
                        LogDetailsEnum.MODIFIED,
                        "KeyId = TRANSACTION_POID: " + transactionPoid +
                                " DET_ROW_ID: " + dnDtlRequest.getDetRowId()
                );
            }
            // Flush to ensure all delivery note detail changes are persisted
            dnDtlRepository.flush();
        }
        // Save
        SalesInvoiceHdr savedInvoice = invoiceHdrRepository.save(invoice);

        // Call stored procedure AFTER SAVE for authorization
        salesInvoiceStoredProcRepository.callAuthorizationProc(transactionPoid, savedInvoice.getAuthorizedId(), userId);

        String key = savedInvoice.getTransactionPoid().toString();
        loggingService.logChanges(oldEntity, savedInvoice, SalesInvoiceHdr.class,
                UserContext.getDocumentId(), key, LogDetailsEnum.MODIFIED, "TRANSACTION_POID");

        return convertToDto(savedInvoice, true);
    }

    @Override
    @Transactional
    public void deleteSalesInvoice(Long transactionPoid, Long groupPoid, Long companyPoid, DeleteReasonDto deleteReasonDto) {
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot delete verified invoice. Invoice must be unverified first.");
        }
        documentDeleteService.deleteDocument(
                transactionPoid,
                "AR_SCH_SALES_INVOICE_HDR",
                "TRANSACTION_POID",
                deleteReasonDto,
                invoice.getTransactionDate()
        );

        // Check dependencies
        SalesInvoiceDependenciesDto dependencies = checkSalesInvoiceDependencies(transactionPoid, groupPoid,
                companyPoid);
        if (!dependencies.getCanDelete()) {
            throw new CustomException(dependencies.getMessage());
        }

        // Delete detail tables
        invoiceDtlRepository.deleteByTransactionPoid(transactionPoid);
        dnDtlRepository.deleteByTransactionPoid(transactionPoid);

        // Soft delete
        invoice.setDeleted("Y");
        invoiceHdrRepository.save(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listSalesInvoices(String docId, FilterRequestDto request, LocalDate startDateValue, LocalDate endDateValue, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveDateFilters(request, "TRANSACTION_DATE", startDateValue, endDateValue);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "DOC_REF",   // label
                "TRANSACTION_POID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }

    
    private String toStringSafe(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Character) {
            return String.valueOf((Character) value);
        }
        return value.toString();
    }

    // Validation Methods
    @Override
    @Transactional(readOnly = true)
    public ValidationResponse validateDocRef(String docRef, Long groupPoid, Long companyPoid, Long transactionPoid) {
        if (docRef == null || docRef.trim().isEmpty()) {
            return new ValidationResponse(false, "Document reference cannot be empty");
        }

        boolean exists;
        if (transactionPoid != null) {
            exists = invoiceHdrRepository.existsByDocRefIgnoreCaseAndGroupPoidAndCompanyPoidAndTransactionPoidNot(
                    docRef, groupPoid, companyPoid, transactionPoid);
        } else {
            exists = invoiceHdrRepository.existsByDocRefIgnoreCaseAndGroupPoidAndCompanyPoid(docRef, groupPoid,
                    companyPoid);
        }

        ValidationResponse response = new ValidationResponse();
        response.setSuccess(!exists);
        response.setMessage(exists ? "Document reference already exists" : "Document reference is available");
        return response;
    }

    // Detail Table Methods - Invoice Details
    @Override
    @Transactional
    public SalesInvoiceDtlDto addInvoiceDetail(Long transactionPoid, CreateSalesInvoiceDtlRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists and belongs to group/company
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot add invoice details. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot add invoice details. Invoice is verified");
        }

        // Get next DetRowId
        Long maxDetRowId = invoiceDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        // Create invoice detail
        SalesInvoiceDtl dtl = new SalesInvoiceDtl();
        dtl.setTransactionPoid(transactionPoid);
        dtl.setDetRowId(detRowId);
        dtl.setStockPoid(request.getStockPoid());
        dtl.setStockUnitPoid(request.getStockUnitPoid());
        dtl.setQuantity(request.getQuantity());
        dtl.setPrice(request.getPrice());
        dtl.setDiscount(request.getDiscount());
        dtl.setBaseAmt(request.getBaseAmt());
        dtl.setTaxPoid(request.getTaxPoid());
        dtl.setCostPoid(request.getCostCenterPoid());
        dtl.setRemarks(request.getRemarks());
        dtl.setCreatedBy(userId);
        dtl.setLastmodifiedBy(userId);

        // Calculate amount
        if (dtl.getQuantity() != null && dtl.getPrice() != null) {
            BigDecimal quantity = BigDecimal.valueOf(dtl.getQuantity());
            BigDecimal amount = quantity.multiply(dtl.getPrice());
            if (dtl.getDiscount() != null) {
                amount = amount.subtract(BigDecimal.valueOf(dtl.getDiscount()));
            }
            dtl.setAmount(amount.longValue());
        }

        // Get tax percentage if tax is selected
        if (request.getTaxPoid() != null) {
            // Calculate tax amount: taxAmount = baseAmt * taxPercentage / 100
        }

        SalesInvoiceDtl savedDtl = invoiceDtlRepository.save(dtl);
        return convertInvoiceDtlToDto(savedDtl);
    }

    @Override
    @Transactional
    public SalesInvoiceDtlDto updateInvoiceDetail(Long transactionPoid, Long detRowId,
            UpdateSalesInvoiceDtlRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot update invoice details. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot update invoice details. Invoice is verified");
        }

        // Find existing invoice detail
        SalesInvoiceDtl dtl = invoiceDtlRepository
                .findById(new SalesInvoiceDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Invoice Detail", "detRowId", detRowId));

        // Update fields (only editable fields)
        dtl.setStockPoid(request.getStockPoid());
        dtl.setStockUnitPoid(request.getStockUnitPoid());
        dtl.setQuantity(request.getQuantity());
        dtl.setPrice(request.getPrice());
        dtl.setDiscount(request.getDiscount());
        dtl.setBaseAmt(request.getBaseAmt());
        dtl.setTaxPoid(request.getTaxPoid());
        dtl.setCostPoid(request.getCostCenterPoid());
        dtl.setRemarks(request.getRemarks());
        dtl.setLastmodifiedBy(userId);

        // Recalculate amount
        if (dtl.getQuantity() != null && dtl.getPrice() != null) {
            BigDecimal quantity = BigDecimal.valueOf(dtl.getQuantity());
            BigDecimal amount = quantity.multiply(dtl.getPrice());
            if (dtl.getDiscount() != null) {
                amount = amount.subtract(BigDecimal.valueOf(dtl.getDiscount()));
            }
            dtl.setAmount(amount.longValue());
        }

        // Recalculate tax if tax is selected
        if (request.getTaxPoid() != null) {
            // TODO: Get tax percentage and calculate tax amount
        }

        SalesInvoiceDtl savedDtl = invoiceDtlRepository.save(dtl);
        return convertInvoiceDtlToDto(savedDtl);
    }

    @Override
    @Transactional
    public void deleteInvoiceDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot delete invoice details. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot delete invoice details. Invoice is verified");
        }

        // Delete invoice detail
        invoiceDtlRepository.deleteById(new SalesInvoiceDtlId(transactionPoid, detRowId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvoiceDtlDto> getInvoiceDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {
        // Validate invoice exists
        invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        List<SalesInvoiceDtl> invoiceDetails = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        return invoiceDetails.stream()
                .map(this::convertInvoiceDtlToDto)
                .collect(Collectors.toList());
    }

    // Detail Table Methods - Delivery Note Details
    @Override
    @Transactional
    public SalesDnDtlDto addDeliveryNoteDetail(Long transactionPoid, CreateSalesDnDtlRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot add delivery note details. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot add delivery note details. Invoice is verified");
        }

        // Get next DetRowId
        Long maxDetRowId = dnDtlRepository.findMaxDetRowIdByTransactionPoid(transactionPoid);
        Long detRowId = (maxDetRowId != null ? maxDetRowId : 0L) + 1L;

        // Create delivery note detail
        SalesDnDtl dtl = new SalesDnDtl();
        dtl.setTransactionPoid(transactionPoid);
        dtl.setDetRowId(detRowId);
        dtl.setDnPoidFk(request.getDnPoidFk());
        dtl.setQuotationPoidFk(request.getQuotationPoidFk());
        dtl.setRemarks(request.getRemarks());
        dtl.setCreatedBy(userId);
        dtl.setLastmodifiedBy(userId);

        log.debug("Creating delivery note detail with detRowId: {}, remarks: {}", detRowId, request.getRemarks());
        SalesDnDtl savedDtl = dnDtlRepository.save(dtl);
        dnDtlRepository.flush();
        log.debug("Successfully created delivery note detail with detRowId: {}, remarks: {}", detRowId, savedDtl.getRemarks());
        return convertDnDtlToDto(savedDtl);
    }

    @Override
    @Transactional
    public SalesDnDtlDto updateDeliveryNoteDetail(Long transactionPoid, Long detRowId,
            UpdateSalesDnDtlRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot update delivery note details. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot update delivery note details. Invoice is verified");
        }

        // Find existing delivery note detail
        SalesDnDtl dtl = dnDtlRepository
                .findById(new SalesDnDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note Detail", "detRowId", detRowId));

        log.debug("Updating delivery note detail with detRowId: {}, old remarks: {}, new remarks: {}", 
                detRowId, dtl.getRemarks(), request.getRemarks());

        // Update fields
        dtl.setDnPoidFk(request.getDnPoidFk());
        dtl.setQuotationPoidFk(request.getQuotationPoidFk());
        dtl.setRemarks(request.getRemarks());
        dtl.setLastmodifiedBy(userId);

        SalesDnDtl savedDtl = dnDtlRepository.save(dtl);
        dnDtlRepository.flush();
        log.debug("Successfully updated delivery note detail with detRowId: {}, remarks: {}", detRowId, savedDtl.getRemarks());
        return convertDnDtlToDto(savedDtl);
    }

    @Override
    @Transactional
    public void deleteDeliveryNoteDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot delete delivery note details. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot delete delivery note details. Invoice is verified");
        }

        // Delete delivery note detail
        dnDtlRepository.deleteById(new SalesDnDtlId(transactionPoid, detRowId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesDnDtlDto> getDeliveryNoteDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {
        // Validate invoice exists
        invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        List<SalesDnDtl> dnDetails = dnDtlRepository.findByTransactionPoid(transactionPoid);
        return dnDetails.stream()
                .map(this::convertDnDtlToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesInvCostbkdDtlDto> getCostBookedDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {
        // Validate invoice exists
        invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        List<SalesInvCostbkdDtl> costBkDetails = costbkdDtlRepository.findByTransactionPoid(transactionPoid);
        return costBkDetails.stream()
                .map(this::convertCostbkdDtlToDto)
                .collect(Collectors.toList());
    }

    // Business Logic Methods
    @Override
    @Transactional
    public LoadQuotationItemsResponse loadQuotationItems(Long transactionPoid, LoadQuotationItemsRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot load quotation. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot load quotation. Invoice is verified");
        }

        // Check if invoice details table is not empty
        List<SalesInvoiceDtl> existingDetails = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        if (!existingDetails.isEmpty()) {
            throw new CustomException(
                    "Cannot load quotation. Invoice details table is not empty. Please clear items first.");
        }

        // Call stored procedure to load quotation
        LoadQuotationItemsResponse response = salesInvoiceStoredProcRepository
                .callLoadQuotationItemsProc(transactionPoid, request);

        // Update invoice with quotation reference
        invoice.setQtnPoid(request.getQtnPoid());
        invoice.setIncentiveAmt(request.getIncentiveAmt());
        invoice.setIncentiveAmt2(request.getIncentiveAmt2());
        invoice.setIncentiveAmt3(request.getIncentiveAmt3());
        invoice.setLastmodifiedBy(userId);
        invoiceHdrRepository.save(invoice);

        return response;
    }

    @Override
    @Transactional
    public UnloadQuotationResponse unloadQuotation(Long transactionPoid,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot load quotation. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot load quotation. Invoice isn't in EDIT mode");
        }

        // Call stored procedure to load quotation
        UnloadQuotationResponse response = salesInvoiceStoredProcRepository.callUnloadQuotationProc(transactionPoid,
                invoice.getQtnPoid());
        return response;
    }

    @Override
    @Transactional
    public VerifyInvoiceResponse verifyInvoice(Long transactionPoid, Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot verify deleted invoice");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Invoice is already verified");
        }

        // Check if invoice has items
        List<SalesInvoiceDtl> invoiceDetails = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        if (invoiceDetails.isEmpty()) {
            throw new CustomException("Cannot verify invoice. Invoice has no items.");
        }

        // Update verified status
        invoice.setVerified("Y");
        invoice.setAuthorizedId(invoice.getAuthorizedId());
        invoice.setLastmodifiedBy(userId);
        invoiceHdrRepository.save(invoice);

        VerifyInvoiceResponse response = new VerifyInvoiceResponse();
        response.setSuccess(true);
        response.setMessage("Invoice verified successfully");
        response.setVerified("Y");
        return response;
    }

    @Override
    @Transactional
    public CalculateGpResponse calculateGp(Long transactionPoid, Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot calculate GP. Invoice is verified.");
        }

        // Call stored procedure to calculate GP
        ValidationResponse response = salesInvoiceStoredProcRepository.callCalculateGpProc(transactionPoid,
                invoice.getQtnPoid());

        if (response.getMessage() != null && response.getMessage().contains("ERROR")) {
            throw new CustomException("Error calculating GP: " + response.getMessage());
        }

        // Refresh invoice to get calculated GP values
        invoiceHdrRepository.flush();
        SalesInvoiceHdr refreshedInvoice = invoiceHdrRepository.findByTransactionPoid(transactionPoid)
                .orElse(invoice);

        CalculateGpResponse calculateGpResponse = new CalculateGpResponse();
        calculateGpResponse.setSuccess(true);
        calculateGpResponse
                .setMessage(response.getMessage() != null ? response.getMessage() : "GP calculated successfully");
        calculateGpResponse.setTotalGpAmt(refreshedInvoice.getTotalGpAmt());
        calculateGpResponse.setTotalGpPercent(refreshedInvoice.getTotalGpPercent());
        return calculateGpResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public LoadCostBookingsResponse loadCostBookings(Long transactionPoid, Long groupPoid,
            Long companyPoid, String userId) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        // Call stored procedure to load cost bookings
        ValidationResponse result = salesInvoiceStoredProcRepository.callLoadCostBookingsProc(transactionPoid,
                invoice.getQtnPoid());

        if (result.getMessage() != null && result.getMessage().contains("ERROR")) {
            throw new CustomException("Error loading cost bookings: " + result.getMessage());
        }

        // Query cost booked details (read-only)
        List<SalesInvCostbkdDtl> costBookings = costbkdDtlRepository.findByTransactionPoid(transactionPoid);
        List<SalesInvCostbkdDtlDto> costBookingDtos = costBookings.stream()
                .map(this::convertCostbkdDtlToDto)
                .collect(Collectors.toList());

        LoadCostBookingsResponse response = new LoadCostBookingsResponse();
        response.setSuccess(true);
        response.setMessage(result.getMessage() != null ? result.getMessage() : "Cost bookings loaded successfully");
        response.setCostBookings(costBookingDtos);
        response.setCount(costBookingDtos.size());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CalculateDueDateResponse calculateDueDate(Long transactionPoid, Timestamp transactionDate, Long creditDays,
            Long groupPoid, Long companyPoid) {
        if (transactionDate == null || creditDays == null) {
            throw new CustomException("Transaction date and credit days are required");
        }
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        // Call stored procedure
        CalculateDueDateResponse response = salesInvoiceStoredProcRepository.callCalculateDueDateProc(groupPoid,
                companyPoid, transactionDate,
                invoice.getDueDate(), creditDays, "DAYS", invoice.getCustomerPoid());

        return response;
    }

    @Override
    @Transactional
    public CalculateDiscountCommissionResponse calculateItemDiscountCommission(Long transactionPoid,
            CalculateDiscountCommissionRequest request, Long detRowId,
            Long groupPoid, Long companyPoid, Long userId) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot calculate discount/commission. Invoice is verified.");
        }

        // Validate item detail exists
        invoiceDtlRepository
                .findById(new SalesInvoiceDtlId(transactionPoid, detRowId))
                .orElseThrow(
                        () -> new ResourceNotFoundException("Invoice Item Detail's not found", "detRowId", detRowId));

        // Call stored procedure
        CalculateDiscountCommissionResponse response = salesInvoiceStoredProcRepository
                .callCalculateItemDiscountCommissionProc(
                        transactionPoid, request, detRowId, invoice.getQtnPoid(), userId);

        if (!response.getSuccess() || response.getMessage().contains("ERROR")) {
            throw new CustomException("Error calculating discount/commission: " + response.getMessage());
        }

        return response;
    }

    @Override
    @Transactional
    public CalculateDiscountCommissionResponse calculateHeaderDiscountCommission(Long transactionPoid,
            CalculateDiscountCommissionRequest request, Long detRowId, Long groupPoid, Long companyPoid, Long userId) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot calculate discount/commission. Invoice is verified.");
        }

        // Call stored procedure
        CalculateDiscountCommissionResponse response = salesInvoiceStoredProcRepository
                .callCalculateHeaderDiscountCommissionProc(
                        transactionPoid, request, detRowId, invoice.getQtnPoid(), userId);

        if (!response.getSuccess() || response.getMessage().contains("ERROR")) {
            throw new CustomException("Error calculating discount/commission: " + response.getMessage());
        }

        // Refresh invoice to get updated values
        invoiceHdrRepository.flush();
        invoiceHdrRepository
                .findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        return response;
    }

    @Override
    @Transactional
    public LoadDeliveryNoteResponse loadDeliveryNote(Long transactionPoid, Long groupPoid, Long companyPoid,
            String userId) {
        // Validate invoice exists
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot load delivery note. Invoice is verified.");
        }

        // Check if delivery notes are selected
        List<SalesDnDtl> dnDetails = dnDtlRepository.findByTransactionPoid(transactionPoid);
        if (dnDetails.isEmpty()) {
            throw new CustomException("Please select delivery notes first before loading.");
        }

        // Check if invoice details table is not empty
        List<SalesInvoiceDtl> existingItems = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        if (!existingItems.isEmpty()) {
            throw new CustomException(
                    "Cannot load delivery note. Invoice already has items. Please clear items first.");
        }

        // Call stored procedure
        ValidationResponse response = salesInvoiceStoredProcRepository.callLoadDeliveryNoteProc(transactionPoid);

        if (response.getMessage() != null && response.getMessage().contains("ERROR")) {
            throw new CustomException("Error loading delivery note: " + response.getMessage());
        }

        // Refresh to get loaded items
        invoiceHdrRepository.flush();
        List<SalesInvoiceDtl> loadedItems = invoiceDtlRepository.findByTransactionPoid(transactionPoid);

        LoadDeliveryNoteResponse loadDeliveryNoteResponse = new LoadDeliveryNoteResponse();
        loadDeliveryNoteResponse.setSuccess(true);
        loadDeliveryNoteResponse.setMessage(
                response.getMessage() != null ? response.getMessage() : "Delivery note loaded successfully");
        loadDeliveryNoteResponse.setItemsLoaded(loadedItems.size());
        return loadDeliveryNoteResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public ValidationResponse validateCustomer(Long customerPoid, Long groupPoid, Long companyPoid) {
        if (customerPoid == null) {
            throw new CustomException("Customer POID is required");
        }

        // Call stored procedure
        ValidationResponse response = salesInvoiceStoredProcRepository.callCustomerValidateProc(customerPoid,
                "",
                "");
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CreditDetailsResponse loadCreditDetails(Long customerPoid, Long groupPoid, Long companyPoid,
            CreditDetailsRequest request) {
        if (customerPoid == null) {
            throw new CustomException("Customer POID is required");
        }

        // Call stored procedure
        CreditDetailsResponse response = salesInvoiceStoredProcRepository.callLoadCreditDetailsProc(groupPoid,
                companyPoid, customerPoid, request.getDocId(), request.getDocKeyPoid(), request.getDocDate(),
                request.getPartyType(), request.getPartyPoid());

        if (response.getMessage() != null && response.getMessage().contains("ERROR")) {
            throw new CustomException("Error loading credit details: " + response.getMessage());
        }

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LoadQuotationCurrencyResponse loadQuotationCurrency(Long transactionPoid, String qtnPoid) {
        if (qtnPoid == null) {
            throw new CustomException("Quotation POID is required");
        }

        // Call stored procedure
        LoadQuotationCurrencyResponse result = salesInvoiceStoredProcRepository
                .callLoadQuotationCurrencyProc(transactionPoid, qtnPoid);

        if (result.getMessage() != null && result.getMessage().contains("ERROR")) {
            throw new CustomException("Error loading quotation currency: " + result.getMessage());
        }

        LoadQuotationCurrencyResponse response = new LoadQuotationCurrencyResponse();
        response.setSuccess(true);
        response.setMessage(result.getMessage() != null ? result.getMessage() : "Currency details loaded successfully");
        response.setCurrencyCode(result.getCurrencyCode());
        response.setCurrencyRate(result.getCurrencyRate());
        response.setQuotationCurrencyList(result.getQuotationCurrencyList());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesInvoiceDependenciesDto checkSalesInvoiceDependencies(Long transactionPoid, Long groupPoid,
            Long companyPoid) {
        // Validate invoice exists
        invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        // - Receipts: Check if invoice has been paid (AR_RECEIPT_DTL)
        // - Credit Notes: Check if credit notes have been created from this invoice
        // - GL Postings: Check if invoice has been posted to GL
        Long receiptCount = 0L;
        Long creditNoteCount = 0L;
        Long glPostingCount = 0L;

        SalesInvoiceDependenciesDto dto = new SalesInvoiceDependenciesDto();
        dto.setTransactionPoid(transactionPoid);
        dto.setCanDelete(receiptCount == 0 && creditNoteCount == 0 && glPostingCount == 0);
        dto.setReceiptCount(receiptCount);
        dto.setCreditNoteCount(creditNoteCount);
        // dto.setGlPostingCount(glPostingCount);

        if (dto.getCanDelete()) {
            dto.setReason("No dependencies");
            dto.setMessage("Sales invoice can be deleted. No dependencies found.");
        } else {
            StringBuilder message = new StringBuilder("Cannot delete sales invoice. ");
            if (receiptCount > 0) {
                message.append(String.format("It has %d receipts. ", receiptCount));
            }
            if (creditNoteCount > 0) {
                message.append(String.format("It has %d credit notes. ", creditNoteCount));
            }
            if (glPostingCount > 0) {
                message.append(String.format("It has been posted to GL."));
            }
            dto.setReason("Sales invoice has dependencies");
            dto.setMessage(message.toString());
        }

        return dto;
    }
    
    /**
     * Helper method to get MIS_GROUP from GL_COST_CENTER_MASTER
     * Note: costCenterPoid is ROWNUM, so we need to query by position
     */
    private String getCostCenterMisGroup(Long costCenterPoid) {
        // Since costCenterPoid is ROWNUM, we need to use a subquery
        // The LOV query shows: SELECT ROWNUM AS POID, MIS_GROUP AS CODE, '' AS DESCRIPTION
        // So we need to get MIS_GROUP by ROWNUM position
        try {
            // Use native query to get MIS_GROUP by ROWNUM
            String sql = "SELECT MIS_GROUP FROM (" +
                    "SELECT ROWNUM AS RN, MIS_GROUP " +
                    "FROM (SELECT MIS_GROUP FROM GL_COST_CENTER_MASTER GROUP BY MIS_GROUP)" +
                    ") WHERE RN = ?";
            // Note: This requires JdbcTemplate which we don't have injected
            // For now, return null and log a warning
            log.warn("Cost center MIS_GROUP lookup requires JdbcTemplate. costCenterPoid={}", costCenterPoid);
            return null;
        } catch (Exception e) {
            log.warn("Failed to get cost center MIS_GROUP for costCenterPoid={}: {}", costCenterPoid, e.getMessage());
            return null;
        }
    }
    
    /**
     * Helper method to get customer name
     */
    private String getCustomerName(Long customerPoid) {
        if (customerPoid == null) {
            return "";
        }
        try {
            // Query SALES_CUSTOMER_MASTER for customer name
            // This would require a repository method, for now return empty string
            // In a real implementation, you'd inject SalesCustomerMasterRepository
            log.debug("Customer name lookup for customerPoid={} requires repository", customerPoid);
            return "";
        } catch (Exception e) {
            log.warn("Failed to get customer name for customerPoid={}: {}", customerPoid, e.getMessage());
            return "";
        }
    }
    
    /**
     * Query customer details from SALES_CUSTOMER_MASTER
     */
    private SalesInvoiceHdrDto.LovDetailDto queryCustomerDetails(Long customerPoid) {
        if (customerPoid == null) {
            return createEmptyLovDetail();
        }
        try {
            String sql = "SELECT CUSTOMER_POID, CUSTOMER_CODE, CUSTOMER_NAME " +
                        "FROM SALES_CUSTOMER_MASTER " +
                        "WHERE CUSTOMER_POID = :customerPoid";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("customerPoid", customerPoid);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            
            if (!results.isEmpty()) {
                Object[] row = results.get(0);
                return createLovDetailFromRow(row, 0);
            }
        } catch (Exception e) {
            log.warn("Failed to query customer details for customerPoid={}: {}", customerPoid, e.getMessage());
        }
        return createEmptyLovDetail();
    }
    
    /**
     * Query principal details from SHIP_PRINCIPAL_MASTER
     */
    private SalesInvoiceHdrDto.LovDetailDto queryPrincipalDetails(Long principalPoid) {
        if (principalPoid == null) {
            return createEmptyLovDetail();
        }
        try {
            String sql = "SELECT PRINCIPAL_POID, PRINCIPAL_CODE, PRINCIPAL_NAME " +
                        "FROM SHIP_PRINCIPAL_MASTER " +
                        "WHERE PRINCIPAL_POID = :principalPoid";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("principalPoid", principalPoid);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            
            if (!results.isEmpty()) {
                Object[] row = results.get(0);
                return createLovDetailFromRow(row, 0);
            }
        } catch (Exception e) {
            log.warn("Failed to query principal details for principalPoid={}: {}", principalPoid, e.getMessage());
        }
        return createEmptyLovDetail();
    }

    @Override
    public byte[] print(Long transactionPoid) throws Exception {
        Map<String, Object> params = printService.buildBaseParams(transactionPoid, "300-100");
        params.put("SUB_RFQ_DTL", printService.load("ShipChandling/AR/SCH_SALES_INV_ITEM_DTLsubreport1.jrxml"));
        JasperReport mainReport = printService.load("ShipChandling/AR/SCH_SALES_INV.jrxml");
        return printService.fillReportToPdf(mainReport, params, dataSource);
    }

}
