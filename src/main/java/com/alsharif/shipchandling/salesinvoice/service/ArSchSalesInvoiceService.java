package com.alsharif.shipchandling.salesinvoice.service;

import com.alsharif.shipchandling.salesinvoice.dto.*;
import com.alsharif.shipchandling.salesinvoice.dto.ArSchSalesInvoiceHdrDto;
import com.alsharif.shipchandling.salesinvoice.dto.CreateArSchSalesInvoiceRequest;
import com.alsharif.shipchandling.salesinvoice.dto.UpdateArSchSalesInvoiceRequest;


import java.sql.Timestamp;
import java.util.List;

public interface ArSchSalesInvoiceService {
    
    ArSchSalesInvoiceHdrDto createSalesInvoice(CreateArSchSalesInvoiceRequest request, 
                                                Long groupPoid, Long companyPoid, String userId);
    
    ArSchSalesInvoiceHdrDto getSalesInvoiceByPoid(Long transactionPoid, Long groupPoid, 
                                                   Long companyPoid, Boolean includeDetails);
    
    ArSchSalesInvoiceHdrDto updateSalesInvoice(Long transactionPoid, UpdateArSchSalesInvoiceRequest request, 
                                                Long groupPoid, Long companyPoid, String userId);
    
    void deleteSalesInvoice(Long transactionPoid, Long groupPoid, Long companyPoid);
    
    List<ArSchSalesInvoiceHdrDto> getAllSalesInvoices(Long groupPoid, Long companyPoid, 
                                                       String invStatus, String verified, 
                                                       Long customerPoid, Long principalPoid, 
                                                       Long qtnPoid, String search);
    
    // Validation APIs
    ValidationResponse validateDocRef(String docRef, Long groupPoid, Long companyPoid, Long transactionPoid);
    
    // Detail Table APIs - Invoice Details
    ArSchSalesInvoiceDtlDto addInvoiceDetail(Long transactionPoid, CreateArSchSalesInvoiceDtlRequest request, 
                                              Long groupPoid, Long companyPoid, String userId);
    
    ArSchSalesInvoiceDtlDto updateInvoiceDetail(Long transactionPoid, Long detRowId, 
                                                 CreateArSchSalesInvoiceDtlRequest request, 
                                                 Long groupPoid, Long companyPoid, String userId);
    
    void deleteInvoiceDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid);
    
    List<ArSchSalesInvoiceDtlDto> getInvoiceDetails(Long transactionPoid, Long groupPoid, Long companyPoid);
    
    // Detail Table APIs - Delivery Note Details
    ArSchSalesDnDtlDto addDeliveryNoteDetail(Long transactionPoid, CreateArSchSalesDnDtlRequest request, 
                                              Long groupPoid, Long companyPoid, String userId);
    
    ArSchSalesDnDtlDto updateDeliveryNoteDetail(Long transactionPoid, Long detRowId, 
                                                 CreateArSchSalesDnDtlRequest request, 
                                                 Long groupPoid, Long companyPoid, String userId);
    
    void deleteDeliveryNoteDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid);
    
    List<ArSchSalesDnDtlDto> getDeliveryNoteDetails(Long transactionPoid, Long groupPoid, Long companyPoid);
    
    // Business Logic APIs
    LoadQuotationResponse loadQuotation(Long transactionPoid, LoadQuotationRequest request, 
                                        Long groupPoid, Long companyPoid, String userId);
    
    LoadDeliveryNoteResponse loadDeliveryNote(Long transactionPoid, Long groupPoid, 
                                              Long companyPoid, String userId);
    
    VerifyInvoiceResponse verifyInvoice(Long transactionPoid, VerifyInvoiceRequest request, 
                                        Long groupPoid, Long companyPoid, String userId);
    
    CalculateGpResponse calculateGp(Long transactionPoid, Long groupPoid, Long companyPoid, String userId);
    
    LoadCostBookingsResponse loadCostBookings(Long transactionPoid, Long groupPoid, 
                                               Long companyPoid, String userId);
    
    LoadCreditDetailsResponse loadCreditDetails(Long customerPoid, Long groupPoid, 
                                                 Long companyPoid, String userId);
    
    CalculateDueDateResponse calculateDueDate(Timestamp transactionDate, Long creditDays, 
                                              Long groupPoid, Long companyPoid, String userId);
    
    SalesInvoiceDependenciesDto checkSalesInvoiceDependencies(Long transactionPoid, 
                                                               Long groupPoid, Long companyPoid);
}
