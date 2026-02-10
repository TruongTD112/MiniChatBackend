package miniapp.com.vn.minichatbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.document.MessageDocument;
import miniapp.com.vn.minichatbackend.dto.ws.ConversationMessageWsPayload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Broadcast tin nhắn mới (user hoặc bot) tới client đang subscribe /topic/facebook/channel/{channelId}.
 * Chỉ user của đúng business mới subscribe được nên chỉ họ mới nhận.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationMessageBroadcastService {

    private static final String TOPIC_PREFIX = "/topic/facebook/channel/";

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastNewMessage(Long channelId, ConversationMessageWsPayload payload) {
        if (channelId == null || payload == null) {
            return;
        }
        String destination = TOPIC_PREFIX + channelId;
        try {
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("Broadcast message to {}: conversationId={} direction={}",
                    destination, payload.getConversationId(), payload.getDirection());
        } catch (Exception e) {
            log.error("Failed to broadcast message to channelId={}", channelId, e);
        }
    }

    public void broadcastFromDocument(MessageDocument doc) {
        if (doc == null || doc.getChannelId() == null) {
            return;
        }
        ConversationMessageWsPayload payload = ConversationMessageWsPayload.builder()
                .conversationId(doc.getConversationId())
                .channelId(doc.getChannelId())
                .messageId(doc.getId())
                .externalMessageId(doc.getExternalMessageId())
                .direction(doc.getDirection())
                .text(doc.getText())
                .senderId(doc.getSenderId())
                .recipientId(doc.getRecipientId())
                .platform(doc.getPlatform())
                .createdAt(doc.getCreatedAt() != null ? doc.getCreatedAt().toString() : null)
                .attachments(doc.getAttachments() != null ? doc.getAttachments().stream()
                        .map(a -> new ConversationMessageWsPayload.AttachmentInfo(a.getType(), a.getUrl()))
                        .collect(java.util.stream.Collectors.toList()) : null)
                .build();
        broadcastNewMessage(doc.getChannelId(), payload);
    }
}
