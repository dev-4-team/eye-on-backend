package com.on.eye.api.global.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * JSON 직렬화를 지원하는 Redis 템플릿 빈 설정 복잡한 객체를 Redis에 저장할 때 사용. template.setHashKeySerializer(new
     * StringRedisSerializer()); template.setHashValueSerializer(new
     * GenericJackson2JsonRedisSerializer()); template.afterPropertiesSet();
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // key는 문자열로 직렬화
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // value는 JSON으로 직렬화
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));

        return redisTemplate;
    }

    /** 문자열 기반 Redis 템플릿 빈 설정 응원 수 카운터 관리에 사용. */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
}
