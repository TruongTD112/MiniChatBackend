package miniapp.com.vn.minichatbackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.document.MessageDocument;
import miniapp.com.vn.minichatbackend.dto.webhook.FacebookWebhookPayload;
import miniapp.com.vn.minichatbackend.entity.Channel;
import miniapp.com.vn.minichatbackend.entity.Conversation;
import miniapp.com.vn.minichatbackend.entity.CustomerUser;
import miniapp.com.vn.minichatbackend.repo.ChannelRepository;
import miniapp.com.vn.minichatbackend.repo.ConversationRepository;
import miniapp.com.vn.minichatbackend.repo.CustomerUserRepository;
import miniapp.com.vn.minichatbackend.repo.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Xử lý sự kiện từ Facebook Webhook: xác định conversation (channel + customer),
 * tạo/find Conversation và Customer nếu cần, lưu tin nhắn vào MongoDB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FacebookWebhookService {

    private static final String PLATFORM_FACEBOOK = "FACEBOOK";

    private final ChannelRepository channelRepository;
    private final CustomerUserRepository customerUserRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private InboundMessageQueueService inboundMessageQueueService;

    @Autowired(required = false)
    private ConversationMessageBroadcastService conversationMessageBroadcastService;

    private static final AtomicBoolean queueUnavailableLogged = new AtomicBoolean(false);

    /**
     * Xử lý payload webhook: chỉ xử lý entry có messaging (message từ user).
     * Với mỗi message: resolve channel -> customer -> conversation, lưu message vào MongoDB.
     */
    @Transactional
    public void handleWebhookPayload(FacebookWebhookPayload payload) {
        if (payload == null || !"page".equalsIgnoreCase(payload.getObject()) || payload.getEntries() == null) {
            return;
        }
        for (FacebookWebhookPayload.FacebookWebhookEntry entry : payload.getEntries()) {
            String pageId = entry.getPageId();
            if (pageId == null || entry.getMessaging() == null) {
                continue;
            }
            Optional<Channel> channelOpt = channelRepository.findByChannelIdAndPlatform(pageId, PLATFORM_FACEBOOK);
            if (channelOpt.isEmpty()) {
                log.warn("Webhook: Channel not found for pageId={}, skip entry", pageId);
                continue;
            }
            Channel channel = channelOpt.get();
            for (FacebookWebhookPayload.FacebookWebhookMessaging messaging : entry.getMessaging()) {
                if (messaging.getMessage() == null) {
                    continue; // delivery, read, postback... bỏ qua
                }
                // Chỉ lưu tin từ người dùng; bỏ qua echo (tin do Page gửi bị Facebook gửi lại)
                if (Boolean.TRUE.equals(messaging.getMessage().getIsEcho())) {
                    continue;
                }
                handleInboundMessage(channel, messaging);
            }
        }
    }

    private void handleInboundMessage(Channel channel, FacebookWebhookPayload.FacebookWebhookMessaging messaging) {
        String senderPsid = messaging.getSender() != null ? messaging.getSender().getId() : null;
        String recipientPageId = messaging.getRecipient() != null ? messaging.getRecipient().getId() : null;
        FacebookWebhookPayload.FacebookWebhookMessage msg = messaging.getMessage();
        if (senderPsid == null || msg == null) {
            return;
        }

        CustomerUser customer = findOrCreateCustomer(senderPsid);
        Conversation conversation = findOrCreateConversation(channel.getId(), customer.getId());

        // Cập nhật last_message_at cho conversation
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        MessageDocument doc = toMessageDocument(channel.getId(), conversation.getId(), recipientPageId, senderPsid, msg, messaging.getTimestamp());
        try {
            if (messageRepository.findByExternalMessageIdAndPlatform(msg.getMid(), PLATFORM_FACEBOOK).isEmpty()) {
                messageRepository.save(doc);
                log.info("Webhook: Saved message mid={} conversationId={} channelId={}", msg.getMid(), conversation.getId(), channel.getId());
                if (inboundMessageQueueService != null) {
                    inboundMessageQueueService.offerToDelayedQueue(doc);
                } else {
                    if (queueUnavailableLogged.compareAndSet(false, true)) {
                        log.warn("InboundMessageQueueService == null");
                    }
                }
                if (conversationMessageBroadcastService != null) {
                    conversationMessageBroadcastService.broadcastFromDocument(doc);
                }
            }
        } catch (Exception e) {
            log.error("Webhook: Failed to save message to MongoDB mid={}", msg.getMid(), e);
        }
    }

    private CustomerUser findOrCreateCustomer(String psid) {
        return customerUserRepository.findByProviderAndProviderId(PLATFORM_FACEBOOK, psid)
            .orElseGet(() -> {
                CustomerUser c = CustomerUser.builder()
                    .name("Facebook User")
                    .provider(PLATFORM_FACEBOOK)
                    .providerId(psid)
                    .status(1)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                return customerUserRepository.save(c);
            });
    }

    private Conversation findOrCreateConversation(Long channelId, Long customerId) {
        return conversationRepository.findFirstByChannelIdAndCustomerId(channelId, customerId)
            .orElseGet(() -> {
                LocalDateTime now = LocalDateTime.now();
                Conversation c = Conversation.builder()
                    .channelId(channelId)
                    .customerId(customerId)
                    .lastMessageAt(now)
                    .handlerType("HUMAN")
                    .status(1)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
                return conversationRepository.save(c);
            });
    }

    private MessageDocument toMessageDocument(Long channelId, Long conversationId, String recipientPageId,
                                              String senderPsid, FacebookWebhookPayload.FacebookWebhookMessage msg,
                                              Long timestamp) {
        List<MessageDocument.AttachmentInfo> attachments = null;
        if (msg.getAttachments() != null && !msg.getAttachments().isEmpty()) {
            attachments = msg.getAttachments().stream()
                .map(a -> {
                    String url = a.getPayload() != null ? a.getPayload().getUrl() : null;
                    return new MessageDocument.AttachmentInfo(a.getType(), url);
                })
                .collect(Collectors.toList());
        }
        Instant createdAt = timestamp != null ? Instant.ofEpochMilli(timestamp) : Instant.now();
        Map<String, Object> rawPayload = null;
        try {
            rawPayload = objectMapper.convertValue(msg, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ignored) {
        }
        return MessageDocument.builder()
            .conversationId(conversationId)
            .channelId(channelId)
            .externalMessageId(msg.getMid())
            .senderId(senderPsid)
            .recipientId(recipientPageId)
            .direction("INBOUND")
            .text(msg.getText())
            .attachments(attachments)
            .platform(PLATFORM_FACEBOOK)
            .createdAt(createdAt)
            .rawPayload(rawPayload)
            .build();
    }
}
