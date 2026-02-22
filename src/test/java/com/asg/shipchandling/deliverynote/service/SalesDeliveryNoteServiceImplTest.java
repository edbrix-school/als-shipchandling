package com.asg.shipchandling.deliverynote.service;

import com.asg.shipchandling.deliverynote.dto.SalesDeliveryNoteDependenciesDto;
import com.asg.shipchandling.deliverynote.dto.SalesDeliveryNoteHdrDto;
import com.asg.shipchandling.deliverynote.entity.SalesDeliveryNoteHdr;
import com.asg.shipchandling.deliverynote.repository.SalesDeliveryNoteHdrRepository;
import com.asg.shipchandling.deliverynote.repository.SalesDeliveryNoteHdrRepositoryImpl;
import com.asg.shipchandling.deliverynote.repository.SalesDeliveryNoteItemDtlRepository;
import com.asg.shipchandling.deliverynote.repository.SalesDeliveryNoteRepository;
import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.exceptions.ResourceNotFoundException;
import com.asg.shipchandling.salesinvoice.repository.SalesDnDtlRepository;
import com.asg.shipchandling.StockMaster.repository.StockMasterRepository;
import com.asg.shipchandling.stockunitmaster.repository.StockUnitRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesDeliveryNoteServiceImpl unit tests")
class SalesDeliveryNoteServiceImplTest {

    @Mock
    private SalesDeliveryNoteHdrRepository deliveryNoteHdrRepository;
    @Mock
    private SalesDeliveryNoteItemDtlRepository itemDtlRepository;
    @Mock
    private SalesDeliveryNoteRepository salesDeliveryNoteRepository;
    @Mock
    private SalesDeliveryNoteHdrRepositoryImpl deliveryNoteHdrRepositoryImpl;
    @Mock
    private SalesDnDtlRepository salesDnDtlRepository;
    @Mock
    private StockMasterRepository stockMasterRepository;
    @Mock
    private StockUnitRepository stockUnitRepository;
    @Mock
    private com.asg.common.lib.service.DocumentSearchService documentService;
    @Mock
    private com.asg.shipchandling.commonlov.service.LovService lovService;
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
    private SalesDeliveryNoteServiceImpl deliveryNoteService;

    private static final Long TRANSACTION_POID = 1L;
    private static final Long COMPANY_POID = 10L;
    private static final Long GROUP_POID = 100L;

    @Nested
    @DisplayName("getDeliveryNoteByPoid")
    class GetDeliveryNoteByPoid {

        @Test
        @DisplayName("throws ResourceNotFoundException when delivery note not found")
        void throwsWhenNotFound() {
            when(deliveryNoteHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> deliveryNoteService.getDeliveryNoteByPoid(GROUP_POID, TRANSACTION_POID, COMPANY_POID, false))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Delivery Note");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when delivery note is deleted")
        void throwsWhenDeleted() {
            SalesDeliveryNoteHdr hdr = new SalesDeliveryNoteHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setCompanyPoid(COMPANY_POID);
            hdr.setDeleted("Y");
            when(deliveryNoteHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(hdr));

            assertThatThrownBy(() -> deliveryNoteService.getDeliveryNoteByPoid(GROUP_POID, TRANSACTION_POID, COMPANY_POID, false))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns DTO when found and not deleted")
        void returnsDtoWhenFound() {
            SalesDeliveryNoteHdr hdr = new SalesDeliveryNoteHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setCompanyPoid(COMPANY_POID);
            hdr.setDeleted("N");
            hdr.setDocRef("DN-001");
            when(deliveryNoteHdrRepository.findByTransactionPoidAndCompanyPoid(TRANSACTION_POID, COMPANY_POID))
                    .thenReturn(Optional.of(hdr));

            SalesDeliveryNoteHdrDto result = deliveryNoteService.getDeliveryNoteByPoid(GROUP_POID, TRANSACTION_POID, COMPANY_POID, false);

            assertThat(result).isNotNull();
            assertThat(result.getTransactionPoid()).isEqualTo(TRANSACTION_POID);
            assertThat(result.getDocRef()).isEqualTo("DN-001");
        }
    }

    @Nested
    @DisplayName("checkDeliveryNoteDependencies")
    class CheckDeliveryNoteDependencies {

        @Test
        @DisplayName("throws CustomException when delivery note not found")
        void throwsWhenNotFound() {
            when(deliveryNoteHdrRepository.findById(TRANSACTION_POID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deliveryNoteService.checkDeliveryNoteDependencies(TRANSACTION_POID, COMPANY_POID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("Delivery Note not found");
        }

        @Test
        @DisplayName("throws CustomException when company does not match")
        void throwsWhenCompanyMismatch() {
            SalesDeliveryNoteHdr hdr = new SalesDeliveryNoteHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setCompanyPoid(999L);
            when(deliveryNoteHdrRepository.findById(TRANSACTION_POID)).thenReturn(Optional.of(hdr));

            assertThatThrownBy(() -> deliveryNoteService.checkDeliveryNoteDependencies(TRANSACTION_POID, COMPANY_POID))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining("does not belong to company");
        }

        @Test
        @DisplayName("returns DTO with linkedToQuotation and canDelete when found")
        void returnsDtoWhenFound() {
            SalesDeliveryNoteHdr hdr = new SalesDeliveryNoteHdr();
            hdr.setTransactionPoid(TRANSACTION_POID);
            hdr.setCompanyPoid(COMPANY_POID);
            hdr.setQtnRefNo(null);
            when(deliveryNoteHdrRepository.findById(TRANSACTION_POID)).thenReturn(Optional.of(hdr));
            when(salesDnDtlRepository.countByDnPoidFkAndCompanyPoidAndInvoiceNotDeleted(TRANSACTION_POID, COMPANY_POID)).thenReturn(0L);

            SalesDeliveryNoteDependenciesDto result = deliveryNoteService.checkDeliveryNoteDependencies(TRANSACTION_POID, COMPANY_POID);

            assertThat(result).isNotNull();
            assertThat(result.isLinkedToQuotation()).isFalse();
        }
    }
}
