package com.ngchunho.utils;

import com.ngchunho.utils.properties.DynamicRedisProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({DynamicRedisProperties.class})
@SpringBootApplication
public class DynamicRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicRedisApplication.class, args);
    }

}
