package com.jamil.ahadith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class AhadithApplication {

    public static void main(String[] args) {

        SpringApplication.run(AhadithApplication.class, args);
        System.out.println("Ahadith Application Started");
    }

}
