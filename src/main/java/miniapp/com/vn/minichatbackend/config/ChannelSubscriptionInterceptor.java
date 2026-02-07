package miniapp.com.vn.minichatbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.entity.Channel;
import miniapp.com.vn.minichatbackend.repo.BackOfficeBusinessRepository;
import miniapp.com.vn.minichatbackend.repo.ChannelRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chỉ cho phép subscribe /topic/facebook/channel/{channelId} khi user thuộc business của channel đó.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelSubscriptionInterceptor implements ChannelInterceptor {

    private static final Pattern TOPIC_FACEBOOK_CHANNEL = Pattern.compile("^/topic/facebook/channel/(\\d+)$");

    private final ChannelRepository channelRepository;
    private final BackOfficeBusinessRepository backOfficeBusinessRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() != StompCommand.SUBSCRIBE) {
            return message;
        }
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/topic/facebook/channel/")) {
            return message;
        }
        Matcher m = TOPIC_FACEBOOK_CHANNEL.matcher(destination);
        if (!m.matches()) {
            return message;
        }
        long channelId = Long.parseLong(m.group(1));
        String userStr = accessor.getUser() != null ? accessor.getUser().getName() : null;
        if (userStr == null || userStr.isBlank()) {
            log.warn("WebSocket SUBSCRIBE denied: no user, destination={}", destination);
            return null;
        }
        long userId;
        try {
            userId = Long.parseLong(userStr);
        } catch (NumberFormatException e) {
            log.warn("WebSocket SUBSCRIBE denied: invalid user id, destination={}", destination);
            return null;
        }
        Optional<Channel> channelOpt = channelRepository.findById(channelId);
        if (channelOpt.isEmpty()) {
            log.warn("WebSocket SUBSCRIBE denied: channel not found, channelId={}", channelId);
            return null;
        }
        Channel ch = channelOpt.get();
        boolean hasAccess = backOfficeBusinessRepository
                .findByBackOfficeUserIdAndBusinessId(userId, ch.getBusinessId())
                .isPresent();
        if (!hasAccess) {
            log.warn("WebSocket SUBSCRIBE denied: user {} has no access to channel {} (business {})",
                    userId, channelId, ch.getBusinessId());
            return null;
        }
        log.debug("WebSocket SUBSCRIBE allowed: userId={}, channelId={}", userId, channelId);
        return message;
    }
}
