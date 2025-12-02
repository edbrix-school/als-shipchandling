package com.asg.shipchandling.commonlov.service;

import com.asg.shipchandling.commonlov.dto.LovResponse;
import org.springframework.stereotype.Service;

@Service
public interface LovService {
    LovResponse getLovList(String lovName, Long docKeyPoid, String filterValue, String filterField);
}
