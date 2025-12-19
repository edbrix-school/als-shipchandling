package com.asg.shipchandling.StockMaster.service;

import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.shipchandling.StockMaster.dto.CreateStockMasterDtlRequest;
import com.asg.shipchandling.StockMaster.dto.CreateStockMasterRequest;
import com.asg.shipchandling.StockMaster.dto.CreateStockMasterWarehouseDtlRequest;
import com.asg.shipchandling.StockMaster.dto.StockMasterDependenciesDto;
import com.asg.shipchandling.StockMaster.dto.StockMasterDtlDto;
import com.asg.shipchandling.StockMaster.dto.StockMasterDto;
import com.asg.shipchandling.StockMaster.dto.StockMasterViewResponse;
import com.asg.shipchandling.StockMaster.dto.StockMasterWarehouseDtlDto;
import com.asg.shipchandling.StockMaster.dto.UpdateStockMasterRequest;
import com.asg.shipchandling.StockMaster.dto.ValidationResponse;
import com.asg.shipchandling.StockMaster.dto.StockDetailsResponse;
import com.asg.shipchandling.StockMaster.entity.StockMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface StockMasterService {
    StockMasterViewResponse getStockMasterById(Long stockPoid, boolean includeDetails, Long groupPoid);

    Page<StockMasterEntity> getStockMasters(Map<String, String> filters, Pageable pageable);

    List<Map<String, Object>> getStockMastersTree(Long groupPoid);

    ValidationResponse validateStockCode(String stockCode, Long groupPoid, Long stockPoid);

    ValidationResponse validateStockName(String stockName, Long groupPoid, Long excludeStockPoid);

    StockMasterDependenciesDto checkStockMasterDependencies(Long stockPoid, Long groupPoid);

    void deleteStockMaster(Long stockPoid, Long groupPoid);

    StockMasterDto createStockMaster(CreateStockMasterRequest request, Long groupPoid, Long companyPoid, String userId);

    StockMasterDto updateStockMaster(Long stockPoid, UpdateStockMasterRequest request, Long groupPoid, Long companyPoid, String userId);

    StockMasterDtlDto addSupplierDetail(Long stockPoid, CreateStockMasterDtlRequest request, 
                                        Long groupPoid, String userId);

    StockMasterDtlDto updateSupplierDetail(Long stockPoid, Long detRowId, 
                                           CreateStockMasterDtlRequest request, 
                                           Long groupPoid, String userId);

    void deleteSupplierDetail(Long stockPoid, Long detRowId, Long groupPoid);

     List<StockMasterDtlDto> getSupplierDetails(Long stockPoid, Long groupPoid);

    StockMasterWarehouseDtlDto addWarehouseDetail(Long stockPoid, CreateStockMasterWarehouseDtlRequest request, 
                                                    Long groupPoid, String userId);



    StockMasterWarehouseDtlDto updateWarehouseDetail(Long stockPoid, Long detRowId, 
                                                       CreateStockMasterWarehouseDtlRequest request, 
                                                       Long groupPoid, String userId);

    void deleteWarehouseDetail(Long stockPoid, Long detRowId, Long groupPoid);

    List<StockMasterWarehouseDtlDto> getWarehouseDetails(Long stockPoid, Long groupPoid);

    StockMasterDto getStockMasterByBarcode(String barcode, Long groupPoid);

    List<Map<String, Object>> getStockMastersHierarchical(Long groupPoid, Long parentPoid, String filterValue, boolean includeDeleted, Long companyPoid, Long userPoid);

    List<Map<String, Object>> getStockMastersTreeStructure(Long groupPoid, String filterValue, boolean includeDeleted, Long companyPoid, Long userPoid);

    StockDetailsResponse getStockDetails(Long stockPoid, Long companyPoid);
    
    StockDetailsResponse getStockDetailsByCode(String stockCode, Long companyPoid);

    Map<String, Object> listStockMaster(String docId, FilterRequestDto request, Pageable pageable);

}
