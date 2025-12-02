package com.asg.shipchandling.group.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "GLOBAL_GROUP_MASTER")
public class GroupEntity {
    @Id
    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @Column(name = "GROUP_CODE", nullable = false, length = 20)
    private String groupCode;

    @Column(name = "GROUP_NAME", nullable = false, length = 100)
    private String groupName;

    @Column(name = "GROUP_NAME2", length = 20)
    private String groupName2;

    @Column(name = "ADDRESS", length = 100)
    private String address;

    @Column(name = "COUNTRY", length = 20)
    private String country;

    @Column(name = "CONTACT_PERSON", length = 20)
    private String contactPerson;

    @Column(name = "TELEPHONE", length = 20)
    private String telephone;

    @Column(name = "EMAIL", length = 20)
    private String email;

    @Column(name = "FAX", length = 20)
    private String fax;

    @Column(name = "SEQNO")
    private Integer seqNo;

    @Column(name = "ACTIVE", length = 1)
    private String active;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "FINANCIAL_PERIOD_START")
    private LocalDate financialPeriodStart;

    @Column(name = "FINANCIAL_PERION_END")
    private LocalDate financialPeriodEnd;

    @Column(name = "TRANS_PERIOD_START")
    private LocalDate transPeriodStart;

    @Column(name = "TRANS_PERIOD_END")
    private LocalDate transPeriodEnd;

    @Column(name = "REPORT_PERIOD_START")
    private LocalDate reportPeriodStart;

    @Column(name = "REPORT_PERIOD_END")
    private LocalDate reportPeriodEnd;

    @Lob
    @Column(name = "ISO_LOGO")
    private byte[] isoLogo;
}
