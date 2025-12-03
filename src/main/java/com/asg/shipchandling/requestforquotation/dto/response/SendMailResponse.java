package com.asg.shipchandling.requestforquotation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMailResponse {
    private Boolean success;
    private String message;
    private Integer emailsSent;
}
