package com.alsharif.shipchandling.salesquotationsch.service;

import com.alsharif.shipchandling.salesquotationsch.dto.*;
import com.alsharif.shipchandling.salesquotationsch.dto.request.*;
import com.alsharif.shipchandling.salesquotationsch.dto.response.SalesQuotationSchListResponse;
import com.alsharif.shipchandling.salesquotationsch.dto.response.StoredProcedureResponse;
import com.alsharif.shipchandling.salesquotationsch.dto.response.ValidationResponse;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SalesQuotationSchService {

        // Basic CRUD Operations
        SalesQuotationSchHdrDto createSalesQuotationSch(CreateSalesQuotationSchRequest request, Long groupPoid,
                        Long companyPoid, String userId);

        SalesQuotationSchHdrDto getSalesQuotationSchByPoid(Long transactionPoid, Long groupPoid,
                        Long companyPoid, Boolean includeDetails);

        SalesQuotationSchHdrDto updateSalesQuotationSch(Long groupPoid, Long transactionPoid,
                        UpdateSalesQuotationSchRequest request,
                        Long companyPoid, String userId);

        void deleteSalesQuotationSch(Long groupPoid, Long transactionPoid, Long companyPoid);

        // Search method with advanced filtering
        SalesQuotationSchListResponse search(SalesQuotationSchFilter filter, String userId);
        
        // Filter method with dynamic filters (similar to stock unit)
        SalesQuotationSchListResponse listSalesQuotationSchWithFilters(FilterRequestDto filterRequest, Long companyPoid, Pageable pageable);

        // Validation APIs
        ValidationResponse validateDocRef(String docRef, Long transactionPoid);

        // Item Details APIs
        SalesQuotationSchItemDtlDto addItemDetail(Long transactionPoid, CreateSalesQuotationSchItemDtlRequest request,
                        Long companyPoid, String userId);

        SalesQuotationSchItemDtlDto updateItemDetail(Long transactionPoid, Long detRowId,
                        CreateSalesQuotationSchItemDtlRequest request,
                        Long companyPoid, String userId);

        void deleteItemDetail(Long transactionPoid, Long detRowId, Long companyPoid);

        List<SalesQuotationSchItemDtlDto> getItemDetails(Long transactionPoid, Long companyPoid);

        List<SalesQuotationSchCustomerDetailsDto> refreshPreviousQuotationData(Long transactionPoid, Long groupPoid, Long companyPoid, Long customerPoid);

        // Stored Procedure Operations
        StoredProcedureResponse importItems(ImportItemsRequest request);
        StoredProcedureResponse clearItems(ClearItemsRequest request);
        StoredProcedureResponse refreshDetail(RefreshDetailRequest request);
        StoredProcedureResponse createRfq(CreateRfqRequest request);
        StoredProcedureResponse createDeliveryNote(CreateDeliveryNoteRequest request);
        StoredProcedureResponse selectAll(SelectAllRequest request);
        ValidationResponse validateCustomer(ValidateCustomerRequest request);
        StoredProcedureResponse updateQuantity(UpdateQuantityRequest request);
        ValidationResponse validateCheckbox(ValidateCheckboxRequest request);
        StoredProcedureResponse calculate(CalculateRequest request);
}
