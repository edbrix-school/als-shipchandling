package com.asg.shipchandling.StockMaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.shipchandling.StockMaster.dto.*;
import com.asg.shipchandling.StockMaster.entity.StockMasterEntity;
import com.asg.shipchandling.StockMaster.repository.StockCategoryMasterRepository;
import com.asg.shipchandling.StockMaster.repository.StockMasterDtlRepository;
import com.asg.shipchandling.StockMaster.repository.StockMasterRepository;
import com.asg.shipchandling.StockMaster.repository.StockMasterWarehouseDtlRepository;
import com.asg.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockMasterServiceImpl unit tests")
class StockMasterServiceImplTest {

    @Mock
    private StockMasterRepository stockMasterRepository;
    @Mock
    private StockMasterDtlRepository dtlRepository;
    @Mock
    private StockMasterWarehouseDtlRepository warehouseRepository;
    @Mock
    private StockCategoryMasterRepository categoryMasterRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private LoggingService loggingService;
    @Mock
    private DocumentDeleteService documentDeleteService;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private StockMasterServiceImpl stockMasterService;

    private static final Long STOCK_POID = 1L;
    private static final Long GROUP_POID = 100L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(stockMasterService, "stockMasterRepository", stockMasterRepository);
        ReflectionTestUtils.setField(stockMasterService, "dtlRepository", dtlRepository);
        ReflectionTestUtils.setField(stockMasterService, "warehouseRepository", warehouseRepository);
        ReflectionTestUtils.setField(stockMasterService, "categoryMasterRepository", categoryMasterRepository);
        ReflectionTestUtils.setField(stockMasterService, "jdbcTemplate", jdbcTemplate);
        ReflectionTestUtils.setField(stockMasterService, "loggingService", loggingService);
        ReflectionTestUtils.setField(stockMasterService, "documentDeleteService", documentDeleteService);
        ReflectionTestUtils.setField(stockMasterService, "entityManager", entityManager);
    }

    @Nested
    @DisplayName("getStockMasterById")
    class GetStockMasterById {

        @Test
        @DisplayName("throws ResourceNotFoundException when stock not found")
        void throwsWhenNotFound() {
            when(stockMasterRepository.findById(STOCK_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockMasterService.getStockMasterById(STOCK_POID, false, GROUP_POID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Stock Master");
        }

        @Test
        @DisplayName("returns null when groupPoid does not match")
        void returnsNullWhenGroupMismatch() {
            StockMasterEntity entity = createStockEntity(STOCK_POID, "S1", "Stock1", 999L);
            when(stockMasterRepository.findById(STOCK_POID)).thenReturn(Optional.of(entity));

            StockMasterViewResponse result = stockMasterService.getStockMasterById(STOCK_POID, false, GROUP_POID);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns response when found and group matches")
        void returnsResponseWhenFound() {
            StockMasterEntity entity = createStockEntity(STOCK_POID, "S1", "Stock1", GROUP_POID);
            when(stockMasterRepository.findById(STOCK_POID)).thenReturn(Optional.of(entity));
            when(stockMasterRepository.findStockMasterWithDetails(STOCK_POID)).thenReturn(Collections.emptyList());
            when(dtlRepository.findByStockPoid(STOCK_POID)).thenReturn(Collections.emptyList());
            when(warehouseRepository.findByStockPoid(STOCK_POID)).thenReturn(Collections.emptyList());

            StockMasterViewResponse result = stockMasterService.getStockMasterById(STOCK_POID, false, GROUP_POID);

            assertThat(result).isNotNull();
            assertThat(result.getStockPoid()).isEqualTo(STOCK_POID);
            assertThat(result.getStockCode()).isEqualTo("S1");
            assertThat(result.getStockName()).isEqualTo("Stock1");
        }
    }

    @Nested
    @DisplayName("createStockMaster")
    class CreateStockMaster {

        @Test
        @DisplayName("throws IllegalArgumentException when stock name is null")
        void throwsWhenStockNameNull() {
            CreateStockMasterRequest request = validCreateRequest();
            request.setStockName(null);

            assertThatThrownBy(() -> stockMasterService.createStockMaster(request, GROUP_POID, 1L, "user1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Stock name");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when categoryPoid is null")
        void throwsWhenCategoryPoidNull() {
            CreateStockMasterRequest request = validCreateRequest();
            request.setCategoryPoid(null);

            assertThatThrownBy(() -> stockMasterService.createStockMaster(request, GROUP_POID, 1L, "user1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Category POID");
        }

        @Test
        @DisplayName("throws ResourceAlreadyExistsException when stock name exists")
        void throwsWhenStockNameExists() {
            CreateStockMasterRequest request = validCreateRequest();
            when(stockMasterRepository.existsByStockNameAndGroupPoid("NewStock", GROUP_POID)).thenReturn(true);

            assertThatThrownBy(() -> stockMasterService.createStockMaster(request, GROUP_POID, 1L, "user1"))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Stock name");
        }
    }

    @Nested
    @DisplayName("updateStockMaster")
    class UpdateStockMaster {

        @Test
        @DisplayName("throws ResourceNotFoundException when stock not found")
        void throwsWhenNotFound() {
            UpdateStockMasterRequest request = new UpdateStockMasterRequest();
            when(stockMasterRepository.findByStockPoidAndGroupPoid(STOCK_POID, GROUP_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockMasterService.updateStockMaster(STOCK_POID, request, GROUP_POID, 1L, "user1"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws IllegalStateException when stock is deleted")
        void throwsWhenDeleted() {
            StockMasterEntity entity = createStockEntity(STOCK_POID, "S1", "Stock1", GROUP_POID);
            entity.setDeleted("Y");
            UpdateStockMasterRequest request = new UpdateStockMasterRequest();
            when(stockMasterRepository.findByStockPoidAndGroupPoid(STOCK_POID, GROUP_POID)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> stockMasterService.updateStockMaster(STOCK_POID, request, GROUP_POID, 1L, "user1"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("deleted");
        }
    }

    @Nested
    @DisplayName("deleteStockMaster")
    class DeleteStockMaster {

        @Test
        @DisplayName("throws ResourceNotFoundException when stock not found")
        void throwsWhenNotFound() {
            when(stockMasterRepository.findByStockPoidAndGroupPoid(STOCK_POID, GROUP_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockMasterService.deleteStockMaster(STOCK_POID, GROUP_POID, new DeleteReasonDto()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("soft deletes and calls documentDeleteService")
        void softDeletesAndCallsDocumentDelete() {
            StockMasterEntity entity = createStockEntity(STOCK_POID, "S1", "Stock1", GROUP_POID);
            when(stockMasterRepository.findByStockPoidAndGroupPoid(STOCK_POID, GROUP_POID)).thenReturn(Optional.of(entity));
            when(stockMasterRepository.save(any(StockMasterEntity.class))).thenAnswer(i -> i.getArgument(0));

            stockMasterService.deleteStockMaster(STOCK_POID, GROUP_POID, new DeleteReasonDto());

            verify(documentDeleteService).deleteDocument(eq(STOCK_POID), eq("STOCK_MASTER"), eq("STOCK_POID"), any(DeleteReasonDto.class), any());
            verify(dtlRepository).deleteByStockPoid(STOCK_POID);
            verify(warehouseRepository).deleteByStockPoid(STOCK_POID);
            verify(stockMasterRepository).save(argThat(s -> "Y".equals(s.getDeleted())));
        }
    }

    @Nested
    @DisplayName("validateStockCode")
    class ValidateStockCode {

        @Test
        @DisplayName("returns unique when code is unique")
        void returnsUniqueWhenUnique() {
            when(stockMasterRepository.existsByStockCodeIgnoreCaseAndGroupPoid("CODE", GROUP_POID)).thenReturn(false);

            ValidationResponse result = stockMasterService.validateStockCode("CODE", GROUP_POID, null);

            assertThat(result).isNotNull();
            assertThat(result.getIsUnique()).isTrue();
        }

        @Test
        @DisplayName("returns not unique when code exists")
        void returnsNotUniqueWhenExists() {
            when(stockMasterRepository.existsByStockCodeIgnoreCaseAndGroupPoid("CODE", GROUP_POID)).thenReturn(true);

            ValidationResponse result = stockMasterService.validateStockCode("CODE", GROUP_POID, null);

            assertThat(result).isNotNull();
            assertThat(result.getIsUnique()).isFalse();
        }
    }

    @Nested
    @DisplayName("validateStockName")
    class ValidateStockName {

        @Test
        @DisplayName("returns unique when name is unique excluding current")
        void returnsUniqueWhenUniqueExcludingCurrent() {
            when(stockMasterRepository.existsByStockNameAndGroupPoidAndStockPoidNot("Name", GROUP_POID, STOCK_POID)).thenReturn(false);

            ValidationResponse result = stockMasterService.validateStockName("Name", GROUP_POID, STOCK_POID);

            assertThat(result).isNotNull();
            assertThat(result.getIsUnique()).isTrue();
        }
    }

    @Nested
    @DisplayName("checkStockMasterDependencies")
    class CheckStockMasterDependencies {

        @Test
        @DisplayName("throws ResourceNotFoundException when stock not found")
        void throwsWhenNotFound() {
            when(stockMasterRepository.findByStockPoidAndGroupPoid(STOCK_POID, GROUP_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockMasterService.checkStockMasterDependencies(STOCK_POID, GROUP_POID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns canDelete true when no dependencies")
        void returnsCanDeleteTrue() {
            StockMasterEntity entity = createStockEntity(STOCK_POID, "S1", "Stock1", GROUP_POID);
            when(stockMasterRepository.findByStockPoidAndGroupPoid(STOCK_POID, GROUP_POID)).thenReturn(Optional.of(entity));

            StockMasterDependenciesDto result = stockMasterService.checkStockMasterDependencies(STOCK_POID, GROUP_POID);

            assertThat(result).isNotNull();
            assertThat(result.getStockPoid()).isEqualTo(STOCK_POID);
            assertThat(result.getCanDelete()).isTrue();
            assertThat(result.getMessage()).contains("No dependencies");
        }
    }

    @Nested
    @DisplayName("getStockMasters")
    class GetStockMasters {

        @Test
        @DisplayName("returns page from repository")
        void returnsPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Map<String, String> filters = Map.of("groupPoid", String.valueOf(GROUP_POID));
            List<StockMasterEntity> content = List.of(createStockEntity(STOCK_POID, "S1", "Stock1", GROUP_POID));
            when(stockMasterRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(content, pageable, 1));

            var result = stockMasterService.getStockMasters(filters, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStockCode()).isEqualTo("S1");
        }
    }

    private static StockMasterEntity createStockEntity(Long poid, String code, String name, Long groupPoid) {
        StockMasterEntity e = new StockMasterEntity();
        e.setStockPoid(poid);
        e.setStockCode(code);
        e.setStockName(name);
        e.setGroupPoid(groupPoid);
        e.setDeleted("N");
        e.setActive("Y");
        return e;
    }

    private static CreateStockMasterRequest validCreateRequest() {
        CreateStockMasterRequest r = new CreateStockMasterRequest();
        r.setStockName("NewStock");
        r.setCategoryPoid(1L);
        r.setStockUnitPoid(1L);
        r.setPurchaseStockUnitPoid(1L);
        r.setTaxPoid(1L);
        r.setInputTaxPoid(1L);
        r.setCurrencyCode("USD");
        r.setPrice1(BigDecimal.ONE);
        return r;
    }
}
