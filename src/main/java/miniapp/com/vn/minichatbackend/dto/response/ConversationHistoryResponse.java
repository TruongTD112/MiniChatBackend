package miniapp.com.vn.minichatbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO chứa lịch sử tin nhắn (messages) của một Conversation từ Facebook
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response chứa lịch sử tin nhắn theo Conversation từ Facebook")
public class ConversationHistoryResponse {
    @Schema(description = "Conversation ID từ Facebook")
    private String conversationId;

    @Schema(description = "Danh sách các tin nhắn (messages) trong conversation, sắp xếp mới nhất trước")
    private List<MessageInfo> messages;

    @Schema(description = "Thông tin pagination từ Facebook")
    private PagingInfo paging;

    /**
     * Thông tin một Message
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Thông tin một tin nhắn trong conversation")
    public static class MessageInfo {
        @Schema(description = "Message ID từ Facebook", example = "m_1234567890")
        private String id;

        @Schema(description = "Nội dung tin nhắn", example = "Xin chào!")
        private String message;

        @Schema(description = "Thời gian tạo tin nhắn")
        private LocalDateTime createdTime;

        @Schema(description = "Người gửi (from)")
        private SenderInfo from;
    }

    /**
     * Thông tin người gửi
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Thông tin người gửi tin nhắn")
    public static class SenderInfo {
        @Schema(description = "Sender ID", example = "1234567890")
        private String id;

        @Schema(description = "Tên người gửi", example = "John Doe")
        private String name;
    }

    /**
     * Thông tin Pagination
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Thông tin Pagination")
    public static class PagingInfo {
        @Schema(description = "Cursor để lấy trang tiếp theo (after)")
        private String after;

        @Schema(description = "Cursor để lấy trang trước (before)")
        private String before;

        @Schema(description = "Link để lấy trang tiếp theo")
        private String next;

        @Schema(description = "Link để lấy trang trước")
        private String previous;
    }
}
