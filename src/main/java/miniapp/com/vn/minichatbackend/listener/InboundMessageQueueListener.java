package miniapp.com.vn.minichatbackend.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.config.WebhookQueueProperties;
import miniapp.com.vn.minichatbackend.dto.queue.ConversationTurn;
import miniapp.com.vn.minichatbackend.dto.queue.InboundMessageQueuePayload;
import org.redisson.api.RBlockingQueue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lắng nghe main queue (Redis): lấy tin từ queue, cập nhật list N bản tin gần nhất theo conversation,
 * rồi xử lý (gọi handler). Chạy trong thread riêng, blocking take từ main queue.
 */
@Slf4j
@Component
@RequiredArgsConstructor
//@ConditionalOnBean(name = "messageMainQueue")
@ConditionalOnProperty(name = "app.webhook.queue.listener-enabled", havingValue = "true")
public class InboundMessageQueueListener {

    private final RBlockingQueue<InboundMessageQueuePayload> messageMainQueue;
    private final WebhookQueueProperties queueProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final InboundMessageProcessor inboundMessageProcessor;

    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @PostConstruct
    public void start() {
        log.info("InboundMessageQueueListener starting...");
        running.set(true);
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "inbound-message-queue-listener");
            t.setDaemon(false);
            return t;
        });
        executor.submit(this::runLoop);
        log.info("InboundMessageQueueListener started, mainQueue={}, delay={}s, maxMessagesPerConv={}",
                queueProperties.getMainQueueName(), queueProperties.getDelaySeconds(),
                queueProperties.getConversationMaxMessages());
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        if (executor != null) {
            executor.shutdownNow();
        }
        log.info("InboundMessageQueueListener stopped");
    }

    private void runLoop() {
        while (running.get()) {
            try {
                InboundMessageQueuePayload payload = messageMainQueue.take();
                processPayload(payload);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (!running.get()) break;
                log.warn("Listener interrupted", e);
            } catch (Exception e) {
                log.error("Error processing message from queue", e);
            }
        }
    }

    private void processPayload(InboundMessageQueuePayload payload) {
        if (payload == null || payload.getConversationId() == null) {
            return;
        }

        // 1. Always update conversation history (cache) first
        String listKey = queueProperties.getConversationRecentPrefix() + payload.getConversationId();
        int maxN = Math.max(1, queueProperties.getConversationMaxMessages());

        try {
            // Push right (RPUSH) lượt hội thoại (user) vào list, sau đó giữ chỉ N bản gần nhất (LTRIM -N -1)
            ConversationTurn turn = new ConversationTurn("user", payload.getText() != null ? payload.getText() : "");
            redisTemplate.opsForList().rightPush(listKey, turn);
            redisTemplate.opsForList().trim(listKey, -maxN, -1);
        } catch (Exception e) {
            log.error("Failed to update conversation recent list: conversationId={}", payload.getConversationId(), e);
        }
        
        // 2. Check debounce
        if (payload.getDebounceTimestamp() != null) {
             String debounceKey = queueProperties.getDebounceKeyPrefix() + payload.getConversationId();
             try {
                 Object value = redisTemplate.opsForValue().get(debounceKey);
                 if (value instanceof Number) {
                     long latest = ((Number) value).longValue();
                     if (payload.getDebounceTimestamp() < latest) {
                         log.info("Debouncing message: conversationId={} msgTimestamp={} latestTimestamp={} -> SKIP processing (history updated)",
                                 payload.getConversationId(), payload.getDebounceTimestamp(), latest);
                         return;
                     }
                 }
             } catch (Exception e) {
                 log.error("Failed to check debounce key", e);
             }
        }

        try {
            inboundMessageProcessor.process(payload);
        } catch (Exception e) {
            log.error("Processor error for messageId={} conversationId={}", payload.getMessageId(), payload.getConversationId(), e);
        }
    }
}
