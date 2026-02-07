package miniapp.com.vn.minichatbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Cấu hình Redis: Spring Data Redis (RedisTemplate) và Redisson (RedissonClient).
 * Cấu hình kết nối tại application.properties: spring.data.redis.*
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate dùng cho Spring Data Redis (opsForValue, opsForList, ...).
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * RedissonClient được cung cấp bởi redisson-spring-boot-starter (tự cấu hình từ spring.data.redis).
     * Có thể inject RedissonClient khi cần dùng distributed lock, queue, v.v.
     */
    // RedissonClient đã được starter tạo sẵn, không cần @Bean tại đây
}
