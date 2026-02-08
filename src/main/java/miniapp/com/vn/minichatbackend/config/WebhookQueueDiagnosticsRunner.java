package miniapp.com.vn.minichatbackend.config;

import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.service.InboundMessageQueueService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Sau khi context khởi động xong, log rõ trạng thái Redis/Redisson và webhook queue
 * để dễ debug khi InboundMessageQueueService null.
 */
@Slf4j
@Component
@Order(100)
public class WebhookQueueDiagnosticsRunner implements ApplicationRunner {

    private static final String REDISSON_CLIENT_CLASS = "org.redisson.api.RedissonClient";

    private final ApplicationContext applicationContext;
    private final WebhookQueueProperties queueProperties;

    public WebhookQueueDiagnosticsRunner(ApplicationContext applicationContext,
                                        WebhookQueueProperties queueProperties) {
        this.applicationContext = applicationContext;
        this.queueProperties = queueProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean configEnabled = queueProperties.isEnabled();
        boolean redissonPresent = hasBeanOfType(REDISSON_CLIENT_CLASS);
        boolean queueServicePresent = applicationContext.getBeanProvider(InboundMessageQueueService.class).getIfAvailable() != null;

        log.info("--- Webhook queue diagnostics ---");
        log.info("  app.webhook.queue.enabled = {}", configEnabled);
        log.info("  RedissonClient bean       = {}", redissonPresent ? "YES" : "NO");
        log.info("  InboundMessageQueueService = {}", queueServicePresent ? "YES" : "NO");

        if (queueServicePresent) {
            log.info("  => Webhook queue: ACTIVE (messages will be pushed to delayed queue)");
        } else {
            if (!configEnabled) {
                log.info("  => Webhook queue: DISABLED (app.webhook.queue.enabled=false)");
            } else if (!redissonPresent) {
                log.warn("  => Webhook queue: UNAVAILABLE - RedissonClient not found. Check Redis connection (spring.data.redis.*). Redis may be down or connection failed at startup.");
            } else {
                log.warn("  => Webhook queue: UNAVAILABLE - InboundMessageQueueService bean missing (Redisson present but queue bean not created). Check RedissonQueueConfig / RDelayedQueue.");
            }
        }
        log.info("--------------------------------");
    }

    private boolean hasBeanOfType(String className) {
        try {
            Class<?> type = Class.forName(className);
            return applicationContext.getBeanProvider(type).getIfAvailable() != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
