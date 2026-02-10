package miniapp.com.vn.minichatbackend.dto.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload gửi qua WebSocket khi có tin nhắn mới (user hoặc bot) trong conversation.
 * Client subscribe /topic/facebook/channel/{channelId} sẽ nhận được, dùng conversationId để nhóm theo hội thoại.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessageWsPayload {

    private Long conversationId;
    private Long channelId;
    private String messageId;
    private String externalMessageId;
    /** INBOUND = user -> page, OUTBOUND = page/bot -> user */
    private String direction;
    private String text;
    private String senderId;
    private String recipientId;
    private String platform;
    /** ISO-8601 */
    private String createdAt;

    private java.util.List<AttachmentInfo> attachments;

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AttachmentInfo {
        private String type;
        private String url;
    }
}
