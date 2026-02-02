package com.asg.shipchandling.salesquotationsch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "GLOBAL_NEW_ADDRESS_DETAILS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalNewAddressDetails {

    @Id
    @Column(name = "NEW_ADDRESS_POID", nullable = false)
    private Long newAddressPoid;

    @Column(name = "DOC_ID", length = 50)
    private String docId;

    @Column(name = "DOC_KEY_POID")
    private Long docKeyPoid;

    @Column(name = "ADDRESS_NAME", length = 100)
    private String addressName;

    @Column(name = "OFF_TEL1", length = 30)
    private String offTel1;

    @Column(name = "OFF_TEL2", length = 30)
    private String offTel2;

    @Column(name = "CONTACT_PERSON", length = 50)
    private String contactPerson;

    @Column(name = "DESIGNATION", length = 50)
    private String designation;

    @Column(name = "MOBILE", length = 30)
    private String mobile;

    @Column(name = "FAX", length = 30)
    private String fax;

    @Column(name = "EMAIL1", length = 110)
    private String email1;

    @Column(name = "EMAIL2", length = 110)
    private String email2;

    @Column(name = "WEBSITE", length = 100)
    private String website;

    @Column(name = "PO_BOX", length = 30)
    private String poBox;

    @Column(name = "OFF_NO", length = 30)
    private String offNo;

    @Column(name = "BLDG", length = 30)
    private String bldg;

    @Column(name = "ROAD", length = 30)
    private String road;

    @Column(name = "AREA_CITY", length = 30)
    private String areaCity;

    @Column(name = "STATE", length = 30)
    private String state;

    @Column(name = "COUNTRY_POID")
    private Long countryPoid;

    @Column(name = "LAND_MARK", length = 50)
    private String landMark;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "OLD_ACCNO_REF", length = 20)
    private String oldAccnoRef;

    @Column(name = "STATUS", length = 50)
    private String status;

    @Column(name = "REMARKS", length = 50)
    private String remarks;

    @Column(name = "DOC_FIELD_NAME", length = 50)
    private String docFieldName;
}
