package com.example.movra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MovraApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovraApplication.class, args);
    }

}
