package com.distributedstorage.replication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ReplicationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReplicationServiceApplication.class, args);
    }
}