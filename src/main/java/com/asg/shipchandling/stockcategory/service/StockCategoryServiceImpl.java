package com.asg.shipchandling.stockcategory.service;

import com.asg.common.lib.dto.FilterDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.utility.PaginationUtil;
import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.stockcategory.dto.*;
import com.asg.shipchandling.stockcategory.dto.PoidDetailsDto;
import com.asg.shipchandling.stockcategory.dto.request.CreateStockCategoryRequest;
import com.asg.shipchandling.stockcategory.dto.request.UpdateStockCategoryRequest;
import com.asg.shipchandling.stockcategory.entity.StockCategoryMaster;
import com.asg.shipchandling.stockcategory.repository.StockCategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockCategoryServiceImpl implements StockCategoryService {

    private final StockCategoryRepository stockCategoryRepository;
    private final DocumentSearchService documentService;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public StockCategoryMasterDto createStockCategory(CreateStockCategoryRequest request, Long groupPoid, String userId) {
        log.info("createStockCategory service started for categoryName={} groupPoid={} userId={}",
                request.getCategoryName(), groupPoid, userId);
        
        // Validate category name uniqueness
        if (stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(request.getCategoryName(), groupPoid)) {
            log.warn("createStockCategory validation failed - categoryName={} already exists for groupPoid={}",
                    request.getCategoryName(), groupPoid);
            throw new ResourceAlreadyExistsException("categoryName" , request.getCategoryName());
        }

        // Validate parent category if SUB_GROUP
        if ("SUB_GROUP".equals(request.getCategoryType())) {
            if (request.getParentCategoryPoid() == null) {
                log.warn("createStockCategory validation failed - missing parent for SUB_GROUP categoryName={}",
                        request.getCategoryName());
                throw new CustomException("Parent category is required for SUB_GROUP type");
            }
            StockCategoryMaster parent = stockCategoryRepository.findByCategoryPoidAndGroupPoid(
                            request.getParentCategoryPoid(), groupPoid)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category", "categoryPoid",
                            request.getParentCategoryPoid()));

            if (!"GROUP".equals(parent.getCategoryType())) {
                log.warn("createStockCategory validation failed - parentCategoryPoid={} is not GROUP type",
                        request.getParentCategoryPoid());
                throw new CustomException("Parent category must be of type GROUP");
            }
        }

        // Auto-generate categoryCode
        String categoryCode = generateCategoryCode(groupPoid);
        log.info("createStockCategory auto-generated categoryCode={} for groupPoid={}", categoryCode, groupPoid);

        // Create entity
        StockCategoryMaster category = new StockCategoryMaster();
        BeanUtils.copyProperties(request, category);
        category.setCategoryCode(categoryCode);  // Set auto-generated code
        category.setGroupPoid(groupPoid);
        category.setCreatedBy(userId);
        category.setLastmodifiedBy(userId);
        category.setActive(request.getActive() != null ? request.getActive() : "Y");
        category.setDeleted("N");
        category.setCategoryType(request.getCategoryType() != null ? request.getCategoryType() : "GROUP");
        
        // Set GL fields
        category.setStockGlPoid(request.getStockGlPoid());
        category.setSalesGlPoid(request.getSalesGlPoid());
        category.setCostOfSalesGlPoid(request.getCostOfSalesGlPoid());
        
        // Save
        StockCategoryMaster savedCategory = stockCategoryRepository.save(category);
        log.info("createStockCategory persisted categoryPoid={} categoryCode={} stockGlPoid={} salesGlPoid={} costOfSalesGlPoid={}", 
                savedCategory.getCategoryPoid(), savedCategory.getCategoryCode(),
                savedCategory.getStockGlPoid(), savedCategory.getSalesGlPoid(), savedCategory.getCostOfSalesGlPoid());

        // Convert to DTO
        StockCategoryMasterDto dto = convertToDto(savedCategory);
        log.info("createStockCategory completed for categoryPoid={} with stockGlPoid={} salesGlPoid={} costOfSalesGlPoid={}",
                dto.getCategoryPoid(), dto.getStockGlPoid(), dto.getSalesGlPoid(), dto.getCostOfSalesGlPoid());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StockCategoryMasterDto getStockCategoryByPoid(Long categoryPoid, Long groupPoid) {
        log.info("getStockCategoryByPoid service started for categoryPoid={} groupPoid={}", categoryPoid, groupPoid);
        StockCategoryMaster category = stockCategoryRepository
                .findByCategoryPoidAndGroupPoid(categoryPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Category", "categoryPoid", categoryPoid));

        // Check if deleted
        if ("Y".equals(category.getDeleted())) {
            log.warn("getStockCategoryByPoid found categoryPoid={} marked as deleted", categoryPoid);
            throw new ResourceNotFoundException("Stock Category", "categoryPoid", categoryPoid);
        }

        StockCategoryMasterDto dto = convertToDtoWithDetails(category);
        log.info("getStockCategoryByPoid completed for categoryPoid={} categoryCode={} stockGlPoid={} salesGlPoid={} costOfSalesGlPoid={}", 
                categoryPoid, dto.getCategoryCode(), dto.getStockGlPoid(), dto.getSalesGlPoid(), dto.getCostOfSalesGlPoid());
        return dto;
    }

    @Override
    @Transactional
    public StockCategoryMasterDto updateStockCategory(Long categoryPoid, UpdateStockCategoryRequest request,
                                                      Long groupPoid, String userId) {
        log.info("updateStockCategory service started for categoryPoid={} groupPoid={} userId={}", categoryPoid, groupPoid, userId);
        StockCategoryMaster category = stockCategoryRepository
                .findByCategoryPoidAndGroupPoid(categoryPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Category", "categoryPoid", categoryPoid));

        // Check if deleted
        if ("Y".equals(category.getDeleted())) {
            log.warn("updateStockCategory attempt on deleted categoryPoid={}", categoryPoid);
            throw new CustomException("Cannot update deleted category");
        }

        // Validate uniqueness (excluding current record)
        if (stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                request.getCategoryCode(), groupPoid, categoryPoid)) {
            log.warn("updateStockCategory validation failed - categoryCode={} already exists for groupPoid={}",
                    request.getCategoryCode(), groupPoid);
            throw new ResourceAlreadyExistsException("categoryCode", request.getCategoryCode());
        }

        if (stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                request.getCategoryName(), groupPoid, categoryPoid)) {
            log.warn("updateStockCategory validation failed - categoryName={} already exists for groupPoid={}",
                    request.getCategoryName(), groupPoid);
            throw new ResourceAlreadyExistsException("categoryName", request.getCategoryName());
        }

        // Validate parent category if SUB_GROUP
        if ("SUB_GROUP".equals(request.getCategoryType())) {
            if (request.getParentCategoryPoid() == null) {
                log.warn("updateStockCategory validation failed - missing parent for SUB_GROUP categoryPoid={}", categoryPoid);
                throw new CustomException("Parent category is required for SUB_GROUP type");
            }

            // Prevent self-reference
            if (request.getParentCategoryPoid().equals(categoryPoid)) {
                log.warn("updateStockCategory validation failed - circular reference for categoryPoid={}", categoryPoid);
                throw new CustomException("Category cannot be its own parent");
            }

            StockCategoryMaster parent = stockCategoryRepository.findByCategoryPoidAndGroupPoid(
                            request.getParentCategoryPoid(), groupPoid)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category", "categoryPoid",
                            request.getParentCategoryPoid()));

            if (!"GROUP".equals(parent.getCategoryType())) {
                log.warn("updateStockCategory validation failed - parentCategoryPoid={} is not GROUP type", request.getParentCategoryPoid());
                throw new CustomException("Parent category must be of type GROUP");
            }

            // Check for circular reference
            validateNoCircularReference(categoryPoid, request.getParentCategoryPoid());
        } else {
            // If changing to GROUP, remove parent
            request.setParentCategoryPoid(null);
        }

        // Check if category has children and changing to SUB_GROUP
        if ("SUB_GROUP".equals(request.getCategoryType()) &&
                stockCategoryRepository.countChildrenByParentCategoryPoid(categoryPoid) > 0) {
            log.warn("updateStockCategory validation failed - categoryPoid={} has child categories", categoryPoid);
            throw new CustomException("Cannot change category type to SUB_GROUP. Category has child categories.");
        }

        // Update fields
        category.setCategoryCode(request.getCategoryCode());
        category.setCategoryName(request.getCategoryName());
        category.setCategoryName2(request.getCategoryName2());
        category.setCategoryType(request.getCategoryType());
        category.setParentCategoryPoid(request.getParentCategoryPoid());
        
        // Update GL fields
        category.setStockGlPoid(request.getStockGlPoid());
        category.setSalesGlPoid(request.getSalesGlPoid());
        category.setCostOfSalesGlPoid(request.getCostOfSalesGlPoid());
        
        category.setOutputTaxPoid(request.getOutputTaxPoid());
        category.setInputTaxPoid(request.getInputTaxPoid());
        category.setCostCenterPoid(request.getCostCenterPoid());
        category.setSeqno(request.getSeqno());
        category.setActive(request.getActive() != null ? request.getActive() : category.getActive());
        category.setLastmodifiedBy(userId);

        // Save the entity
        StockCategoryMaster savedCategory = stockCategoryRepository.save(category);
        log.info("updateStockCategory saved - stockGlPoid={} salesGlPoid={} costOfSalesGlPoid={}",
                savedCategory.getStockGlPoid(), savedCategory.getSalesGlPoid(), savedCategory.getCostOfSalesGlPoid());

        StockCategoryMasterDto dto = convertToDto(savedCategory);
        log.info("updateStockCategory completed for categoryPoid={} categoryCode={} stockGlPoid={} salesGlPoid={} costOfSalesGlPoid={}", 
                categoryPoid, dto.getCategoryCode(), dto.getStockGlPoid(), dto.getSalesGlPoid(), dto.getCostOfSalesGlPoid());
        return dto;
    }

    @Override
    @Transactional
    public void deleteStockCategory(Long categoryPoid, Long groupPoid) {
        log.info("deleteStockCategory service started for categoryPoid={} groupPoid={}", categoryPoid, groupPoid);
        StockCategoryMaster category = stockCategoryRepository
                .findByCategoryPoidAndGroupPoid(categoryPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Category", "categoryPoid", categoryPoid));

        // Check dependencies
        Long childCount = stockCategoryRepository.countChildrenByParentCategoryPoid(categoryPoid);
        Long stockItemCount = stockCategoryRepository.countStockItemsByCategoryPoid(categoryPoid);

        if (childCount > 0 || stockItemCount > 0) {
            log.warn("deleteStockCategory blocked for categoryPoid={} childCount={} stockItemCount={}",
                    categoryPoid, childCount, stockItemCount);
            throw new CustomException(
                    String.format("Cannot delete category. It has %d child categories and %d stock items.",
                            childCount, stockItemCount));
        }

        // Soft delete
        category.setDeleted("Y");
        stockCategoryRepository.save(category);
        log.info("deleteStockCategory completed with soft delete for categoryPoid={}", categoryPoid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockCategoryTreeDto> getStockCategoryTree(Long groupPoid, String categoryType, String active) {
        log.info("getStockCategoryTree service started for groupPoid={} categoryType={} active={}", groupPoid, categoryType, active);
        List<StockCategoryMaster> categories = stockCategoryRepository.findParentCategoriesByGroupPoid(groupPoid);

        List<StockCategoryTreeDto> result = categories.stream()
                .filter(c -> categoryType == null || categoryType.equals(c.getCategoryType()))
                .filter(c -> active == null || active.equals(c.getActive()))
                .map(this::convertToTreeDto)
                .collect(Collectors.toList());
        log.info("getStockCategoryTree service completed for groupPoid={} nodeCount={}", groupPoid, result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockCategoryMasterDto> getAllStockCategories(Long groupPoid, String categoryType, String active) {
        log.info("getAllStockCategories service started for groupPoid={} categoryType={} active={}", groupPoid, categoryType, active);
        List<StockCategoryMaster> categories = stockCategoryRepository
                .findByGroupPoidAndDeletedNotOrDeletedIsNull(groupPoid, "Y");

        List<StockCategoryMasterDto> result = categories.stream()
                .filter(c -> categoryType == null || categoryType.equals(c.getCategoryType()))
                .filter(c -> active == null || active.equals(c.getActive()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
        log.info("getAllStockCategories service completed for groupPoid={} count={}", groupPoid, result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockCategoryMasterDto> getChildCategories(Long parentCategoryPoid, Long groupPoid) {
        log.info("getChildCategories service started for parentCategoryPoid={} groupPoid={}", parentCategoryPoid, groupPoid);
        List<StockCategoryMaster> children = stockCategoryRepository
                .findChildrenByParentCategoryPoidAndGroupPoid(parentCategoryPoid, groupPoid);

        List<StockCategoryMasterDto> result = children.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        log.info("getChildCategories service completed for parentCategoryPoid={} childCount={}", parentCategoryPoid, result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCategoryCode(String categoryCode, Long groupPoid, Long excludeCategoryPoid) {
        log.info("validateCategoryCode service started for categoryCode={} groupPoid={} excludeCategoryPoid={}",
                categoryCode, groupPoid, excludeCategoryPoid);
        if (excludeCategoryPoid != null) {
            boolean result = !stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                    categoryCode, groupPoid, excludeCategoryPoid);
            log.info("validateCategoryCode service completed for categoryCode={} isUnique={}", categoryCode, result);
            return result;
        }
        boolean result = !stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(categoryCode, groupPoid);
        log.info("validateCategoryCode service completed for categoryCode={} isUnique={}", categoryCode, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateCategoryName(String categoryName, Long groupPoid, Long excludeCategoryPoid) {
        log.info("validateCategoryName service started for categoryName={} groupPoid={} excludeCategoryPoid={}",
                categoryName, groupPoid, excludeCategoryPoid);
        if (excludeCategoryPoid != null) {
            boolean result = !stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoidAndCategoryPoidNot(
                    categoryName, groupPoid, excludeCategoryPoid);
            log.info("validateCategoryName service completed for categoryName={} isUnique={}", categoryName, result);
            return result;
        }
        boolean result = !stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(categoryName, groupPoid);
        log.info("validateCategoryName service completed for categoryName={} isUnique={}", categoryName, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public StockCategoryGlValuesDto getParentCategoryGlValues(Long parentCategoryPoid, Long groupPoid) {
        log.info("getParentCategoryGlValues service started for parentCategoryPoid={} groupPoid={}", parentCategoryPoid, groupPoid);
        StockCategoryMaster parent = stockCategoryRepository
                .findByCategoryPoidAndGroupPoid(parentCategoryPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category", "categoryPoid", parentCategoryPoid));

        // Validate it's a GROUP type
        if (!"GROUP".equals(parent.getCategoryType())) {
            log.warn("getParentCategoryGlValues validation failed - parentCategoryPoid={} is not GROUP type", parentCategoryPoid);
            throw new CustomException("Parent category must be of type GROUP");
        }

        StockCategoryGlValuesDto dto = new StockCategoryGlValuesDto();
        dto.setParentCategoryPoid(parent.getCategoryPoid());
        dto.setParentCategoryCode(parent.getCategoryCode());
        dto.setParentCategoryName(parent.getCategoryName());
        dto.setStockGlPoid(parent.getStockGlPoid());
        dto.setSalesGlPoid(parent.getSalesGlPoid());
        dto.setCostOfSalesGlPoid(parent.getCostOfSalesGlPoid());
        log.info("getParentCategoryGlValues service completed for parentCategoryPoid={} stockGlPoid={} salesGlPoid={}",
                parentCategoryPoid, dto.getStockGlPoid(), dto.getSalesGlPoid());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDependenciesDto checkCategoryDependencies(Long categoryPoid, Long groupPoid) {
        log.info("checkCategoryDependencies service started for categoryPoid={} groupPoid={}", categoryPoid, groupPoid);
        StockCategoryMaster category = stockCategoryRepository
                .findByCategoryPoidAndGroupPoid(categoryPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Category", "categoryPoid", categoryPoid));
        log.debug("checkCategoryDependencies evaluating categoryCode={} deleted={}", category.getCategoryCode(), category.getDeleted());

        Long childCount = stockCategoryRepository.countChildrenByParentCategoryPoid(categoryPoid);
        Long stockItemCount = stockCategoryRepository.countStockItemsByCategoryPoid(categoryPoid);

        CategoryDependenciesDto dto = getDependenciesDto(categoryPoid, childCount, stockItemCount);
        log.info("checkCategoryDependencies service completed for categoryPoid={} canDelete={} childCount={} stockItemCount={}",
                categoryPoid, dto.getCanDelete(), childCount, stockItemCount);
        return dto;
    }

    private static CategoryDependenciesDto getDependenciesDto(Long categoryPoid, Long childCount, Long stockItemCount) {
        CategoryDependenciesDto dto = new CategoryDependenciesDto();
        dto.setCategoryPoid(categoryPoid);
        dto.setChildCategoryCount(childCount);
        dto.setStockItemCount(stockItemCount);
        dto.setCanDelete(childCount == 0 && stockItemCount == 0);

        if (dto.getCanDelete()) {
            dto.setReason("No dependencies");
            dto.setMessage("Category can be deleted. No dependencies found.");
        } else {
            dto.setReason("Category has dependencies");
            dto.setMessage(String.format("Cannot delete category. It has %d child categories and %d stock items.",
                    childCount, stockItemCount));
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockCategoryHierarchyDto> getCategoryHierarchy(Long categoryPoid, Long groupPoid) {
        log.info("getCategoryHierarchy service started for categoryPoid={} groupPoid={}", categoryPoid, groupPoid);
        StockCategoryMaster category = stockCategoryRepository
                .findByCategoryPoidAndGroupPoid(categoryPoid, groupPoid)
                .orElseThrow(() -> new ResourceNotFoundException("Stock Category", "categoryPoid", categoryPoid));
        log.debug("getCategoryHierarchy building path starting from categoryCode={} categoryType={}", category.getCategoryCode(), category.getCategoryType());

        List<StockCategoryHierarchyDto> hierarchy = new java.util.ArrayList<>();
        Long currentCategoryPoid = categoryPoid;
        int level = 0;
        int maxDepth = 100; // Prevent infinite loops

        // Build hierarchy from current category up to root
        while (currentCategoryPoid != null && level < maxDepth) {
            StockCategoryMaster currentCategory = stockCategoryRepository
                    .findByCategoryPoidAndGroupPoid(currentCategoryPoid, groupPoid)
                    .orElse(null);

            if (currentCategory == null) {
                break;
            }

            StockCategoryHierarchyDto hierarchyDto = new StockCategoryHierarchyDto();
            hierarchyDto.setCategoryPoid(currentCategory.getCategoryPoid());
            hierarchyDto.setCategoryCode(currentCategory.getCategoryCode());
            hierarchyDto.setCategoryName(currentCategory.getCategoryName());
            hierarchyDto.setCategoryType(currentCategory.getCategoryType());
            hierarchyDto.setLevel(level);

            hierarchy.add(0, hierarchyDto); // Add at beginning to maintain root → leaf order

            // Move to parent
            if (currentCategory.getParentCategoryPoid() != null) {
                currentCategoryPoid = currentCategory.getParentCategoryPoid().longValue();
                level++;
            } else {
                break; // Reached root
            }
        }

        log.info("getCategoryHierarchy service completed for categoryPoid={} levelsFound={}", categoryPoid, hierarchy.size());
        return hierarchy;
    }

    private void validateNoCircularReference(Long categoryPoid, Long parentCategoryPoid) {
        log.debug("validateNoCircularReference check started for categoryPoid={} newParentCategoryPoid={}", categoryPoid, parentCategoryPoid);
        Long currentParent = parentCategoryPoid;
        int depth = 0;
        int maxDepth = 100; // Prevent infinite loops

        while (currentParent != null && depth < maxDepth) {
            if (currentParent.equals(categoryPoid)) {
                log.warn("validateNoCircularReference detected circular reference for categoryPoid={} at depth={}", categoryPoid, depth);
                throw new CustomException("Circular reference detected. Cannot set parent category.");
            }

            StockCategoryMaster parent = stockCategoryRepository.findByCategoryPoid(currentParent)
                    .orElse(null);

            if (parent == null) {
                log.debug("validateNoCircularReference terminating - parent not found for parentCategoryPoid={}", currentParent);
                break;
            }

            currentParent = parent.getParentCategoryPoid() != null ?
                    parent.getParentCategoryPoid().longValue() : null;
            depth++;
        }
        log.debug("validateNoCircularReference completed for categoryPoid={} depthTraversed={}", categoryPoid, depth);
    }

    /**
     * Auto-generate category code in format CAT### (e.g., CAT001, CAT002, etc.)
     */
    private String generateCategoryCode(Long groupPoid) {
        // Find the highest category code for this group
        List<StockCategoryMaster> categories = stockCategoryRepository.findByGroupPoid(groupPoid);
        
        int maxNumber = 0;
        for (StockCategoryMaster cat : categories) {
            if (cat.getCategoryCode() != null && cat.getCategoryCode().startsWith("CAT")) {
                try {
                    String numberPart = cat.getCategoryCode().substring(3);
                    int num = Integer.parseInt(numberPart);
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException e) {
                    // Ignore if not in expected format
                }
            }
        }
        
        // Generate next code
        int nextNumber = maxNumber + 1;
        return String.format("CAT%03d", nextNumber);
    }

    private StockCategoryMasterDto convertToDto(StockCategoryMaster category) {
        StockCategoryMasterDto dto = new StockCategoryMasterDto();
        BeanUtils.copyProperties(category, dto);
        // GL fields are already Long, no conversion needed
        return dto;
    }

    private StockCategoryMasterDto convertToDtoWithDetails(StockCategoryMaster category) {
        StockCategoryMasterDto dto = convertToDto(category);
        
        // Fetch and populate related details using EntityManager
        if (category.getParentCategoryPoid() != null) {
            dto.setParentCategoryPoidDetails(fetchPoidDetails(
                    "SELECT CATEGORY_POID, CATEGORY_CODE, CATEGORY_NAME FROM STOCK_CATEGORY_MASTER WHERE CATEGORY_POID = :poid",
                    category.getParentCategoryPoid()));
        }
         if (category.getStockGlPoid() != null) {
             dto.setStockGlPoidDetails(fetchPoidDetails(
                     "SELECT GL_POID, GL_CODE, GL_DESCRIPTION FROM GL_MASTER WHERE GL_POID = :poid",
                     category.getStockGlPoid()));
         }
         if (category.getSalesGlPoid() != null) {
             dto.setSalesGlPoidDetails(fetchPoidDetails(
                     "SELECT GL_POID, GL_CODE, GL_DESCRIPTION FROM GL_MASTER WHERE GL_POID = :poid",
                     category.getSalesGlPoid()));
         }
         if (category.getCostOfSalesGlPoid() != null) {
             dto.setCostOfSalesGlPoidDetails(fetchPoidDetails(
                     "SELECT GL_POID, GL_CODE, GL_DESCRIPTION FROM GL_MASTER WHERE GL_POID = :poid",
                     category.getCostOfSalesGlPoid()));
         }
        if (category.getOutputTaxPoid() != null) {
            dto.setOutputTaxPoidDetails(fetchPoidDetails(
                    "SELECT TAX_POID, TAX_CODE, TAX_NAME FROM GLOBAL_TAX_MASTER WHERE TAX_POID = :poid",
                    category.getOutputTaxPoid()));
        }
        if (category.getInputTaxPoid() != null) {
            dto.setInputTaxPoidDetails(fetchPoidDetails(
                    "SELECT TAX_POID, NBR_TAX_CODE, TAX_NAME FROM GLOBAL_TAX_MASTER WHERE TAX_POID = :poid",
                    category.getInputTaxPoid()));
        }
        if (category.getCostCenterPoid() != null) {
            dto.setCostCenterPoidDetails(fetchPoidDetails(
                    "SELECT COST_CENTER_POID, COST_CENTER_CODE, COST_CENTER_DESCRIPTION FROM GL_COST_CENTER_MASTER WHERE COST_CENTER_POID = :poid",
                    category.getCostCenterPoid()));
        }
        
        return dto;
    }

    private PoidDetailsDto fetchPoidDetails(String sql, Object poid) {
        try {
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("poid", poid);
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            if (results != null && !results.isEmpty()) {
                Object[] result = results.get(0);
                if (result != null && result.length >= 3) {
                    return new PoidDetailsDto(
                            result[0] != null ? ((Number) result[0]).longValue() : null,
                            result[1] != null ? result[1].toString() : null,
                            result[2] != null ? result[2].toString() : null
                    );
                }
            }
        } catch (Exception e) {
            log.debug("Failed to fetch POID details for poid={}: {}", poid, e.getMessage());
        }
        return null;
    }

    private StockCategoryTreeDto convertToTreeDto(StockCategoryMaster category) {
        StockCategoryTreeDto dto = new StockCategoryTreeDto();
        dto.setCategoryPoid(category.getCategoryPoid());
        dto.setCode(category.getCategoryCode());
        dto.setDescription(category.getCategoryName());
        dto.setItemType(category.getCategoryType());
        dto.setParentCategoryPoid(category.getParentCategoryPoid() != null ?
                category.getParentCategoryPoid().longValue() : null);
        dto.setSeqno(category.getSeqno());
        dto.setDeleted(category.getDeleted());

        // Check if has children
        Long childCount = stockCategoryRepository.countChildrenByParentCategoryPoid(category.getCategoryPoid());
        dto.setHasChildren(childCount > 0);

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getStockCategoriesHierarchical(Long groupPoid, Long parentPoid, String filterValue, boolean includeDeleted, boolean tree) {
        log.info("getStockCategoriesHierarchical service started for groupPoid={} parentPoid={} filterValue={} includeDeleted={} tree={}", 
                groupPoid, parentPoid, filterValue, includeDeleted, tree);
        
        List<Map<String, Object>> result = new ArrayList<>();
        boolean hasFilter = filterValue != null && !filterValue.trim().isEmpty();
        String searchPattern = hasFilter ? filterValue.toLowerCase() : null;
        
        // Fetch all categories for the group to build category map
        List<StockCategoryMaster> allCategories;
        if (includeDeleted) {
            allCategories = stockCategoryRepository.findByGroupPoid(groupPoid);
        } else {
            allCategories = stockCategoryRepository.findByGroupPoidAndDeletedNotOrDeletedIsNull(groupPoid, "Y");
        }
        
        Map<Long, StockCategoryMaster> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(StockCategoryMaster::getCategoryPoid, cat -> cat));
        
        if (parentPoid == null) {
            // Return MAIN_GROUP items (root categories with no parent)
            List<StockCategoryMaster> rootCategories;
            if (includeDeleted) {
                rootCategories = stockCategoryRepository.findRootCategoriesByGroupPoid(groupPoid);
            } else {
                rootCategories = stockCategoryRepository.findParentCategoriesByGroupPoid(groupPoid);
            }
            
            for (StockCategoryMaster category : rootCategories) {
                // Check if this category has child categories
                Long childCount = stockCategoryRepository.countChildrenByParentCategoryPoid(category.getCategoryPoid());
                
                String type = (childCount > 0) ? "MAIN_GROUP" : "LEDGER";
                Map<String, Object> item = convertCategoryToHierarchicalItem(category, type, 0, includeDeleted);
                
                if (tree && childCount > 0) {
                    // Add children for tree structure only if it has children
                    List<Map<String, Object>> children = getChildrenForCategory(category.getCategoryPoid(), groupPoid, 1, includeDeleted, filterValue, categoryMap, tree);
                    item.put("children", children);

                    // If filtering, keep node when it matches or has matching descendants
                    if (hasFilter && children.isEmpty() && !matchesCategory(category, searchPattern)) {
                        continue;
                    }
                } else if (hasFilter && !matchesCategory(category, searchPattern)) {
                    // Tree disabled: only include nodes that match filter
                    continue;
                }
                result.add(item);
            }
        } else {
            // Check if parentPoid is a category
            Optional<StockCategoryMaster> parentCategory = stockCategoryRepository.findByCategoryPoidAndGroupPoid(parentPoid, groupPoid);
            
            if (parentCategory.isPresent()) {
                // Parent is a category - return child categories (SUB_GROUP)
                StockCategoryMaster parent = parentCategory.get();
                int level = calculateCategoryLevel(parent, groupPoid) + 1;
                
                // Get child categories
                List<StockCategoryMaster> childCategories;
                if (includeDeleted) {
                    childCategories = stockCategoryRepository.findChildrenByParentCategoryPoidAndGroupPoidAll(parentPoid, groupPoid);
                } else {
                    childCategories = stockCategoryRepository.findChildrenByParentCategoryPoidAndGroupPoid(parentPoid, groupPoid);
                }
                
                for (StockCategoryMaster category : childCategories) {
                    // Check if this category has child categories
                    Long childCount = stockCategoryRepository.countChildrenByParentCategoryPoid(category.getCategoryPoid());
                    
                    String type = (childCount > 0) ? "SUB_GROUP" : "LEDGER";
                    Map<String, Object> item = convertCategoryToHierarchicalItem(category, type, level, includeDeleted);
                    
                    if (tree && childCount > 0) {
                        // Add children for tree structure only if it has children
                        List<Map<String, Object>> children = getChildrenForCategory(category.getCategoryPoid(), groupPoid, level + 1, includeDeleted, filterValue, categoryMap, tree);
                        item.put("children", children);

                        if (hasFilter && children.isEmpty() && !matchesCategory(category, searchPattern)) {
                            continue;
                        }
                    } else if (hasFilter && !matchesCategory(category, searchPattern)) {
                        continue;
                    }
                    result.add(item);
                }
            }
        }
        
        log.info("getStockCategoriesHierarchical service completed for groupPoid={} resultCount={}", groupPoid, result.size());
        return result;
    }
    
    private List<Map<String, Object>> getChildrenForCategory(Long categoryPoid, Long groupPoid, int level, boolean includeDeleted, String filterValue, Map<Long, StockCategoryMaster> categoryMap, boolean tree) {
        List<Map<String, Object>> children = new ArrayList<>();
        
        List<StockCategoryMaster> childCategories;
        if (includeDeleted) {
            childCategories = stockCategoryRepository.findChildrenByParentCategoryPoidAndGroupPoidAll(categoryPoid, groupPoid);
        } else {
            childCategories = stockCategoryRepository.findChildrenByParentCategoryPoidAndGroupPoid(categoryPoid, groupPoid);
        }

        boolean hasFilter = filterValue != null && !filterValue.trim().isEmpty();
        String searchPattern = hasFilter ? filterValue.toLowerCase() : null;
        
        for (StockCategoryMaster category : childCategories) {
            // Check if this category has child categories
            Long childCount = stockCategoryRepository.countChildrenByParentCategoryPoid(category.getCategoryPoid());
            
            String type = (childCount > 0) ? "SUB_GROUP" : "LEDGER";
            Map<String, Object> item = convertCategoryToHierarchicalItem(category, type, level, includeDeleted);
            
            if (tree && childCount > 0) {
                // Recursively add children only if it has children
                List<Map<String, Object>> grandChildren = getChildrenForCategory(category.getCategoryPoid(), groupPoid, level + 1, includeDeleted, filterValue, categoryMap, tree);
                item.put("children", grandChildren);
                
                // If filterValue is provided and this category doesn't match, but has matching children, still include it
                if (hasFilter && grandChildren.isEmpty() && !matchesCategory(category, searchPattern)) {
                    // Category doesn't match and has no matching children, skip it
                    continue;
                }
            } else if (hasFilter && !matchesCategory(category, searchPattern)) {
                // Tree disabled: only include nodes that match filter
                continue;
            }
            children.add(item);
        }
        
        return children;
    }

    private boolean matchesCategory(StockCategoryMaster category, String searchPattern) {
        return searchPattern != null && (
                (category.getCategoryCode() != null && category.getCategoryCode().toLowerCase().contains(searchPattern)) ||
                (category.getCategoryName() != null && category.getCategoryName().toLowerCase().contains(searchPattern))
        );
    }
    
    private int calculateCategoryLevel(StockCategoryMaster category, Long groupPoid) {
        if (category.getParentCategoryPoid() == null) {
            return 0;
        }
        
        Optional<StockCategoryMaster> parent = stockCategoryRepository.findByCategoryPoidAndGroupPoid(category.getParentCategoryPoid(), groupPoid);
        
        if (parent.isPresent()) {
            return calculateCategoryLevel(parent.get(), groupPoid) + 1;
        }
        
        return 0;
    }
    
    private Map<String, Object> convertCategoryToHierarchicalItem(StockCategoryMaster category, String type, int level, boolean includeDeleted) {
        Map<String, Object> item = new HashMap<>();
        item.put("categoryPoid", category.getCategoryPoid());
        item.put("categoryCode", category.getCategoryCode());
        item.put("categoryName", category.getCategoryName());
        item.put("type", type);
        item.put("level", level);
        item.put("active", category.getActive() != null && "Y".equalsIgnoreCase(category.getActive()));
        item.put("deleted", category.getDeleted() != null && "Y".equalsIgnoreCase(category.getDeleted()));
        item.put("groupPoid", category.getGroupPoid());
        
        // Set parentPoid from PARENT_CATEGORY_POID
        // For MAIN_GROUP: parentPoid will be null (root categories)
        // For SUB_GROUP: parentPoid will be the parent category's CATEGORY_POID
        if (category.getParentCategoryPoid() != null) {
            item.put("parentPoid", category.getParentCategoryPoid());
        }
        
        return item;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> listStockCategories(String docId, FilterRequestDto request, Pageable pageable) {
        String operator = documentService.resolveOperator(request);
        String isDeleted = documentService.resolveIsDeleted(request);
        List<FilterDto> filters = documentService.resolveFilters(request);

        RawSearchResult raw = documentService.search(docId, filters, operator, pageable, isDeleted,
                "CATEGORY_NAME",   // label
                "CATEGORY_POID");    // value);

        Page<Map<String, Object>> page = new PageImpl<>(raw.records(), pageable, raw.totalRecords());

        return PaginationUtil.wrapPage(page, raw.displayFields());
    }
}
