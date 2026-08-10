package com.itop.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.itop")
@EnableJpaRepositories(basePackages = "com.itop.core.repository")
@EntityScan(basePackages = "com.itop.core.entity")
public class ITopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ITopApplication.class, args);
    }
}