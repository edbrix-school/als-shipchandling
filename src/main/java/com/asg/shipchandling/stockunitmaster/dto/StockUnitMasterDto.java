package com.asg.shipchandling.stockunitmaster.dto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
@Getter
@Setter
@RequiredArgsConstructor
public class StockUnitMasterDto {


    private Long stockUnitPoid;

    private String stockUnitCode;

    private String stockUnitName;

    private String stockUnitName2;

    private Long groupPoid;

    private String createdBy;

    private OffsetDateTime createdDate;

    private String lastModifiedBy;

    private OffsetDateTime lastModifiedDate;

    private String active;

    private Integer seqNo;

    private String deleted;

    private String classified;

}
