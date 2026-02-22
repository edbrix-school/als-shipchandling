package com.asg.shipchandling.commonlov.service;

import com.asg.shipchandling.commonlov.dto.LovItem;
import com.asg.shipchandling.commonlov.dto.LovResponse;
import com.asg.shipchandling.commonlov.repository.LovRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LovServiceImpl unit tests")
class LovServiceImplTest {

    @Mock
    private LovRepository lovRepository;

    @InjectMocks
    private LovServiceImpl lovService;

    private static final String LOV_NAME = "TEST_LOV";
    private static final Long DOC_KEY_POID = 100L;
    private static final String FILTER_VALUE = "filter";
    private static final Long GROUP_POID = 1L;
    private static final Long COMPANY_POID = 1L;
    private static final Long USER_POID = 1L;

    @Nested
    @DisplayName("getLovList")
    class GetLovList {

        @Test
        @DisplayName("returns response from repository")
        void returnsResponseFromRepository() {
            List<LovItem> items = Arrays.asList(
                    new LovItem(1L, "CODE1", "Desc1", "Label1", 1L, 1),
                    new LovItem(2L, "CODE2", "Desc2", "Label2", 2L, 2)
            );
            LovResponse expected = new LovResponse(items);
            when(lovRepository.getLovList(LOV_NAME, DOC_KEY_POID, FILTER_VALUE, GROUP_POID, COMPANY_POID, USER_POID))
                    .thenReturn(expected);

            LovResponse result = lovService.getLovList(LOV_NAME, DOC_KEY_POID, FILTER_VALUE, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getItems()).hasSize(2);
            assertThat(result.getItems()).containsExactlyElementsOf(items);
            verify(lovRepository).getLovList(LOV_NAME, DOC_KEY_POID, FILTER_VALUE, GROUP_POID, COMPANY_POID, USER_POID);
        }

        @Test
        @DisplayName("returns empty list when repository returns response with null items")
        void returnsResponseWithNullItems() {
            LovResponse response = new LovResponse(null);
            when(lovRepository.getLovList(anyString(), any(), anyString(), anyLong(), anyLong(), anyLong()))
                    .thenReturn(response);

            LovResponse result = lovService.getLovList(LOV_NAME, null, "", GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getItems()).isNull();
        }

        @Test
        @DisplayName("returns empty response when repository returns empty list")
        void returnsEmptyList() {
            LovResponse response = new LovResponse(Collections.emptyList());
            when(lovRepository.getLovList(anyString(), any(), anyString(), anyLong(), anyLong(), anyLong()))
                    .thenReturn(response);

            LovResponse result = lovService.getLovList(LOV_NAME, DOC_KEY_POID, "", GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getLovItemByPoid")
    class GetLovItemByPoid {

        @Test
        @DisplayName("returns empty LovItem when poid is null")
        void returnsEmptyWhenPoidNull() {
            LovItem result = lovService.getLovItemByPoid(null, LOV_NAME, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getPoid()).isNull();
            assertThat(result.getCode()).isNull();
        }

        @Test
        @DisplayName("returns empty LovItem when lovName is blank")
        void returnsEmptyWhenLovNameBlank() {
            LovItem result = lovService.getLovItemByPoid(1L, "  ", GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getPoid()).isNull();
        }

        @Test
        @DisplayName("returns empty LovItem when lovName is null")
        void returnsEmptyWhenLovNameNull() {
            LovItem result = lovService.getLovItemByPoid(1L, null, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getPoid()).isNull();
        }

        @Test
        @DisplayName("returns matching item when found in list")
        void returnsMatchingItemWhenFound() {
            LovItem expected = new LovItem(10L, "CODE10", "Desc10", "Label10", 10L, 10);
            LovResponse listResponse = new LovResponse(Arrays.asList(
                    new LovItem(1L, "C1", "D1", "L1", 1L, 1),
                    expected,
                    new LovItem(20L, "C20", "D20", "L20", 20L, 20)
            ));
            when(lovRepository.getLovList(LOV_NAME, 10L, "", GROUP_POID, COMPANY_POID, USER_POID))
                    .thenReturn(listResponse);

            LovItem result = lovService.getLovItemByPoid(10L, LOV_NAME, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getPoid()).isEqualTo(10L);
            assertThat(result.getCode()).isEqualTo("CODE10");
        }

        @Test
        @DisplayName("returns LovItem with poid only when not found in list")
        void returnsPoidOnlyWhenNotFound() {
            LovResponse listResponse = new LovResponse(Arrays.asList(
                    new LovItem(1L, "C1", "D1", "L1", 1L, 1)
            ));
            when(lovRepository.getLovList(LOV_NAME, 99L, "", GROUP_POID, COMPANY_POID, USER_POID))
                    .thenReturn(listResponse);

            LovItem result = lovService.getLovItemByPoid(99L, LOV_NAME, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getPoid()).isEqualTo(99L);
            assertThat(result.getCode()).isNull();
            assertThat(result.getDescription()).isNull();
        }

        @Test
        @DisplayName("returns empty LovItem when list response is null")
        void returnsEmptyWhenListResponseNull() {
            when(lovRepository.getLovList(LOV_NAME, 1L, "", GROUP_POID, COMPANY_POID, USER_POID))
                    .thenReturn(null);

            LovItem result = lovService.getLovItemByPoid(1L, LOV_NAME, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getPoid()).isNull();
        }

        @Test
        @DisplayName("returns empty LovItem when list items are null")
        void returnsEmptyWhenItemsNull() {
            LovResponse listResponse = new LovResponse(null);
            when(lovRepository.getLovList(LOV_NAME, 1L, "", GROUP_POID, COMPANY_POID, USER_POID))
                    .thenReturn(listResponse);

            LovItem result = lovService.getLovItemByPoid(1L, LOV_NAME, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getPoid()).isNull();
        }
    }

    @Nested
    @DisplayName("getLovItemByCode")
    class GetLovItemByCode {

        @Test
        @DisplayName("returns empty LovItem when code is blank")
        void returnsEmptyWhenCodeBlank() {
            LovItem result = lovService.getLovItemByCode("  ", LOV_NAME, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isNull();
        }

        @Test
        @DisplayName("returns empty LovItem when code is null")
        void returnsEmptyWhenCodeNull() {
            LovItem result = lovService.getLovItemByCode(null, LOV_NAME, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isNull();
        }

        @Test
        @DisplayName("returns empty LovItem when lovName is blank")
        void returnsEmptyWhenLovNameBlank() {
            LovItem result = lovService.getLovItemByCode("CODE1", "  ", GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isNull();
        }

        @Test
        @DisplayName("returns matching item when found by code")
        void returnsMatchingItemWhenFound() {
            String code = "TARGET_CODE";
            LovItem expected = new LovItem(5L, code, "Desc", "Label", 5L, 5);
            LovResponse listResponse = new LovResponse(Arrays.asList(
                    new LovItem(1L, "C1", "D1", "L1", 1L, 1),
                    expected
            ));
            when(lovRepository.getLovList(LOV_NAME, null, "", GROUP_POID, COMPANY_POID, USER_POID))
                    .thenReturn(listResponse);

            LovItem result = lovService.getLovItemByCode(code, LOV_NAME, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(code);
            assertThat(result.getPoid()).isEqualTo(5L);
        }

        @Test
        @DisplayName("returns LovItem with code only when not found")
        void returnsCodeOnlyWhenNotFound() {
            when(lovRepository.getLovList(LOV_NAME, null, "", GROUP_POID, COMPANY_POID, USER_POID))
                    .thenReturn(new LovResponse(Collections.singletonList(
                            new LovItem(1L, "OTHER", "D", "L", 1L, 1)
                    )));

            LovItem result = lovService.getLovItemByCode("MISSING", LOV_NAME, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("MISSING");
            assertThat(result.getPoid()).isNull();
        }

        @Test
        @DisplayName("returns code-only LovItem when list response is null")
        void returnsCodeOnlyWhenListResponseNull() {
            when(lovRepository.getLovList(LOV_NAME, null, "", GROUP_POID, COMPANY_POID, USER_POID))
                    .thenReturn(null);

            LovItem result = lovService.getLovItemByCode("CODE", LOV_NAME, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("CODE");
            assertThat(result.getPoid()).isNull();
        }

        @Test
        @DisplayName("returns code-only LovItem when items list is null")
        void returnsCodeOnlyWhenItemsNull() {
            when(lovRepository.getLovList(LOV_NAME, null, "", GROUP_POID, COMPANY_POID, USER_POID))
                    .thenReturn(new LovResponse(null));

            LovItem result = lovService.getLovItemByCode("CODE", LOV_NAME, GROUP_POID, COMPANY_POID, USER_POID);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("CODE");
            assertThat(result.getPoid()).isNull();
        }
    }
}
