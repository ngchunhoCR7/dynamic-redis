package com.ngchunho.utils.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DynamicRedisProperties
 *
 * @author ngchunho
 * @version 1.0.0
 * @description
 * @date 2022/2/16 20:01
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "redis-source")
public class DynamicRedisProperties {

    private String primary = "master";

    private Map<String, RedisSourceProperties> dynamic = new LinkedHashMap<>();
}
