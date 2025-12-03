package com.asg.shipchandling.welcome;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class WelcomeController {

    @GetMapping("/welcome")
    public String welcome(){
        String welcome = "Welcome to ship chandling module";
        log.info("welcome endpoint invoked, returning greeting");
        return welcome;
    }

}
