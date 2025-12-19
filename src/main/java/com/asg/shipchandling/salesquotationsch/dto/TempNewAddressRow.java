package com.asg.shipchandling.salesquotationsch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TempNewAddressRow {
    private Long newAddressPoid;
    private String docFieldName;
    private String addressName;
    private String offTel1;
    private String offTel2;
    private String contactPerson;
    private String designation;
    private String mobile;
    private String fax;
    private String email1;
    private String email2;
    private String website;
    private String poBox;
    private String offNo;
    private String bldg;
    private String road;
    private String areaCity;
    private String state;
    private Long countryPoid;
    private String landMark;
}


