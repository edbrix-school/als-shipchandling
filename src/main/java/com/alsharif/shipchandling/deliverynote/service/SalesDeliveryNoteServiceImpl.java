package com.alsharif.shipchandling.deliverynote.service;

import com.alsharif.shipchandling.deliverynote.dto.*;
import com.alsharif.shipchandling.deliverynote.entity.*;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import com.alsharif.shipchandling.exceptions.CustomException;
import com.alsharif.shipchandling.deliverynote.repository.*;
import com.alsharif.shipchandling.deliverynote.service.SalesDeliveryNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesDeliveryNoteServiceImpl implements SalesDeliveryNoteService {

    private final SalesDeliveryNoteHdrRepository deliveryNoteHdrRepository;
    private final SalesDeliveryNoteItemDtlRepository itemDtlRepository;
    // Add OracleDataSource or DataSource injection for stored procedure calls
    // private final DataSource dataSource;

    @Override
    @Transactional
    public SalesDeliveryNoteHdrDto createDeliveryNote(CreateSalesDeliveryNoteRequest request, Long groupPoid,
            Long companyPoid, String userId) {
        // Validate required fields
        validateDeliveryNoteRequest(request);

        // Create entity
        SalesDeliveryNoteHdr deliveryNote = new SalesDeliveryNoteHdr();
        BeanUtils.copyProperties(request, deliveryNote);
        deliveryNote.setGroupPoid(groupPoid);
        deliveryNote.setCompanyPoid(companyPoid);
        deliveryNote.setCreatedBy(userId);
        deliveryNote.setLastmodifiedBy(userId);
        deliveryNote.setDeleted("N");
        deliveryNote
                .setDescriptionPrintYn(request.getDescriptionPrintYn() != null ? request.getDescriptionPrintYn() : "Y");

        // Save to get transactionPoid
        SalesDeliveryNoteHdr savedDeliveryNote = deliveryNoteHdrRepository.save(deliveryNote);
        deliveryNoteHdrRepository.flush();

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
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public SalesDeliveryNoteHdrDto getDeliveryNoteByPoid(Long transactionPoid, Long groupPoid,
            Long companyPoid, Boolean includeDetails) {
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        if ("Y".equals(deliveryNote.getDeleted())) {
            throw new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid);
        }

        return convertToDto(deliveryNote, includeDetails != null && includeDetails);
    }

    @Override
    @Transactional
    public SalesDeliveryNoteHdrDto updateDeliveryNote(Long transactionPoid, CreateSalesDeliveryNoteRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        if ("Y".equals(deliveryNote.getDeleted())) {
            throw new CustomException("Cannot update deleted delivery note");
        }

        // Check if status allows editing
        if ("CLOSED".equals(deliveryNote.getDeliveryStatus())) {
            throw new CustomException("Cannot update delivery note. Current document is in closed status");
        }

        // Validate customer change if customer is being changed
        if (!deliveryNote.getCustomerPoid().equals(request.getCustomerPoid())) {
            ValidateCustomerChangeResponse validation = validateCustomerChange(
                    transactionPoid, request.getCustomerPoid(), groupPoid, companyPoid);
            if (!validation.getCanChange()) {
                throw new CustomException(validation.getMessage());
            }
        }

        // Validate required fields
        validateDeliveryNoteRequest(request);

        // Update fields (excluding read-only fields)
        BeanUtils.copyProperties(request, deliveryNote, "transactionPoid", "docRef", "createdBy",
                "createdDate", "qtnRefNo");
        deliveryNote.setLastmodifiedBy(userId);

        // Remove items with CheckAll = "N" before updating
        itemDtlRepository.deleteByTransactionPoidAndCheckAllN(transactionPoid);

        // Update item details (only items with CheckAll = "Y")
        updateItemDetails(transactionPoid,
                request.getItemDetails() != null ? request.getItemDetails().stream()
                        .filter(item -> "Y".equals(item.getCheckAll()))
                        .collect(Collectors.toList()) : List.of(),
                userId);

        // Save
        SalesDeliveryNoteHdr savedDeliveryNote = deliveryNoteHdrRepository.save(deliveryNote);

        // Calculate totals
        calculateTotals(transactionPoid);

        // Call stored procedure to update quotation header with deleted details
        callUpdateDeletedDetailsProcedure(transactionPoid, groupPoid, companyPoid, userId);

        return convertToDto(savedDeliveryNote, true);
    }

    @Override
    @Transactional
    public void deleteDeliveryNote(Long transactionPoid, Long groupPoid, Long companyPoid) {
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        // TODO: Check dependencies (sales invoices, etc.)

        // Delete item details
        itemDtlRepository.deleteByTransactionPoid(transactionPoid);

        // Soft delete
        deliveryNote.setDeleted("Y");
        deliveryNoteHdrRepository.save(deliveryNote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesDeliveryNoteHdrDto> getAllDeliveryNotes(Long groupPoid, Long companyPoid,
            String deliveryStatus, Long customerPoid,
            Long salesmanPoid, String qtnRefNo,
            Timestamp fromDate, Timestamp toDate,
            String search) {
        List<SalesDeliveryNoteHdr> deliveryNotes = deliveryNoteHdrRepository
                .findByGroupPoidAndCompanyPoidAndDeletedNotOrDeletedIsNull(groupPoid, companyPoid, "Y");

        return deliveryNotes.stream()
                .filter(dn -> deliveryStatus == null || deliveryStatus.equals(dn.getDeliveryStatus()))
                .filter(dn -> customerPoid == null || customerPoid.equals(dn.getCustomerPoid()))
                .filter(dn -> salesmanPoid == null || salesmanPoid.equals(dn.getSalesmanPoid()))
                .filter(dn -> qtnRefNo == null || qtnRefNo.equals(dn.getQtnRefNo()))
                .filter(dn -> {
                    if (fromDate == null && toDate == null) {
                        return true;
                    }
                    if (fromDate != null && dn.getTransactionDate().before(fromDate)) {
                        return false;
                    }
                    if (toDate != null && dn.getTransactionDate().after(toDate)) {
                        return false;
                    }
                    return true;
                })
                .filter(dn -> {
                    if (search == null || search.trim().isEmpty()) {
                        return true;
                    }
                    String searchLower = search.toLowerCase();
                    return (dn.getDocRef() != null && dn.getDocRef().toLowerCase().contains(searchLower)) ||
                            (dn.getVesselName() != null && dn.getVesselName().toLowerCase().contains(searchLower)) ||
                            (dn.getQtnRefNo() != null && dn.getQtnRefNo().toLowerCase().contains(searchLower));
                })
                .sorted((dn1, dn2) -> dn2.getTransactionDate().compareTo(dn1.getTransactionDate()))
                .map(dn -> convertToDto(dn, false))
                .collect(Collectors.toList());
    }

    // Validation Methods
    @Override
    @Transactional(readOnly = true)
    public ValidationResponse validateDocRef(String docRef, Long groupPoid, Long transactionPoid) {
        if (docRef == null || docRef.trim().isEmpty()) {
            return new ValidationResponse(false, "Document reference cannot be empty");
        }

        boolean exists;
        if (transactionPoid != null) {
            exists = deliveryNoteHdrRepository.existsByDocRefIgnoreCaseAndGroupPoidAndTransactionPoidNot(
                    docRef, groupPoid, transactionPoid);
        } else {
            exists = deliveryNoteHdrRepository.existsByDocRefIgnoreCaseAndGroupPoid(docRef, groupPoid);
        }

        ValidationResponse response = new ValidationResponse();
        response.setIsUnique(!exists);
        response.setMessage(exists ? "Document reference already exists" : "Document reference is available");
        return response;
    }

    // Item Details Methods
    @Override
    @Transactional
    public SalesDeliveryNoteItemDtlDto addItemDetail(Long transactionPoid,
            CreateSalesDeliveryNoteItemDtlRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate delivery note exists
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        if ("Y".equals(deliveryNote.getDeleted())) {
            throw new CustomException("Cannot add item details. Delivery note is deleted");
        }

        if ("CLOSED".equals(deliveryNote.getDeliveryStatus())) {
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

        return convertItemDtlToDto(savedItemDtl);
    }

    @Override
    @Transactional
    public SalesDeliveryNoteItemDtlDto updateItemDetail(Long transactionPoid, Long detRowId,
            CreateSalesDeliveryNoteItemDtlRequest request,
            Long groupPoid, Long companyPoid, String userId) {
        // Validate delivery note exists
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        if ("Y".equals(deliveryNote.getDeleted())) {
            throw new CustomException("Cannot update item details. Delivery note is deleted");
        }

        if ("CLOSED".equals(deliveryNote.getDeliveryStatus())) {
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
                throw new CustomException("Cannot change stock. Item is loaded from quotation.");
            }
            if (request.getQuantity() != null && !request.getQuantity().equals(itemDtl.getQuantity())) {
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

        return convertItemDtlToDto(savedItemDtl);
    }

    @Override
    @Transactional
    public void deleteItemDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid) {
        // Validate delivery note exists
        SalesDeliveryNoteHdr deliveryNote = deliveryNoteHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        if ("Y".equals(deliveryNote.getDeleted())) {
            throw new CustomException("Cannot delete item details. Delivery note is deleted");
        }

        if ("CLOSED".equals(deliveryNote.getDeliveryStatus())) {
            throw new CustomException("Cannot delete item details. Delivery note is closed");
        }

        // Delete item detail
        itemDtlRepository.deleteById(new SalesDeliveryNoteItemDtlId(transactionPoid, detRowId));

        // Recalculate totals
        calculateTotals(transactionPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesDeliveryNoteItemDtlDto> getItemDetails(Long transactionPoid, Long groupPoid, Long companyPoid) {
        // Validate delivery note exists
        deliveryNoteHdrRepository
                .findByTransactionPoidAndGroupPoidAndCompanyPoid(transactionPoid, groupPoid, companyPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery Note", "transactionPoid", transactionPoid));

        List<SalesDeliveryNoteItemDtl> itemDetails = itemDtlRepository.findByTransactionPoid(transactionPoid);
        return itemDetails.stream()
                .map(this::convertItemDtlToDto)
                .collect(Collectors.toList());
    }

    // Helper methods
    private void validateDeliveryNoteRequest(CreateSalesDeliveryNoteRequest request) {
        if (request.getCustomerPoid() == null) {
            throw new CustomException("Customer is required");
        }
        if (request.getTransactionDate() == null) {
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
            dto.setItemDetails(itemDetails.stream()
                    .map(this::convertItemDtlToDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private SalesDeliveryNoteItemDtlDto convertItemDtlToDto(SalesDeliveryNoteItemDtl itemDtl) {
        SalesDeliveryNoteItemDtlDto dto = new SalesDeliveryNoteItemDtlDto();
        BeanUtils.copyProperties(itemDtl, dto);
        return dto;
    }

    // Placeholder for stored procedure call (implemented in Part 2)
    private void callUpdateDeletedDetailsProcedure(Long transactionPoid, Long groupPoid, Long companyPoid,
            String userId) {
        // TODO: Implement stored procedure call
        // PROC_DN_UPDATE_DELETED_DTLSQH
    }

    @Override
    public ValidateCustomerChangeResponse validateCustomerChange(Long transactionPoid, Long newCustomerPoid,
            Long groupPoid, Long companyPoid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'validateCustomerChange'");
    }

    @Override
    public LoadQuotationItemsResponse loadQuotationItems(Long transactionPoid, Long groupPoid, Long companyPoid,
            String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadQuotationItems'");
    }

    @Override
    public SalesDeliveryNoteDependenciesDto checkDeliveryNoteDependencies(Long transactionPoid, Long groupPoid,
            Long companyPoid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkDeliveryNoteDependencies'");
    }
}
