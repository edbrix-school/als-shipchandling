package com.asg.shipchandling.deliverynote.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.shipchandling.deliverynote.dto.*;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SalesDeliveryNoteService {

    // Basic CRUD Operations
    SalesDeliveryNoteHdrDto createDeliveryNote(CreateSalesDeliveryNoteRequest request, Long groupPoid, Long companyPoid, String userId);

    SalesDeliveryNoteHdrDto getDeliveryNoteByPoid(Long transactionPoid, Long groupPoid, Long companyPoid, Boolean includeDetails);

    SalesDeliveryNoteHdrDto updateDeliveryNote(Long groupPoid, Long transactionPoid, UpdateSalesDeliveryNoteRequest request, Long companyPoid, String userId);

    void deleteDeliveryNote(Long groupPoid, Long transactionPoid, Long companyPoid, DeleteReasonDto deleteReasonDto);

    PaginatedResponse<SalesDeliveryNoteHdrDto> getAllDeliveryNotes(Long groupPoid, Long companyPoid, String deliveryStatus, Long customerPoid, Long salesmanPoid, String qtnRefNo, LocalDateTime fromDate, LocalDateTime toDate, String search, Integer page, Integer size);

//    Page<SalesDeliveryNoteHdrDto> getAllDeliveryNotesWithFilters(Long groupPoid, Long companyPoid, GetAllDeliveryNoteFilterRequest filterRequest, int page, int size);

    // Validation APIs
    ValidationResponse validateDocRef(String docRef, Long transactionPoid);

    // Item Details APIs
    SalesDeliveryNoteItemDtlDto addItemDetail(Long transactionPoid, CreateSalesDeliveryNoteItemDtlRequest request, Long companyPoid, String userId);

    SalesDeliveryNoteItemDtlDto updateItemDetail(Long transactionPoid, Long detRowId, CreateSalesDeliveryNoteItemDtlRequest request, Long companyPoid, String userId);

    void deleteItemDetail(Long transactionPoid, Long detRowId, Long companyPoid);

    List<SalesDeliveryNoteItemDtlDto> getItemDetails(Long transactionPoid, Long companyPoid);

    // Business Logic APIs
    ValidateCustomerChangeResponse validateCustomerChange(Long transactionPoid, Long customerPoid);

    List<QuotationItemDto> loadQuotationItems(Long groupPoid, Long transactionPoid, Long companyPoid, Long userPoid);

    SalesDeliveryNoteDependenciesDto checkDeliveryNoteDependencies(Long transactionPoid, Long companyPoid);

    Map<String, Object> listDeliveryNotes(String docId, FilterRequestDto request, LocalDate startDateValue, LocalDate endDateValue, Pageable pageable);

    byte[] print(Long transactionPoid) throws Exception;
}
