package com.autoadda.apis.parts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, LoadBalancerAutoConfiguration.class })
@EnableDiscoveryClient
public class PartsManagementServcieApplication {
	public static void main(String[] args) {
		SpringApplication.run(PartsManagementServcieApplication.class, args);
	}
}