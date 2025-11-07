package com.alsharif.shipchandling.stockunitmaster.service;

import com.alsharif.shipchandling.exceptions.GlobalExceptionHandler;
import com.alsharif.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import com.alsharif.shipchandling.group.repository.GroupRepository;
import com.alsharif.shipchandling.stockunitmaster.dto.FilterDto;
import com.alsharif.shipchandling.stockunitmaster.dto.FilterRequestDto;
import com.alsharif.shipchandling.stockunitmaster.dto.StockUnitMasterDto;
import com.alsharif.shipchandling.stockunitmaster.entity.StockUnitMaster;
import com.alsharif.shipchandling.stockunitmaster.repository.StockUnitRepository;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StockUnitServiceImpl implements StockUnitService {

    @Autowired
    StockUnitRepository stockUnitRepository;

    @Autowired
    GroupRepository groupRepository;

        private static final Logger log = LoggerFactory.getLogger(StockUnitServiceImpl.class);

    @Override
    public StockUnitMasterDto getStockUnitByPoid(Long stockUnitPoid) {
        if (!stockUnitRepository.existsByStockUnitPoid(stockUnitPoid)) {
            throw new ResourceNotFoundException("StockUnit", "stockUnitPoid", stockUnitPoid);
        }
        StockUnitMasterDto stockUnitMasterDto = new StockUnitMasterDto();

        StockUnitMaster stockUnitMaster = stockUnitRepository.findByStockUnitPoid(stockUnitPoid);
        BeanUtils.copyProperties(stockUnitMaster, stockUnitMasterDto);
        return stockUnitMasterDto;
    }

    @Override
    public StockUnitMasterDto createStockUnit(StockUnitMasterDto stockUnitMasterDto) {
        StockUnitMasterDto responseDto = new StockUnitMasterDto();

        if (stockUnitRepository.existsByStockUnitCode(stockUnitMasterDto.getStockUnitCode())) {
            throw new ResourceAlreadyExistsException("stockUnitCode", stockUnitMasterDto.getStockUnitCode());
        }

        if (stockUnitRepository.existsByStockUnitName(stockUnitMasterDto.getStockUnitName())) {
            throw new ResourceAlreadyExistsException("stockUnitName", stockUnitMasterDto.getStockUnitName());
        }

        groupRepository.findById(stockUnitMasterDto.getGroupPoid()).orElseThrow(
                () -> new ResourceNotFoundException("Group", "groupPoid", stockUnitMasterDto.getGroupPoid()));

        StockUnitMaster entity = mapDtoToEntity(stockUnitMasterDto);

        StockUnitMaster responseEntity = stockUnitRepository.save(entity);
        BeanUtils.copyProperties(responseEntity, responseDto);

        return responseDto;
    }

    private StockUnitMaster mapDtoToEntity(StockUnitMasterDto dto) {
        StockUnitMaster entity = new StockUnitMaster();
        entity.setStockUnitCode(dto.getStockUnitCode());
        entity.setStockUnitName(dto.getStockUnitName());
        entity.setStockUnitName2(dto.getStockUnitName2());
        entity.setGroupPoid(dto.getGroupPoid());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedDate(LocalDateTime.now());
        entity.setActive(dto.getActive());
        entity.setSeqNo(dto.getSeqNo());
        entity.setDeleted("N");
        entity.setClassified(dto.getClassified());

        return entity;
    }

    @Override
    @Transactional
    public StockUnitMasterDto updateStockUnit(Long stockUnitPoid, StockUnitMasterDto stockUnitMasterDto) {
        StockUnitMaster existingStockUnit = stockUnitRepository.findByStockUnitPoid(stockUnitPoid);
        if (!stockUnitRepository.existsByStockUnitPoid(stockUnitPoid)) {
            throw new ResourceNotFoundException("StockUnit", "stockUnitPoid", stockUnitPoid);
        }

         if (stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot(stockUnitMasterDto.getStockUnitCode(),
                stockUnitMasterDto.getStockUnitPoid())) {
            throw new ResourceAlreadyExistsException("Stock Unit Code already exists, please enter unique code.",
                    stockUnitMasterDto.getStockUnitCode());
        }
        if (stockUnitRepository.existsByStockUnitNameIgnoreCaseAndStockUnitPoidNot(stockUnitMasterDto.getStockUnitName(),
                stockUnitMasterDto.getStockUnitPoid())) {
            throw new ResourceAlreadyExistsException("Stock Unit Name already exists, please enter unique name.",
                    stockUnitMasterDto.getStockUnitName());
        }

        groupRepository.findById(stockUnitMasterDto.getGroupPoid()).orElseThrow(
                () -> new ResourceNotFoundException("Group", "groupPoid", stockUnitMasterDto.getGroupPoid()));

        if (stockUnitMasterDto.getStockUnitName() != null) {
            existingStockUnit.setStockUnitName(stockUnitMasterDto.getStockUnitName());
        }
        if (stockUnitMasterDto.getStockUnitName2() != null) {
            existingStockUnit.setStockUnitName2(stockUnitMasterDto.getStockUnitName2());
        }
        if (stockUnitMasterDto.getActive() != null) {
            existingStockUnit.setActive(stockUnitMasterDto.getActive());
        }
        if (stockUnitMasterDto.getGroupPoid() != null) {
            existingStockUnit.setGroupPoid(stockUnitMasterDto.getGroupPoid());
        }
        if (stockUnitMasterDto.getSeqNo() != null) {
            existingStockUnit.setSeqNo(stockUnitMasterDto.getSeqNo());
        }
        existingStockUnit.setLastModifiedDate(LocalDateTime.now());

        StockUnitMaster updatedStockUnit = stockUnitRepository.save(existingStockUnit);
        StockUnitMasterDto responseDto = new StockUnitMasterDto();
        BeanUtils.copyProperties(updatedStockUnit, responseDto);

        responseDto.setActive(updatedStockUnit.getActive());

        // Set audit fields
        responseDto.setCreatedBy(updatedStockUnit.getCreatedBy());
        responseDto.setCreatedDate(updatedStockUnit.getCreatedDate() != null
                ? updatedStockUnit.getCreatedDate().atOffset(java.time.ZoneOffset.UTC)
                : null);
        responseDto.setLastModifiedBy(updatedStockUnit.getLastModifiedBy());
        responseDto.setLastModifiedDate(updatedStockUnit.getLastModifiedDate() != null
                ? updatedStockUnit.getLastModifiedDate().atOffset(java.time.ZoneOffset.UTC)
                : null);

        return responseDto;
    }

    @Override
    public void softDeleteStockUnit(Long stockUnitPoid) {
        StockUnitMaster existingStockunit = stockUnitRepository.findByStockUnitPoid(stockUnitPoid);
        if (!stockUnitRepository.existsByStockUnitPoid(stockUnitPoid)) {
            throw new ResourceNotFoundException("StockUnit", "stockUnitPoid", stockUnitPoid);
        }
        existingStockunit.setDeleted("Y");
        existingStockunit.setActive("N");
        existingStockunit.setLastModifiedDate(LocalDateTime.now());
        existingStockunit.setLastModifiedBy(existingStockunit.getLastModifiedBy());
        stockUnitRepository.save(existingStockunit);
    } 

    @Override
    public Page<StockUnitMasterDto> listStockUnits(String docId, FilterRequestDto request, Pageable pageable) {
        Page<StockUnitMaster> countries = stockUnitRepository.findAll(pageable);
        List<StockUnitMasterDto> stockUnitMasterDtos = new ArrayList<>();
        countries.forEach(Country -> {
            StockUnitMasterDto countryDto = new StockUnitMasterDto();
            BeanUtils.copyProperties(Country, countryDto);
            stockUnitMasterDtos.add(countryDto);
        });
        Page<StockUnitMasterDto> result = new PageImpl<>(stockUnitMasterDtos, pageable,
                countries.getTotalElements());
        return result;
    }

    // @Override
    // public Map<String, Object> listCountries(String docId, FilterRequestDto
    // request, Pageable pageable) {
    // String operator = documentService.resolveOperator(request);
    // String isDeleted = documentService.resolveIsDeleted(request);
    // List<FilterDto> filters = documentService.resolveFilters(request);

    // RawSearchResult raw = documentService.search(docId, filters, operator,
    // pageable, isDeleted,
    // "COUNTRY_NAME", // label
    // "COUNTRY_POID"); // value

    // Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable,
    // raw.totalRecords());

    // return PaginationUtil.wrapPage(page, raw.displayFields());
    // }

}
