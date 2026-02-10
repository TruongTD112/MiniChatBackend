package miniapp.com.vn.minichatbackend.service;

import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.config.WebhookQueueProperties;
import miniapp.com.vn.minichatbackend.document.MessageDocument;
import miniapp.com.vn.minichatbackend.dto.queue.InboundMessageQueuePayload;
import org.redisson.api.RDelayedQueue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Đẩy tin nhắn nhận từ webhook vào Redis Delayed Queue.
 * Chỉ tạo bean khi app.webhook.queue.enabled=true (cùng với messageDelayedQueue trong RedissonQueueConfig).
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.webhook.queue.producer-enabled", havingValue = "true")
public class InboundMessageQueueService {

    private final RDelayedQueue<InboundMessageQueuePayload> messageDelayedQueue;
    private final WebhookQueueProperties queueProperties;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    public InboundMessageQueueService(RDelayedQueue<InboundMessageQueuePayload> messageDelayedQueue,
                                      WebhookQueueProperties queueProperties,
                                      org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
        this.messageDelayedQueue = messageDelayedQueue;
        this.queueProperties = queueProperties;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Đẩy tin vừa lưu Mongo vào delayed queue (sau delay sẽ sang main queue).
     */
    public void offerToDelayedQueue(MessageDocument doc) {
        if (doc == null || doc.getId() == null) {
            return;
        }
        
        long timestamp = System.currentTimeMillis();
        long delay = Math.max(1, queueProperties.getDelaySeconds());
        
        // Save debounce timestamp and buffer message to Redis
        if (doc.getConversationId() != null) {
            String conversationId = doc.getConversationId().toString();
            String debounceKey = queueProperties.getDebounceKeyPrefix() + conversationId;
            String bufferKey = queueProperties.getDebounceBufferPrefix() + conversationId;
            
            try {
                // 1. Update latest timestamp
                redisTemplate.opsForValue().set(debounceKey, timestamp, delay * 2, TimeUnit.SECONDS);
                
                // 2. Buffer message text
                if (doc.getText() != null && !doc.getText().isEmpty()) {
                    redisTemplate.opsForList().rightPush(bufferKey, doc.getText());
                    redisTemplate.expire(bufferKey, delay * 5, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                log.warn("Failed to update debounce key/buffer for conversationId={}", conversationId, e);
            }
        }

        InboundMessageQueuePayload payload = InboundMessageQueuePayload.builder()
                .conversationId(doc.getConversationId())
                .channelId(doc.getChannelId())
                .messageId(doc.getId())
                .externalMessageId(doc.getExternalMessageId())
                .senderId(doc.getSenderId())
                .recipientId(doc.getRecipientId())
                .text(doc.getText())
                .platform(doc.getPlatform())
                .createdAt(doc.getCreatedAt() != null ? doc.getCreatedAt().toString() : null)
                .debounceTimestamp(timestamp)
                .build();
        
        messageDelayedQueue.offer(payload, delay, TimeUnit.SECONDS);
        log.debug("Queued message to delayed queue: messageId={} conversationId={} delay={}s timestamp={}",
                doc.getId(), doc.getConversationId(), delay, timestamp);
    }
}
