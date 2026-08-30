package com.ticketing.cart;

import com.ticketing.api.config.DefaultFeignConfig;
import com.ticketing.cart.config.LoadBalancerConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@LoadBalancerClients(defaultConfiguration = LoadBalancerConfiguration.class)
@EnableFeignClients(basePackages = "com.ticketing.api.client", defaultConfiguration = DefaultFeignConfig.class)
@MapperScan("com.ticketing.cart.mapper")
@SpringBootApplication
public class CartApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}