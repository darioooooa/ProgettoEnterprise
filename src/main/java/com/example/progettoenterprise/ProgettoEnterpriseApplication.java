package com.example.progettoenterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class ProgettoEnterpriseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProgettoEnterpriseApplication.class, args);
    }

}
