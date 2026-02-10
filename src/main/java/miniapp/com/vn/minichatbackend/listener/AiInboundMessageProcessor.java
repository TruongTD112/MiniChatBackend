package miniapp.com.vn.minichatbackend.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.config.WebhookQueueProperties;
import miniapp.com.vn.minichatbackend.document.MessageDocument;
import miniapp.com.vn.minichatbackend.dto.queue.ConversationTurn;
import miniapp.com.vn.minichatbackend.dto.queue.InboundMessageQueuePayload;
import miniapp.com.vn.minichatbackend.dto.request.AiCoreChatRequest;
import miniapp.com.vn.minichatbackend.dto.response.AiCoreChatResponse;
import miniapp.com.vn.minichatbackend.dto.response.SendMessageResponse;
import miniapp.com.vn.minichatbackend.entity.Channel;
import miniapp.com.vn.minichatbackend.entity.Conversation;
import miniapp.com.vn.minichatbackend.repo.ChannelRepository;
import miniapp.com.vn.minichatbackend.repo.ConversationRepository;
import miniapp.com.vn.minichatbackend.repo.MessageRepository;
import miniapp.com.vn.minichatbackend.service.AiCoreChatService;
import miniapp.com.vn.minichatbackend.service.ChannelService;
import miniapp.com.vn.minichatbackend.service.ConversationMessageBroadcastService;
import miniapp.com.vn.minichatbackend.service.FacebookConversationService;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Xử lý tin inbound: lấy context từ cache (Redis), tìm business_id từ channelId,
 * gọi AI Core, đẩy response vào cache, lưu Mongo và gửi về khách hàng (Facebook + WebSocket).
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class AiInboundMessageProcessor implements InboundMessageProcessor {

    private static final String PLATFORM_FACEBOOK = "FACEBOOK";

    private final ChannelRepository channelRepository;
    private final ConversationRepository conversationRepository;
    private final WebhookQueueProperties queueProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AiCoreChatService aiCoreChatService;
    private final MessageRepository messageRepository;
    private final ConversationMessageBroadcastService conversationMessageBroadcastService;
    private final FacebookConversationService facebookConversationService;
    private final ChannelService channelService;

    @Override
    public void process(InboundMessageQueuePayload payload) {
        if (payload == null || payload.getConversationId() == null || payload.getChannelId() == null) {
            return;
        }

        Optional<Channel> channelOpt = channelRepository.findById(payload.getChannelId());
        if (channelOpt.isEmpty()) {
            log.warn("AiInbound: channel not found channelId={}", payload.getChannelId());
            return;
        }
        Channel channel = channelOpt.get();
        Long businessId = channel.getBusinessId();
        if (businessId == null) {
            log.warn("AiInbound: channel has no businessId channelId={}", payload.getChannelId());
            return;
        }

        Optional<Conversation> convOpt = conversationRepository.findById(payload.getConversationId());
        if (convOpt.isEmpty()) {
            log.warn("AiInbound: conversation not found conversationId={}", payload.getConversationId());
            return;
        }
        Long customerId = convOpt.get().getCustomerId();
        if (customerId == null) {
            log.warn("AiInbound: conversation has no customerId conversationId={}", payload.getConversationId());
            return;
        }

        List<AiCoreChatRequest.ConversationTurnItem> conversations = getConversationsFromCache(payload.getConversationId());

        AiCoreChatRequest request = AiCoreChatRequest.builder()
                .message(payload.getText() != null ? payload.getText() : "")
                .conversations(conversations)
                .customerId(customerId)
                .businessId(businessId)
                .build();

        AiCoreChatResponse aiResponse = aiCoreChatService.sendMessage(request);
        if (aiResponse == null || aiResponse.getData() == null || aiResponse.getData().getResponse() == null) {
            log.warn("AiInbound: no valid response from AI Core conversationId={}", payload.getConversationId());
            return;
        }

        String responseText = aiResponse.getData().getResponse();
        if (responseText == null) {
            responseText = "";
        }

        // Split by newline
        String[] splitMessages = responseText.split("\\R"); // \R matches any Unicode linebreak sequence

        for (String msgPart : splitMessages) {
            if (msgPart == null || msgPart.trim().isEmpty()) {
                continue;
            }

            String trimmedMsg = msgPart.trim();
            boolean isImage = isImageUrl(trimmedMsg);

            // 1. Context Cache (Redis)
            pushAssistantTurnToCache(payload.getConversationId(), trimmedMsg);

            // 2. DB (Mongo) & Broadcast (WebSocket)
            MessageDocument.MessageDocumentBuilder outboundBuilder = MessageDocument.builder()
                    .conversationId(payload.getConversationId())
                    .channelId(payload.getChannelId())
                    .externalMessageId(null)
                    .senderId(channel.getChannelId())
                    .recipientId(payload.getSenderId())
                    .direction("OUTBOUND")
                    .platform(PLATFORM_FACEBOOK)
                    .createdAt(Instant.now());

            if (isImage) {
                outboundBuilder.text("[Image]"); // Display placeholder in chat history for image
                outboundBuilder.attachments(List.of(new MessageDocument.AttachmentInfo("image", trimmedMsg)));
            } else {
                outboundBuilder.text(trimmedMsg);
            }

            MessageDocument outbound = outboundBuilder.build();

            try {
                messageRepository.save(outbound);
                conversationMessageBroadcastService.broadcastFromDocument(outbound);
            } catch (Exception e) {
                log.error("AiInbound: failed to save/broadcast outbound conversationId={}", payload.getConversationId(), e);
            }

            // 3. Send to Facebook
            if (PLATFORM_FACEBOOK.equalsIgnoreCase(channel.getPlatform())) {
                String pageAccessToken = channelService.getPageAccessToken(channel.getId());
                if (pageAccessToken != null) {
                    Result<SendMessageResponse> sendResult;
                    if (isImage) {
                        // Send as image attachment
                        sendResult = facebookConversationService.sendMessage(
                                pageAccessToken,
                                payload.getSenderId(),
                                null,
                                trimmedMsg
                        );
                    } else {
                        // Send as text
                        sendResult = facebookConversationService.sendMessage(
                                pageAccessToken,
                                payload.getSenderId(),
                                trimmedMsg,
                                null
                        );
                    }
                    if (!sendResult.isSuccess()) {
                        log.warn("AiInbound: Facebook send failed conversationId={} {}", payload.getConversationId(), sendResult.getMessage());
                    }
                } else {
                    log.warn("AiInbound: no page token for channelId={}", channel.getId());
                }
            }
        }
    }

    private boolean isImageUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        String lower = url.toLowerCase();
        return lower.startsWith("http") && (
                lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                lower.endsWith(".png") || lower.endsWith(".gif") ||
                lower.endsWith(".webp") || lower.endsWith(".bmp")
        );
    }

    private List<AiCoreChatRequest.ConversationTurnItem> getConversationsFromCache(Long conversationId) {
        String listKey = queueProperties.getConversationRecentPrefix() + conversationId;
        try {
            List<Object> raw = redisTemplate.opsForList().range(listKey, 0, -1);
            if (raw == null || raw.isEmpty()) {
                return new ArrayList<>();
            }
            List<AiCoreChatRequest.ConversationTurnItem> result = new ArrayList<>();
            for (Object item : raw) {
                if (item instanceof ConversationTurn) {
                    ConversationTurn t = (ConversationTurn) item;
                    result.add(new AiCoreChatRequest.ConversationTurnItem(t.getRole(), t.getContent()));
                } else if (item instanceof java.util.Map) {
                    try {
                        ConversationTurn t = objectMapper.convertValue(item, ConversationTurn.class);
                        result.add(new AiCoreChatRequest.ConversationTurnItem(t.getRole(), t.getContent()));
                    } catch (Exception e) {
                        log.trace("Skip unconvertible cache item: {}", e.getMessage());
                    }
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to read conversation cache conversationId={}", conversationId, e);
            return new ArrayList<>();
        }
    }

    private void pushAssistantTurnToCache(Long conversationId, String responseText) {
        String listKey = queueProperties.getConversationRecentPrefix() + conversationId;
        int maxN = Math.max(1, queueProperties.getConversationMaxMessages());
        try {
            ConversationTurn turn = new ConversationTurn("assistant", responseText != null ? responseText : "");
            redisTemplate.opsForList().rightPush(listKey, turn);
            redisTemplate.opsForList().trim(listKey, -maxN, -1);
        } catch (Exception e) {
            log.error("Failed to push assistant turn to cache conversationId={}", conversationId, e);
        }
    }
}
