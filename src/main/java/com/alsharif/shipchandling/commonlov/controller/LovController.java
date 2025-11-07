package com.alsharif.shipchandling.commonlov.controller;

import com.alsharif.shipchandling.commonlov.dto.LovResponse;
import com.alsharif.shipchandling.commonlov.service.LovServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.alsharif.shipchandling.common.ApiResponse.success;

@Controller
@RequestMapping("lov")
public class LovController {

    @Autowired
    LovServiceImpl lovService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLovList(
                                  @RequestParam("lovName") String lovName,
                                  @RequestParam(value = "docKeyPoid", required = false) Long docKeyPoid,
                                  @RequestParam(value = "filterValue", required = false) String filterValue) {
        LovResponse lovResponse = lovService.getLovList( lovName, docKeyPoid, filterValue);
        return  success("Task fetched successfully", lovResponse);

    }

}
