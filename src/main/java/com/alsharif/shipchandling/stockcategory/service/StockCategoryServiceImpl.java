package com.alsharif.shipchandling.stockcategory.service;

import com.alsharif.shipchandling.exceptions.CustomException;
import com.alsharif.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.alsharif.shipchandling.exceptions.ResourceNotFoundException;
import com.alsharif.shipchandling.stockcategory.dto.*;
import com.alsharif.shipchandling.stockcategory.dto.request.CreateStockCategoryRequest;
import com.alsharif.shipchandling.stockcategory.dto.request.UpdateStockCategoryRequest;
import com.alsharif.shipchandling.stockcategory.entity.StockCategoryMaster;
import com.alsharif.shipchandling.stockcategory.repository.StockCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockCategoryServiceImpl implements StockCategoryService {

    private final StockCategoryRepository stockCategoryRepository;

    @Override
    @Transactional
    public StockCategoryMasterDto createStockCategory(CreateStockCategoryRequest request, Long groupPoid, String userId) {
        log.info("createStockCategory service started for categoryCode={} groupPoid={} userId={}",
                request.getCategoryCode(), groupPoid, userId);
        // Validate category code uniqueness
        if (stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid(request.getCategoryCode(), groupPoid)) {
            log.warn("createStockCategory validation failed - categoryCode={} already exists for groupPoid={}",
                    request.getCategoryCode(), groupPoid);
            throw new ResourceAlreadyExistsException("categoryCode" , request.getCategoryCode());
        }

        // Validate category name uniqueness
        if (stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(request.getCategoryName(), groupPoid)) {
            log.warn("createStockCategory validation failed - categoryName={} already exists for groupPoid={}",
                    request.getCategoryName(), groupPoid);
            throw new ResourceAlreadyExistsException("categoryName" , request.getCategoryName());
        }

        // Validate parent category if SUB_GROUP
        if ("SUB_GROUP".equals(request.getCategoryType())) {
            if (request.getParentCategoryPoid() == null) {
                log.warn("createStockCategory validation failed - missing parent for SUB_GROUP categoryCode={}",
                        request.getCategoryCode());
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

        // Create entity
        StockCategoryMaster category = new StockCategoryMaster();
        BeanUtils.copyProperties(request, category);
        category.setGroupPoid(groupPoid);
        category.setCreatedBy(userId);
        category.setLastmodifiedBy(userId);
        category.setActive(request.getActive() != null ? request.getActive() : "Y");
        category.setDeleted("N");
        category.setCategoryType(request.getCategoryType() != null ? request.getCategoryType() : "GROUP");
            // Save
            StockCategoryMaster savedCategory = stockCategoryRepository.save(category);
            log.info("createStockCategory persisted categoryPoid={} categoryCode={}", savedCategory.getCategoryPoid(), savedCategory.getCategoryCode());

            // IMPORTANT: Retrieve GL values after insert (they are set by DB trigger)
            // Need to refresh entity to get trigger-populated values
            stockCategoryRepository.flush();
            StockCategoryMaster refreshedCategory = stockCategoryRepository.findByCategoryPoid(
                    savedCategory.getCategoryPoid()).orElse(savedCategory);

            // Convert to DTO
            StockCategoryMasterDto dto = new StockCategoryMasterDto();
            BeanUtils.copyProperties(refreshedCategory, dto);
            log.info("createStockCategory completed for categoryPoid={} with stockGlPoid={}",
                    dto.getCategoryPoid(), dto.getStockGlPoid());
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

        StockCategoryMasterDto dto = new StockCategoryMasterDto();
        BeanUtils.copyProperties(category, dto);
        log.info("getStockCategoryByPoid completed for categoryPoid={} categoryCode={}", categoryPoid, dto.getCategoryCode());
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
        category.setOutputTaxPoid(request.getOutputTaxPoid());
        category.setInputTaxPoid(request.getInputTaxPoid());
        category.setCostCenterPoid(request.getCostCenterPoid());
        category.setSeqno(request.getSeqno());
        category.setActive(request.getActive() != null ? request.getActive() : category.getActive());
        category.setLastmodifiedBy(userId);

        // Save and refresh to get trigger-updated GL values
        StockCategoryMaster savedCategory = stockCategoryRepository.save(category);
        stockCategoryRepository.flush();
        StockCategoryMaster refreshedCategory = stockCategoryRepository.findByCategoryPoid(
                savedCategory.getCategoryPoid()).orElse(savedCategory);

        StockCategoryMasterDto dto = new StockCategoryMasterDto();
        BeanUtils.copyProperties(refreshedCategory, dto);
        log.info("updateStockCategory completed for categoryPoid={} categoryCode={}", categoryPoid, dto.getCategoryCode());
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

    private StockCategoryMasterDto convertToDto(StockCategoryMaster category) {
        StockCategoryMasterDto dto = new StockCategoryMasterDto();
        BeanUtils.copyProperties(category, dto);
        return dto;
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
}
