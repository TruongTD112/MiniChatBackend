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
        // 1. Validation
        if (payload == null || payload.getConversationId() == null) {
            return;
        }

        String conversationId = payload.getConversationId().toString();
        String finalMessageText = payload.getText() != null ? payload.getText() : "";

        // 2. Check debounce & Aggregate
        if (payload.getDebounceTimestamp() != null) {
             String debounceKey = queueProperties.getDebounceKeyPrefix() + conversationId;
             try {
                 Object value = redisTemplate.opsForValue().get(debounceKey);
                 if (value instanceof Number) {
                     long latest = ((Number) value).longValue();
                     if (payload.getDebounceTimestamp() < latest) {
                         log.info("Debouncing message: conversationId={} msgTimestamp={} latestTimestamp={} -> SKIP processing (buffered)",
                                 conversationId, payload.getDebounceTimestamp(), latest);
                         return;
                     }
                 }
                 
                 // This is the latest message -> Aggregate from buffer
                 String bufferKey = queueProperties.getDebounceBufferPrefix() + conversationId;
                 java.util.List<Object> buffered = redisTemplate.opsForList().range(bufferKey, 0, -1);
                 redisTemplate.delete(bufferKey);
                 
                 if (buffered != null && !buffered.isEmpty()) {
                     StringBuilder sb = new StringBuilder();
                     for (Object obj : buffered) {
                         if (obj != null) {
                             if (sb.length() > 0) sb.append("\n");
                             sb.append(obj.toString());
                         }
                     }
                     finalMessageText = sb.toString();
                     payload.setText(finalMessageText);
                     log.debug("Aggregated messages for conversationId={}: {}", conversationId, finalMessageText);
                 }
                 
             } catch (Exception e) {
                 log.error("Failed to check debounce key / aggregate", e);
             }
        }

        // 3. Update conversation history (cache) with FINAL aggregated text
        String listKey = queueProperties.getConversationRecentPrefix() + conversationId;
        int maxN = Math.max(1, queueProperties.getConversationMaxMessages());

        try {
            ConversationTurn turn = new ConversationTurn("user", finalMessageText);
            redisTemplate.opsForList().rightPush(listKey, turn);
            redisTemplate.opsForList().trim(listKey, -maxN, -1);
        } catch (Exception e) {
            log.error("Failed to update conversation recent list: conversationId={}", conversationId, e);
        }

        // 4. Process
        try {
            inboundMessageProcessor.process(payload);
        } catch (Exception e) {
            log.error("Processor error for messageId={} conversationId={}", payload.getMessageId(), conversationId, e);
        }
    }
}
