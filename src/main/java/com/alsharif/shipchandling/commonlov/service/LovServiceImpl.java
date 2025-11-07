package com.alsharif.shipchandling.commonlov.service;
import com.alsharif.shipchandling.commonlov.dto.LovResponse;
import com.alsharif.shipchandling.commonlov.repository.LovRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LovServiceImpl implements LovService{

    @Autowired
    LovRepository lovRepository;

    @Override
    public LovResponse getLovList(String lovName, Long docKeyPoid, String filterValue) {
        return lovRepository.getLovList(lovName,docKeyPoid,filterValue);
    }
}
