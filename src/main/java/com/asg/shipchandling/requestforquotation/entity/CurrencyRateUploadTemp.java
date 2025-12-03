package com.asg.shipchandling.requestforquotation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "CURRENCY_RATE_UPLOAD_TEMP")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CurrencyRateUploadTempId.class)
public class CurrencyRateUploadTemp {

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
    private Timestamp effectiveDate;

    @Column(name = "BASE_CURRENCY", length = 20)
    private String baseCurrency;

    @Column(name = "CONVERSION_RATE")
    private BigDecimal conversionRate;

    @Column(name = "REMARKS", length = 200)
    private String remarks;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastmodifiedBy;

    @UpdateTimestamp
    @Column(name = "LASTMODIFIED_DATE")
    private Timestamp lastmodifiedDate;
}

