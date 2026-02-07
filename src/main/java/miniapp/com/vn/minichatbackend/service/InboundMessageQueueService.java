package miniapp.com.vn.minichatbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.config.WebhookQueueProperties;
import miniapp.com.vn.minichatbackend.document.MessageDocument;
import miniapp.com.vn.minichatbackend.dto.queue.InboundMessageQueuePayload;
import org.redisson.api.RDelayedQueue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Đẩy tin nhắn nhận từ webhook vào Redis Delayed Queue.
 * Sau delaySeconds, Redisson chuyển tin sang main queue để listener xử lý.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(RDelayedQueue.class)
    @ConditionalOnProperty(name = "app.webhook.queue.enabled", havingValue = "true")
public class InboundMessageQueueService {

    private final RDelayedQueue<InboundMessageQueuePayload> messageDelayedQueue;
    private final WebhookQueueProperties queueProperties;

    /**
     * Đẩy tin vừa lưu Mongo vào delayed queue (sau delay sẽ sang main queue).
     */
    public void offerToDelayedQueue(MessageDocument doc) {
        if (doc == null || doc.getId() == null) {
            return;
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
                .build();
        long delay = Math.max(1, queueProperties.getDelaySeconds());
        messageDelayedQueue.offer(payload, delay, TimeUnit.SECONDS);
        log.debug("Queued message to delayed queue: messageId={} conversationId={} delay={}s",
                doc.getId(), doc.getConversationId(), delay);
    }
}
