package com.alsharif.shipchandling.deliverynote.service;

import com.alsharif.shipchandling.deliverynote.dto.*;

import java.sql.Timestamp;
import java.util.List;

public interface SalesDeliveryNoteService {

        // Basic CRUD Operations
        SalesDeliveryNoteHdrDto createDeliveryNote(CreateSalesDeliveryNoteRequest request, Long groupPoid,
                        Long companyPoid, String userId);

        SalesDeliveryNoteHdrDto getDeliveryNoteByPoid(Long transactionPoid, Long groupPoid,
                        Long companyPoid, Boolean includeDetails);

        SalesDeliveryNoteHdrDto updateDeliveryNote(Long groupPoid, Long transactionPoid,
                        CreateSalesDeliveryNoteRequest request,
                        Long companyPoid, String userId);

        void deleteDeliveryNote(Long groupPoid, Long transactionPoid, Long companyPoid);

        PaginatedResponse<SalesDeliveryNoteHdrDto> getAllDeliveryNotes(Long groupPoid, Long companyPoid,
                        String deliveryStatus, Long customerPoid,
                        Long salesmanPoid, String qtnRefNo,
                        Timestamp fromDate, Timestamp toDate,
                        String search, Integer page, Integer size);

        // Validation APIs
        ValidationResponse validateDocRef(String docRef, Long transactionPoid);

        // Item Details APIs
        SalesDeliveryNoteItemDtlDto addItemDetail(Long transactionPoid, CreateSalesDeliveryNoteItemDtlRequest request,
                        Long companyPoid, String userId);

        SalesDeliveryNoteItemDtlDto updateItemDetail(Long transactionPoid, Long detRowId,
                        CreateSalesDeliveryNoteItemDtlRequest request,
                        Long companyPoid, String userId);

        void deleteItemDetail(Long transactionPoid, Long detRowId, Long companyPoid);

        List<SalesDeliveryNoteItemDtlDto> getItemDetails(Long transactionPoid, Long companyPoid);

        // Business Logic APIs
        ValidateCustomerChangeResponse validateCustomerChange(Long transactionPoid,
                        Long companyPoid);

        LoadQuotationItemsResponse loadQuotationItems(Long groupPoid, Long transactionPoid,
                        Long companyPoid, String userId);

        SalesDeliveryNoteDependenciesDto checkDeliveryNoteDependencies(Long transactionPoid,
                        Long companyPoid);
}
