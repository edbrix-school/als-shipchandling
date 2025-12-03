package com.asg.shipchandling.commonlov.service;
import com.asg.shipchandling.commonlov.dto.LovResponse;
import com.asg.shipchandling.commonlov.repository.LovRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class LovServiceImpl implements LovService{

    @Autowired
    LovRepository lovRepository;

    @Override
    public LovResponse getLovList(String lovName, Long docKeyPoid, String filterValue, String filterField) {
        log.info("Fetching LOV list for lovName={} docKeyPoid={} filterValue={} filterField={}", lovName, docKeyPoid, filterValue, filterField);
        LovResponse response = lovRepository.getLovList(lovName,docKeyPoid,filterValue, filterField);
        log.info("Fetched LOV list for lovName={} itemCount={}", lovName,
                response != null && response.getItems() != null ? response.getItems().size() : 0);
        return response;
    }
}
