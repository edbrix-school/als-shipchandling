package com.alsharif.shipchandling.salesinvoice.service;

import com.alsharif.shipchandling.salesinvoice.dto.*;
import com.alsharif.shipchandling.salesinvoice.entity.*;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import com.alsharif.shipchandling.exceptions.CustomException;
import com.alsharif.shipchandling.salesinvoice.repository.*;
import com.alsharif.shipchandling.salesinvoice.service.ArSchSalesInvoiceService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArSchSalesInvoiceServiceImpl implements ArSchSalesInvoiceService {

    private final ArSchSalesInvoiceHdrRepository invoiceHdrRepository;
    private final ArSchSalesInvoiceDtlRepository invoiceDtlRepository;
    private final ArSchSalesDnDtlRepository dnDtlRepository;
    private final ArSchSalesInvCostbkdDtlRepository costbkdDtlRepository;
    // Add DataSource for stored procedure calls
    // private final DataSource dataSource;

    @Override
    @Transactional
    public ArSchSalesInvoiceHdrDto createSalesInvoice(CreateArSchSalesInvoiceRequest request,
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

        // Create entity
        ArSchSalesInvoiceHdr invoice = new ArSchSalesInvoiceHdr();
        BeanUtils.copyProperties(request, invoice);
        invoice.setGroupPoid(groupPoid);
        invoice.setCompanyPoid(companyPoid);
        invoice.setCreatedBy(userId);
        invoice.setLastmodifiedBy(userId);
        invoice.setInvStatus("IN_PROGRESS");
        invoice.setVerified("N");
        invoice.setDeleted("N");

        // Save to get transactionPoid
        ArSchSalesInvoiceHdr savedInvoice = invoiceHdrRepository.save(invoice);
        invoiceHdrRepository.flush();

        // Call stored procedure BEFORE SAVE for validation
        callEditValidateProcedure(savedInvoice.getTransactionPoid(), groupPoid);

        // Save detail tables
        if (request.getInvoiceDetails() != null && !request.getInvoiceDetails().isEmpty()) {
            saveInvoiceDetails(savedInvoice.getTransactionPoid(), request.getInvoiceDetails(), userId);
        }

        if (request.getDeliveryNoteDetails() != null && !request.getDeliveryNoteDetails().isEmpty()) {
            saveDeliveryNoteDetails(savedInvoice.getTransactionPoid(), request.getDeliveryNoteDetails(), userId);
        }

        // Call stored procedure AFTER SAVE for authorization
        callAuthorizationProcedure(savedInvoice.getTransactionPoid(), null, userId);

        // Refresh to get auto-generated DocRef
        invoiceHdrRepository.flush();
        ArSchSalesInvoiceHdr refreshedInvoice = invoiceHdrRepository.findByTransactionPoid(
                savedInvoice.getTransactionPoid()).orElse(savedInvoice);

        // Convert to DTO
        ArSchSalesInvoiceHdrDto dto = convertToDto(refreshedInvoice, true);
        return dto;
    }

    private ArSchSalesInvoiceHdrDto convertToDto(ArSchSalesInvoiceHdr invoice, boolean includeDetails) {
        ArSchSalesInvoiceHdrDto dto = new ArSchSalesInvoiceHdrDto();
        BeanUtils.copyProperties(invoice, dto);

        if (includeDetails) {
            List<ArSchSalesInvoiceDtl> details = invoiceDtlRepository
                    .findByTransactionPoid(invoice.getTransactionPoid());
            List<ArSchSalesInvoiceDtlDto> detailDtos = details.stream()
                    .map(this::convertInvoiceDtlToDto)
                    .collect(Collectors.toList());
            dto.setInvoiceDetails(detailDtos);

            List<ArSchSalesDnDtl> dnDetails = dnDtlRepository.findByTransactionPoid(invoice.getTransactionPoid());
            List<ArSchSalesDnDtlDto> dnDetailDtos = dnDetails.stream()
                    .map(this::convertDnDtlToDto)
                    .collect(Collectors.toList());
            dto.setDeliveryNoteDetails(dnDetailDtos);

            List<ArSchSalesInvCostbkdDtl> costDetails = costbkdDtlRepository
                    .findByTransactionPoid(invoice.getTransactionPoid());
            List<ArSchSalesInvCostbkdDtlDto> costDetailDtos = costDetails.stream()
                    .map(this::convertCostbkdDtlToDto)
                    .collect(Collectors.toList());
            dto.setCostBookedDetails(costDetailDtos);
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public ArSchSalesInvoiceHdrDto getSalesInvoiceByPoid(Long transactionPoid, Long groupPoid,
            Long companyPoid, Boolean includeDetails) {
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid);
        }

        return convertToDto(invoice, includeDetails != null && includeDetails);
    }

    @Override
    @Transactional
    public ArSchSalesInvoiceHdrDto updateSalesInvoice(Long transactionPoid, UpdateArSchSalesInvoiceRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot update deleted invoice");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot update verified invoice. Invoice must be unverified first.");
        }

        // Validate party type
        if (request.getPartyType() != null) {
            if (!"CUSTOMER".equalsIgnoreCase(request.getPartyType()) &&
                    !"PRINCIPAL".equalsIgnoreCase(request.getPartyType())) {
                throw new CustomException("Party type must be CUSTOMER or PRINCIPAL");
            }
        }

        // Update fields
        BeanUtils.copyProperties(request, invoice, "transactionPoid", "docRef", "createdBy", "createdDate",
                "invStatus", "verified", "invAmount", "totalGpAmt", "totalGpPercent",
                "totalCost", "discountAmt", "discountPercent", "invDiscount",
                "incentiveAmt", "incentivePercent", "incentiveAmt2", "incentivePercent2",
                "incentiveAmt3", "incentivePercent3", "paymentMode", "dueDate");
        invoice.setLastmodifiedBy(userId);

        // Call stored procedure BEFORE SAVE for validation
        callEditValidateProcedure(transactionPoid, groupPoid);

        // Update detail tables
        updateInvoiceDetails(transactionPoid, request.getInvoiceDetails(), userId);
        updateDeliveryNoteDetails(transactionPoid, request.getDeliveryNoteDetails(), userId);

        // Save
        ArSchSalesInvoiceHdr savedInvoice = invoiceHdrRepository.save(invoice);

        // Call stored procedure AFTER SAVE for authorization
        callAuthorizationProcedure(transactionPoid, null, userId);

        return convertToDto(savedInvoice, true);
    }

    @Override
    @Transactional
    public void deleteSalesInvoice(Long transactionPoid, Long groupPoid, Long companyPoid) {
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
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
    public List<ArSchSalesInvoiceHdrDto> getAllSalesInvoices(Long groupPoid, Long companyPoid,
            String invStatus, String verified,
            Long customerPoid, Long principalPoid,
            Long qtnPoid, String search) {
        List<ArSchSalesInvoiceHdr> invoices = invoiceHdrRepository
                .findByGroupPoidAndCompanyPoidAndDeletedNotOrDeletedIsNull(groupPoid, companyPoid, "Y");

        return invoices.stream()
                .filter(inv -> invStatus == null || invStatus.equals(inv.getInvStatus()))
                .filter(inv -> verified == null || verified.equals(inv.getVerified()))
                .filter(inv -> customerPoid == null || customerPoid.equals(inv.getCustomerPoid()))
                .filter(inv -> principalPoid == null || principalPoid.equals(inv.getPrincipalPoid()))
                .filter(inv -> qtnPoid == null || qtnPoid.equals(inv.getQtnPoid()))
                .filter(inv -> {
                    if (search == null || search.trim().isEmpty()) {
                        return true;
                    }
                    String searchLower = search.toLowerCase();
                    return (inv.getDocRef() != null && inv.getDocRef().toLowerCase().contains(searchLower)) ||
                            (inv.getVesselName() != null && inv.getVesselName().toLowerCase().contains(searchLower)) ||
                            (inv.getPortName() != null && inv.getPortName().toLowerCase().contains(searchLower));
                })
                .sorted((i1, i2) -> {
                    if (i1.getTransactionDate() != null && i2.getTransactionDate() != null) {
                        return i2.getTransactionDate().compareTo(i1.getTransactionDate());
                    }
                    return i1.getDocRef().compareToIgnoreCase(i2.getDocRef());
                })
                .map(inv -> convertToDto(inv, false))
                .collect(Collectors.toList());
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
        response.setIsUnique(!exists);
        response.setMessage(exists ? "Document reference already exists" : "Document reference is available");
        return response;
    }

    // Detail Table Methods - Invoice Details
    @Override
    @Transactional
    public ArSchSalesInvoiceDtlDto addInvoiceDetail(Long transactionPoid, CreateArSchSalesInvoiceDtlRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists and belongs to group/company
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
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
        ArSchSalesInvoiceDtl dtl = new ArSchSalesInvoiceDtl();
        dtl.setTransactionPoid(transactionPoid);
        dtl.setDetRowId(detRowId);
        dtl.setStockPoid(request.getStockPoid());
        dtl.setStockUnitPoid(request.getStockUnitPoid());
        dtl.setQuantity(request.getQuantity());
        dtl.setPrice(request.getPrice());
        dtl.setDiscount(request.getDiscount());
        dtl.setBaseAmt(request.getBaseAmt());
        dtl.setTaxPoid(request.getTaxPoid());
        dtl.setCostCenterPoid(request.getCostCenterPoid());
        dtl.setRemarks(request.getRemarks());
        dtl.setCreatedBy(userId);
        dtl.setLastmodifiedBy(userId);

        // Calculate amount
        if (dtl.getQuantity() != null && dtl.getPrice() != null) {
            Long amount = dtl.getQuantity().multiply(dtl.getPrice());
            if (dtl.getDiscount() != null) {
                amount = amount.subtract(dtl.getDiscount());
            }
            dtl.setAmount(amount);
        }

        // Get tax percentage if tax is selected
        if (request.getTaxPoid() != null) {
            // TODO: Get tax percentage from tax master and set taxPercentage
            // Calculate tax amount: taxAmount = baseAmt * taxPercentage / 100
        }

        ArSchSalesInvoiceDtl savedDtl = invoiceDtlRepository.save(dtl);
        return convertInvoiceDtlToDto(savedDtl);
    }

    @Override
    @Transactional
    public ArSchSalesInvoiceDtlDto updateInvoiceDetail(Long transactionPoid, Long detRowId,
            CreateArSchSalesInvoiceDtlRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot update invoice details. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot update invoice details. Invoice is verified");
        }

        // Find existing invoice detail
        ArSchSalesInvoiceDtl dtl = invoiceDtlRepository
                .findById(new ArSchSalesInvoiceDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Invoice Detail", "detRowId", detRowId));

        // Update fields (only editable fields)
        dtl.setStockPoid(request.getStockPoid());
        dtl.setStockUnitPoid(request.getStockUnitPoid());
        dtl.setQuantity(request.getQuantity());
        dtl.setPrice(request.getPrice());
        dtl.setDiscount(request.getDiscount());
        dtl.setBaseAmt(request.getBaseAmt());
        dtl.setTaxPoid(request.getTaxPoid());
        dtl.setCostCenterPoid(request.getCostCenterPoid());
        dtl.setRemarks(request.getRemarks());
        dtl.setLastmodifiedBy(userId);

        // Recalculate amount
        if (dtl.getQuantity() != null && dtl.getPrice() != null) {
            Long amount = dtl.getQuantity().multiply(dtl.getPrice());
            if (dtl.getDiscount() != null) {
                amount = amount.subtract(dtl.getDiscount());
            }
            dtl.setAmount(amount);
        }

        // Recalculate tax if tax is selected
        if (request.getTaxPoid() != null) {
            // TODO: Get tax percentage and calculate tax amount
        }

        ArSchSalesInvoiceDtl savedDtl = invoiceDtlRepository.save(dtl);
        return convertInvoiceDtlToDto(savedDtl);
    }

    @Override
    @Transactional
    public void deleteInvoiceDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot delete invoice details. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot delete invoice details. Invoice is verified");
        }

        // Delete invoice detail
        invoiceDtlRepository.deleteById(new ArSchSalesInvoiceDtlId(transactionPoid, detRowId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArSchSalesInvoiceDtlDto> getInvoiceDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {
        // Validate invoice exists
        invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        List<ArSchSalesInvoiceDtl> invoiceDetails = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        return invoiceDetails.stream()
                .map(this::convertInvoiceDtlToDto)
                .collect(Collectors.toList());
    }

    // Detail Table Methods - Delivery Note Details
    @Override
    @Transactional
    public ArSchSalesDnDtlDto addDeliveryNoteDetail(Long transactionPoid, CreateArSchSalesDnDtlRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
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
        ArSchSalesDnDtl dtl = new ArSchSalesDnDtl();
        dtl.setTransactionPoid(transactionPoid);
        dtl.setDetRowId(detRowId);
        dtl.setDnPoidFk(request.getDnPoidFk());
        dtl.setQuotationPoidFk(request.getQuotationPoidFk());
        dtl.setRemarks(request.getRemarks());
        dtl.setCreatedBy(userId);
        dtl.setLastmodifiedBy(userId);

        ArSchSalesDnDtl savedDtl = dnDtlRepository.save(dtl);
        return convertDnDtlToDto(savedDtl);
    }

    @Override
    @Transactional
    public ArSchSalesDnDtlDto updateDeliveryNoteDetail(Long transactionPoid, Long detRowId,
            CreateArSchSalesDnDtlRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot update delivery note details. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot update delivery note details. Invoice is verified");
        }

        // Find existing delivery note detail
        ArSchSalesDnDtl dtl = dnDtlRepository
                .findById(new ArSchSalesDnDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note Detail", "detRowId", detRowId));

        // Update fields
        dtl.setDnPoidFk(request.getDnPoidFk());
        dtl.setQuotationPoidFk(request.getQuotationPoidFk());
        dtl.setRemarks(request.getRemarks());
        dtl.setLastmodifiedBy(userId);

        ArSchSalesDnDtl savedDtl = dnDtlRepository.save(dtl);
        return convertDnDtlToDto(savedDtl);
    }

    @Override
    @Transactional
    public void deleteDeliveryNoteDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot delete delivery note details. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot delete delivery note details. Invoice is verified");
        }

        // Delete delivery note detail
        dnDtlRepository.deleteById(new ArSchSalesDnDtlId(transactionPoid, detRowId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArSchSalesDnDtlDto> getDeliveryNoteDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {
        // Validate invoice exists
        invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        List<ArSchSalesDnDtl> dnDetails = dnDtlRepository.findByTransactionPoid(transactionPoid);
        return dnDetails.stream()
                .map(this::convertDnDtlToDto)
                .collect(Collectors.toList());
    }

    // Business Logic Methods
    @Override
    @Transactional
    public LoadQuotationResponse loadQuotation(Long transactionPoid, LoadQuotationRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot load quotation. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot load quotation. Invoice is verified");
        }

        // Check if invoice details table is not empty
        List<ArSchSalesInvoiceDtl> existingDetails = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        if (!existingDetails.isEmpty()) {
            throw new CustomException(
                    "Cannot load quotation. Invoice details table is not empty. Please clear items first.");
        }

        // Call stored procedure to load quotation
        LoadQuotationResponse response = callLoadQuotationProcedure(transactionPoid, request.getQtnPoid(),
                request.getIncentiveAmt(),
                request.getIncentiveAmt2(),
                request.getIncentiveAmt3(),
                groupPoid, companyPoid, userId);

        // Update invoice with quotation reference
        invoice.setQtnPoid(request.getQtnPoid());
        invoice.setCurrencyCode(response.getCurrencyCode());
        invoice.setCurrencyRate(response.getCurrencyRate());
        invoice.setIncentiveAmt(request.getIncentiveAmt());
        invoice.setIncentiveAmt2(request.getIncentiveAmt2());
        invoice.setIncentiveAmt3(request.getIncentiveAmt3());
        invoice.setLastmodifiedBy(userId);
        invoiceHdrRepository.save(invoice);

        return response;
    }

    @Override
    @Transactional
    public LoadDeliveryNoteResponse loadDeliveryNote(Long transactionPoid, Long groupPoid,
            Long companyPoid, String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot load delivery note. Invoice is deleted");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot load delivery note. Invoice is verified");
        }

        // Check if invoice details table is not empty
        List<ArSchSalesInvoiceDtl> existingDetails = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        if (!existingDetails.isEmpty()) {
            throw new CustomException(
                    "Cannot load delivery note. Invoice details table is not empty. Please clear items first.");
        }

        // Check if delivery note details exist
        List<ArSchSalesDnDtl> dnDetails = dnDtlRepository.findByTransactionPoid(transactionPoid);
        if (dnDetails.isEmpty()) {
            throw new CustomException(
                    "No delivery notes selected. Please select delivery notes in Delivery Note Details tab first.");
        }

        // Call stored procedure to load delivery note
        String result = callLoadDeliveryNoteProcedure(transactionPoid, groupPoid, companyPoid, userId);

        if (result != null && result.contains("ERROR")) {
            throw new CustomException("Error loading delivery note: " + result);
        }

        // Count items loaded
        List<ArSchSalesInvoiceDtl> loadedDetails = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        int itemsLoaded = loadedDetails.size();

        LoadDeliveryNoteResponse response = new LoadDeliveryNoteResponse();
        response.setSuccess(true);
        response.setMessage(result != null ? result : "Delivery note loaded successfully");
        response.setItemsLoaded(itemsLoaded);
        return response;
    }

    @Override
    @Transactional
    public VerifyInvoiceResponse verifyInvoice(Long transactionPoid, VerifyInvoiceRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getDeleted())) {
            throw new CustomException("Cannot verify deleted invoice");
        }

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Invoice is already verified");
        }

        // Check if invoice has items
        List<ArSchSalesInvoiceDtl> invoiceDetails = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        if (invoiceDetails.isEmpty()) {
            throw new CustomException("Cannot verify invoice. Invoice has no items.");
        }

        // Update verified status
        invoice.setVerified("Y");
        invoice.setAuthorizedId(request.getAuthorizedId());
        invoice.setLastmodifiedBy(userId);
        invoiceHdrRepository.save(invoice);

        VerifyInvoiceResponse response = new VerifyInvoiceResponse();
        response.setSuccess(true);
        response.setMessage("Invoice verified successfully");
        return response;
    }

    @Override
    @Transactional
    public CalculateGpResponse calculateGp(Long transactionPoid, Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        // Call stored procedure to calculate GP
        String result = callCalculateGpProcedure(transactionPoid, groupPoid, companyPoid, userId);

        if (result != null && result.contains("ERROR")) {
            throw new CustomException("Error calculating GP: " + result);
        }

        // Refresh invoice to get calculated GP values
        invoiceHdrRepository.flush();
        ArSchSalesInvoiceHdr refreshedInvoice = invoiceHdrRepository.findByTransactionPoid(transactionPoid)
                .orElse(invoice);

        CalculateGpResponse response = new CalculateGpResponse();
        response.setSuccess(true);
        response.setMessage(result != null ? result : "GP calculated successfully");
        response.setTotalGpAmt(refreshedInvoice.getTotalGpAmt());
        response.setTotalGpPercent(refreshedInvoice.getTotalGpPercent());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LoadCostBookingsResponse loadCostBookings(Long transactionPoid, Long groupPoid,
            Long companyPoid, String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        // Call stored procedure to load cost bookings
        String result = callLoadCostBookingsProcedure(transactionPoid, groupPoid, companyPoid, userId);

        if (result != null && result.contains("ERROR")) {
            throw new CustomException("Error loading cost bookings: " + result);
        }

        // Query cost booked details (read-only)
        List<ArSchSalesInvCostbkdDtl> costBookings = costbkdDtlRepository.findByTransactionPoid(transactionPoid);
        List<ArSchSalesInvCostbkdDtlDto> costBookingDtos = costBookings.stream()
                .map(this::convertCostbkdDtlToDto)
                .collect(Collectors.toList());

        LoadCostBookingsResponse response = new LoadCostBookingsResponse();
        response.setSuccess(true);
        response.setMessage(result != null ? result : "Cost bookings loaded successfully");
        response.setCostBookings(costBookingDtos);
        response.setCount(costBookingDtos.size());
        return response;
    }

    // Continue from loadCostBookings() method...

    @Override
    @Transactional
    public CalculateGpResponse calculateGp(Long transactionPoid, Long groupPoid, Long companyPoid) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot calculate GP. Invoice is verified.");
        }

        // Call stored procedure
        String result = callCalculateGpProcedure(transactionPoid, groupPoid);

        if (result != null && result.contains("ERROR")) {
            throw new CustomException("Error calculating GP: " + result);
        }

        // Refresh invoice to get updated GP values
        invoiceHdrRepository.flush();
        ArSchSalesInvoiceHdr refreshedInvoice = invoiceHdrRepository
                .findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        CalculateGpResponse response = new CalculateGpResponse();
        response.setSuccess(true);
        response.setMessage(result != null ? result : "GP calculated successfully");
        response.setTotalGpAmt(refreshedInvoice.getTotalGpAmt());
        response.setTotalGpPercent(refreshedInvoice.getTotalGpPercent());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CalculateDueDateResponse calculateDueDate(Timestamp transactionDate, Long creditDays,
            Long groupPoid, Long companyPoid) {
        if (transactionDate == null || creditDays == null) {
            throw new CustomException("Transaction date and credit days are required");
        }

        // Call stored procedure
        Timestamp dueDate = callCalculateDueDateProcedure(groupPoid, companyPoid, transactionDate, creditDays);

        CalculateDueDateResponse response = new CalculateDueDateResponse();
        response.setDueDate(dueDate);
        return response;
    }

    @Override
    @Transactional
    public CalculateDiscountCommissionResponse calculateItemDiscountCommission(Long transactionPoid, Long detRowId,
            Long incentiveAmt,
            Long incentiveAmt2,
            Long incentiveAmt3,
            Long incentivePercent,
            Long incentivePercent2,
            Long incentivePercent3,
            Long baseAmt,
            Long groupPoid) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoid(transactionPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot calculate discount/commission. Invoice is verified.");
        }

        // Validate item detail exists
        ArSchSalesInvoiceDtl itemDtl = invoiceDtlRepository
                .findById(new ArSchSalesInvoiceDtlId(transactionPoid, detRowId))
                .orElseThrow(() -> new ResourceNotFoundException("Invoice Item Detail", "detRowId", detRowId));

        // Call stored procedure
        DiscountCommissionResult result = callCalculateItemDiscountCommissionProcedure(
                transactionPoid, detRowId, incentiveAmt, incentiveAmt2, incentiveAmt3,
                incentivePercent, incentivePercent2, incentivePercent3, baseAmt, groupPoid);

        if (result.getResult() != null && result.getResult().contains("ERROR")) {
            throw new CustomException("Error calculating discount/commission: " + result.getResult());
        }

        // Update item detail with calculated values
        itemDtl.setNetSales(result.getNetSales());
        itemDtl.setIncentive(result.getIncentive());
        invoiceDtlRepository.save(itemDtl);

        CalculateDiscountCommissionResponse response = new CalculateDiscountCommissionResponse();
        response.setSuccess(true);
        response.setMessage(
                result.getResult() != null ? result.getResult() : "Discount/commission calculated successfully");
        response.setNetSales(result.getNetSales());
        response.setIncentive(result.getIncentive());
        return response;
    }

    @Override
    @Transactional
    public CalculateDiscountCommissionResponse calculateHeaderDiscountCommission(Long transactionPoid,
            Long incentiveAmt,
            Long incentiveAmt2,
            Long incentiveAmt3,
            Long incentivePercent,
            Long incentivePercent2,
            Long incentivePercent3,
            Long groupPoid) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoid(transactionPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot calculate discount/commission. Invoice is verified.");
        }

        // Call stored procedure
        DiscountCommissionResult result = callCalculateHeaderDiscountCommissionProcedure(
                transactionPoid, incentiveAmt, incentiveAmt2, incentiveAmt3,
                incentivePercent, incentivePercent2, incentivePercent3, groupPoid);

        if (result.getResult() != null && result.getResult().contains("ERROR")) {
            throw new CustomException("Error calculating discount/commission: " + result.getResult());
        }

        // Refresh invoice to get updated values
        invoiceHdrRepository.flush();
        ArSchSalesInvoiceHdr refreshedInvoice = invoiceHdrRepository
                .findByTransactionPoid(transactionPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        CalculateDiscountCommissionResponse response = new CalculateDiscountCommissionResponse();
        response.setSuccess(true);
        response.setMessage(
                result.getResult() != null ? result.getResult() : "Discount/commission calculated successfully");
        response.setNetSales(refreshedInvoice.getInvAmount());
        response.setIncentive(refreshedInvoice.getIncentiveAmt());
        return response;
    }

    @Override
    @Transactional
    public LoadQuotationResponse loadQuotation(Long transactionPoid, Long qtnPoid,
            Long incentiveAmt, Long incentiveAmt2, Long incentiveAmt3,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot load quotation. Invoice is verified.");
        }

        // Check if invoice details table is not empty
        List<ArSchSalesInvoiceDtl> existingItems = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        if (!existingItems.isEmpty()) {
            throw new CustomException("Cannot load quotation. Invoice already has items. Please clear items first.");
        }

        // Call stored procedure
        QuotationLoadResult result = callLoadQuotationProcedure(
                transactionPoid, qtnPoid, incentiveAmt, incentiveAmt2, incentiveAmt3, groupPoid);

        if (result.getResult() != null && result.getResult().contains("ERROR")) {
            throw new CustomException("Error loading quotation: " + result.getResult());
        }

        // Update invoice with quotation reference
        invoice.setQtnPoid(qtnPoid);
        invoice.setIncentiveAmt(result.getIncentiveAmt());
        invoice.setIncentiveAmt2(result.getIncentiveAmt2());
        invoice.setIncentiveAmt3(result.getIncentiveAmt3());
        invoice.setLastmodifiedBy(userId);
        invoiceHdrRepository.save(invoice);

        // Refresh to get loaded items
        invoiceHdrRepository.flush();
        List<ArSchSalesInvoiceDtl> loadedItems = invoiceDtlRepository.findByTransactionPoid(transactionPoid);

        LoadQuotationResponse response = new LoadQuotationResponse();
        response.setSuccess(true);
        response.setMessage(result.getResult() != null ? result.getResult() : "Quotation loaded successfully");
        response.setItemsLoaded(loadedItems.size());
        response.setIncentiveAmt(result.getIncentiveAmt());
        response.setIncentiveAmt2(result.getIncentiveAmt2());
        response.setIncentiveAmt3(result.getIncentiveAmt3());
        return response;
    }

    @Override
    @Transactional
    public LoadDeliveryNoteResponse loadDeliveryNote(Long transactionPoid, Long groupPoid, Long companyPoid,
            String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot load delivery note. Invoice is verified.");
        }

        // Check if delivery notes are selected
        List<ArSchSalesDnDtl> dnDetails = dnDtlRepository.findByTransactionPoid(transactionPoid);
        if (dnDetails.isEmpty()) {
            throw new CustomException("Please select delivery notes first before loading.");
        }

        // Check if invoice details table is not empty
        List<ArSchSalesInvoiceDtl> existingItems = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        if (!existingItems.isEmpty()) {
            throw new CustomException(
                    "Cannot load delivery note. Invoice already has items. Please clear items first.");
        }

        // Call stored procedure
        String result = callLoadDeliveryNoteProcedure(transactionPoid, groupPoid);

        if (result != null && result.contains("ERROR")) {
            throw new CustomException("Error loading delivery note: " + result);
        }

        // Refresh to get loaded items
        invoiceHdrRepository.flush();
        List<ArSchSalesInvoiceDtl> loadedItems = invoiceDtlRepository.findByTransactionPoid(transactionPoid);

        LoadDeliveryNoteResponse response = new LoadDeliveryNoteResponse();
        response.setSuccess(true);
        response.setMessage(result != null ? result : "Delivery note loaded successfully");
        response.setItemsLoaded(loadedItems.size());
        return response;
    }

    @Override
    @Transactional
    public UnloadQuotationResponse unloadQuotation(Long transactionPoid, Long groupPoid, Long companyPoid,
            String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Cannot unload quotation. Invoice is verified.");
        }

        // Call stored procedure
        String result = callUnloadQuotationProcedure(transactionPoid, groupPoid);

        if (result != null && result.contains("ERROR")) {
            throw new CustomException("Error unloading quotation: " + result);
        }

        // Clear quotation reference
        invoice.setQtnPoid(null);
        invoice.setIncentiveAmt(null);
        invoice.setIncentiveAmt2(null);
        invoice.setIncentiveAmt3(null);
        invoice.setIncentivePercent(null);
        invoice.setIncentivePercent2(null);
        invoice.setIncentivePercent3(null);
        invoice.setLastmodifiedBy(userId);
        invoiceHdrRepository.save(invoice);

        UnloadQuotationResponse response = new UnloadQuotationResponse();
        response.setSuccess(true);
        response.setMessage(result != null ? result : "Quotation unloaded successfully");
        return response;
    }

    @Override
    @Transactional
    public VerifyInvoiceResponse verifyInvoice(Long transactionPoid, Long groupPoid, Long companyPoid, String userId) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        if ("Y".equals(invoice.getVerified())) {
            throw new CustomException("Invoice is already verified");
        }

        // Validate invoice has items
        List<ArSchSalesInvoiceDtl> items = invoiceDtlRepository.findByTransactionPoid(transactionPoid);
        if (items.isEmpty()) {
            throw new CustomException("Cannot verify invoice. Invoice has no items.");
        }

        // Set verified status
        invoice.setVerified("Y");
        invoice.setLastmodifiedBy(userId);
        invoiceHdrRepository.save(invoice);

        VerifyInvoiceResponse response = new VerifyInvoiceResponse();
        response.setSuccess(true);
        response.setMessage("Invoice verified successfully");
        response.setVerified(true);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ValidateCustomerResponse validateCustomer(Long customerPoid, Long groupPoid, Long companyPoid) {
        if (customerPoid == null) {
            throw new CustomException("Customer POID is required");
        }

        // Call stored procedure
        String result = callValidateCustomerProcedure(groupPoid, companyPoid, customerPoid);

        ValidateCustomerResponse response = new ValidateCustomerResponse();
        response.setValid(result == null || !result.contains("ERROR"));
        response.setMessage(result != null ? result : "Customer validation passed");
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LoadCreditDetailsResponse loadCreditDetails(Long customerPoid, Long groupPoid, Long companyPoid) {
        if (customerPoid == null) {
            throw new CustomException("Customer POID is required");
        }

        // Call stored procedure
        CreditDetailsResult result = callLoadCreditDetailsProcedure(groupPoid, companyPoid, customerPoid);

        if (result.getResult() != null && result.getResult().contains("ERROR")) {
            throw new CustomException("Error loading credit details: " + result.getResult());
        }

        LoadCreditDetailsResponse response = new LoadCreditDetailsResponse();
        response.setSuccess(true);
        response.setMessage(result.getResult() != null ? result.getResult() : "Credit details loaded successfully");
        response.setCreditDays(result.getCreditDays());
        response.setPaymentMode(result.getPaymentMode());
        response.setDueDate(result.getDueDate());
        response.setCreditLimit(result.getCreditLimit());
        response.setOutstandingAmount(result.getOutstandingAmount());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public LoadQuotationCurrencyResponse loadQuotationCurrency(Long qtnPoid) {
        if (qtnPoid == null) {
            throw new CustomException("Quotation POID is required");
        }

        // Call stored procedure
        QuotationCurrencyResult result = callLoadQuotationCurrencyProcedure(qtnPoid);

        if (result.getResult() != null && result.getResult().contains("ERROR")) {
            throw new CustomException("Error loading quotation currency: " + result.getResult());
        }

        LoadQuotationCurrencyResponse response = new LoadQuotationCurrencyResponse();
        response.setSuccess(true);
        response.setMessage(result.getResult() != null ? result.getResult() : "Currency details loaded successfully");
        response.setCurrencyCode(result.getCurrencyCode());
        response.setCurrencyRate(result.getCurrencyRate());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesInvoiceDependenciesDto checkSalesInvoiceDependencies(Long transactionPoid, Long groupPoid,
            Long companyPoid) {
        // Validate invoice exists
        ArSchSalesInvoiceHdr invoice = invoiceHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Sales Invoice", "transactionPoid", transactionPoid));

        // TODO: Check for dependencies
        // - Receipts: Check if invoice has been paid (AR_RECEIPT_DTL)
        // - Credit Notes: Check if credit notes have been created from this invoice
        // - GL Postings: Check if invoice has been posted to GL
        Long receiptCount = 0L; // TODO: Implement receipt check
        Long creditNoteCount = 0L; // TODO: Implement credit note check
        Long glPostingCount = 0L; // TODO: Implement GL posting check

        SalesInvoiceDependenciesDto dto = new SalesInvoiceDependenciesDto();
        dto.setTransactionPoid(transactionPoid);
        dto.setCanDelete(receiptCount == 0 && creditNoteCount == 0 && glPostingCount == 0);
        dto.setReceiptCount(receiptCount);
        dto.setCreditNoteCount(creditNoteCount);
        dto.setGlPostingCount(glPostingCount);

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

    // Helper methods for stored procedures
    private String callCalculateGpProcedure(Long transactionPoid, Long groupPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AR_SCH_GP_CALC(?,?,?); END;";
        // Parameters: P_TRANSACTION_POID, P_GROUP_POID (optional), P_RESULT (OUT
        // VARCHAR)
        // Return result string
        return null;
    }

    private Timestamp callCalculateDueDateProcedure(Long groupPoid, Long companyPoid,
            Timestamp transactionDate, Long creditDays) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_CALC_DUEDAYS(?,?,?,?,?,?); END;";
        // Parameters: P_GROUP_POID, P_COMPANY_POID, P_TRANSACTION_DATE, P_CREDIT_DAYS,
        // P_DUE_DATE (OUT DATE), P_RESULT (OUT VARCHAR)
        // Return due date
        return null;
    }

    private DiscountCommissionResult callCalculateItemDiscountCommissionProcedure(Long transactionPoid, Long detRowId,
            Long incentiveAmt, Long incentiveAmt2, Long incentiveAmt3,
            Long incentivePercent, Long incentivePercent2, Long incentivePercent3,
            Long baseAmt, Long groupPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AR_SCH_DIS_COM_CAL(?,?,?,?,?,?,?,?,?,?,?,?); END;";
        // Parameters: P_TRANSACTION_POID, P_DET_ROW_ID, P_INCENTIVE_AMT,
        // P_INCENTIVE_AMT2, P_INCENTIVE_AMT3,
        // P_INCENTIVE_PERCENT, P_INCENTIVE_PERCENT2, P_INCENTIVE_PERCENT3, P_BASE_AMT,
        // P_NET_SALES (OUT), P_INCENTIVE (OUT), P_RESULT (OUT VARCHAR)
        // Return DiscountCommissionResult
        return new DiscountCommissionResult();
    }

    private DiscountCommissionResult callCalculateHeaderDiscountCommissionProcedure(Long transactionPoid,
            Long incentiveAmt, Long incentiveAmt2, Long incentiveAmt3,
            Long incentivePercent, Long incentivePercent2, Long incentivePercent3,
            Long groupPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AR_SCH_DIS_COM_CAL_HDR(?,?,?,?,?,?,?,?); END;";
        // Parameters: Similar to item level but for header
        // Return DiscountCommissionResult
        return new DiscountCommissionResult();
    }

    private QuotationLoadResult callLoadQuotationProcedure(Long transactionPoid, Long qtnPoid,
            Long incentiveAmt, Long incentiveAmt2, Long incentiveAmt3,
            Long groupPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AR_SCH_QTN_LOAD_BUTTON(?,?,?,?,?,?,?); END;";
        // Parameters: P_TRANSACTION_POID, P_QTN_POID, P_INCENTIVE_AMT,
        // P_INCENTIVE_AMT2, P_INCENTIVE_AMT3, P_RESULT (OUT VARCHAR), P_CURSOR (OUT
        // CURSOR)
        // Process cursor to get loaded items and incentive amounts
        // Return QuotationLoadResult
        return new QuotationLoadResult();
    }

    private String callLoadDeliveryNoteProcedure(Long transactionPoid, Long groupPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AR_SCH_SALESINV_DN_LOAD(?,?); END;";
        // Parameters: P_TRANSACTION_POID, P_RESULT (OUT VARCHAR)
        // Return result string
        return null;
    }

    private String callUnloadQuotationProcedure(Long transactionPoid, Long groupPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AR_SCH_UNLOAD_QUOTATION1(?,?,?); END;";
        // Parameters: P_TRANSACTION_POID, P_GROUP_POID (optional), P_RESULT (OUT
        // VARCHAR)
        // Return result string
        return null;
    }

    private String callValidateCustomerProcedure(Long groupPoid, Long companyPoid, Long customerPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_VALIDATE_CUSTOMER(?,?,?,?); END;";
        // Parameters: P_GROUP_POID, P_COMPANY_POID, P_CUSTOMER_POID, P_RESULT (OUT
        // VARCHAR)
        // Return result string
        return null;
    }

    private CreditDetailsResult callLoadCreditDetailsProcedure(Long groupPoid, Long companyPoid, Long customerPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_LOAD_CREDIT_DETAILS(?,?,?,?,?,?,?,?); END;";
        // Parameters: P_GROUP_POID, P_COMPANY_POID, P_CUSTOMER_POID, P_CREDIT_DAYS
        // (OUT), P_DUE_DATE (OUT),
        // P_PAYMENT_MODE (OUT), P_RESULT (OUT VARCHAR), P_CURSOR (OUT CURSOR)
        // Process cursor and return CreditDetailsResult
        return new CreditDetailsResult();
    }

    private QuotationCurrencyResult callLoadQuotationCurrencyProcedure(Long qtnPoid) {
        // TODO: Implement stored procedure call using CallableStatement
        // String sql = "BEGIN PROC_AR_SCH_QTN_LOAD_CUR1(?,?,?,?); END;";
        // Parameters: P_QTN_POID, P_CURRENCY_CODE (OUT VARCHAR), P_CURRENCY_RATE (OUT
        // NUMERIC), P_RESULT (OUT VARCHAR)
        // Return QuotationCurrencyResult
        return new QuotationCurrencyResult();
    }

    // Helper method to convert ArSchSalesInvCostbkdDtl to DTO
    private ArSchSalesInvCostbkdDtlDto convertCostbkdDtlToDto(ArSchSalesInvCostbkdDtl entity) {
        ArSchSalesInvCostbkdDtlDto dto = new ArSchSalesInvCostbkdDtlDto();
        org.springframework.beans.BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    // Helper DTOs for stored procedure results
    private static class DiscountCommissionResult {
        private Long netSales;
        private Long incentive;
        private String result;

        // Getters and setters
        public Long getNetSales() {
            return netSales;
        }

        public void setNetSales(Long netSales) {
            this.netSales = netSales;
        }

        public Long getIncentive() {
            return incentive;
        }

        public void setIncentive(Long incentive) {
            this.incentive = incentive;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

    private static class QuotationLoadResult {
        private Long incentiveAmt;
        private Long incentiveAmt2;
        private Long incentiveAmt3;
        private String result;

        // Getters and setters
        public Long getIncentiveAmt() {
            return incentiveAmt;
        }

        public void setIncentiveAmt(Long incentiveAmt) {
            this.incentiveAmt = incentiveAmt;
        }

        public Long getIncentiveAmt2() {
            return incentiveAmt2;
        }

        public void setIncentiveAmt2(Long incentiveAmt2) {
            this.incentiveAmt2 = incentiveAmt2;
        }

        public Long getIncentiveAmt3() {
            return incentiveAmt3;
        }

        public void setIncentiveAmt3(Long incentiveAmt3) {
            this.incentiveAmt3 = incentiveAmt3;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

    private static class CreditDetailsResult {
        private Long creditDays;
        private String paymentMode;
        private Timestamp dueDate;
        private Long creditLimit;
        private Long outstandingAmount;
        private String result;

        // Getters and setters
        public Long getCreditDays() {
            return creditDays;
        }

        public void setCreditDays(Long creditDays) {
            this.creditDays = creditDays;
        }

        public String getPaymentMode() {
            return paymentMode;
        }

        public void setPaymentMode(String paymentMode) {
            this.paymentMode = paymentMode;
        }

        public Timestamp getDueDate() {
            return dueDate;
        }

        public void setDueDate(Timestamp dueDate) {
            this.dueDate = dueDate;
        }

        public Long getCreditLimit() {
            return creditLimit;
        }

        public void setCreditLimit(Long creditLimit) {
            this.creditLimit = creditLimit;
        }

        public Long getOutstandingAmount() {
            return outstandingAmount;
        }

        public void setOutstandingAmount(Long outstandingAmount) {
            this.outstandingAmount = outstandingAmount;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

    private static class QuotationCurrencyResult {
        private String currencyCode;
        private Long currencyRate;
        private String result;

        // Getters and setters
        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }

        public Long getCurrencyRate() {
            return currencyRate;
        }

        public void setCurrencyRate(Long currencyRate) {
            this.currencyRate = currencyRate;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }
    }

    @Override
    public LoadCreditDetailsResponse loadCreditDetails(Long customerPoid, Long groupPoid, Long companyPoid,
            String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadCreditDetails'");
    }

    @Override
    public CalculateDueDateResponse calculateDueDate(Timestamp transactionDate, Long creditDays, Long groupPoid,
            Long companyPoid, String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'calculateDueDate'");
    }
}
