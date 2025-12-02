package com.asg.shipchandling.stockunitmaster.service;

import com.asg.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.group.repository.GroupRepository;
import com.asg.shipchandling.stockunitmaster.dto.FilterDto;
import com.asg.shipchandling.stockunitmaster.dto.FilterRequestDto;
import com.asg.shipchandling.stockunitmaster.dto.StockUnitListResponse;
import com.asg.shipchandling.stockunitmaster.dto.StockUnitMasterDto;
import com.asg.shipchandling.stockunitmaster.dto.UnitDependenciesDto;
import com.asg.shipchandling.stockunitmaster.entity.StockUnitMaster;
import com.asg.shipchandling.stockunitmaster.repository.StockUnitRepository;

// import jakarta.transaction.Transactional;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        if (stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot(
                stockUnitMasterDto.getStockUnitCode(),
                stockUnitMasterDto.getStockUnitPoid())) {
            throw new ResourceAlreadyExistsException("Stock Unit Code already exists, please enter unique code.",
                    stockUnitMasterDto.getStockUnitCode());
        }
        if (stockUnitRepository.existsByStockUnitNameIgnoreCaseAndStockUnitPoidNot(
                stockUnitMasterDto.getStockUnitName(),
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

    // @Override
    // public Page<StockUnitMasterDto> listStockUnits(String docId, FilterRequestDto request, Pageable pageable) {
    //     Page<StockUnitMaster> countries = stockUnitRepository.findAll(pageable);
    //     List<StockUnitMasterDto> stockUnitMasterDtos = new ArrayList<>();
    //     countries.forEach(Country -> {
    //         StockUnitMasterDto countryDto = new StockUnitMasterDto();
    //         BeanUtils.copyProperties(Country, countryDto);
    //         stockUnitMasterDtos.add(countryDto);
    //     });
    //     Page<StockUnitMasterDto> result = new PageImpl<>(stockUnitMasterDtos, pageable,
    //             countries.getTotalElements());
    //  return result;
    // }

  @Override
public Page<StockUnitMasterDto> listStockUnitsUsingParams(
        String stockUnitCode,
        String stockUnitName,
        String classified,
        String active,
        String deleted,
        Pageable pageable) {

    Specification<StockUnitMaster> spec = Specification.where(null);

    if (stockUnitCode != null && !stockUnitCode.isEmpty()) {
        spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("stockUnitCode")),
                        "%" + stockUnitCode.toLowerCase() + "%"));
    }

    if (stockUnitName != null && !stockUnitName.isEmpty()) {
        spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("stockUnitName")),
                        "%" + stockUnitName.toLowerCase() + "%"));
    }

    if (classified != null && !classified.isEmpty()) {
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("classified"), classified));
    }

    if (active != null && !active.isEmpty()) {
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("active"), active));
    }

    if (deleted != null && !deleted.isEmpty()) {
        spec = spec.and((root, query, cb) ->
                cb.equal(root.get("deleted"), deleted));
    }

    Page<StockUnitMaster> page = stockUnitRepository.findAll(spec, pageable);

    List<StockUnitMasterDto> dtoList = page.getContent().stream().map(entity -> {
        StockUnitMasterDto dto = new StockUnitMasterDto();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }).toList();

    return new PageImpl<>(dtoList, pageable, page.getTotalElements());
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

    @Override
    public boolean validateStockUnitCode(String stockUnitCode, Long groupPoid, Long excludeStockUnitPoid) {
        boolean exists;

        if (excludeStockUnitPoid != null) {
            // For update case – exclude the current record
            exists = stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndGroupPoidAndStockUnitPoidNot(
                    stockUnitCode, groupPoid, excludeStockUnitPoid);
        } else {
            // For create case
            exists = stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndGroupPoid(
                    stockUnitCode, groupPoid);
        }

        // Return true if unique (doesn't exist)
        return !exists;
    }

    @Override
    public boolean validateStockUnitName(String stockUnitName, Long groupPoid, Long excludeStockUnitPoid) {
        boolean exists;

        if (excludeStockUnitPoid != null) {
            // For update case – exclude the current record
            exists = stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoidAndStockUnitPoidNot(
                    stockUnitName, groupPoid, excludeStockUnitPoid);
        } else {
            // For create case
            exists = stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid(
                    stockUnitName, groupPoid);
        }

        // Return true if unique (doesn't exist)
        return !exists;
    }

    @Override
    @Transactional(readOnly = true)
    public UnitDependenciesDto checkUnitDependencies(Long stockUnitPoid, Long groupPoid) {
        StockUnitMaster unit = stockUnitRepository
                .findByStockUnitPoidAndGroupPoid(stockUnitPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Unit", "stockUnitPoid", stockUnitPoid));

        Long stockItemCount = stockUnitRepository.countStockItemsByStockUnitPoid(stockUnitPoid);

        UnitDependenciesDto dto = new UnitDependenciesDto();
        dto.setStockUnitPoid(stockUnitPoid);
        dto.setStockItemCount(stockItemCount);
        dto.setCanDelete(stockItemCount == 0);

        if (dto.getCanDelete()) {
            dto.setReason("No dependencies");
            dto.setMessage("Unit can be deleted. No dependencies found.");
        } else {
            dto.setReason("Unit has dependencies");
            dto.setMessage(String.format("Cannot delete unit. It is used by %d stock items.", stockItemCount));
        }

        return dto;
    }


    @Override
    @Transactional(readOnly = true)
    public List<StockUnitMasterDto> getActiveStockUnits(Long groupPoid, String classified, String search) {
        List<StockUnitMaster> units = stockUnitRepository.findActiveUnitsByGroupPoid(groupPoid);

        return units.stream()
                .filter(u -> classified == null || (u.getClassified() != null && classified.equals(u.getClassified())))
                .filter(u -> {
                    if (search == null || search.trim().isEmpty()) {
                        return true;
                    }
                    String searchLower = search.toLowerCase();
                    return (u.getStockUnitCode() != null && u.getStockUnitCode().toLowerCase().contains(searchLower)) ||
                           (u.getStockUnitName() != null && u.getStockUnitName().toLowerCase().contains(searchLower));
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

     private StockUnitMasterDto convertToDto(StockUnitMaster entity) {
        StockUnitMasterDto dto = new StockUnitMasterDto();
        dto.setStockUnitPoid(entity.getStockUnitPoid());
        dto.setStockUnitCode(entity.getStockUnitCode());
        dto.setStockUnitName(entity.getStockUnitName());
        dto.setClassified(entity.getClassified());
        dto.setGroupPoid(entity.getGroupPoid());
        dto.setSeqNo(entity.getSeqNo());
        dto.setActive(entity.getActive());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StockUnitListResponse listStockUnitsWithFilters(FilterRequestDto filterRequest, Pageable pageable) {
        Specification<StockUnitMaster> spec = null;

        // Handle isDeleted filter
        String isDeleted = filterRequest.isDeleted();
        if (isDeleted != null && !isDeleted.trim().isEmpty()) {
            if ("Y".equalsIgnoreCase(isDeleted)) {
                // Show only deleted records
                spec = (root, query, cb) -> 
                    cb.equal(root.get("deleted"), "Y");
            } else if ("N".equalsIgnoreCase(isDeleted)) {
                // Show only non-deleted records
                spec = (root, query, cb) -> 
                    cb.or(
                        cb.isNull(root.get("deleted")),
                        cb.notEqual(root.get("deleted"), "Y")
                    );
            }
        } else {
            // Default: Show only non-deleted records when isDeleted is not provided
            spec = (root, query, cb) -> 
                cb.or(
                    cb.isNull(root.get("deleted")),
                    cb.notEqual(root.get("deleted"), "Y")
                );
        }

        // Handle operator and filters
        String operator = filterRequest.operator() != null ? filterRequest.operator().toUpperCase() : "OR";
        List<FilterDto> filters = filterRequest.filters() != null ? filterRequest.filters() : new ArrayList<>();

        if (!filters.isEmpty()) {
            boolean isAndOperator = "AND".equals(operator);
            List<Specification<StockUnitMaster>> filterSpecs = new ArrayList<>();
            
            for (FilterDto filter : filters) {
                String searchField = filter.searchField();
                String searchValue = filter.searchValue();
                
                if (searchField == null || searchValue == null || searchValue.trim().isEmpty()) {
                    continue;
                }

                Specification<StockUnitMaster> filterSpec = buildFilterSpecification(searchField, searchValue);
                
                if (filterSpec != null) {
                    filterSpecs.add(filterSpec);
                }
            }
            
            // Combine filters based on operator
            if (!filterSpecs.isEmpty()) {
                Specification<StockUnitMaster> combinedFilterSpec = filterSpecs.get(0);
                for (int i = 1; i < filterSpecs.size(); i++) {
                    if (isAndOperator) {
                        combinedFilterSpec = combinedFilterSpec.and(filterSpecs.get(i));
                    } else {
                        combinedFilterSpec = combinedFilterSpec.or(filterSpecs.get(i));
                    }
                }
                if (spec != null) {
                    spec = spec.and(combinedFilterSpec);
                } else {
                    spec = combinedFilterSpec;
                }
            }
        }

        // Execute query
        Page<StockUnitMaster> page;
        if (spec != null) {
            page = stockUnitRepository.findAll(spec, pageable);
        } else {
            page = stockUnitRepository.findAll(pageable);
        }

        // Convert to DTOs
        List<StockUnitMasterDto> dtoList = page.getContent().stream()
            .map(entity -> {
                StockUnitMasterDto dto = new StockUnitMasterDto();
                BeanUtils.copyProperties(entity, dto);
                return dto;
            })
            .collect(Collectors.toList());

        // Build display fields map
        Map<String, String> displayFields = new HashMap<>();
        displayFields.put("stockUnitCode", "text");
        displayFields.put("stockUnitName", "text");

        // Create response
        StockUnitListResponse response = new StockUnitListResponse();
        response.setContent(dtoList);
        response.setLast(page.isLast());
        response.setTotalPages(page.getTotalPages());
        response.setTotalElements(page.getTotalElements());
        response.setPageSize(page.getSize());
        response.setDisplayFields(displayFields);
        response.setPageNumber(page.getNumber());

        return response;
    }

    private Specification<StockUnitMaster> buildFilterSpecification(String searchField, String searchValue) {
        String upperField = searchField.toUpperCase();
        String searchPattern = "%" + searchValue.toLowerCase() + "%";

        if ("STOCK_UNIT_CODE".equals(upperField) || "STOCKUNITCODE".equals(upperField)) {
            return (root, query, cb) -> cb.like(cb.lower(root.get("stockUnitCode")), searchPattern);
        }
        
        if ("STOCK_UNIT_NAME".equals(upperField) || "STOCKUNITNAME".equals(upperField)) {
            return (root, query, cb) -> cb.like(cb.lower(root.get("stockUnitName")), searchPattern);
        }
        
        if ("CLASSIFIED".equals(upperField)) {
            return (root, query, cb) -> cb.equal(root.get("classified"), searchValue);
        }
        
        if ("ACTIVE".equals(upperField)) {
            return (root, query, cb) -> cb.equal(root.get("active"), searchValue.toUpperCase());
        }
        
        if ("DELETED".equals(upperField)) {
            String deletedValue = searchValue.toUpperCase();
            if ("Y".equals(deletedValue)) {
                return (root, query, cb) -> cb.equal(root.get("deleted"), "Y");
            } else if ("N".equals(deletedValue)) {
                return (root, query, cb) -> cb.or(
                    cb.isNull(root.get("deleted")),
                    cb.notEqual(root.get("deleted"), "Y")
                );
            } else {
                return (root, query, cb) -> cb.equal(root.get("deleted"), deletedValue);
            }
        }
        
        if ("GROUP_POID".equals(upperField) || "GROUPPOID".equals(upperField)) {
            try {
                Long groupPoid = Long.parseLong(searchValue);
                return (root, query, cb) -> cb.equal(root.get("groupPoid"), groupPoid);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        if ("GLOBALSEARCH".equals(upperField)) {
            return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("stockUnitCode")), searchPattern),
                cb.like(cb.lower(root.get("stockUnitName")), searchPattern)
            );
        }
        
        return null;
    }

}
