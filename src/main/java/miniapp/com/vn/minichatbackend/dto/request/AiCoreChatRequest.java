package miniapp.com.vn.minichatbackend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body gửi tới AI Core POST /api/chat/message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCoreChatRequest {

    /** Tin nhắn hiện tại từ user */
    private String message;
    /** Lịch sử hội thoại (từ cache) */
    private List<ConversationTurnItem> conversations;
    @JsonProperty("customer_id")
    private Long customerId;
    @JsonProperty("business_id")
    private Long businessId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationTurnItem {
        private String role;
        private String content;
    }
}
