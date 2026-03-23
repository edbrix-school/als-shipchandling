package com.asg.shipchandling.stockunitmaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.repository.GroupRepository;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.shipchandling.StockMaster.entity.StockMasterEntity;
import com.asg.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.stockunitmaster.dto.CreateStockUnitMasterRequest;
import com.asg.shipchandling.stockunitmaster.dto.StockUnitMasterDto;
import com.asg.shipchandling.stockunitmaster.dto.UnitDependenciesDto;
import com.asg.shipchandling.stockunitmaster.entity.StockUnitMaster;
import com.asg.shipchandling.stockunitmaster.repository.StockUnitRepository;
import com.asg.shipchandling.stockunitmaster.util.StockUnitConstraintErrorHandler;
import com.asg.shipchandling.stockunitmaster.exception.StockUnitConstraintViolationException;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockUnitServiceImpl implements StockUnitService {

    private final StockUnitRepository stockUnitRepository;
    private final DocumentSearchService documentService;
    private final GroupRepository groupRepository;
    private final LoggingService loggingService;
    private final DocumentDeleteService documentDeleteService;

    private static final Logger log = LoggerFactory.getLogger(StockUnitServiceImpl.class);

    @Override
    public StockUnitMasterDto getStockUnitByPoid(Long stockUnitPoid) {
        if (!stockUnitRepository.existsByStockUnitPoid(stockUnitPoid)) {
            throw new ResourceNotFoundException("Unit", "UnitPoid", stockUnitPoid);
        }
        StockUnitMasterDto stockUnitMasterDto = new StockUnitMasterDto();

        StockUnitMaster stockUnitMaster = stockUnitRepository.findByStockUnitPoid(stockUnitPoid);
        return entityToDtoWithAuditDates(stockUnitMaster);
    }

    @Override
    @Transactional
    public StockUnitMasterDto createStockUnit(CreateStockUnitMasterRequest request) {
        StockUnitMasterDto responseDto = new StockUnitMasterDto();

        // Validate stock unit name uniqueness
        if (stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid(request.getStockUnitName(), request.getGroupPoid())) {
            throw new ResourceAlreadyExistsException("UnitName", request.getStockUnitName());
        }

        // Validate group exists
        groupRepository.findById(request.getGroupPoid()).orElseThrow(
                () -> new ResourceNotFoundException("Group", "groupPoid", request.getGroupPoid()));

        StockUnitMaster entity = mapRequestToEntity(request);

        try {
            StockUnitMaster responseEntity = stockUnitRepository.save(entity);
            Long stockUnitPoid = responseEntity.getStockUnitPoid();
            BeanUtils.copyProperties(responseEntity, responseDto);
            responseDto.setCreatedDate(responseEntity.getCreatedDate() != null
                    ? responseEntity.getCreatedDate().atOffset(java.time.ZoneOffset.UTC)
                    : null);
            responseDto.setLastModifiedDate(responseEntity.getLastModifiedDate() != null
                    ? responseEntity.getLastModifiedDate().atOffset(java.time.ZoneOffset.UTC)
                    : null);
            String key = stockUnitPoid.toString();
            String documentId = UserContext.getDocumentId();
            loggingService.createLogSummaryEntry(LogDetailsEnum.CREATED, documentId, key);
            return responseDto;
        } catch (DataIntegrityViolationException ex) {
            StockUnitConstraintErrorHandler.handleConstraintViolation(ex);
            // If handleConstraintViolation doesn't throw, re-throw original exception
            throw ex;
        } catch (StockUnitConstraintViolationException ex) {
            // Re-throw as ResourceAlreadyExistsException for unique constraints
            if ("UNIQUE".equals(ex.getViolationType()) || "PK".equals(ex.getViolationType())) {
                throw new ResourceAlreadyExistsException(ex.getMessage(), null);
            }
            // For foreign key parent not found, throw as ResourceNotFoundException
            if ("FK_PARENT".equals(ex.getViolationType())) {
                throw new ResourceNotFoundException(ex.getMessage());
            }
            // For other cases, throw as ResourceAlreadyExistsException
            throw new ResourceAlreadyExistsException(ex.getMessage(), null);
        }
    }

    private StockUnitMaster mapRequestToEntity(CreateStockUnitMasterRequest request) {
        StockUnitMaster entity = new StockUnitMaster();
        entity.setStockUnitCode(request.getStockUnitCode());
        entity.setStockUnitName(request.getStockUnitName());
        entity.setStockUnitName2(request.getStockUnitName2());
        entity.setGroupPoid(request.getGroupPoid());
        entity.setActive(request.getActive() != null ? request.getActive() : "Y");
        entity.setSeqNo(request.getSeqNo());
        entity.setDeleted("N");
        entity.setClassified(request.getClassified());

        return entity;
    }

    @Override
    @Transactional
    public StockUnitMasterDto updateStockUnit(Long stockUnitPoid, StockUnitMasterDto stockUnitMasterDto) {
        StockUnitMaster existingStockUnit = stockUnitRepository.findByStockUnitPoid(stockUnitPoid);

        StockUnitMaster oldEntity = new StockUnitMaster();
        BeanUtils.copyProperties(existingStockUnit, oldEntity);

        if (!stockUnitRepository.existsByStockUnitPoid(stockUnitPoid)) {
            throw new ResourceNotFoundException("Unit", "UnitPoid", stockUnitPoid);
        }

        if (stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot(
                stockUnitMasterDto.getStockUnitCode(),
                stockUnitMasterDto.getStockUnitPoid())) {
            throw new ResourceAlreadyExistsException("Unit Code already exists, please enter unique code.",
                    stockUnitMasterDto.getStockUnitCode());
        }
        if (stockUnitRepository.existsByStockUnitNameIgnoreCaseAndStockUnitPoidNot(
                stockUnitMasterDto.getStockUnitName(),
                stockUnitMasterDto.getStockUnitPoid())) {
            throw new ResourceAlreadyExistsException("Unit Name already exists, please enter unique name.",
                    stockUnitMasterDto.getStockUnitName());
        }

        groupRepository.findById(stockUnitMasterDto.getGroupPoid()).orElseThrow(
                () -> new ResourceNotFoundException("Group", "groupPoid", stockUnitMasterDto.getGroupPoid()));

        // Note: stockUnitCode is immutable after creation and cannot be updated
        // It is validated above to ensure it matches the existing value

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
        if (stockUnitMasterDto.getClassified() != null) {
            existingStockUnit.setClassified(stockUnitMasterDto.getClassified());
        }
        existingStockUnit.setLastModifiedDate(LocalDateTime.now());

        try {
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


            // Log the update
            String key = updatedStockUnit.getStockUnitPoid().toString();
            loggingService.logChanges(oldEntity, updatedStockUnit, StockUnitMaster.class,
                    UserContext.getDocumentId(), key, LogDetailsEnum.MODIFIED, "STOCK_UNIT_POID");

            return responseDto;
        } catch (DataIntegrityViolationException ex) {
            StockUnitConstraintErrorHandler.handleConstraintViolation(ex);
            // If handleConstraintViolation doesn't throw, re-throw original exception
            throw ex;
        } catch (StockUnitConstraintViolationException ex) {
            // Re-throw as ResourceAlreadyExistsException for unique constraints
            if ("UNIQUE".equals(ex.getViolationType()) || "PK".equals(ex.getViolationType())) {
                throw new ResourceAlreadyExistsException(ex.getMessage(), null);
            }
            // For foreign key parent not found, throw as ResourceNotFoundException
            if ("FK_PARENT".equals(ex.getViolationType())) {
                throw new ResourceNotFoundException(ex.getMessage());
            }
            throw new ResourceAlreadyExistsException(ex.getMessage(), null);
        }
    }

    @Override
    public void softDeleteStockUnit(Long stockUnitPoid, DeleteReasonDto deleteReasonDto) {
        StockUnitMaster existingStockunit = stockUnitRepository.findByStockUnitPoid(stockUnitPoid);
        if (!stockUnitRepository.existsByStockUnitPoid(stockUnitPoid)) {
            throw new ResourceNotFoundException("Unit", "UnitPoid", stockUnitPoid);
        }

        documentDeleteService.deleteDocument(
                stockUnitPoid,
                "STOCK_UNIT_MASTER",
                "STOCK_UNIT_POID",
                deleteReasonDto,
                LocalDate.now()
        );
        existingStockunit.setDeleted("Y");
        existingStockunit.setActive("N");
        
        try {
            stockUnitRepository.save(existingStockunit);
        } catch (DataIntegrityViolationException ex) {
            StockUnitConstraintErrorHandler.handleConstraintViolation(ex);
            // If handleConstraintViolation doesn't throw, re-throw original exception
            throw ex;
        } catch (StockUnitConstraintViolationException ex) {
            // For foreign key child found (trying to delete when referenced), throw as conflict
            if ("FK_CHILD".equals(ex.getViolationType())) {
                throw new ResourceAlreadyExistsException(ex.getMessage(), null);
            }
            // Re-throw other constraint violations
            throw new ResourceAlreadyExistsException(ex.getMessage(), null);
        }
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

    List<StockUnitMasterDto> dtoList = page.getContent().stream().map(this::entityToDtoWithAuditDates).toList();

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
                .orElseThrow(() -> new ResourceNotFoundException("Unit", "UnitPoid", stockUnitPoid));

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

    /**
     * Maps entity to DTO and sets audit date fields. Entity uses LocalDateTime while DTO uses
     * OffsetDateTime; BeanUtils skips incompatible types, so dates are set explicitly.
     */
    private StockUnitMasterDto entityToDtoWithAuditDates(StockUnitMaster entity) {
        StockUnitMasterDto dto = new StockUnitMasterDto();
        BeanUtils.copyProperties(entity, dto);
        dto.setCreatedDate(entity.getCreatedDate() != null
                ? entity.getCreatedDate().atOffset(java.time.ZoneOffset.UTC)
                : null);
        dto.setLastModifiedDate(entity.getLastModifiedDate() != null
                ? entity.getLastModifiedDate().atOffset(java.time.ZoneOffset.UTC)
                : null);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listStockUnits(String docId, FilterRequestDto request, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "STOCK_UNIT_NAME",   // label
                "STOCK_UNIT_POID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
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

    @Override
    @Transactional(readOnly = true)
    public List<StockUnitMasterDto> getStockUnitsByCode(String stockUnitCode) {
        log.info("getStockUnitsByCode started for stockUnitCode={}", stockUnitCode);

        if (stockUnitCode == null || stockUnitCode.trim().isEmpty()) {
            throw new IllegalArgumentException("stockUnitCode is required");
        }

        // Create pattern for contains search (case-insensitive)
        String codePattern = "%" + stockUnitCode.trim() + "%";
        List<StockUnitMaster> units = stockUnitRepository.findByStockUnitCodeContains(codePattern);

        List<StockUnitMasterDto> dtoList = units.stream()
                .map(this::entityToDtoWithAuditDates)
                .collect(Collectors.toList());

        log.info("getStockUnitsByCode completed for stockUnitCode={}, found {} units", stockUnitCode, dtoList.size());
        return dtoList;
    }

}
