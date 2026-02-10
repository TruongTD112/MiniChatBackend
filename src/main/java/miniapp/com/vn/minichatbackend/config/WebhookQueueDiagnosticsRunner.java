package miniapp.com.vn.minichatbackend.config;

import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.listener.InboundMessageQueueListener;
import miniapp.com.vn.minichatbackend.service.InboundMessageQueueService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
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
    private final Environment environment;

    public WebhookQueueDiagnosticsRunner(ApplicationContext applicationContext,
                                        WebhookQueueProperties queueProperties,
                                        Environment environment) {
        this.applicationContext = applicationContext;
        this.queueProperties = queueProperties;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean producerEnabled = queueProperties.isProducerEnabled();
        boolean listenerEnabled = queueProperties.isListenerEnabled();
        boolean redissonPresent = hasBeanOfType(REDISSON_CLIENT_CLASS);
        boolean queueServicePresent = applicationContext.getBeanProvider(InboundMessageQueueService.class).getIfAvailable() != null;
        boolean listenerPresent = applicationContext.getBeanProvider(InboundMessageQueueListener.class).getIfAvailable() != null;

        log.info("--- Webhook queue diagnostics ---");
        log.info("  app.webhook.queue.producer-enabled = {}", producerEnabled);
        log.info("  app.webhook.queue.listener-enabled = {}", listenerEnabled);
        log.info("  RedissonClient bean       = {}", redissonPresent ? "YES" : "NO");
        log.info("  InboundMessageQueueService = {}", queueServicePresent ? "YES" : "NO");
        log.info("  InboundMessageQueueListener = {}", listenerPresent ? "YES (started)" : "NO (will not start)");

        if (queueServicePresent && listenerPresent) {
            log.info("  => Webhook queue: ACTIVE (producer & listener both active)");
        } else {
            if (!producerEnabled && !listenerEnabled) {
                 log.info("  => Webhook queue: DISABLED (both producer and listener disabled).");
            } else {
                 if (producerEnabled && !queueServicePresent) {
                     log.warn("  => Producer ENABLED but InboundMessageQueueService missing (maybe Redisson missing?)");
                 } else if (!producerEnabled) {
                     log.info("  => Producer DISABLED.");
                 }

                 if (listenerEnabled && !listenerPresent) {
                     log.warn("  => Listener ENABLED but InboundMessageQueueListener missing (maybe Redisson missing?)");
                 } else if (!listenerEnabled) {
                     log.info("  => Listener DISABLED.");
                 }
            }

            if (!redissonPresent && (producerEnabled || listenerEnabled)) {
                String redisHost = environment.getProperty("spring.data.redis.host", "?");
                String redisUrl = environment.getProperty("REDIS_URL", environment.getProperty("spring.data.redis.url", ""));
                log.warn("  => RedissonClient not found. Queue features will not work.");
                log.warn("  => On Railway: redis.host={}, REDIS_URL set={}. Ensure Redis is linked.", redisHost, !redisUrl.isEmpty());
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
