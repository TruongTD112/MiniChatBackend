package miniapp.com.vn.minichatbackend.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.common.Result;
import miniapp.com.vn.minichatbackend.document.MessageDocument;
import miniapp.com.vn.minichatbackend.dto.queue.InboundMessageQueuePayload;
import miniapp.com.vn.minichatbackend.dto.response.SendMessageResponse;
import miniapp.com.vn.minichatbackend.entity.Channel;
import miniapp.com.vn.minichatbackend.repo.ChannelRepository;
import miniapp.com.vn.minichatbackend.repo.MessageRepository;
import miniapp.com.vn.minichatbackend.service.ChannelService;
import miniapp.com.vn.minichatbackend.service.ConversationMessageBroadcastService;
import miniapp.com.vn.minichatbackend.service.FacebookConversationService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Giả lập bot: khi nhận tin từ user thì gửi "oke" từ Page về user qua Facebook,
 * lưu tin outbound vào Mongo và broadcast qua WebSocket cho dashboard.
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class SimulatedBotInboundMessageProcessor implements InboundMessageProcessor {

    private static final String BOT_REPLY_TEXT = "oke";
    private static final String PLATFORM_FACEBOOK = "FACEBOOK";

    private final ChannelRepository channelRepository;
    private final ChannelService channelService;
    private final FacebookConversationService facebookConversationService;
    private final MessageRepository messageRepository;
    private final ConversationMessageBroadcastService conversationMessageBroadcastService;

    @Override
    public void process(InboundMessageQueuePayload payload) {
        if (payload == null || payload.getChannelId() == null || payload.getSenderId() == null) {
            return;
        }
        var channelOpt = channelRepository.findById(payload.getChannelId());
        if (channelOpt.isEmpty()) {
            log.warn("SimulatedBot: channel not found channelId={}", payload.getChannelId());
            return;
        }
        Channel channel = channelOpt.get();
        if (!PLATFORM_FACEBOOK.equalsIgnoreCase(channel.getPlatform())) {
            return;
        }
        String pageAccessToken = channelService.getPageAccessToken(channel.getId());
        if (pageAccessToken == null) {
            log.warn("SimulatedBot: no page token for channelId={}", channel.getId());
            return;
        }
        // Gửi "oke" từ Page tới user (senderId = PSID của user)
        Result<SendMessageResponse> sendResult = facebookConversationService.sendMessage(
                pageAccessToken,
                payload.getSenderId(),
                BOT_REPLY_TEXT
        );
        if (!sendResult.isSuccess()) {
            log.warn("SimulatedBot: send failed channelId={} conversationId={} {}", 
                    payload.getChannelId(), payload.getConversationId(), sendResult.getMessage());
            return;
        }
        SendMessageResponse sent = sendResult.getData();
        // Lưu tin outbound vào Mongo (Page -> User)
        MessageDocument outbound = MessageDocument.builder()
                .conversationId(payload.getConversationId())
                .channelId(payload.getChannelId())
                .externalMessageId(sent != null ? sent.getMessageId() : null)
                .senderId(channel.getChannelId())
                .recipientId(payload.getSenderId())
                .direction("OUTBOUND")
                .text(BOT_REPLY_TEXT)
                .platform(PLATFORM_FACEBOOK)
                .createdAt(Instant.now())
                .build();
        try {
            messageRepository.save(outbound);
            conversationMessageBroadcastService.broadcastFromDocument(outbound);
            log.info("SimulatedBot: sent 'oke' conversationId={} channelId={}", 
                    payload.getConversationId(), payload.getChannelId());
        } catch (Exception e) {
            log.error("SimulatedBot: failed to save/broadcast outbound", e);
        }
    }
}
