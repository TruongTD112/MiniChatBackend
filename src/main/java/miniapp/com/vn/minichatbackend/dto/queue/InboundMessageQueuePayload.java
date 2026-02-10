package miniapp.com.vn.minichatbackend.dto.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Payload đẩy vào delayed queue / main queue (Redis).
 * Dùng kiểu dữ liệu đơn giản để serialize (Redisson/Jackson).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundMessageQueuePayload implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long conversationId;
    private Long channelId;
    /** ID tin trong MongoDB */
    private String messageId;
    private String externalMessageId;
    private String senderId;
    private String recipientId;
    private String text;
    private String platform;
    /** Thời điểm tạo tin (ISO-8601 string) */
    private String createdAt;
    
    /** Timestamp dùng để debounce (epoch millis) */
    private Long debounceTimestamp;
}
