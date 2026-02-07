package miniapp.com.vn.minichatbackend.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Tin nhắn lưu trong MongoDB (từ webhook Facebook hoặc gửi đi).
 * Mỗi tin gắn với một conversation (id trong MySQL) và channel để biết cuộc hội thoại nào.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
@CompoundIndex(name = "conversation_created", def = "{ 'conversationId' : 1, 'createdAt' : -1 }")
public class MessageDocument {

    @Id
    private String id;

    /** ID conversation trong hệ thống (bảng Conversation - MySQL) */
    @Indexed
    private Long conversationId;

    /** ID channel trong hệ thống (bảng Channel - MySQL) */
    @Indexed
    private Long channelId;

    /** ID tin nhắn từ nền tảng (Facebook: mid) */
    private String externalMessageId;

    /** ID người gửi (Facebook: PSID hoặc Page ID) */
    private String senderId;

    /** ID người nhận (Facebook: PSID hoặc Page ID) */
    private String recipientId;

    /** Hướng: INBOUND (user -> page), OUTBOUND (page -> user) */
    private String direction;

    /** Nội dung text */
    private String text;

    /** Đính kèm (url, type) từ Facebook */
    private List<AttachmentInfo> attachments;

    /** Nền tảng: FACEBOOK, ZALO, ... */
    private String platform;

    /** Thời điểm tạo (từ webhook hoặc lúc gửi) */
    private Instant createdAt;

    /** Payload gốc từ webhook (optional, để debug hoặc xử lý sau) */
    private Map<String, Object> rawPayload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentInfo {
        private String type;  // image, video, audio, file
        private String url;
    }
}
