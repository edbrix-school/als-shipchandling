package com.asg.shipchandling.commonlov.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LovResponse {
    private List<LovItem> items;
}
