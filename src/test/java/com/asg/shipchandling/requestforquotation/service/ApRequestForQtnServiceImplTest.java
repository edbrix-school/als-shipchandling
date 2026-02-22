package com.asg.shipchandling.requestforquotation.service;

import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.requestforquotation.dto.RfqDependenciesDto;
import com.asg.shipchandling.requestforquotation.dto.request.ApRequestForQtnHdrDto;
import com.asg.shipchandling.requestforquotation.entity.ApRequestForQtnHdr;
import com.asg.shipchandling.requestforquotation.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import jakarta.persistence.EntityManager;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApRequestForQtnServiceImpl unit tests")
class ApRequestForQtnServiceImplTest {

    @Mock
    private ApRequestForQtnHdrRepository rfqHdrRepository;
    @Mock
    private ApRequestForQtnItemDtlRepository rfqItemDtlRepository;
    @Mock
    private ApRequestForQtnSupDtlRepository rfqSupDtlRepository;
    @Mock
    private GlobalTaxMasterRepository globalTaxMasterRepository;
    @Mock
    private CurrencyRateUploadTempRepository currencyRateUploadTempRepository;
    @Mock
    private com.asg.common.lib.service.DocumentSearchService documentService;
    @Mock
    private com.asg.common.lib.service.LoggingService loggingService;
    @Mock
    private com.asg.common.lib.service.DocumentDeleteService documentDeleteService;
    @Mock
    private com.asg.common.lib.service.PrintService printService;
    @Mock
    private DataSource dataSource;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ApRequestForQtnServiceImpl rfqService;

    private static final Long TRANSACTION_POID = 1L;
    private static final Long GROUP_POID = 100L;
    private static final Long COMPANY_POID = 10L;

    @Nested
    @DisplayName("createRequestForQuotation")
    class CreateRequestForQuotation {

        @Test
        @DisplayName("throws CustomException when request is null")
        void throwsWhenRequestNull() {
            assertThatThrownBy(() -> rfqService.createRequestForQuotation(null, GROUP_POID, COMPANY_POID, "user1"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Request body cannot be empty");
        }

        @Test
        @DisplayName("throws CustomException when groupPoid is null")
        void throwsWhenGroupPoidNull() {
            assertThatThrownBy(() -> rfqService.createRequestForQuotation(
                    new com.asg.shipchandling.requestforquotation.dto.request.CreateApRequestForQtnRequest(),
                    null, COMPANY_POID, "user1"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Group POID");
        }

        @Test
        @DisplayName("throws CustomException when companyPoid is null")
        void throwsWhenCompanyPoidNull() {
            assertThatThrownBy(() -> rfqService.createRequestForQuotation(
                    new com.asg.shipchandling.requestforquotation.dto.request.CreateApRequestForQtnRequest(),
                    GROUP_POID, null, "user1"))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Company POID");
        }
    }

    @Nested
    @DisplayName("getRequestForQuotationByPoid")
    class GetRequestForQuotationByPoid {

        @Test
        @DisplayName("throws ResourceNotFoundException when RFQ not found")
        void throwsWhenNotFound() {
            when(rfqHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> rfqService.getRequestForQuotationByPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID, false))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("RFQ");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when RFQ is deleted")
        void throwsWhenDeleted() {
            ApRequestForQtnHdr rfq = new ApRequestForQtnHdr();
            rfq.setTransactionPoid(TRANSACTION_POID);
            rfq.setDeleted("Y");
            when(rfqHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(rfq));

            assertThatThrownBy(() -> rfqService.getRequestForQuotationByPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID, false))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns DTO when found and not deleted")
        void returnsDtoWhenFound() {
            ApRequestForQtnHdr rfq = new ApRequestForQtnHdr();
            rfq.setTransactionPoid(TRANSACTION_POID);
            rfq.setGroupPoid(GROUP_POID);
            rfq.setCompanyPoid(COMPANY_POID);
            rfq.setDeleted("N");
            rfq.setDocRef("RFQ-001");
            when(rfqHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(rfq));

            ApRequestForQtnHdrDto result = rfqService.getRequestForQuotationByPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID, false);

            assertThat(result).isNotNull();
            assertThat(result.getTransactionPoid()).isEqualTo(TRANSACTION_POID);
            assertThat(result.getDocRef()).isEqualTo("RFQ-001");
        }
    }

    @Nested
    @DisplayName("checkRfqDependencies")
    class CheckRfqDependencies {

        @Test
        @DisplayName("throws CustomException when groupPoid is null")
        void throwsWhenGroupPoidNull() {
            assertThatThrownBy(() -> rfqService.checkRfqDependencies(TRANSACTION_POID, null, COMPANY_POID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Group POID");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when RFQ not found")
        void throwsWhenNotFound() {
            when(rfqHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> rfqService.checkRfqDependencies(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns canDelete true when no dependencies")
        void returnsCanDeleteTrue() {
            ApRequestForQtnHdr rfq = new ApRequestForQtnHdr();
            rfq.setTransactionPoid(TRANSACTION_POID);
            when(rfqHdrRepository.findByTransactionPoidAndGroupPoidAndCompanyPoid(TRANSACTION_POID, GROUP_POID, COMPANY_POID))
                    .thenReturn(Optional.of(rfq));

            RfqDependenciesDto result = rfqService.checkRfqDependencies(TRANSACTION_POID, GROUP_POID, COMPANY_POID);

            assertThat(result).isNotNull();
            assertThat(result.getTransactionPoid()).isEqualTo(TRANSACTION_POID);
            assertThat(result.getCanDelete()).isTrue();
            assertThat(result.getMessage()).contains("No dependencies");
        }
    }
}
