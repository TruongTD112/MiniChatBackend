package miniapp.com.vn.minichatbackend.config;

import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.dto.queue.InboundMessageQueuePayload;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Redisson queue và InboundMessageQueueService.
 * Không dùng điều kiện ở class vì khi bật @ConditionalOnProperty/@ConditionalOnBean ở đây config không load (property/bean đánh giá sớm).
 * Đặt điều kiện ở từng @Bean: chỉ tạo queue khi app.webhook.queue.enabled=true.
 */
@Slf4j
@Configuration
public class RedissonQueueConfig {

    @Bean
    @ConditionalOnProperty(name = "app.webhook.queue.enabled", havingValue = "true")
    public RBlockingQueue<InboundMessageQueuePayload> messageMainQueue(
            RedissonClient redisson,
            WebhookQueueProperties queueProperties) {
        String name = queueProperties.getMainQueueName();
        log.info("Redisson queue: creating main queue '{}'", name);
        return redisson.getBlockingQueue(name);
    }

    @Bean
    @ConditionalOnProperty(name = "app.webhook.queue.enabled", havingValue = "true")
    public RDelayedQueue<InboundMessageQueuePayload> messageDelayedQueue(
            RedissonClient redisson,
            RBlockingQueue<InboundMessageQueuePayload> mainQueue) {
        log.info("Redisson queue: creating delayed queue (feeds into main queue)");
        return redisson.getDelayedQueue(mainQueue);
    }
}
