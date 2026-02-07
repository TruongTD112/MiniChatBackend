package miniapp.com.vn.minichatbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import miniapp.com.vn.minichatbackend.security.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket STOMP: client kết nối /ws?token=JWT, subscribe /topic/facebook/channel/{channelId}
 * để nhận tin nhắn real-time của channel Facebook (chỉ user thuộc business của channel mới subscribe được).
 */
@Slf4j
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtService jwtService;
    private final ChannelSubscriptionInterceptor channelSubscriptionInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request,
                                                      WebSocketHandler handler,
                                                      Map<String, Object> attributes) {
                        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
                            return null;
                        }
                        String token = servletRequest.getServletRequest().getParameter("token");
                        if (token == null || token.isBlank()) {
                            log.warn("WebSocket handshake: missing token");
                            return null;
                        }
                        try {
                            Integer uid = jwtService.extractUserId(token);
                            if (uid == null) {
                                log.warn("WebSocket handshake: invalid token, no uid");
                                return null;
                            }
                            long userId = uid.longValue();
                            return () -> String.valueOf(userId);
                        } catch (Exception e) {
                            log.warn("WebSocket handshake: token invalid, {}", e.getMessage());
                            return null;
                        }
                    }
                })
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelSubscriptionInterceptor);
    }
}
