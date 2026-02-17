package com.asg.shipchandling.salesinvoice.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
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
import com.asg.shipchandling.salesinvoice.dto.response.CreditDetailsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadCostBookingsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadDeliveryNoteResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadQuotationAndCostBookingsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadQuotationCurrencyResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadQuotationItemsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.RefreshGpProcResponse;
import com.asg.shipchandling.salesinvoice.dto.response.UnloadQuotationResponse;
import com.asg.shipchandling.salesinvoice.dto.response.ValidationResponse;
import com.asg.shipchandling.salesinvoice.dto.response.VerifyInvoiceResponse;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SalesInvoiceService {

        SalesInvoiceHdrDto createSalesInvoice(CreateSalesInvoiceRequest request,
                                              Long groupPoid, Long companyPoid, String userId);

        SalesInvoiceHdrDto getSalesInvoiceByPoid(Long transactionPoid, Long companyPoid, Boolean includeDetails);

        SalesInvoiceHdrDto updateSalesInvoice(Long transactionPoid, UpdateSalesInvoiceRequest request,
                        Long groupPoid, Long companyPoid, String userId);

        void deleteSalesInvoice(Long transactionPoid, Long groupPoid, Long companyPoid, DeleteReasonDto deleteReasonDto);

        Map<String, Object> listSalesInvoices(String docId, FilterRequestDto request, LocalDate startDateValue, LocalDate endDateValue, Pageable pageable);

        // Validation APIs
        ValidationResponse validateDocRef(String docRef, Long groupPoid, Long companyPoid, Long transactionPoid);

        // Detail Table APIs - Invoice Details
        SalesInvoiceDtlDto addInvoiceDetail(Long transactionPoid, CreateSalesInvoiceDtlRequest request,
                                            Long groupPoid, Long companyPoid, String userId);

        SalesInvoiceDtlDto updateInvoiceDetail(Long transactionPoid, Long detRowId,
                        UpdateSalesInvoiceDtlRequest request,
                        Long groupPoid, Long companyPoid, String userId);

        void deleteInvoiceDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid);

        List<SalesInvoiceDtlDto> getInvoiceDetails(Long transactionPoid, Long groupPoid, Long companyPoid);

        // Detail Table APIs - Delivery Note Details
        SalesDnDtlDto addDeliveryNoteDetail(Long transactionPoid, CreateSalesDnDtlRequest request,
                                            Long groupPoid, Long companyPoid, String userId);

        SalesDnDtlDto updateDeliveryNoteDetail(Long transactionPoid, Long detRowId,
                        UpdateSalesDnDtlRequest request,
                        Long groupPoid, Long companyPoid, String userId);

        void deleteDeliveryNoteDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid);

        List<SalesDnDtlDto> getDeliveryNoteDetails(Long transactionPoid, Long groupPoid, Long companyPoid);

        // Business Logic APIs
        LoadQuotationItemsResponse loadQuotationItems(Long transactionPoid, LoadQuotationItemsRequest request,
                        Long groupPoid, Long companyPoid, String userId);

        LoadQuotationAndCostBookingsResponse loadQuotationAndCostBookings(Long transactionPoid,
                                                                          LoadQuotationItemsRequest request, Long groupPoid, Long companyPoid, String userId);

        LoadDeliveryNoteResponse loadDeliveryNote(Long transactionPoid, Long groupPoid,
                        Long companyPoid, String userId);

        UnloadQuotationResponse unloadQuotation(Long transactionPoid, Long groupPoid,
                        Long companyPoid, String userId);

        VerifyInvoiceResponse verifyInvoice(Long transactionPoid,
                        Long groupPoid, Long companyPoid, String userId);

        RefreshGpProcResponse calculateGp(Long transactionPoid, CalculateDiscountCommissionRequest request, Long groupPoid, Long companyPoid, Long userPoid);

        LoadCostBookingsResponse loadCostBookings(Long transactionPoid, Long groupPoid,
                        Long companyPoid, String userId);

        List<SalesInvCostbkdDtlDto> getCostBookedDetails(Long transactionPoid, Long groupPoid, Long companyPoid);

        CreditDetailsResponse loadCreditDetails(Long customerPoid, Long groupPoid,
                        Long companyPoid,      CreditDetailsRequest request);

        CalculateDueDateResponse calculateDueDate(Long transactionPoid, Timestamp transactionDate, Long creditDays,
                        Long groupPoid, Long companyPoid);

        SalesInvoiceDependenciesDto checkSalesInvoiceDependencies(Long transactionPoid,
                                                                  Long groupPoid, Long companyPoid);

        LoadQuotationCurrencyResponse loadQuotationCurrency(Long transactionPoid, Long qtnPoid);

        ValidationResponse validateCustomer(Long customerPoid, Long groupPoid, Long companyPoid);

        CalculateDiscountCommissionResponse calculateItemDiscountCommission(Long transactionPoid, CalculateDiscountCommissionRequest request,
                        Long detRowId, Long groupPoid, Long companyPoid, Long userId);

        CalculateDiscountCommissionResponse calculateHeaderDiscountCommission(Long transactionPoid, CalculateDiscountCommissionRequest request,
                        Long detRowId, Long groupPoid, Long companyPoid, Long userId);

    byte[] print(Long transactionPoid) throws Exception;
}
