package com.distributedstorage.metadata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class MetadataServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetadataServiceApplication.class, args);
    }
}