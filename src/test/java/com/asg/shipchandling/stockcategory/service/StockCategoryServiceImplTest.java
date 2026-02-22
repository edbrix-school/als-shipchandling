package com.asg.shipchandling.stockcategory.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.stockcategory.dto.CategoryDependenciesDto;
import com.asg.shipchandling.stockcategory.dto.StockCategoryGlValuesDto;
import com.asg.shipchandling.stockcategory.dto.StockCategoryHierarchyDto;
import com.asg.shipchandling.stockcategory.dto.StockCategoryMasterDto;
import com.asg.shipchandling.stockcategory.dto.StockCategoryTreeDto;
import com.asg.shipchandling.stockcategory.dto.request.CreateStockCategoryRequest;
import com.asg.shipchandling.stockcategory.dto.request.UpdateStockCategoryRequest;
import com.asg.shipchandling.stockcategory.entity.StockCategoryMaster;
import com.asg.shipchandling.stockcategory.repository.StockCategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockCategoryServiceImpl unit tests")
class StockCategoryServiceImplTest {

    @Mock
    private StockCategoryRepository stockCategoryRepository;
    @Mock
    private DocumentSearchService documentService;
    @Mock
    private LoggingService loggingService;
    @Mock
    private DocumentDeleteService documentDeleteService;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private StockCategoryServiceImpl stockCategoryService;

    private static final Long CATEGORY_POID = 1L;
    private static final Long PARENT_POID = 10L;
    private static final Long GROUP_POID = 100L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stockCategoryService, "entityManager", entityManager);
        Query query = mock(Query.class);
        lenient().when(query.getResultList()).thenReturn(Collections.emptyList());
        lenient().when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    }

    @Nested
    @DisplayName("createStockCategory")
    class CreateStockCategory {

        @Test
        @DisplayName("throws ResourceAlreadyExistsException when category name exists")
        void throwsWhenNameExists() {
            CreateStockCategoryRequest request = new CreateStockCategoryRequest();
            request.setCategoryName("Existing");
            request.setCategoryType("GROUP");
            when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid("Existing", GROUP_POID)).thenReturn(true);

            assertThatThrownBy(() -> stockCategoryService.createStockCategory(request, GROUP_POID, "user1"))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Existing");
        }

        @Test
        @DisplayName("throws CustomException when SUB_GROUP has no parent")
        void throwsWhenSubGroupMissingParent() {
            CreateStockCategoryRequest request = new CreateStockCategoryRequest();
            request.setCategoryName("Sub");
            request.setCategoryType("SUB_GROUP");
            request.setParentCategoryPoid(null);
            when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(anyString(), anyLong())).thenReturn(false);

            assertThatThrownBy(() -> stockCategoryService.createStockCategory(request, GROUP_POID, "user1"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Parent category is required");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when parent not found")
        void throwsWhenParentNotFound() {
            CreateStockCategoryRequest request = new CreateStockCategoryRequest();
            request.setCategoryName("Sub");
            request.setCategoryType("SUB_GROUP");
            request.setParentCategoryPoid(PARENT_POID);
            when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(anyString(), anyLong())).thenReturn(false);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(PARENT_POID, GROUP_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockCategoryService.createStockCategory(request, GROUP_POID, "user1"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Parent category");
        }

        @Test
        @DisplayName("throws CustomException when parent is not GROUP type")
        void throwsWhenParentNotGroupType() {
            CreateStockCategoryRequest request = new CreateStockCategoryRequest();
            request.setCategoryName("Sub");
            request.setCategoryType("SUB_GROUP");
            request.setParentCategoryPoid(PARENT_POID);
            StockCategoryMaster parent = createCategory(PARENT_POID, "P", "Parent", "SUB_GROUP", null);
            when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(anyString(), anyLong())).thenReturn(false);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(PARENT_POID, GROUP_POID)).thenReturn(Optional.of(parent));

            assertThatThrownBy(() -> stockCategoryService.createStockCategory(request, GROUP_POID, "user1"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Parent category must be of type GROUP");
        }

        @Test
        @DisplayName("creates GROUP category and returns DTO")
        void createsGroupCategory() {
            CreateStockCategoryRequest request = new CreateStockCategoryRequest();
            request.setCategoryName("NewCategory");
            request.setCategoryType("GROUP");
            when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoid(anyString(), anyLong())).thenReturn(false);
            when(stockCategoryRepository.findByGroupPoid(GROUP_POID)).thenReturn(Collections.emptyList());
            StockCategoryMaster saved = createCategory(CATEGORY_POID, "CAT001", "NewCategory", "GROUP", null);
            when(stockCategoryRepository.save(any(StockCategoryMaster.class))).thenReturn(saved);

            try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
                userContext.when(UserContext::getDocumentId).thenReturn("doc-1");
                StockCategoryMasterDto result = stockCategoryService.createStockCategory(request, GROUP_POID, "user1");

                assertThat(result).isNotNull();
                assertThat(result.getCategoryPoid()).isEqualTo(CATEGORY_POID);
                assertThat(result.getCategoryName()).isEqualTo("NewCategory");
                verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.CREATED), eq("doc-1"), eq("1"));
            }
        }
    }

    @Nested
    @DisplayName("getStockCategoryByPoid")
    class GetStockCategoryByPoid {

        @Test
        @DisplayName("throws ResourceNotFoundException when category not found")
        void throwsWhenNotFound() {
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockCategoryService.getStockCategoryByPoid(CATEGORY_POID, GROUP_POID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Stock Category");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when category is deleted")
        void throwsWhenDeleted() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Name", "GROUP", null);
            cat.setDeleted("Y");
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.of(cat));

            assertThatThrownBy(() -> stockCategoryService.getStockCategoryByPoid(CATEGORY_POID, GROUP_POID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns DTO when category exists and not deleted")
        void returnsDtoWhenExists() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Name", "GROUP", null);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.of(cat));

            StockCategoryMasterDto result = stockCategoryService.getStockCategoryByPoid(CATEGORY_POID, GROUP_POID);

            assertThat(result).isNotNull();
            assertThat(result.getCategoryPoid()).isEqualTo(CATEGORY_POID);
            assertThat(result.getCategoryCode()).isEqualTo("C1");
        }
    }

    @Nested
    @DisplayName("updateStockCategory")
    class UpdateStockCategory {

        @Test
        @DisplayName("throws ResourceNotFoundException when category not found")
        void throwsWhenNotFound() {
            UpdateStockCategoryRequest request = updateRequest("C1", "Name", "GROUP", null);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockCategoryService.updateStockCategory(CATEGORY_POID, request, GROUP_POID, "user1"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws CustomException when category is deleted")
        void throwsWhenDeleted() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Name", "GROUP", null);
            cat.setDeleted("Y");
            UpdateStockCategoryRequest request = updateRequest("C1", "Name", "GROUP", null);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.of(cat));

            assertThatThrownBy(() -> stockCategoryService.updateStockCategory(CATEGORY_POID, request, GROUP_POID, "user1"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Cannot update deleted");
        }

        @Test
        @DisplayName("throws ResourceAlreadyExistsException when code exists for another")
        void throwsWhenCodeExists() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Name", "GROUP", null);
            UpdateStockCategoryRequest request = updateRequest("OTHER", "Name", "GROUP", null);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.of(cat));
            when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot("OTHER", GROUP_POID, CATEGORY_POID)).thenReturn(true);

            assertThatThrownBy(() -> stockCategoryService.updateStockCategory(CATEGORY_POID, request, GROUP_POID, "user1"))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("categoryCode");
        }

        @Test
        @DisplayName("updates and returns DTO on success")
        void updatesAndReturnsDto() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Name", "GROUP", null);
            UpdateStockCategoryRequest request = updateRequest("C1", "NewName", "GROUP", null);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.of(cat));
            when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoidAndCategoryPoidNot(anyString(), anyLong(), anyLong())).thenReturn(false);
            when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoidAndCategoryPoidNot(anyString(), anyLong(), anyLong())).thenReturn(false);
            when(stockCategoryRepository.save(any(StockCategoryMaster.class))).thenAnswer(i -> i.getArgument(0));

            try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
                userContext.when(UserContext::getDocumentId).thenReturn("doc-1");
                StockCategoryMasterDto result = stockCategoryService.updateStockCategory(CATEGORY_POID, request, GROUP_POID, "user1");

                assertThat(result).isNotNull();
                verify(loggingService).logChanges(any(StockCategoryMaster.class), any(StockCategoryMaster.class), eq(StockCategoryMaster.class), eq("doc-1"), eq("1"), eq(LogDetailsEnum.MODIFIED), eq("CATEGORY_POID"));
            }
        }
    }

    @Nested
    @DisplayName("deleteStockCategory")
    class DeleteStockCategory {

        @Test
        @DisplayName("throws when category has children or stock items")
        void throwsWhenHasDependencies() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Name", "GROUP", null);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.of(cat));
            when(stockCategoryRepository.countChildrenByParentCategoryPoid(CATEGORY_POID)).thenReturn(1L);
            when(stockCategoryRepository.countStockItemsByCategoryPoid(CATEGORY_POID)).thenReturn(0L);

            assertThatThrownBy(() -> stockCategoryService.deleteStockCategory(CATEGORY_POID, GROUP_POID, new DeleteReasonDto()))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Cannot delete category");
        }

        @Test
        @DisplayName("soft deletes and calls documentDeleteService when no dependencies")
        void softDeletesWhenNoDependencies() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Name", "GROUP", null);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.of(cat));
            when(stockCategoryRepository.countChildrenByParentCategoryPoid(CATEGORY_POID)).thenReturn(0L);
            when(stockCategoryRepository.countStockItemsByCategoryPoid(CATEGORY_POID)).thenReturn(0L);
            when(stockCategoryRepository.save(any(StockCategoryMaster.class))).thenAnswer(i -> i.getArgument(0));

            stockCategoryService.deleteStockCategory(CATEGORY_POID, GROUP_POID, new DeleteReasonDto());

            verify(documentDeleteService).deleteDocument(eq(CATEGORY_POID), eq("STOCK_CATEGORY_MASTER"), eq("CATEGORY_POID"), any(DeleteReasonDto.class), any());
            verify(stockCategoryRepository).save(argThat(c -> "Y".equals(c.getDeleted())));
        }
    }

    @Nested
    @DisplayName("getStockCategoryTree")
    class GetStockCategoryTree {

        @Test
        @DisplayName("returns filtered tree by categoryType and active")
        void returnsFilteredTree() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Name", "GROUP", null);
            when(stockCategoryRepository.findParentCategoriesByGroupPoid(GROUP_POID)).thenReturn(List.of(cat));

            List<StockCategoryTreeDto> result = stockCategoryService.getStockCategoryTree(GROUP_POID, "GROUP", "Y", null, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategoryPoid()).isEqualTo(CATEGORY_POID);
            assertThat(result.get(0).getCode()).isEqualTo("C1");
        }
    }

    @Nested
    @DisplayName("getAllStockCategories")
    class GetAllStockCategories {

        @Test
        @DisplayName("returns list filtered by type and active")
        void returnsFilteredList() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Name", "GROUP", null);
            when(stockCategoryRepository.findByGroupPoidAndDeletedNotOrDeletedIsNull(GROUP_POID, "Y")).thenReturn(List.of(cat));

            List<StockCategoryMasterDto> result = stockCategoryService.getAllStockCategories(GROUP_POID, "GROUP", "Y");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategoryCode()).isEqualTo("C1");
        }
    }

    @Nested
    @DisplayName("getChildCategories")
    class GetChildCategories {

        @Test
        @DisplayName("returns child DTOs")
        void returnsChildren() {
            StockCategoryMaster child = createCategory(2L, "C2", "Child", "SUB_GROUP", PARENT_POID);
            when(stockCategoryRepository.findChildrenByParentCategoryPoidAndGroupPoid(PARENT_POID, GROUP_POID)).thenReturn(List.of(child));

            List<StockCategoryMasterDto> result = stockCategoryService.getChildCategories(PARENT_POID, GROUP_POID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategoryPoid()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("validateCategoryCode")
    class ValidateCategoryCode {

        @Test
        @DisplayName("returns true when code is unique")
        void returnsTrueWhenUnique() {
            when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid("CODE", GROUP_POID)).thenReturn(false);

            boolean result = stockCategoryService.validateCategoryCode("CODE", GROUP_POID, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when code exists")
        void returnsFalseWhenExists() {
            when(stockCategoryRepository.existsByCategoryCodeIgnoreCaseAndGroupPoid("CODE", GROUP_POID)).thenReturn(true);

            boolean result = stockCategoryService.validateCategoryCode("CODE", GROUP_POID, null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("validateCategoryName")
    class ValidateCategoryName {

        @Test
        @DisplayName("returns true when name is unique excluding current")
        void returnsTrueWhenUniqueExcludingCurrent() {
            when(stockCategoryRepository.existsByCategoryNameIgnoreCaseAndGroupPoidAndCategoryPoidNot("Name", GROUP_POID, CATEGORY_POID)).thenReturn(false);

            boolean result = stockCategoryService.validateCategoryName("Name", GROUP_POID, CATEGORY_POID);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("getParentCategoryGlValues")
    class GetParentCategoryGlValues {

        @Test
        @DisplayName("throws ResourceNotFoundException when parent not found")
        void throwsWhenNotFound() {
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(PARENT_POID, GROUP_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockCategoryService.getParentCategoryGlValues(PARENT_POID, GROUP_POID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws CustomException when parent is not GROUP type")
        void throwsWhenNotGroupType() {
            StockCategoryMaster parent = createCategory(PARENT_POID, "P", "Parent", "SUB_GROUP", null);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(PARENT_POID, GROUP_POID)).thenReturn(Optional.of(parent));

            assertThatThrownBy(() -> stockCategoryService.getParentCategoryGlValues(PARENT_POID, GROUP_POID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("must be of type GROUP");
        }

        @Test
        @DisplayName("returns GL values when parent is GROUP")
        void returnsGlValues() {
            StockCategoryMaster parent = createCategory(PARENT_POID, "P", "Parent", "GROUP", null);
            parent.setStockGlPoid(1L);
            parent.setSalesGlPoid(2L);
            parent.setCostOfSalesGlPoid(3L);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(PARENT_POID, GROUP_POID)).thenReturn(Optional.of(parent));

            StockCategoryGlValuesDto result = stockCategoryService.getParentCategoryGlValues(PARENT_POID, GROUP_POID);

            assertThat(result.getParentCategoryPoid()).isEqualTo(PARENT_POID);
            assertThat(result.getStockGlPoid()).isEqualTo(1L);
            assertThat(result.getSalesGlPoid()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("checkCategoryDependencies")
    class CheckCategoryDependencies {

        @Test
        @DisplayName("throws ResourceNotFoundException when category not found")
        void throwsWhenNotFound() {
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockCategoryService.checkCategoryDependencies(CATEGORY_POID, GROUP_POID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns canDelete true when no dependencies")
        void returnsCanDeleteTrue() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Name", "GROUP", null);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.of(cat));
            when(stockCategoryRepository.countChildrenByParentCategoryPoid(CATEGORY_POID)).thenReturn(0L);
            when(stockCategoryRepository.countStockItemsByCategoryPoid(CATEGORY_POID)).thenReturn(0L);

            CategoryDependenciesDto result = stockCategoryService.checkCategoryDependencies(CATEGORY_POID, GROUP_POID);

            assertThat(result.getCanDelete()).isTrue();
            assertThat(result.getChildCategoryCount()).isZero();
            assertThat(result.getStockItemCount()).isZero();
        }

        @Test
        @DisplayName("returns canDelete false when has dependencies")
        void returnsCanDeleteFalse() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Name", "GROUP", null);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.of(cat));
            when(stockCategoryRepository.countChildrenByParentCategoryPoid(CATEGORY_POID)).thenReturn(2L);
            when(stockCategoryRepository.countStockItemsByCategoryPoid(CATEGORY_POID)).thenReturn(3L);

            CategoryDependenciesDto result = stockCategoryService.checkCategoryDependencies(CATEGORY_POID, GROUP_POID);

            assertThat(result.getCanDelete()).isFalse();
            assertThat(result.getChildCategoryCount()).isEqualTo(2L);
            assertThat(result.getStockItemCount()).isEqualTo(3L);
        }
    }

    @Nested
    @DisplayName("getCategoryHierarchy")
    class GetCategoryHierarchy {

        @Test
        @DisplayName("returns hierarchy from category to root")
        void returnsHierarchy() {
            StockCategoryMaster cat = createCategory(CATEGORY_POID, "C1", "Leaf", "SUB_GROUP", PARENT_POID);
            StockCategoryMaster parent = createCategory(PARENT_POID, "P", "Parent", "GROUP", null);
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(CATEGORY_POID, GROUP_POID)).thenReturn(Optional.of(cat));
            when(stockCategoryRepository.findByCategoryPoidAndGroupPoid(PARENT_POID, GROUP_POID)).thenReturn(Optional.of(parent));

            List<StockCategoryHierarchyDto> result = stockCategoryService.getCategoryHierarchy(CATEGORY_POID, GROUP_POID);

            assertThat(result).isNotEmpty();
            // Hierarchy is root first (parent), then leaf (cat) - added at index 0 each time
            assertThat(result.get(0).getCategoryPoid()).isEqualTo(PARENT_POID);
            assertThat(result.get(result.size() - 1).getCategoryPoid()).isEqualTo(CATEGORY_POID);
        }
    }

    private static StockCategoryMaster createCategory(Long poid, String code, String name, String type, Long parentPoid) {
        StockCategoryMaster c = new StockCategoryMaster();
        c.setCategoryPoid(poid);
        c.setCategoryCode(code);
        c.setCategoryName(name);
        c.setCategoryType(type);
        c.setParentCategoryPoid(parentPoid);
        c.setActive("Y");
        c.setDeleted("N");
        return c;
    }

    private static UpdateStockCategoryRequest updateRequest(String code, String name, String type, Long parentPoid) {
        UpdateStockCategoryRequest r = new UpdateStockCategoryRequest();
        r.setCategoryCode(code);
        r.setCategoryName(name);
        r.setCategoryType(type);
        r.setParentCategoryPoid(parentPoid);
        r.setActive("Y");
        return r;
    }
}
