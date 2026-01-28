package miniapp.com.vn.minichatbackend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO chứa danh sách Conversations từ Facebook
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response chứa danh sách Conversations từ Facebook")
public class FacebookConversationResponse {
    @Schema(description = "Danh sách các Conversations")
    private List<ConversationInfo> conversations;
    
    @Schema(description = "Thông tin pagination từ Facebook")
    private PagingInfo paging;
    
    /**
     * Thông tin của một Conversation
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Thông tin của một Conversation")
    public static class ConversationInfo {
        @Schema(description = "Conversation ID từ Facebook", example = "t_1234567890")
        private String id;
        
        @Schema(description = "Link đến conversation trên Facebook", example = "https://www.facebook.com/...")
        private String link;
        
        @Schema(description = "Số lượng messages trong conversation", example = "5")
        private Integer messageCount;
        
        @Schema(description = "Thông tin participants trong conversation")
        private List<ParticipantInfo> participants;
        
        @Schema(description = "Thông tin sender")
        private SenderInfo sender;
        
        @Schema(description = "Thông tin message cuối cùng")
        private LastMessageInfo lastMessage;
        
        @Schema(description = "Thời gian cập nhật cuối cùng")
        private LocalDateTime updatedTime;
    }
    
    /**
     * Thông tin Participant
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Thông tin Participant trong conversation")
    public static class ParticipantInfo {
        @Schema(description = "Participant ID", example = "1234567890")
        private String id;
        
        @Schema(description = "Tên của participant", example = "John Doe")
        private String name;
    }
    
    /**
     * Thông tin Sender
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Thông tin Sender")
    public static class SenderInfo {
        @Schema(description = "Sender ID", example = "1234567890")
        private String id;
        
        @Schema(description = "Tên của sender", example = "John Doe")
        private String name;
    }
    
    /**
     * Thông tin Message cuối cùng
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Thông tin Message cuối cùng")
    public static class LastMessageInfo {
        @Schema(description = "Message ID", example = "m_1234567890")
        private String id;
        
        @Schema(description = "Nội dung message", example = "Hello, how can I help you?")
        private String message;
        
        @Schema(description = "Thời gian tạo message")
        private LocalDateTime createdTime;
        
        @Schema(description = "Từ ai (from)")
        private SenderInfo from;
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
        @Schema(description = "Link để lấy trang tiếp theo")
        private String next;
        
        @Schema(description = "Link để lấy trang trước")
        private String previous;
    }
}
