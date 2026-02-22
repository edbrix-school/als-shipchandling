package com.asg.shipchandling.salesinvoice.service;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.RawSearchResult;
import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.salesinvoice.dto.*;
import com.asg.shipchandling.salesinvoice.dto.request.CreateSalesInvoiceDtlRequest;
import com.asg.shipchandling.salesinvoice.dto.request.CreateSalesInvoiceRequest;
import com.asg.shipchandling.salesinvoice.dto.request.UpdateSalesInvoiceDtlRequest;
import com.asg.shipchandling.salesinvoice.dto.request.UpdateSalesInvoiceRequest;
import com.asg.shipchandling.salesinvoice.dto.response.ValidationResponse;
import com.asg.shipchandling.salesinvoice.entity.*;
import com.asg.shipchandling.salesinvoice.repository.*;
import com.asg.shipchandling.StockMaster.repository.StockMasterRepository;
import com.asg.shipchandling.deliverynote.repository.SalesDeliveryNoteHdrRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesInvoiceServiceImpl unit tests")
class SalesInvoiceServiceImplTest {

    @Mock
    private SalesInvoiceHdrRepository invoiceHdrRepository;
    @Mock
    private SalesInvoiceHdrRepositoryImpl invoiceHdrRepositoryImpl;
    @Mock
    private SalesInvoiceDtlRepository invoiceDtlRepository;
    @Mock
    private SalesInvoiceDtlRepositoryImpl invoiceDtlRepositoryImpl;
    @Mock
    private SalesDnDtlRepository dnDtlRepository;
    @Mock
    private SalesInvCostbkdDtlRepository costbkdDtlRepository;
    @Mock
    private SalesInvoiceStoredProcRepository salesInvoiceStoredProcRepository;
    @Mock
    private StockMasterRepository stockMasterRepository;
    @Mock
    private SalesDeliveryNoteHdrRepository deliveryNoteHdrRepository;
    @Mock
    private com.asg.common.lib.service.DocumentSearchService documentService;
    @Mock
    private com.asg.common.lib.service.LoggingService loggingService;
    @Mock
    private com.asg.common.lib.service.DocumentDeleteService documentDeleteService;
    @Mock
    private com.asg.common.lib.service.PrintService printService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private DataSource dataSource;

    @Spy
    @InjectMocks
    private SalesInvoiceServiceImpl invoiceService;

    private static final Long TRANSACTION_POID = 1L;
    private static final Long COMPANY_POID = 10L;
    private static final Long GROUP_POID = 100L;
    private static final String USER_ID = "user1";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(invoiceService, "entityManager", entityManager);
        ReflectionTestUtils.setField(invoiceService, "dataSource", dataSource);
    }

    @Nested
    @DisplayName("createSalesInvoice")
    class CreateSalesInvoice {

        @Test
        @DisplayName("throws CustomException when party type is invalid")
        void throwsWhenPartyTypeInvalid() {
            CreateSalesInvoiceRequest request = new CreateSalesInvoiceRequest();
            request.setPartyType("OTHER");
            request.setCustomerPoid(1L);

            assertThatThrownBy(() -> invoiceService.createSalesInvoice(request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("CUSTOMER or PRINCIPAL");
        }

        @Test
        @DisplayName("throws CustomException when party type is CUSTOMER and customerPoid is null")
        void throwsWhenCustomerRequired() {
            CreateSalesInvoiceRequest request = new CreateSalesInvoiceRequest();
            request.setPartyType("CUSTOMER");
            request.setCustomerPoid(null);

            assertThatThrownBy(() -> invoiceService.createSalesInvoice(request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Customer is required");
        }

        @Test
        @DisplayName("throws CustomException when party type is PRINCIPAL and principalPoid is null")
        void throwsWhenPrincipalRequired() {
            CreateSalesInvoiceRequest request = new CreateSalesInvoiceRequest();
            request.setPartyType("PRINCIPAL");
            request.setPrincipalPoid(null);

            assertThatThrownBy(() -> invoiceService.createSalesInvoice(request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Principal is required");
        }

        @Test
        @DisplayName("throws CustomException when customer validation proc returns error")
        void throwsWhenCustomerValidationFails() {
            CreateSalesInvoiceRequest request = new CreateSalesInvoiceRequest();
            request.setPartyType("CUSTOMER");
            request.setCustomerPoid(1L);
            request.setInvoiceDetails(Collections.emptyList());
            ValidationResponse validation = new ValidationResponse();
            validation.setSuccess(false);
            validation.setMessage("ERROR: Invalid customer");

            when(salesInvoiceStoredProcRepository.callCustomerValidateProc(eq(1L), eq(""), any()))
                    .thenReturn(validation);

            assertThatThrownBy(() -> invoiceService.createSalesInvoice(request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Invalid customer");
        }

        @Test
        @DisplayName("creates invoice and returns DTO when request is valid (CUSTOMER)")
        void createsAndReturnsDto() {
            CreateSalesInvoiceRequest request = new CreateSalesInvoiceRequest();
            request.setPartyType("CUSTOMER");
            request.setCustomerPoid(1L);
            request.setInvoiceDetails(Collections.emptyList());
            request.setDeliveryNoteDetails(Collections.emptyList());

            SalesInvoiceHdr saved = new SalesInvoiceHdr();
            saved.setTransactionPoid(TRANSACTION_POID);
            saved.setCompanyPoid(COMPANY_POID);
            saved.setDeleted("N");

            ValidationResponse validation = new ValidationResponse(true, "SUCCESS");
            when(salesInvoiceStoredProcRepository.callCustomerValidateProc(eq(1L), eq(""), any())).thenReturn(validation);
            when(salesInvoiceStoredProcRepository.callCustomerEditValidateProc(TRANSACTION_POID, 1L)).thenReturn(true);
            when(invoiceHdrRepository.save(any(SalesInvoiceHdr.class))).thenReturn(saved);
            when(invoiceHdrRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Optional.of(saved));
            when(invoiceDtlRepositoryImpl.findByTransactionPoidNative(TRANSACTION_POID)).thenReturn(Collections.emptyList());
            when(dnDtlRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Collections.emptyList());
            when(costbkdDtlRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Collections.emptyList());

            try (var userContext = org.mockito.Mockito.mockStatic(com.asg.common.lib.security.util.UserContext.class)) {
                userContext.when(com.asg.common.lib.security.util.UserContext::getDocumentId).thenReturn("doc-1");
                SalesInvoiceHdrDto result = invoiceService.createSalesInvoice(request, GROUP_POID, COMPANY_POID, USER_ID);

                assertThat(result).isNotNull();
                assertThat(result.getTransactionPoid()).isEqualTo(TRANSACTION_POID);
            }
        }
    }

    @Nested
    @DisplayName("getSalesInvoiceByPoid")
    class GetSalesInvoiceByPoid {

        @Test
        @DisplayName("throws ResourceNotFoundException when invoice not found")
        void throwsWhenNotFound() {
            when(invoiceHdrRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.getSalesInvoiceByPoid(TRANSACTION_POID, COMPANY_POID, false))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sales Invoice");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when companyPoid mismatch")
        void throwsWhenCompanyMismatch() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setCompanyPoid(99L);
            when(invoiceHdrRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Optional.of(invoice));
            when(invoiceHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.getSalesInvoiceByPoid(TRANSACTION_POID, COMPANY_POID, false))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sales Invoice");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when invoice is deleted")
        void throwsWhenDeleted() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setCompanyPoid(COMPANY_POID);
            invoice.setDeleted("Y");
            when(invoiceHdrRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Optional.of(invoice));
            when(invoiceHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));

            assertThatThrownBy(() -> invoiceService.getSalesInvoiceByPoid(TRANSACTION_POID, COMPANY_POID, false))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sales Invoice");
        }

        @Test
        @DisplayName("returns DTO when invoice found and not deleted")
        void returnsDtoWhenFound() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setCompanyPoid(COMPANY_POID);
            invoice.setDeleted("N");
            when(invoiceHdrRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Optional.of(invoice));
            when(invoiceHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));
            when(invoiceHdrRepository.findSalesInvoiceWithDetails(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Collections.singletonList(new Object[74]));
            when(invoiceDtlRepositoryImpl.findByTransactionPoidWithTaxDetails(TRANSACTION_POID))
                    .thenReturn(Collections.emptyList());
            when(dnDtlRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Collections.emptyList());
            when(costbkdDtlRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Collections.emptyList());

            SalesInvoiceHdrDto result = invoiceService.getSalesInvoiceByPoid(TRANSACTION_POID, COMPANY_POID, true);

            assertThat(result).isNotNull();
            assertThat(result.getTransactionPoid()).isEqualTo(TRANSACTION_POID);
        }
    }

    @Nested
    @DisplayName("updateSalesInvoice")
    class UpdateSalesInvoice {

        @Test
        @DisplayName("throws ResourceNotFoundException when invoice not found")
        void throwsWhenNotFound() {
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());
            UpdateSalesInvoiceRequest request = new UpdateSalesInvoiceRequest();

            assertThatThrownBy(() -> invoiceService.updateSalesInvoice(TRANSACTION_POID, request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sales Invoice");
        }

        @Test
        @DisplayName("throws CustomException when invoice is deleted")
        void throwsWhenDeleted() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setDeleted("Y");
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));
            UpdateSalesInvoiceRequest request = new UpdateSalesInvoiceRequest();

            assertThatThrownBy(() -> invoiceService.updateSalesInvoice(TRANSACTION_POID, request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("deleted");
        }

        @Test
        @DisplayName("throws CustomException when invoice is verified")
        void throwsWhenVerified() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setDeleted("N");
            invoice.setVerified("Y");
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));
            UpdateSalesInvoiceRequest request = new UpdateSalesInvoiceRequest();

            assertThatThrownBy(() -> invoiceService.updateSalesInvoice(TRANSACTION_POID, request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("verified");
        }
    }

    @Nested
    @DisplayName("deleteSalesInvoice")
    class DeleteSalesInvoice {

        @Test
        @DisplayName("throws ResourceNotFoundException when invoice not found")
        void throwsWhenNotFound() {
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());
            DeleteReasonDto reason = new DeleteReasonDto();

            assertThatThrownBy(() -> invoiceService.deleteSalesInvoice(TRANSACTION_POID, GROUP_POID, COMPANY_POID, reason))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sales Invoice");
        }

        @Test
        @DisplayName("throws CustomException when invoice is verified")
        void throwsWhenVerified() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setVerified("Y");
            invoice.setTransactionDate(LocalDate.now());
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));
            DeleteReasonDto reason = new DeleteReasonDto();

            assertThatThrownBy(() -> invoiceService.deleteSalesInvoice(TRANSACTION_POID, GROUP_POID, COMPANY_POID, reason))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("verified");
        }

        @Test
        @DisplayName("throws CustomException when dependencies prevent delete")
        void throwsWhenDependenciesExist() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setVerified("N");
            invoice.setTransactionDate(LocalDate.now());
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));
            DeleteReasonDto reason = new DeleteReasonDto();

            SalesInvoiceDependenciesDto deps = new SalesInvoiceDependenciesDto();
            deps.setCanDelete(false);
            deps.setMessage("Cannot delete: has receipts");
            doReturn(deps).when(invoiceService).checkSalesInvoiceDependencies(TRANSACTION_POID, GROUP_POID, COMPANY_POID);

            assertThatThrownBy(() -> invoiceService.deleteSalesInvoice(TRANSACTION_POID, GROUP_POID, COMPANY_POID, reason))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("receipts");
        }

        @Test
        @DisplayName("soft deletes invoice when no dependencies")
        void deletesSuccessfully() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setVerified("N");
            invoice.setDeleted("N");
            invoice.setTransactionDate(LocalDate.now());
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));
            DeleteReasonDto reason = new DeleteReasonDto();

            invoiceService.deleteSalesInvoice(TRANSACTION_POID, GROUP_POID, COMPANY_POID, reason);

            verify(invoiceDtlRepository).deleteByTransactionPoid(TRANSACTION_POID);
            verify(dnDtlRepository).deleteByTransactionPoid(TRANSACTION_POID);
            verify(invoiceHdrRepository).save(argThat(h -> "Y".equals(h.getDeleted())));
        }
    }

    @Nested
    @DisplayName("validateDocRef")
    class ValidateDocRef {

        @Test
        @DisplayName("returns invalid when docRef is null")
        void returnsInvalidWhenNull() {
            ValidationResponse result = invoiceService.validateDocRef(null, GROUP_POID, COMPANY_POID, null);
            assertThat(result.getSuccess()).isFalse();
            assertThat(result.getMessage()).contains("empty");
        }

        @Test
        @DisplayName("returns invalid when docRef is blank")
        void returnsInvalidWhenBlank() {
            ValidationResponse result = invoiceService.validateDocRef("  ", GROUP_POID, COMPANY_POID, null);
            assertThat(result.getSuccess()).isFalse();
        }

        @Test
        @DisplayName("returns valid when docRef is unique (create)")
        void returnsValidWhenUnique() {
            when(invoiceHdrRepository.existsByDocRefIgnoreCaseAndGroupPoidAndCompanyPoid("INV-001", GROUP_POID, COMPANY_POID))
                    .thenReturn(false);

            ValidationResponse result = invoiceService.validateDocRef("INV-001", GROUP_POID, COMPANY_POID, null);
            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getMessage()).contains("available");
        }

        @Test
        @DisplayName("returns invalid when docRef already exists (create)")
        void returnsInvalidWhenExists() {
            when(invoiceHdrRepository.existsByDocRefIgnoreCaseAndGroupPoidAndCompanyPoid("INV-001", GROUP_POID, COMPANY_POID))
                    .thenReturn(true);

            ValidationResponse result = invoiceService.validateDocRef("INV-001", GROUP_POID, COMPANY_POID, null);
            assertThat(result.getSuccess()).isFalse();
            assertThat(result.getMessage()).contains("already exists");
        }

        @Test
        @DisplayName("returns valid when docRef unique for update (excluding transactionPoid)")
        void returnsValidWhenUniqueForUpdate() {
            when(invoiceHdrRepository.existsByDocRefIgnoreCaseAndGroupPoidAndCompanyPoidAndTransactionPoidNot(
                    "INV-001", GROUP_POID, COMPANY_POID, TRANSACTION_POID)).thenReturn(false);

            ValidationResponse result = invoiceService.validateDocRef("INV-001", GROUP_POID, COMPANY_POID, TRANSACTION_POID);
            assertThat(result.getSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("checkSalesInvoiceDependencies")
    class CheckSalesInvoiceDependencies {

        @Test
        @DisplayName("throws ResourceNotFoundException when invoice not found")
        void throwsWhenNotFound() {
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.checkSalesInvoiceDependencies(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sales Invoice");
        }

        @Test
        @DisplayName("returns canDelete true when no dependencies")
        void returnsCanDeleteTrue() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));

            SalesInvoiceDependenciesDto result = invoiceService.checkSalesInvoiceDependencies(TRANSACTION_POID, GROUP_POID, COMPANY_POID);

            assertThat(result).isNotNull();
            assertThat(result.getCanDelete()).isTrue();
            assertThat(result.getTransactionPoid()).isEqualTo(TRANSACTION_POID);
        }
    }

    @Nested
    @DisplayName("addInvoiceDetail")
    class AddInvoiceDetail {

        @Test
        @DisplayName("throws ResourceNotFoundException when invoice not found")
        void throwsWhenNotFound() {
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());
            CreateSalesInvoiceDtlRequest request = new CreateSalesInvoiceDtlRequest();

            assertThatThrownBy(() -> invoiceService.addInvoiceDetail(TRANSACTION_POID, request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sales Invoice");
        }

        @Test
        @DisplayName("throws CustomException when invoice is deleted")
        void throwsWhenDeleted() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setDeleted("Y");
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));
            CreateSalesInvoiceDtlRequest request = new CreateSalesInvoiceDtlRequest();

            assertThatThrownBy(() -> invoiceService.addInvoiceDetail(TRANSACTION_POID, request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("deleted");
        }

        @Test
        @DisplayName("throws CustomException when invoice is verified")
        void throwsWhenVerified() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setDeleted("N");
            invoice.setVerified("Y");
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));
            CreateSalesInvoiceDtlRequest request = new CreateSalesInvoiceDtlRequest();

            assertThatThrownBy(() -> invoiceService.addInvoiceDetail(TRANSACTION_POID, request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("verified");
        }

        @Test
        @DisplayName("adds detail and returns DTO")
        void addsDetailAndReturnsDto() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setDeleted("N");
            invoice.setVerified("N");
            CreateSalesInvoiceDtlRequest request = new CreateSalesInvoiceDtlRequest();
            request.setStockPoid(1L);
            request.setQuantity(10L);
            request.setPrice(BigDecimal.TEN);
            SalesInvoiceDtl savedDtl = new SalesInvoiceDtl();
            savedDtl.setTransactionPoid(TRANSACTION_POID);
            savedDtl.setDetRowId(1L);

            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));
            when(invoiceDtlRepository.findMaxDetRowIdByTransactionPoid(TRANSACTION_POID)).thenReturn(null);
            when(invoiceDtlRepository.save(any(SalesInvoiceDtl.class))).thenReturn(savedDtl);

            SalesInvoiceDtlDto result = invoiceService.addInvoiceDetail(TRANSACTION_POID, request, GROUP_POID, COMPANY_POID, USER_ID);

            assertThat(result).isNotNull();
            verify(invoiceDtlRepository).save(any(SalesInvoiceDtl.class));
        }
    }

    @Nested
    @DisplayName("updateInvoiceDetail")
    class UpdateInvoiceDetail {

        @Test
        @DisplayName("throws ResourceNotFoundException when invoice not found")
        void throwsWhenNotFound() {
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());
            UpdateSalesInvoiceDtlRequest request = new UpdateSalesInvoiceDtlRequest();

            assertThatThrownBy(() -> invoiceService.updateInvoiceDetail(TRANSACTION_POID, 1L, request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sales Invoice");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when detail not found")
        void throwsWhenDetailNotFound() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setDeleted("N");
            invoice.setVerified("N");
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));
            when(invoiceDtlRepository.findById(new SalesInvoiceDtlId(TRANSACTION_POID, 1L))).thenReturn(Optional.empty());
            UpdateSalesInvoiceDtlRequest request = new UpdateSalesInvoiceDtlRequest();

            assertThatThrownBy(() -> invoiceService.updateInvoiceDetail(TRANSACTION_POID, 1L, request, GROUP_POID, COMPANY_POID, USER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Invoice Detail");
        }
    }

    @Nested
    @DisplayName("deleteInvoiceDetail")
    class DeleteInvoiceDetail {

        @Test
        @DisplayName("throws ResourceNotFoundException when invoice not found")
        void throwsWhenNotFound() {
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.deleteInvoiceDetail(TRANSACTION_POID, 1L, GROUP_POID, COMPANY_POID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sales Invoice");
        }

        @Test
        @DisplayName("deletes detail when invoice exists and not deleted/verified")
        void deletesSuccessfully() {
            SalesInvoiceHdr invoice = new SalesInvoiceHdr();
            invoice.setTransactionPoid(TRANSACTION_POID);
            invoice.setDeleted("N");
            invoice.setVerified("N");
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(invoice));

            invoiceService.deleteInvoiceDetail(TRANSACTION_POID, 1L, GROUP_POID, COMPANY_POID);

            verify(invoiceDtlRepository).deleteById(new SalesInvoiceDtlId(TRANSACTION_POID, 1L));
        }
    }

    @Nested
    @DisplayName("getInvoiceDetails")
    class GetInvoiceDetails {

        @Test
        @DisplayName("throws ResourceNotFoundException when invoice not found")
        void throwsWhenNotFound() {
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.getInvoiceDetails(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Sales Invoice");
        }

        @Test
        @DisplayName("returns empty list when invoice has no details")
        void returnsEmptyList() {
            when(invoiceHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(new SalesInvoiceHdr()));
            when(invoiceDtlRepository.findByTransactionPoid(TRANSACTION_POID)).thenReturn(Collections.emptyList());

            List<SalesInvoiceDtlDto> result = invoiceService.getInvoiceDetails(TRANSACTION_POID, GROUP_POID, COMPANY_POID);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listSalesInvoices")
    class ListSalesInvoices {

        @Test
        @DisplayName("delegates to documentService and returns paginated result")
        void returnsPaginatedResult() {
            Pageable pageable = PageRequest.of(0, 10);
            com.asg.common.lib.dto.FilterRequestDto request = mock(com.asg.common.lib.dto.FilterRequestDto.class);
            RawSearchResult raw = mock(RawSearchResult.class);
            when(documentService.resolveOperator(request)).thenReturn("AND");
            when(documentService.resolveIsDeleted(request)).thenReturn("N");
            when(documentService.resolveDateFilters(any(), anyString(), any(), any())).thenReturn(Collections.emptyList());
            when(documentService.search(any(), any(), eq("AND"), eq(pageable), eq("N"), anyString(), anyString())).thenReturn(raw);
            when(raw.records()).thenReturn(Collections.singletonList(new HashMap<>()));
            when(raw.totalRecords()).thenReturn(1L);
            when(raw.displayFields()).thenReturn(Collections.emptyMap());

            Map<String, Object> result = invoiceService.listSalesInvoices("AR_SCH_SALES_INVOICE_HDR", request,
                    null, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result).containsKeys("content", "totalElements");
        }
    }

    @Nested
    @DisplayName("validateCustomer")
    class ValidateCustomer {

        @Test
        @DisplayName("throws CustomException when customerPoid is null")
        void throwsWhenNull() {
            assertThatThrownBy(() -> invoiceService.validateCustomer(null, GROUP_POID, COMPANY_POID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Customer POID is required");
        }

        @Test
        @DisplayName("returns response from stored procedure")
        void returnsProcResponse() {
            ValidationResponse procResponse = new ValidationResponse(true, "SUCCESS");
            when(salesInvoiceStoredProcRepository.callCustomerValidateProc(eq(1L), eq(""), eq("")))
                    .thenReturn(procResponse);

            ValidationResponse result = invoiceService.validateCustomer(1L, GROUP_POID, COMPANY_POID);

            assertThat(result).isNotNull();
            assertThat(result.getSuccess()).isTrue();
            assertThat(result.getMessage()).isEqualTo("SUCCESS");
        }
    }
}
