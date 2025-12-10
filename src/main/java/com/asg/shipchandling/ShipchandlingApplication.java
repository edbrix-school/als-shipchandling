package com.asg.shipchandling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.asg"})
@EnableJpaRepositories(basePackages = {"com.asg.common.lib.repository", "com.asg.shipchandling"})
@EntityScan(basePackages = {"com.asg.common.lib.entity", "com.asg.shipchandling"})
public class ShipchandlingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShipchandlingApplication.class, args);
	}

}
