package com.asg.shipchandling.requestforquotation.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.shipchandling.requestforquotation.dto.RfqDependenciesDto;
import com.asg.shipchandling.requestforquotation.dto.request.*;
import com.asg.shipchandling.requestforquotation.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ApRequestForQtnService {
    
    ApRequestForQtnHdrDto createRequestForQuotation(CreateApRequestForQtnRequest request,
                                                    Long groupPoid, Long companyPoid, String userId);
    
    ApRequestForQtnHdrDto getRequestForQuotationByPoid(Long transactionPoid, Long groupPoid, 
                                                        Long companyPoid, Boolean includeDetails);
    
    ApRequestForQtnHdrDto updateRequestForQuotation(Long transactionPoid, UpdateApRequestForQtnRequest request,
                                                     Long groupPoid, Long companyPoid, String userId);
    
    void deleteRequestForQuotation(Long transactionPoid, Long groupPoid, Long companyPoid, DeleteReasonDto deleteReasonDto);

    Map<String, Object> listRequestForQuotations(String docId, FilterRequestDto request, LocalDate startDateValue, LocalDate endDateValue, Pageable pageable);

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

    byte[] printConfirmedSupplier(Long transactionPoid) throws Exception;

    byte[] print(Long transactionPoid) throws Exception;
}