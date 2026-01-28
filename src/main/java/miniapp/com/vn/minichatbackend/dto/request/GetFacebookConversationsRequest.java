package miniapp.com.vn.minichatbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO để lấy danh sách Conversations từ Facebook Page
 */
@Getter
@Setter
@Schema(description = "Request để lấy danh sách Conversations từ Facebook Page")
public class GetFacebookConversationsRequest {
    /**
     * Channel ID trong database (ID của Channel đã connect)
     */
    @Schema(
        description = "Channel ID trong database (ID của Channel đã được connect)",
        example = "1",
        required = true
    )
    private Long channelId;
    
    /**
     * Số lượng conversations muốn lấy (limit)
     * Optional, mặc định là 25
     */
    @Schema(
        description = "Số lượng conversations muốn lấy (limit). Mặc định là 25",
        example = "25",
        required = false
    )
    private Integer limit;
}
