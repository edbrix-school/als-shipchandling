package com.asg.shipchandling.salesquotationsch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDetailsResponse {
    private BigDecimal addressPoid;
    private String addressName;
    private String contactPerson;
    private String email1;
    private String telephone;
    private String mobile;
}
