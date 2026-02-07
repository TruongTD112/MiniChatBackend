package miniapp.com.vn.minichatbackend.config;

import miniapp.com.vn.minichatbackend.dto.queue.InboundMessageQueuePayload;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Redisson: Main queue (destination) và Delayed queue.
 * Tin đẩy vào delayed queue sau delaySeconds sẽ tự chuyển sang main queue.
 */
@Configuration
@ConditionalOnBean(RedissonClient.class)
public class RedissonQueueConfig {

    @Bean
    public RBlockingQueue<InboundMessageQueuePayload> messageMainQueue(
            RedissonClient redisson,
            WebhookQueueProperties queueProperties) {
        return redisson.getBlockingQueue(queueProperties.getMainQueueName());
    }

    @Bean
    public RDelayedQueue<InboundMessageQueuePayload> messageDelayedQueue(
            RedissonClient redisson,
            RBlockingQueue<InboundMessageQueuePayload> mainQueue) {
        return redisson.getDelayedQueue(mainQueue);
    }
}
