package com.autoadda.apis.part;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, LoadBalancerAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients
public class PartServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run ( PartServiceApplication.class, args );
    }
}