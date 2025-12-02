package com.asg.shipchandling.requestforquotation.service;

import com.asg.shipchandling.requestforquotation.dto.RfqDependenciesDto;
import com.asg.shipchandling.requestforquotation.dto.request.*;
import com.asg.shipchandling.requestforquotation.dto.response.*;
import com.asg.shipchandling.requestforquotation.dto.request.*;
import com.asg.shipchandling.requestforquotation.dto.response.*;

import java.math.BigDecimal;
import java.util.List;

public interface ApRequestForQtnService {
    
    ApRequestForQtnHdrDto createRequestForQuotation(CreateApRequestForQtnRequest request,
                                                    Long groupPoid, Long companyPoid, String userId);
    
    ApRequestForQtnHdrDto getRequestForQuotationByPoid(Long transactionPoid, Long groupPoid, 
                                                        Long companyPoid, Boolean includeDetails);
    
    ApRequestForQtnHdrDto updateRequestForQuotation(Long transactionPoid, UpdateApRequestForQtnRequest request,
                                                     Long groupPoid, Long companyPoid, String userId);
    
    void deleteRequestForQuotation(Long transactionPoid, Long groupPoid, Long companyPoid);
    
    org.springframework.data.domain.Page<ApRequestForQtnHdrDto> getAllRequestForQuotations(Long groupPoid, Long companyPoid,
                                                            String status, Long divisionPoid,
                                                            Long salesQtnPoid, String search,
                                                            java.time.LocalDate fromDate, java.time.LocalDate toDate,
                                                            int page, int size);
    
    org.springframework.data.domain.Page<ApRequestForQtnListResponseDto> getAllRequestForQuotationsWithFilters(
                                                            Long groupPoid, Long companyPoid,
                                                            GetAllRfqFilterRequest filterRequest,
                                                            int page, int size);
    
    // Detail Table APIs
    ApRequestForQtnItemDtlDto addItemDetail(Long transactionPoid, CreateApRequestForQtnItemDtlRequest request,
                                            Long groupPoid, Long companyPoid, String userId);
    
    ApRequestForQtnItemDtlDto updateItemDetail(Long transactionPoid, Long detRowId, 
                                                CreateApRequestForQtnItemDtlRequest request, 
                                                Long groupPoid, Long companyPoid, String userId);
    
    void deleteItemDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid);
    
    List<ApRequestForQtnItemDtlDto> getItemDetails(Long transactionPoid, Long groupPoid, Long companyPoid);
    
    ApRequestForQtnSupDtlDto addSupplierDetail(Long transactionPoid, CreateApRequestForQtnSupDtlRequest request,
                                               Long groupPoid, Long companyPoid, String userId);
    
    ApRequestForQtnSupDtlDto updateSupplierDetail(Long transactionPoid, Long detRowId, 
                                                   CreateApRequestForQtnSupDtlRequest request, 
                                                   Long groupPoid, Long companyPoid, String userId);
    
    void deleteSupplierDetail(Long transactionPoid, Long detRowId, Long groupPoid, Long companyPoid);
    
    List<ApRequestForQtnSupDtlDto> getSupplierDetails(Long transactionPoid, Long groupPoid, Long companyPoid);
    
    // Business Logic APIs
    AddSuppliersResponse addRelatedSuppliers(Long transactionPoid, Long groupPoid, Long companyPoid, String userId);
    
    SendMailResponse sendMailToSuppliers(Long transactionPoid, Long groupPoid, Long companyPoid, String userId);
    
    CreatePurchaseOrderResponse createPurchaseOrder(Long transactionPoid, Long supplierPoid,
                                                    Long groupPoid, Long companyPoid, String userId);
    
    UpdateCostResponse updateCost(Long transactionPoid, Boolean confirm,
                                  Long groupPoid, Long companyPoid, String userId);
    
    LastPriceResponse getLastPrice(Long stockPoid, Long stockUnitPoid, Long supplierPoid,
                                   Long groupPoid, Long companyPoid, String userId);
    
    DefaultUnitResponse getDefaultStockUnit(Long stockPoid);
    
    ItemsWithoutSuppliersResponse getItemsWithoutSuppliers(Long transactionPoid,
                                                           Long groupPoid, Long companyPoid, String userId);
    
    RfqDependenciesDto checkRfqDependencies(Long transactionPoid, Long groupPoid, Long companyPoid);

    BigDecimal getTaxPercentage(Long groupPoid, Long companyPoid, Long taxPoid);
}