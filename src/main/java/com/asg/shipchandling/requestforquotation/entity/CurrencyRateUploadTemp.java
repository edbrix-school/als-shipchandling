package com.asg.shipchandling.requestforquotation.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "CURRENCY_RATE_UPLOAD_TEMP")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CurrencyRateUploadTempId.class)
public class CurrencyRateUploadTemp extends BaseEntity {

    @Id
    @Column(name = "GROUP_POID", nullable = false)
    private BigDecimal groupPoid;

    @Id
    @Column(name = "COMPANY_POID", nullable = false)
    private BigDecimal companyPoid;

    @Id
    @Column(name = "CURRENCY_CODE", nullable = false, length = 20)
    private String currencyCode;

    @Id
    @Column(name = "EFFECTIVE_DATE", nullable = false)
    private LocalDateTime effectiveDate;

    @Column(name = "BASE_CURRENCY", length = 20)
    private String baseCurrency;

    @Column(name = "CONVERSION_RATE")
    private BigDecimal conversionRate;

    @Column(name = "REMARKS", length = 200)
    private String remarks;

}

