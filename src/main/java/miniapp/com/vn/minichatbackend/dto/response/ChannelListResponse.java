package miniapp.com.vn.minichatbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO chứa danh sách Channels đã connect
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response chứa danh sách Channels đã connect")
public class ChannelListResponse {
    @Schema(description = "Danh sách các Channels")
    private List<ChannelInfo> channels;
    
    /**
     * Thông tin của một Channel
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "Thông tin của một Channel")
    public static class ChannelInfo {
        @Schema(description = "ID của Channel trong database", example = "1")
        private Long id;
        
        @Schema(description = "Channel ID (Page ID từ Facebook)", example = "123456789012345")
        private String channelId;
        
        @Schema(description = "Tên của Channel/Page", example = "My Business Page")
        private String name;
        
        @Schema(description = "URL avatar của Channel/Page", example = "https://scontent.xx.fbcdn.net/v/...")
        private String avatarUrl;
        
        @Schema(description = "Platform (FACEBOOK, ZALO, etc.)", example = "FACEBOOK")
        private String platform;
        
        @Schema(description = "Status của Channel (1: active, 0: inactive)", example = "1")
        private Integer status;
        
        @Schema(description = "Thời gian tạo", example = "2024-01-01T10:00:00")
        private LocalDateTime createdAt;
        
        @Schema(description = "Thời gian cập nhật", example = "2024-01-01T10:00:00")
        private LocalDateTime updatedAt;
    }
}
