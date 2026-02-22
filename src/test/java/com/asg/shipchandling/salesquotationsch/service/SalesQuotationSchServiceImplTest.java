package com.asg.shipchandling.salesquotationsch.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.shipchandling.salesquotationsch.dto.SalesQuotationSchFilter;
import com.asg.shipchandling.salesquotationsch.dto.SalesQuotationSchHdrDto;
import com.asg.shipchandling.salesquotationsch.dto.SalesQuotationSchItemDtlDto;
import com.asg.shipchandling.salesquotationsch.dto.request.CreateSalesQuotationSchItemDtlRequest;
import com.asg.shipchandling.salesquotationsch.dto.request.CreateSalesQuotationSchRequest;
import com.asg.shipchandling.salesquotationsch.dto.request.UpdateSalesQuotationSchRequest;
import com.asg.shipchandling.salesquotationsch.dto.response.ValidationResponse;
import com.asg.shipchandling.salesquotationsch.entity.SalesQuotationSchHdr;
import com.asg.shipchandling.salesquotationsch.entity.SalesQuotationSchItemDtl;
import com.asg.shipchandling.salesquotationsch.repository.SalesQuotationSchHdrRepository;
import com.asg.shipchandling.salesquotationsch.repository.SalesQuotationSchItemDtlRepository;
import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesQuotationSchServiceImpl unit tests")
class SalesQuotationSchServiceImplTest {

    @Mock
    private SalesQuotationSchHdrRepository quotationSchHdrRepository;
    @Mock
    private SalesQuotationSchItemDtlRepository itemDtlRepository;
    @Mock
    private com.asg.shipchandling.salesquotationsch.repository.SalesQuotationSchStoredProcRepository quotationSchStoredProcRepository;
    @Mock
    private com.asg.shipchandling.StockMaster.service.StockMasterService stockMasterService;
    @Mock
    private com.asg.shipchandling.common.repository.GlobalCurrencyMasterRepository globalCurrencyMasterRepository;
    @Mock
    private com.asg.shipchandling.common.repository.GlobalCurrencyRatesRepository globalCurrencyRatesRepository;
    @Mock
    private com.asg.common.lib.service.DocumentSearchService documentService;
    @Mock
    private com.asg.common.lib.service.LoggingService loggingService;
    @Mock
    private com.asg.common.lib.service.DocumentDeleteService documentDeleteService;
    @Mock
    private com.asg.common.lib.service.PrintService printService;
    @Mock
    private com.asg.shipchandling.common.repository.GlobalAddressDetailsRepository globalAddressDetailsRepository;
    @Mock
    private com.asg.shipchandling.salesquotationsch.repository.GlobalNewAddressDetailsRepository globalNewAddressDetailsRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private DataSource dataSource;

    @InjectMocks
    private SalesQuotationSchServiceImpl quotationSchService;

    private static final Long TRANSACTION_POID = 1L;
    private static final Long COMPANY_POID = 10L;
    private static final Long GROUP_POID = 100L;
    private static final Long USER_POID = 5L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(quotationSchService, "entityManager", entityManager);
        ReflectionTestUtils.setField(quotationSchService, "dataSource", dataSource);
    }

    @Nested
    @DisplayName("createSalesQuotationSch")
    class CreateSalesQuotationSch {

        @Test
        @DisplayName("throws CustomException when customer is null and newAddressYN is false")
        void throwsWhenCustomerRequired() {
            CreateSalesQuotationSchRequest request = new CreateSalesQuotationSchRequest();
            request.setCustomerPoid(null);
            request.setNewAddressYN(false);
            request.setSalesmanPoid(1L);

            assertThatThrownBy(() -> quotationSchService.createSalesQuotationSch(request, GROUP_POID, COMPANY_POID, USER_POID, "user1"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Customer is required");
        }

        @Test
        @DisplayName("throws CustomException when newAddressYN is true but addressDetails is null")
        void throwsWhenNewAddressWithoutDetails() {
            CreateSalesQuotationSchRequest request = new CreateSalesQuotationSchRequest();
            request.setCustomerPoid(BigDecimal.ONE);
            request.setNewAddressYN(true);
            request.setAddressDetails(null);
            request.setSalesmanPoid(1L);

            assertThatThrownBy(() -> quotationSchService.createSalesQuotationSch(request, GROUP_POID, COMPANY_POID, USER_POID, "user1"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Address details are required");
        }

        @Test
        @DisplayName("creates quotation and returns DTO when request is valid")
        void createsAndReturnsDto() {
            CreateSalesQuotationSchRequest request = new CreateSalesQuotationSchRequest();
            request.setCustomerPoid(BigDecimal.ONE);
            request.setNewAddressYN(false);
            request.setSalesmanPoid(1L);
            request.setItemDetails(Collections.emptyList());

            SalesQuotationSchHdr saved = new SalesQuotationSchHdr();
            saved.setTransactionPoid(TRANSACTION_POID);
            saved.setCompanyPoid(COMPANY_POID);
            saved.setDocRef("QTN-001");
            saved.setDeleted("N");
            when(quotationSchHdrRepository.save(any(SalesQuotationSchHdr.class))).thenReturn(saved);
            when(quotationSchHdrRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Optional.of(saved));
            when(itemDtlRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Collections.emptyList());

            try (var userContext = org.mockito.Mockito.mockStatic(com.asg.common.lib.security.util.UserContext.class)) {
                userContext.when(com.asg.common.lib.security.util.UserContext::getDocumentId).thenReturn("doc-1");
                SalesQuotationSchHdrDto result = quotationSchService.createSalesQuotationSch(request, GROUP_POID, COMPANY_POID, USER_POID, "user1");

                assertThat(result).isNotNull();
                assertThat(result.getTransactionPoid()).isEqualTo(TRANSACTION_POID);
                assertThat(result.getDocRef()).isEqualTo("QTN-001");
            }
        }
    }

    @Nested
    @DisplayName("getSalesQuotationSchByPoid")
    class GetSalesQuotationSchByPoid {

        @Test
        @DisplayName("throws ResourceNotFoundException when quotation not found")
        void throwsWhenNotFound() {
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> quotationSchService.getSalesQuotationSchByPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID, USER_POID, false))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sales Quotation SCH");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when quotation is deleted")
        void throwsWhenDeleted() {
            SalesQuotationSchHdr hdr = new SalesQuotationSchHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setCompanyPoid(COMPANY_POID);
            hdr.setDeleted("Y");
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(hdr));

            assertThatThrownBy(() -> quotationSchService.getSalesQuotationSchByPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID, USER_POID, false))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns DTO when found and not deleted")
        void returnsDtoWhenFound() {
            SalesQuotationSchHdr hdr = new SalesQuotationSchHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setCompanyPoid(COMPANY_POID);
            hdr.setDeleted("N");
            hdr.setDocRef("QTN-001");
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(hdr));
            when(quotationSchHdrRepository.findSalesQuotationSchHeader(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Collections.emptyList());
            lenient().when(quotationSchStoredProcRepository.callNewTempAddressLoadListProc(anyLong(), anyLong(), anyLong(), anyString(), anyLong()))
                    .thenReturn(Collections.emptyList());

            SalesQuotationSchHdrDto result = quotationSchService.getSalesQuotationSchByPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID, USER_POID, false);

            assertThat(result).isNotNull();
            assertThat(result.getTransactionPoid()).isEqualTo(TRANSACTION_POID);
            assertThat(result.getDocRef()).isEqualTo("QTN-001");
        }
    }

    @Nested
    @DisplayName("updateSalesQuotationSch")
    class UpdateSalesQuotationSch {

        @Test
        @DisplayName("throws ResourceNotFoundException when quotation not found")
        void throwsWhenNotFound() {
            UpdateSalesQuotationSchRequest request = new UpdateSalesQuotationSchRequest();
            request.setCustomerPoid(BigDecimal.ONE);
            request.setNewAddressYN(false);
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> quotationSchService.updateSalesQuotationSch(GROUP_POID, TRANSACTION_POID, request, COMPANY_POID, USER_POID, "user1"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws CustomException when quotation is deleted")
        void throwsWhenDeleted() {
            SalesQuotationSchHdr hdr = new SalesQuotationSchHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setDeleted("Y");
            UpdateSalesQuotationSchRequest request = new UpdateSalesQuotationSchRequest();
            request.setCustomerPoid(BigDecimal.ONE);
            request.setNewAddressYN(false);
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(hdr));

            assertThatThrownBy(() -> quotationSchService.updateSalesQuotationSch(GROUP_POID, TRANSACTION_POID, request, COMPANY_POID, USER_POID, "user1"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Cannot update deleted");
        }
    }

    @Nested
    @DisplayName("deleteSalesQuotationSch")
    class DeleteSalesQuotationSch {

        @Test
        @DisplayName("throws ResourceNotFoundException when quotation not found")
        void throwsWhenNotFound() {
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> quotationSchService.deleteSalesQuotationSch(GROUP_POID, TRANSACTION_POID, COMPANY_POID, new DeleteReasonDto()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("soft deletes and calls documentDeleteService")
        void softDeletes() {
            SalesQuotationSchHdr hdr = new SalesQuotationSchHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setCompanyPoid(COMPANY_POID);
            hdr.setTransactionDate(LocalDate.now());
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(hdr));
            when(quotationSchHdrRepository.save(any(SalesQuotationSchHdr.class))).thenAnswer(i -> i.getArgument(0));

            quotationSchService.deleteSalesQuotationSch(GROUP_POID, TRANSACTION_POID, COMPANY_POID, new DeleteReasonDto());

            verify(documentDeleteService).deleteDocument(eq(TRANSACTION_POID), eq("SALES_QUOTATION_HDR"), eq("TRANSACTION_POID"), any(DeleteReasonDto.class), any(LocalDate.class));
            verify(itemDtlRepository).deleteByTransactionPoid(TRANSACTION_POID);
            verify(quotationSchHdrRepository).save(argThat(q -> "Y".equals(q.getDeleted())));
        }
    }

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("throws when filter is null")
        void throwsWhenFilterNull() {
            assertThatThrownBy(() -> quotationSchService.search(null, "user1"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("filter is required");
        }

        @Test
        @DisplayName("throws when companyPoid is null in filter")
        void throwsWhenCompanyPoidNull() {
            SalesQuotationSchFilter filter = new SalesQuotationSchFilter();
            filter.setCompanyPoid(null);

            assertThatThrownBy(() -> quotationSchService.search(filter, "user1"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("companyPoid is required");
        }

        @Test
        @DisplayName("returns list response with content")
        void returnsListResponse() {
            SalesQuotationSchFilter filter = new SalesQuotationSchFilter();
            filter.setCompanyPoid(COMPANY_POID);
            filter.setPage(0);
            filter.setSize(10);
            SalesQuotationSchHdr hdr = new SalesQuotationSchHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setCompanyPoid(COMPANY_POID);
            when(quotationSchHdrRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(hdr), PageRequest.of(0, 10), 1));

            var result = quotationSchService.search(filter, "user1");

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("validateDocRef")
    class ValidateDocRef {

        @Test
        @DisplayName("returns invalid when docRef is null")
        void returnsInvalidWhenNull() {
            ValidationResponse result = quotationSchService.validateDocRef(null, null);

            assertThat(result).isNotNull();
            assertThat(result.getIsValid()).isFalse();
            assertThat(result.getMessage()).contains("cannot be empty");
        }

        @Test
        @DisplayName("returns invalid when docRef is blank")
        void returnsInvalidWhenBlank() {
            ValidationResponse result = quotationSchService.validateDocRef("  ", null);

            assertThat(result.getIsValid()).isFalse();
        }

        @Test
        @DisplayName("returns valid when docRef does not exist")
        void returnsValidWhenUnique() {
            when(quotationSchHdrRepository.countByCompanyPoidAndDocRefExcluding(null, "DOC-001", TRANSACTION_POID))
                    .thenReturn(0L);

            ValidationResponse result = quotationSchService.validateDocRef("DOC-001", TRANSACTION_POID);

            assertThat(result.getIsUnique()).isTrue();
            assertThat(result.getIsValid()).isTrue();
        }

        @Test
        @DisplayName("returns invalid when docRef exists")
        void returnsInvalidWhenExists() {
            when(quotationSchHdrRepository.countByCompanyPoidAndDocRefExcluding(null, "DOC-001", TRANSACTION_POID))
                    .thenReturn(1L);

            ValidationResponse result = quotationSchService.validateDocRef("DOC-001", TRANSACTION_POID);

            assertThat(result.getIsUnique()).isFalse();
            assertThat(result.getIsValid()).isFalse();
            assertThat(result.getMessage()).contains("already exists");
        }
    }

    @Nested
    @DisplayName("addItemDetail")
    class AddItemDetail {

        @Test
        @DisplayName("throws ResourceNotFoundException when quotation not found")
        void throwsWhenQuotationNotFound() {
            CreateSalesQuotationSchItemDtlRequest request = new CreateSalesQuotationSchItemDtlRequest();
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> quotationSchService.addItemDetail(TRANSACTION_POID, request, COMPANY_POID, "user1"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws CustomException when quotation is deleted")
        void throwsWhenQuotationDeleted() {
            SalesQuotationSchHdr hdr = new SalesQuotationSchHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setDeleted("Y");
            CreateSalesQuotationSchItemDtlRequest request = new CreateSalesQuotationSchItemDtlRequest();
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(hdr));

            assertThatThrownBy(() -> quotationSchService.addItemDetail(TRANSACTION_POID, request, COMPANY_POID, "user1"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("deleted");
        }

        @Test
        @DisplayName("adds item and returns DTO")
        void addsItemAndReturnsDto() {
            SalesQuotationSchHdr hdr = new SalesQuotationSchHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setDeleted("N");
            CreateSalesQuotationSchItemDtlRequest request = new CreateSalesQuotationSchItemDtlRequest();
            request.setStockPoid(1L);
            request.setQuantity(10L);
            request.setPrice(BigDecimal.TEN);
            SalesQuotationSchItemDtl savedItem = new SalesQuotationSchItemDtl();
            savedItem.setTransactionPoid(TRANSACTION_POID);
            savedItem.setDetRowId(1L);
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(hdr));
            when(quotationSchHdrRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Optional.of(hdr));
            when(itemDtlRepository.getMaxDetRowIdByTransactionPoid(TRANSACTION_POID)).thenReturn(null);
            when(itemDtlRepository.save(any(SalesQuotationSchItemDtl.class))).thenReturn(savedItem);

            SalesQuotationSchItemDtlDto result = quotationSchService.addItemDetail(TRANSACTION_POID, request, COMPANY_POID, "user1");

            assertThat(result).isNotNull();
            verify(itemDtlRepository).save(any(SalesQuotationSchItemDtl.class));
        }
    }

    @Nested
    @DisplayName("updateItemDetail")
    class UpdateItemDetail {

        @Test
        @DisplayName("throws ResourceNotFoundException when item detail not found")
        void throwsWhenItemNotFound() {
            SalesQuotationSchHdr hdr = new SalesQuotationSchHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setDeleted("N");
            CreateSalesQuotationSchItemDtlRequest request = new CreateSalesQuotationSchItemDtlRequest();
            Long detRowId = 1L;
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(hdr));
            when(itemDtlRepository.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> quotationSchService.updateItemDetail(TRANSACTION_POID, detRowId, request, COMPANY_POID, "user1"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Item Detail");
        }
    }

    @Nested
    @DisplayName("deleteItemDetail")
    class DeleteItemDetail {

        @Test
        @DisplayName("throws CustomException when quotation is deleted")
        void throwsWhenQuotationDeleted() {
            SalesQuotationSchHdr hdr = new SalesQuotationSchHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setDeleted("Y");
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(hdr));

            assertThatThrownBy(() -> quotationSchService.deleteItemDetail(TRANSACTION_POID, 1L, COMPANY_POID))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("getItemDetails")
    class GetItemDetails {

        @Test
        @DisplayName("throws ResourceNotFoundException when quotation not found")
        void throwsWhenNotFound() {
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> quotationSchService.getItemDetails(TRANSACTION_POID, COMPANY_POID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns empty list when no items")
        void returnsEmptyList() {
            when(quotationSchHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(new SalesQuotationSchHdr()));
            when(itemDtlRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Collections.emptyList());

            List<SalesQuotationSchItemDtlDto> result = quotationSchService.getItemDetails(TRANSACTION_POID, COMPANY_POID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listSalesQuotationSch")
    class ListSalesQuotationSch {

        @Test
        @DisplayName("returns wrapped page from document search")
        void returnsWrappedPage() {
            com.asg.common.lib.dto.FilterRequestDto request = mock(com.asg.common.lib.dto.FilterRequestDto.class);
            Pageable pageable = PageRequest.of(0, 10);
            RawSearchResult raw = mock(RawSearchResult.class);
            when(documentService.resolveOperator(request)).thenReturn("AND");
            when(documentService.resolveIsDeleted(request)).thenReturn("N");
            when(documentService.resolveDateFilters(any(), anyString(), any(), any())).thenReturn(Collections.emptyList());
            when(documentService.search(anyString(), anyList(), eq("AND"), eq(pageable), eq("N"), anyString(), anyString())).thenReturn(raw);
            when(raw.records()).thenReturn(Collections.emptyList());
            when(raw.totalRecords()).thenReturn(0L);
            when(raw.displayFields()).thenReturn(Collections.emptyMap());

            Map<String, Object> result = quotationSchService.listSalesQuotationSch("docId", request, LocalDate.now(), LocalDate.now(), pageable);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Stored procedure delegation")
    class StoredProcedureDelegation {

        @Test
        @DisplayName("importItems delegates to repository and returns response")
        void importItemsDelegates() {
            var req = new com.asg.shipchandling.salesquotationsch.dto.request.ImportItemsRequest();
            var expected = new com.asg.shipchandling.salesquotationsch.dto.response.StoredProcedureResponse();
            expected.setSuccess(true);
            when(quotationSchStoredProcRepository.callImportItemsProc(any())).thenReturn(expected);

            var result = quotationSchService.importItems(req);

            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            verify(quotationSchStoredProcRepository).callImportItemsProc(any());
        }

        @Test
        @DisplayName("clearItems delegates to repository")
        void clearItemsDelegates() {
            var req = new com.asg.shipchandling.salesquotationsch.dto.request.ClearItemsRequest();
            var expected = new com.asg.shipchandling.salesquotationsch.dto.response.StoredProcedureResponse();
            when(quotationSchStoredProcRepository.callClearItemsProc(any())).thenReturn(expected);

            var result = quotationSchService.clearItems(req);

            assertThat(result).isNotNull();
            verify(quotationSchStoredProcRepository).callClearItemsProc(any());
        }
    }
}
