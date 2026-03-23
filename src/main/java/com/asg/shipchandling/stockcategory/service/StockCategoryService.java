package com.asg.shipchandling.stockcategory.service;


import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.shipchandling.stockcategory.dto.*;
import com.asg.shipchandling.stockcategory.dto.request.CreateStockCategoryRequest;
import com.asg.shipchandling.stockcategory.dto.request.UpdateStockCategoryRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
@Service
public interface StockCategoryService {

    StockCategoryMasterDto createStockCategory(CreateStockCategoryRequest request, Long groupPoid, String userId);

    StockCategoryMasterDto getStockCategoryByPoid(Long categoryPoid, Long groupPoid);

    StockCategoryMasterDto updateStockCategory(Long categoryPoid, UpdateStockCategoryRequest request,
                                               Long groupPoid, String userId);

    void deleteStockCategory(Long categoryPoid, Long groupPoid, DeleteReasonDto deleteReasonDto);

    List<StockCategoryTreeDto> getStockCategoryTree(Long groupPoid, String categoryType, String active, String sortBy, String sortOrder);

    List<StockCategoryMasterDto> getAllStockCategories(Long groupPoid, String categoryType, String active);

    List<StockCategoryMasterDto> getChildCategories(Long parentCategoryPoid, Long groupPoid);

    boolean validateCategoryCode(String categoryCode, Long groupPoid, Long excludeCategoryPoid);

    boolean validateCategoryName(String categoryName, Long groupPoid, Long excludeCategoryPoid);

    StockCategoryGlValuesDto getParentCategoryGlValues(Long parentCategoryPoid, Long groupPoid);

    CategoryDependenciesDto checkCategoryDependencies(Long categoryPoid, Long groupPoid);

    List<StockCategoryHierarchyDto> getCategoryHierarchy(Long categoryPoid, Long groupPoid);

    List<Map<String, Object>> getStockCategoriesHierarchical(Long groupPoid, Long parentPoid, String filterValue, boolean includeDeleted, boolean tree, String sortBy, String sortOrder);
}