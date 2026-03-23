package com.asg.shipchandling.salesquotationsch.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.shipchandling.salesquotationsch.dto.*;
import com.asg.shipchandling.salesquotationsch.dto.request.*;
import com.asg.shipchandling.salesquotationsch.dto.response.SalesQuotationSchListResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.StoredProcedureResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.ValidationResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.ExcelImportResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.AddressDetailsResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.CurrencyRateResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SalesQuotationSchService {

        // Basic CRUD Operations
        SalesQuotationSchHdrDto createSalesQuotationSch(CreateSalesQuotationSchRequest request, Long groupPoid,
                                                        Long companyPoid, Long userPoid, String userId);

        SalesQuotationSchHdrDto getSalesQuotationSchByPoid(Long transactionPoid, Long groupPoid,
                        Long companyPoid, Long userPoid, Boolean includeDetails);

        SalesQuotationSchHdrDto updateSalesQuotationSch(Long groupPoid, Long transactionPoid,
                        UpdateSalesQuotationSchRequest request,
                        Long companyPoid, Long userPoid, String userId);

        void deleteSalesQuotationSch(Long groupPoid, Long transactionPoid, Long companyPoid, DeleteReasonDto deleteReasonDto);

        // Search method with advanced filtering
        SalesQuotationSchListResponse search(SalesQuotationSchFilter filter, String userId);
        
        // Filter method with dynamic filters (similar to stock unit)
        Map<String, Object> listSalesQuotationSch(String docId, FilterRequestDto request, LocalDate startDateValue, LocalDate endDateValue, Pageable pageable);

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
        
        // Excel Import
        ExcelImportResponse importItemsFromExcel(Long transactionPoid, Long companyPoid, String userId, 
                MultipartFile file);
        
        List<AddressDetailsResponse> getCustomerAddress(Long userPoid, Long customerPoid, String addressType);
        
        AddressDetailsResponse getAddressDetailsByPoid(BigDecimal addressPoid);
        
        // Currency Rate
        CurrencyRateResponse getLatestCurrencyRate(Long currencyPoid);

    byte[] print(Long transactionPoid) throws Exception;
}
