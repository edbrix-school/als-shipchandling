package com.alsharif.shipchandling.welcome;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@Slf4j
public class WelcomeController {

    @GetMapping("/welcome")
    public String welcome(){
        String welcome = "Welcome to ship chandling module";
        System.out.println(welcome);
        return welcome;
    }

}
