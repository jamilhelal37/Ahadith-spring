package com.jamil.ahadith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AhadithApplication {
    private static final Logger log = LoggerFactory.getLogger(AhadithApplication.class);

    public static void main(String[] args) {

        SpringApplication.run(AhadithApplication.class, args);
        log.info("Ahadith Application Started");
    }

}
