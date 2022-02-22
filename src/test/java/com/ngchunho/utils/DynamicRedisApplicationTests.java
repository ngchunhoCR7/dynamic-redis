package com.ngchunho.utils;

import com.ngchunho.utils.config.DynamicRedisConfiguration;
import com.ngchunho.utils.util.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class DynamicRedisApplicationTests {

    /**
     * Redis连接1
     */
    private static final String MASTER = "master";

    /**
     * Redis连接2
     */
    private static final String SLAVE = "slave";

    @Test
    void contextLoads() {
        // 获取连接1的RedisTemplate
        RedisTemplate<String, Object> master = DynamicRedisConfiguration.redisTemplate(MASTER);
        master.opsForValue().set("aaa", "redisTemplate");
        System.out.println("master redisTemplate set success ...");

        // 获取连接1的RedisUtil
        RedisUtil masterUtil = RedisUtil.getInstance(MASTER);
        masterUtil.set("bbb", "redisUtil");
        System.out.println("master redisUtil set success ...");

        System.out.println("------------------------------------");

        // 获取连接2的RedisTemplate
        RedisTemplate<String, Object> slave = DynamicRedisConfiguration.redisTemplate(SLAVE);
        slave.opsForValue().set("aaa", "redisTemplate");
        System.out.println("slave redisTemplate set success ...");

        // 获取连接2的RedisUtil
        RedisUtil slaveUtil = RedisUtil.getInstance(SLAVE);
        slaveUtil.set("bbb", "redisUtil");
        System.out.println("slave redisUtil set success ...");
    }

}
