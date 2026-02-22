package com.asg.shipchandling.stockunitmaster.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.repository.GroupRepository;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDeleteService;
import com.asg.common.lib.service.DocumentSearchService;
import com.asg.common.lib.service.LoggingService;
import com.asg.shipchandling.exceptions.ResourceAlreadyExistsException;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.stockunitmaster.dto.CreateStockUnitMasterRequest;
import com.asg.shipchandling.stockunitmaster.dto.StockUnitMasterDto;
import com.asg.shipchandling.stockunitmaster.dto.UnitDependenciesDto;
import com.asg.shipchandling.stockunitmaster.entity.StockUnitMaster;
import com.asg.shipchandling.stockunitmaster.exception.StockUnitConstraintViolationException;
import com.asg.shipchandling.stockunitmaster.repository.StockUnitRepository;
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
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockUnitServiceImpl unit tests")
class StockUnitServiceImplTest {

    @Mock
    private StockUnitRepository stockUnitRepository;
    @Mock
    private DocumentSearchService documentService;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private LoggingService loggingService;
    @Mock
    private DocumentDeleteService documentDeleteService;

    @InjectMocks
    private StockUnitServiceImpl stockUnitService;

    private static final Long STOCK_UNIT_POID = 1L;
    private static final Long GROUP_POID = 10L;

    @Nested
    @DisplayName("getStockUnitByPoid")
    class GetStockUnitByPoid {

        @Test
        @DisplayName("throws ResourceNotFoundException when unit does not exist")
        void throwsWhenNotExists() {
            when(stockUnitRepository.existsByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(false);

            assertThatThrownBy(() -> stockUnitService.getStockUnitByPoid(STOCK_UNIT_POID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Unit").hasMessageContaining("UnitPoid");
        }

        @Test
        @DisplayName("returns DTO when unit exists")
        void returnsDtoWhenExists() {
            StockUnitMaster entity = createEntity(STOCK_UNIT_POID, "CODE", "Name", GROUP_POID);
            when(stockUnitRepository.existsByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(true);
            when(stockUnitRepository.findByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(entity);

            StockUnitMasterDto result = stockUnitService.getStockUnitByPoid(STOCK_UNIT_POID);

            assertThat(result).isNotNull();
            assertThat(result.getStockUnitPoid()).isEqualTo(STOCK_UNIT_POID);
            assertThat(result.getStockUnitCode()).isEqualTo("CODE");
            assertThat(result.getStockUnitName()).isEqualTo("Name");
        }
    }

    @Nested
    @DisplayName("createStockUnit")
    class CreateStockUnit {

        @Test
        @DisplayName("throws ResourceAlreadyExistsException when unit name already exists")
        void throwsWhenNameExists() {
            CreateStockUnitMasterRequest request = createRequest("C1", "ExistingName", GROUP_POID);
            when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid("ExistingName", GROUP_POID)).thenReturn(true);

            assertThatThrownBy(() -> stockUnitService.createStockUnit(request))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("ExistingName");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when group does not exist")
        void throwsWhenGroupNotFound() {
            CreateStockUnitMasterRequest request = createRequest("C1", "Name", GROUP_POID);
            when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid(anyString(), anyLong())).thenReturn(false);
            when(groupRepository.findById(GROUP_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockUnitService.createStockUnit(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Group");
        }

        @Test
        @DisplayName("creates unit and returns DTO on success")
        void createsAndReturnsDto() {
            CreateStockUnitMasterRequest request = createRequest("C1", "NewUnit", GROUP_POID);
            StockUnitMaster saved = createEntity(STOCK_UNIT_POID, "C1", "NewUnit", GROUP_POID);
            when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid(anyString(), anyLong())).thenReturn(false);
            when(groupRepository.findById(GROUP_POID)).thenReturn(Optional.of(mock(com.asg.common.lib.entity.GroupEntity.class)));
            when(stockUnitRepository.save(any(StockUnitMaster.class))).thenReturn(saved);

            try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
                userContext.when(UserContext::getDocumentId).thenReturn("doc-1");
                StockUnitMasterDto result = stockUnitService.createStockUnit(request);

                assertThat(result).isNotNull();
                assertThat(result.getStockUnitPoid()).isEqualTo(STOCK_UNIT_POID);
                assertThat(result.getStockUnitCode()).isEqualTo("C1");
                assertThat(result.getStockUnitName()).isEqualTo("NewUnit");
                verify(loggingService).createLogSummaryEntry(eq(LogDetailsEnum.CREATED), eq("doc-1"), eq("1"));
            }
        }

        @Test
        @DisplayName("sets default active Y when request active is null")
        void setsDefaultActive() {
            CreateStockUnitMasterRequest request = createRequest("C1", "Name", GROUP_POID);
            request.setActive(null);
            StockUnitMaster saved = createEntity(STOCK_UNIT_POID, "C1", "Name", GROUP_POID);
            when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid(anyString(), anyLong())).thenReturn(false);
            when(groupRepository.findById(GROUP_POID)).thenReturn(Optional.of(mock(com.asg.common.lib.entity.GroupEntity.class)));
            when(stockUnitRepository.save(argThat(e -> "Y".equals(e.getActive())))).thenReturn(saved);

            try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
                userContext.when(UserContext::getDocumentId).thenReturn("doc-1");
                stockUnitService.createStockUnit(request);
            }
        }
    }

    @Nested
    @DisplayName("updateStockUnit")
    class UpdateStockUnit {

        @Test
        @DisplayName("throws ResourceNotFoundException when unit does not exist")
        void throwsWhenNotExists() {
            StockUnitMasterDto dto = new StockUnitMasterDto();
            dto.setStockUnitPoid(STOCK_UNIT_POID);
            StockUnitMaster existingFromDb = createEntity(STOCK_UNIT_POID, "C1", "Name", GROUP_POID);
            when(stockUnitRepository.findByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(existingFromDb);
            when(stockUnitRepository.existsByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(false);

            assertThatThrownBy(() -> stockUnitService.updateStockUnit(STOCK_UNIT_POID, dto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ResourceAlreadyExistsException when code exists for another unit")
        void throwsWhenCodeExists() {
            StockUnitMaster existing = createEntity(STOCK_UNIT_POID, "C1", "Name", GROUP_POID);
            StockUnitMasterDto dto = new StockUnitMasterDto();
            dto.setStockUnitPoid(STOCK_UNIT_POID);
            dto.setStockUnitCode("OTHER_CODE");
            dto.setStockUnitName("Name");
            dto.setGroupPoid(GROUP_POID);
            when(stockUnitRepository.findByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(existing);
            when(stockUnitRepository.existsByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(true);
            when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot("OTHER_CODE", STOCK_UNIT_POID)).thenReturn(true);

            assertThatThrownBy(() -> stockUnitService.updateStockUnit(STOCK_UNIT_POID, dto))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Unit Code");
        }

        @Test
        @DisplayName("throws ResourceAlreadyExistsException when name exists for another unit")
        void throwsWhenNameExists() {
            StockUnitMaster existing = createEntity(STOCK_UNIT_POID, "C1", "Name", GROUP_POID);
            StockUnitMasterDto dto = new StockUnitMasterDto();
            dto.setStockUnitPoid(STOCK_UNIT_POID);
            dto.setStockUnitCode("C1");
            dto.setStockUnitName("OtherName");
            dto.setGroupPoid(GROUP_POID);
            when(stockUnitRepository.findByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(existing);
            when(stockUnitRepository.existsByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(true);
            when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot(anyString(), anyLong())).thenReturn(false);
            when(stockUnitRepository.existsByStockUnitNameIgnoreCaseAndStockUnitPoidNot("OtherName", STOCK_UNIT_POID)).thenReturn(true);

            assertThatThrownBy(() -> stockUnitService.updateStockUnit(STOCK_UNIT_POID, dto))
                    .isInstanceOf(ResourceAlreadyExistsException.class)
                    .hasMessageContaining("Unit Name");
        }

        @Test
        @DisplayName("updates and returns DTO on success")
        void updatesAndReturnsDto() {
            StockUnitMaster existing = createEntity(STOCK_UNIT_POID, "C1", "OldName", GROUP_POID);
            StockUnitMasterDto dto = new StockUnitMasterDto();
            dto.setStockUnitPoid(STOCK_UNIT_POID);
            dto.setStockUnitCode("C1");
            dto.setStockUnitName("NewName");
            dto.setStockUnitName2("Name2");
            dto.setGroupPoid(GROUP_POID);
            dto.setActive("Y");
            dto.setSeqNo(1);
            existing.setStockUnitName("NewName");
            existing.setLastModifiedDate(LocalDateTime.now());
            when(stockUnitRepository.findByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(existing);
            when(stockUnitRepository.existsByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(true);
            when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot(anyString(), anyLong())).thenReturn(false);
            when(stockUnitRepository.existsByStockUnitNameIgnoreCaseAndStockUnitPoidNot(anyString(), anyLong())).thenReturn(false);
            when(groupRepository.findById(GROUP_POID)).thenReturn(Optional.of(mock(com.asg.common.lib.entity.GroupEntity.class)));
            when(stockUnitRepository.save(any(StockUnitMaster.class))).thenReturn(existing);

            try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
                userContext.when(UserContext::getDocumentId).thenReturn("doc-1");
                StockUnitMasterDto result = stockUnitService.updateStockUnit(STOCK_UNIT_POID, dto);

                assertThat(result).isNotNull();
                assertThat(result.getStockUnitName()).isEqualTo("NewName");
                verify(loggingService).logChanges(any(StockUnitMaster.class), any(StockUnitMaster.class), eq(StockUnitMaster.class), eq("doc-1"), eq("1"), eq(LogDetailsEnum.MODIFIED), eq("STOCK_UNIT_POID"));
            }
        }
    }

    @Nested
    @DisplayName("softDeleteStockUnit")
    class SoftDeleteStockUnit {

        @Test
        @DisplayName("throws ResourceNotFoundException when unit does not exist")
        void throwsWhenNotExists() {
            when(stockUnitRepository.existsByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(false);

            assertThatThrownBy(() -> stockUnitService.softDeleteStockUnit(STOCK_UNIT_POID, new DeleteReasonDto()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("marks unit deleted and calls documentDeleteService")
        void marksDeletedAndCallsDocumentDelete() {
            StockUnitMaster existing = createEntity(STOCK_UNIT_POID, "C1", "Name", GROUP_POID);
            when(stockUnitRepository.existsByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(true);
            when(stockUnitRepository.findByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(existing);
            when(stockUnitRepository.save(any(StockUnitMaster.class))).thenAnswer(i -> i.getArgument(0));

            stockUnitService.softDeleteStockUnit(STOCK_UNIT_POID, new DeleteReasonDto());

            verify(documentDeleteService).deleteDocument(eq(STOCK_UNIT_POID), eq("STOCK_UNIT_MASTER"), eq("STOCK_UNIT_POID"), any(DeleteReasonDto.class), any());
            verify(stockUnitRepository).save(argThat(e -> "Y".equals(e.getDeleted()) && "N".equals(e.getActive())));
        }
    }

    @Nested
    @DisplayName("listStockUnitsUsingParams")
    class ListStockUnitsUsingParams {

        @Test
        @DisplayName("returns empty page when no data")
        void returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(stockUnitRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(Collections.emptyList(), pageable, 0));

            var result = stockUnitService.listStockUnitsUsingParams(null, null, null, null, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("returns page with DTOs when data exists")
        void returnsPageWithContent() {
            Pageable pageable = PageRequest.of(0, 10);
            List<StockUnitMaster> content = List.of(createEntity(1L, "C1", "N1", GROUP_POID));
            when(stockUnitRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(content, pageable, 1));

            var result = stockUnitService.listStockUnitsUsingParams("C1", null, null, null, null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStockUnitCode()).isEqualTo("C1");
        }
    }

    @Nested
    @DisplayName("validateStockUnitCode")
    class ValidateStockUnitCode {

        @Test
        @DisplayName("returns true when code is unique for create")
        void returnsTrueWhenUniqueForCreate() {
            when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndGroupPoid("CODE", GROUP_POID)).thenReturn(false);

            boolean result = stockUnitService.validateStockUnitCode("CODE", GROUP_POID, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when code exists for create")
        void returnsFalseWhenExistsForCreate() {
            when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndGroupPoid("CODE", GROUP_POID)).thenReturn(true);

            boolean result = stockUnitService.validateStockUnitCode("CODE", GROUP_POID, null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("returns true when code unique excluding current for update")
        void returnsTrueWhenUniqueExcludingCurrent() {
            when(stockUnitRepository.existsByStockUnitCodeIgnoreCaseAndGroupPoidAndStockUnitPoidNot("CODE", GROUP_POID, STOCK_UNIT_POID)).thenReturn(false);

            boolean result = stockUnitService.validateStockUnitCode("CODE", GROUP_POID, STOCK_UNIT_POID);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("validateStockUnitName")
    class ValidateStockUnitName {

        @Test
        @DisplayName("returns true when name is unique")
        void returnsTrueWhenUnique() {
            when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid("Name", GROUP_POID)).thenReturn(false);

            boolean result = stockUnitService.validateStockUnitName("Name", GROUP_POID, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when name exists")
        void returnsFalseWhenExists() {
            when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid("Name", GROUP_POID)).thenReturn(true);

            boolean result = stockUnitService.validateStockUnitName("Name", GROUP_POID, null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("checkUnitDependencies")
    class CheckUnitDependencies {

        @Test
        @DisplayName("throws ResourceNotFoundException when unit not found")
        void throwsWhenUnitNotFound() {
            when(stockUnitRepository.findByStockUnitPoidAndGroupPoid(STOCK_UNIT_POID, GROUP_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> stockUnitService.checkUnitDependencies(STOCK_UNIT_POID, GROUP_POID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns canDelete true when no stock items use unit")
        void returnsCanDeleteTrueWhenNoDependencies() {
            StockUnitMaster unit = createEntity(STOCK_UNIT_POID, "C1", "N1", GROUP_POID);
            when(stockUnitRepository.findByStockUnitPoidAndGroupPoid(STOCK_UNIT_POID, GROUP_POID)).thenReturn(Optional.of(unit));
            when(stockUnitRepository.countStockItemsByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(0L);

            UnitDependenciesDto result = stockUnitService.checkUnitDependencies(STOCK_UNIT_POID, GROUP_POID);

            assertThat(result.getStockUnitPoid()).isEqualTo(STOCK_UNIT_POID);
            assertThat(result.getCanDelete()).isTrue();
            assertThat(result.getStockItemCount()).isZero();
            assertThat(result.getMessage()).contains("No dependencies");
        }

        @Test
        @DisplayName("returns canDelete false when stock items use unit")
        void returnsCanDeleteFalseWhenDependencies() {
            StockUnitMaster unit = createEntity(STOCK_UNIT_POID, "C1", "N1", GROUP_POID);
            when(stockUnitRepository.findByStockUnitPoidAndGroupPoid(STOCK_UNIT_POID, GROUP_POID)).thenReturn(Optional.of(unit));
            when(stockUnitRepository.countStockItemsByStockUnitPoid(STOCK_UNIT_POID)).thenReturn(5L);

            UnitDependenciesDto result = stockUnitService.checkUnitDependencies(STOCK_UNIT_POID, GROUP_POID);

            assertThat(result.getCanDelete()).isFalse();
            assertThat(result.getStockItemCount()).isEqualTo(5L);
            assertThat(result.getMessage()).contains("5 stock items");
        }
    }

    @Nested
    @DisplayName("getActiveStockUnits")
    class GetActiveStockUnits {

        @Test
        @DisplayName("returns list filtered by classified and search")
        void returnsFilteredList() {
            StockUnitMaster u1 = createEntity(1L, "C1", "Name1", GROUP_POID);
            u1.setClassified("Y");
            when(stockUnitRepository.findActiveUnitsByGroupPoid(GROUP_POID)).thenReturn(List.of(u1));

            List<StockUnitMasterDto> result = stockUnitService.getActiveStockUnits(GROUP_POID, "Y", "Name1");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStockUnitCode()).isEqualTo("C1");
        }

        @Test
        @DisplayName("filters out units when classified does not match")
        void filtersByClassified() {
            StockUnitMaster u1 = createEntity(1L, "C1", "N1", GROUP_POID);
            u1.setClassified("N");
            when(stockUnitRepository.findActiveUnitsByGroupPoid(GROUP_POID)).thenReturn(List.of(u1));

            List<StockUnitMasterDto> result = stockUnitService.getActiveStockUnits(GROUP_POID, "Y", null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listStockUnits")
    class ListStockUnits {

        @Test
        @DisplayName("returns wrapped page from document search")
        void returnsWrappedPage() {
            FilterRequestDto request = mock(FilterRequestDto.class);
            Pageable pageable = PageRequest.of(0, 10);
            RawSearchResult raw = mock(RawSearchResult.class);
            when(documentService.resolveOperator(request)).thenReturn("AND");
            when(documentService.resolveIsDeleted(request)).thenReturn("N");
            when(documentService.resolveFilters(request)).thenReturn(Collections.emptyList());
            when(documentService.search(anyString(), anyList(), eq("AND"), eq(pageable), eq("N"), anyString(), anyString())).thenReturn(raw);
            when(raw.records()).thenReturn(Collections.emptyList());
            when(raw.totalRecords()).thenReturn(0L);
            when(raw.displayFields()).thenReturn(Collections.emptyMap());

            Map<String, Object> result = stockUnitService.listStockUnits("docId", request, pageable);

            assertThat(result).isNotNull();
            verify(documentService).search(eq("docId"), anyList(), eq("AND"), eq(pageable), eq("N"), eq("STOCK_UNIT_NAME"), eq("STOCK_UNIT_POID"));
        }
    }

    @Nested
    @DisplayName("getStockUnitsByCode")
    class GetStockUnitsByCode {

        @Test
        @DisplayName("throws IllegalArgumentException when code is null")
        void throwsWhenCodeNull() {
            assertThatThrownBy(() -> stockUnitService.getStockUnitsByCode(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("stockUnitCode is required");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when code is blank")
        void throwsWhenCodeBlank() {
            assertThatThrownBy(() -> stockUnitService.getStockUnitsByCode("   "))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("returns list of DTOs matching code pattern")
        void returnsMatchingUnits() {
            List<StockUnitMaster> units = List.of(createEntity(1L, "ABC", "Name", GROUP_POID));
            when(stockUnitRepository.findByStockUnitCodeContains("%code%")).thenReturn(units);

            List<StockUnitMasterDto> result = stockUnitService.getStockUnitsByCode("code");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStockUnitCode()).isEqualTo("ABC");
        }
    }

    @Nested
    @DisplayName("constraint violation handling")
    class ConstraintViolation {

        @Test
        @DisplayName("create converts StockUnitConstraintViolationException UNIQUE to ResourceAlreadyExistsException")
        void createConvertsUniqueConstraint() {
            CreateStockUnitMasterRequest request = createRequest("C1", "Name", GROUP_POID);
            when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid(anyString(), anyLong())).thenReturn(false);
            when(groupRepository.findById(GROUP_POID)).thenReturn(Optional.of(mock(com.asg.common.lib.entity.GroupEntity.class)));
            when(stockUnitRepository.save(any(StockUnitMaster.class))).thenThrow(new StockUnitConstraintViolationException("Code exists", "UK1", "UNIQUE"));

            assertThatThrownBy(() -> stockUnitService.createStockUnit(request))
                    .isInstanceOf(ResourceAlreadyExistsException.class);
        }

        @Test
        @DisplayName("create converts StockUnitConstraintViolationException FK_PARENT to ResourceNotFoundException")
        void createConvertsFkParent() {
            CreateStockUnitMasterRequest request = createRequest("C1", "Name", GROUP_POID);
            when(stockUnitRepository.existsBystockUnitNameIgnoreCaseAndGroupPoid(anyString(), anyLong())).thenReturn(false);
            when(groupRepository.findById(GROUP_POID)).thenReturn(Optional.of(mock(com.asg.common.lib.entity.GroupEntity.class)));
            when(stockUnitRepository.save(any(StockUnitMaster.class))).thenThrow(new StockUnitConstraintViolationException("Group not found", "FK1", "FK_PARENT"));

            assertThatThrownBy(() -> stockUnitService.createStockUnit(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    private static StockUnitMaster createEntity(Long poid, String code, String name, Long groupPoid) {
        StockUnitMaster e = new StockUnitMaster();
        e.setStockUnitPoid(poid);
        e.setStockUnitCode(code);
        e.setStockUnitName(name);
        e.setGroupPoid(groupPoid);
        e.setActive("Y");
        e.setDeleted("N");
        e.setCreatedDate(LocalDateTime.now());
        return e;
    }

    private static CreateStockUnitMasterRequest createRequest(String code, String name, Long groupPoid) {
        CreateStockUnitMasterRequest r = new CreateStockUnitMasterRequest();
        r.setStockUnitCode(code);
        r.setStockUnitName(name);
        r.setGroupPoid(groupPoid);
        r.setActive("Y");
        return r;
    }
}
