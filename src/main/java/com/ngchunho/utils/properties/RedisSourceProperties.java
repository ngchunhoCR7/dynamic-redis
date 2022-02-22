package com.ngchunho.utils.properties;

import com.ngchunho.utils.common.RedisConnectionType;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

/**
 * RedisSourceProperties
 *
 * @author ngchunho
 * @version 1.0.0
 * @description
 * @date 2022/2/17 14:48
 */
@Slf4j
@Accessors(chain = true)
public class RedisSourceProperties extends RedisProperties {

    private RedisConnectionType type;

    public RedisConnectionType getType() {
        return type;
    }

    public void setType(RedisConnectionType type) {
        this.type = type;
    }
}
