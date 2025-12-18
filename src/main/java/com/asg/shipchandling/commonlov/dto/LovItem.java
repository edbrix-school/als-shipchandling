package com.asg.shipchandling.commonlov.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LovItem {
    private Long poid;
    private String code;
    private String description;
    private String label;
    private Long value;
    private Integer seqNo;
}
