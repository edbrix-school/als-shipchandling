package com.alsharif.shipchandling.salesinvoice.service;

import com.alsharif.shipchandling.salesinvoice.dto.*;
import com.alsharif.shipchandling.salesinvoice.dto.request.CalculateDiscountCommissionRequest;
import com.alsharif.shipchandling.salesinvoice.dto.request.CreateSalesDnDtlRequest;
import com.alsharif.shipchandling.salesinvoice.dto.request.CreateSalesInvoiceDtlRequest;
import com.alsharif.shipchandling.salesinvoice.dto.request.CreateSalesInvoiceRequest;
import com.alsharif.shipchandling.salesinvoice.dto.request.CreditDetailsRequest;
import com.alsharif.shipchandling.salesinvoice.dto.request.LoadQuotationItemsRequest;
import com.alsharif.shipchandling.salesinvoice.dto.request.UpdateSalesDnDtlRequest;
import com.alsharif.shipchandling.salesinvoice.dto.request.UpdateSalesInvoiceDtlRequest;
import com.alsharif.shipchandling.salesinvoice.dto.request.UpdateSalesInvoiceRequest;
import com.alsharif.shipchandling.salesinvoice.dto.response.CalculateDiscountCommissionResponse;
import com.alsharif.shipchandling.salesinvoice.dto.response.CalculateDueDateResponse;
import com.alsharif.shipchandling.salesinvoice.dto.response.CalculateGpResponse;
import com.alsharif.shipchandling.salesinvoice.dto.response.CreditDetailsResponse;
import com.alsharif.shipchandling.salesinvoice.dto.response.LoadCostBookingsResponse;
import com.alsharif.shipchandling.salesinvoice.dto.response.LoadDeliveryNoteResponse;
import com.alsharif.shipchandling.salesinvoice.dto.response.LoadQuotationCurrencyResponse;
import com.alsharif.shipchandling.salesinvoice.dto.response.LoadQuotationItemsResponse;
import com.alsharif.shipchandling.salesinvoice.dto.response.UnloadQuotationResponse;
import com.alsharif.shipchandling.salesinvoice.dto.response.ValidationResponse;
import com.alsharif.shipchandling.salesinvoice.dto.response.VerifyInvoiceResponse;
import com.alsharif.shipchandling.salesinvoice.entity.*;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import com.alsharif.shipchandling.exceptions.CustomException;
import com.alsharif.shipchandling.salesinvoice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
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
        BeanUtils.copyProperties(request, invoice);
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
                }
            }

            if (request.getDeliveryNoteDetails() != null && !request.getDeliveryNoteDetails().isEmpty()) {
                for (CreateSalesDnDtlRequest dnDtl : request.getDeliveryNoteDetails()) {

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
                    dtl.setRemarks(request.getRemarks());
                    dtl.setCreatedBy(userId);
                    dtl.setLastmodifiedBy(userId);

                    SalesDnDtl savedDtl = dnDtlRepository.save(dtl);
                }
            }

            // Call stored procedure AFTER SAVE for authorization
            salesInvoiceStoredProcRepository.callAuthorizationProc(savedInvoice.getTransactionPoid(),
                    savedInvoice.getAuthorizedId(), userId);

            // Refresh to get auto-generated DocRef
            invoiceHdrRepository.flush();
        }
        SalesInvoiceHdr refreshedInvoice = invoiceHdrRepository.findByTransactionPoid(
                savedInvoice.getTransactionPoid()).orElse(savedInvoice);

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
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesInvoiceHdrDto getSalesInvoiceByPoid(Long transactionPoid, Long groupPoid,
            Long companyPoid, Boolean includeDetails) {
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid);
        }

        return convertToDto(invoice, includeDetails != null && includeDetails);
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
        for (UpdateSalesInvoiceDtlRequest invDetail : request.getInvoiceDetails()) {

            // Find existing invoice detail
            SalesInvoiceDtl dtl = invoiceDtlRepository
                    .findById(new SalesInvoiceDtlId(transactionPoid, invDetail.getDetRowId()))
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Invoice Detail", "detRowId", invDetail.getDetRowId()));

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
        }

        for (UpdateSalesDnDtlRequest dnDtlRquest : request.getDeliveryNoteDetails()) {
            // Find existing delivery note detail
            SalesDnDtl dtl = dnDtlRepository
                    .findById(new SalesDnDtlId(transactionPoid, dnDtlRquest.getDetRowId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Delivery Note Detail", "detRowId",
                            dnDtlRquest.getDetRowId()));

            // Update fields
            dtl.setDnPoidFk(dnDtlRquest.getDnPoidFk());
            dtl.setQuotationPoidFk(dnDtlRquest.getQuotationPoidFk());
            dtl.setRemarks(dnDtlRquest.getRemarks());
            dtl.setLastmodifiedBy(userId);

            dnDtlRepository.save(dtl);
        }
        // Save
        SalesInvoiceHdr savedInvoice = invoiceHdrRepository.save(invoice);

        // Call stored procedure AFTER SAVE for authorization
        salesInvoiceStoredProcRepository.callAuthorizationProc(transactionPoid, savedInvoice.getAuthorizedId(), userId);

        return convertToDto(savedInvoice, true);
    }

    @Override
    @Transactional
    public void deleteSalesInvoice(Long transactionPoid, Long groupPoid, Long companyPoid) {
        SalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot delete verified invoice. Invoice must be unverified first.");
        }

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
    public PaginatedResponse<SalesInvoiceHdrDto> getAllSalesInvoices(Long groupPoid, Long companyPoid,
            String invStatus, String verified,
            Long customerPoid, Long principalPoid,
            String qtnPoid, String search,
            Timestamp fromDate, Timestamp toDate,
            Integer page, Integer size) {
        log.info("getAllSalesInvoices service started for groupPoid={} companyPoid={} page={} size={}",
                groupPoid, companyPoid, page, size);

        // Set default values for pagination
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 10; // Default page size is 10

        // Create Pageable with sorting by transaction date descending, then docRef
        // ascending
        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                Sort.by("transactionDate").descending().and(Sort.by("docRef").ascending()));

        // Use the repository implementation method with filters and customer name
        Page<Object[]> invoicesPage = invoiceHdrRepositoryImpl.findAllWithFiltersAndCustomerName(
                groupPoid, companyPoid, invStatus, verified, customerPoid, principalPoid,
                qtnPoid, fromDate, toDate, search, pageable);

        // Convert to DTOs - Object[] contains [SalesInvoiceHdr, customerName]
        List<SalesInvoiceHdrDto> data = invoicesPage.getContent().stream()
                .map(result -> {
                    SalesInvoiceHdr entity = (SalesInvoiceHdr) result[0];
                    String customerName = result[1] != null ? result[1].toString() : null;
                    SalesInvoiceHdrDto dto = convertToDto(entity, false);
                    dto.setCustomerName(customerName);
                    return dto;
                })
                .collect(Collectors.toList());

        // Create paginated response
        PaginatedResponse<SalesInvoiceHdrDto> response = new PaginatedResponse<>();
        response.setData(data);
        response.setPage(invoicesPage.getNumber());
        response.setSize(invoicesPage.getSize());
        response.setTotalElements(invoicesPage.getTotalElements());
        response.setTotalPages(invoicesPage.getTotalPages());
        response.setFirst(invoicesPage.isFirst());
        response.setLast(invoicesPage.isLast());

        log.info("getAllSalesInvoices completed for groupPoid={} companyPoid={} totalElements={}",
                groupPoid, companyPoid, response.getTotalElements());
        return response;
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

        SalesDnDtl savedDtl = dnDtlRepository.save(dtl);
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

        // Update fields
        dtl.setDnPoidFk(request.getDnPoidFk());
        dtl.setQuotationPoidFk(request.getQuotationPoidFk());
        dtl.setRemarks(request.getRemarks());
        dtl.setLastmodifiedBy(userId);

        SalesDnDtl savedDtl = dnDtlRepository.save(dtl);
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

}
