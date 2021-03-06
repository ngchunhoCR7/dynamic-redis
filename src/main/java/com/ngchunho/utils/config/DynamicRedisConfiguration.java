package com.ngchunho.utils.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.ngchunho.utils.properties.DynamicRedisProperties;
import com.ngchunho.utils.properties.RedisSourceProperties;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DynamicRedisConfiguration
 *
 * @author ngchunho
 * @version 1.0.0
 * @description
 * @date 2022/2/17 15:20
 */
@Configuration
public class DynamicRedisConfiguration {

    public static Map<String, RedisTemplate<String, Object>> redisTemplateMap = new LinkedHashMap<>(8);

    public static RedisTemplate<String, Object> redisTemplate(String name) {
        return redisTemplateMap.get(name);
    }

    private static Map<String, RedisSourceProperties> redisSourcePropertiesMap;

    public DynamicRedisConfiguration(DynamicRedisProperties dynamicRedisProperties) {
        redisSourcePropertiesMap = dynamicRedisProperties.getDynamic();
        initRedisTemplate();
    }

    private void initRedisTemplate() {
        for (Map.Entry<String, RedisSourceProperties> redisSource : redisSourcePropertiesMap.entrySet()) {
            String name = redisSource.getKey();
            RedisSourceProperties properties = redisSource.getValue();
            switch (properties.getType()) {
                case NODE:
                    redisTemplateMap.put(name, createRedisConfiguration(properties));
                    break;
                case CLUSTER:
                    redisTemplateMap.put(name, createRedisClusterConfiguration(properties));
                    break;
                case SENTINEL:
                    redisTemplateMap.put(name, createRedisSentinelConfiguration(properties));
                    break;
                default:
                    // TODO ????????????
                    throw new RuntimeException("");
            }
        }
    }

    private RedisTemplate<String, Object> createRedisConfiguration(RedisSourceProperties properties) {
        // ??????RedisStandaloneConfiguration
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(properties.getHost());
        configuration.setPort(properties.getPort());
        configuration.setDatabase(properties.getDatabase());
        configuration.setUsername(properties.getUsername());
        configuration.setPassword(properties.getPassword());

        // ??????LettuceConnectionFactory
        LettuceConnectionFactory lettuceConnectionFactory = createLettuceConnectionFactory(properties, configuration);
        // ??????RedisTemplate
        return redisTemplate(lettuceConnectionFactory);
    }

    private RedisTemplate<String, Object> createRedisClusterConfiguration(RedisSourceProperties properties) {
        // ??????RedisClusterConfiguration
        RedisClusterConfiguration configuration = new RedisClusterConfiguration();
        configuration.setClusterNodes(getRedisNodeList(properties.getCluster().getNodes()));
        configuration.setMaxRedirects(properties.getCluster().getMaxRedirects());
        configuration.setUsername(properties.getUsername());
        configuration.setPassword(properties.getPassword());

        // ??????LettuceConnectionFactory
        LettuceConnectionFactory lettuceConnectionFactory = createLettuceConnectionFactory(properties, configuration);
        // ??????RedisTemplate
        return redisTemplate(lettuceConnectionFactory);
    }

    private RedisTemplate<String, Object> createRedisSentinelConfiguration(RedisSourceProperties properties) {
        // ??????RedisSentinelConfiguration
        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
        configuration.setSentinels(getRedisNodeList(properties.getSentinel().getNodes()));
        configuration.setSentinelPassword(properties.getSentinel().getPassword());
        configuration.setMaster(properties.getSentinel().getMaster());
        configuration.setDatabase(properties.getDatabase());
        configuration.setUsername(properties.getUsername());
        configuration.setPassword(properties.getPassword());

        // ??????LettuceConnectionFactory
        LettuceConnectionFactory lettuceConnectionFactory = createLettuceConnectionFactory(properties, configuration);
        // ??????RedisTemplate
        return redisTemplate(lettuceConnectionFactory);
    }

    /**
     * ??????RedisNodeList
     *
     * @param nodes
     * @return
     */
    private List<RedisNode> getRedisNodeList(List<String> nodes) {
        return nodes.stream().map(this::readHostAndPortFromString).collect(Collectors.toList());
    }

    /**
     * ?????????RedisNode
     *
     * @param hostAndPort
     * @return
     */
    private RedisNode readHostAndPortFromString(String hostAndPort) {
        String[] args = StringUtils.split(hostAndPort, ":");
        Assert.notNull(args, "HostAndPort need to be seperated by  ':'.");
        Assert.isTrue(args.length == 2, "Host and Port String needs to specified as host:port");
        return new RedisNode(args[0], Integer.valueOf(args[1]));
    }

    /**
     * ??????LettuceConnectionFactory
     *
     * @param properties
     * @param redisConfiguration
     * @return
     */
    public LettuceConnectionFactory createLettuceConnectionFactory(RedisSourceProperties properties, RedisConfiguration redisConfiguration) {
        // ??????redis lettuce pool?????????????????????
        RedisProperties.Pool pool = properties.getLettuce().getPool();
        GenericObjectPoolConfig<Object> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(pool.getMaxActive());
        poolConfig.setMaxIdle(pool.getMaxIdle());
        poolConfig.setMinIdle(pool.getMinIdle());
        poolConfig.setMaxWait(pool.getMaxWait());

        // ??????lettuce pool????????????
        LettucePoolingClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder()
                .shutdownTimeout(properties.getLettuce().getShutdownTimeout())
//                .commandTimeout(properties.getTimeout())
                .poolConfig(poolConfig)
                .build();

        // ??????lettuce????????????
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisConfiguration, clientConfiguration);
        lettuceConnectionFactory.afterPropertiesSet();
        return lettuceConnectionFactory;
    }

    /**
     * ??????RedisTemplate
     *
     * @param lettuceConnectionFactory
     * @return
     */
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        // ???????????????
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jackson2JsonRedisSerializer.setObjectMapper(mapper);
        // ??????redisTemplate
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        RedisSerializer<?> stringSerializer = new StringRedisSerializer();
        // key?????????
        redisTemplate.setKeySerializer(stringSerializer);
        // value?????????
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        // Hash key?????????
        redisTemplate.setHashKeySerializer(stringSerializer);
        // Hash value?????????
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
