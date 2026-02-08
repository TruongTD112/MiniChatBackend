package miniapp.com.vn.minichatbackend.config;

import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.dto.queue.InboundMessageQueuePayload;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Redisson queue. Tạo messageMainQueue/messageDelayedQueue ngay khi có RedissonClient
 * (không gắn @ConditionalOnProperty) để tránh trên Railway điều kiện đánh giá sai thứ tự khiến bean không được tạo.
 * Bật/tắt queue vẫn do app.webhook.queue.enabled và do InboundMessageQueueService / InboundMessageQueueListener có @ConditionalOnProperty.
 */
@Slf4j
@Configuration
public class RedissonQueueConfig {

    @Bean
    public RBlockingQueue<InboundMessageQueuePayload> messageMainQueue(
            RedissonClient redisson,
            WebhookQueueProperties queueProperties) {
        String name = queueProperties.getMainQueueName();
        log.info("Redisson queue: creating main queue '{}'", name);
        return redisson.getBlockingQueue(name);
    }

    @Bean
    public RDelayedQueue<InboundMessageQueuePayload> messageDelayedQueue(
            RedissonClient redisson,
            RBlockingQueue<InboundMessageQueuePayload> mainQueue) {
        log.info("Redisson queue: creating delayed queue (feeds into main queue)");
        return redisson.getDelayedQueue(mainQueue);
    }
}
