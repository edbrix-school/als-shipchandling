package com.alsharif.shipchandling.stockunitmaster.service;

import com.alsharif.shipchandling.stockunitmaster.dto.FilterRequestDto;
import com.alsharif.shipchandling.stockunitmaster.dto.StockUnitMasterDto;
import com.alsharif.shipchandling.stockunitmaster.dto.UnitDependenciesDto;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface StockUnitService {
     StockUnitMasterDto getStockUnitByPoid(Long stockUnitPoid);

     StockUnitMasterDto createStockUnit(StockUnitMasterDto stockUnitMasterDto);

     StockUnitMasterDto updateStockUnit(Long stockUnitPoid, StockUnitMasterDto stockUnitMasterDto);

     Page<StockUnitMasterDto> listStockUnitsUsingParams(
        String stockUnitCode,
        String stockUnitName,
        String classified,
        String active,
        String deleted,
        Pageable pageable);

     void softDeleteStockUnit(Long stockUnitPoid);

     boolean validateStockUnitCode(String stockUnitCode, Long groupPoid, Long excludeStockUnitPoid);

     boolean validateStockUnitName(String stockUnitName, Long groupPoid, Long excludeStockUnitPoid);

     UnitDependenciesDto checkUnitDependencies(Long stockUnitPoid, Long groupPoid);

     List<StockUnitMasterDto> getActiveStockUnits(Long groupPoid, String classified, String search);


}
