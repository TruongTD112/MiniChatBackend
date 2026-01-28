package miniapp.com.vn.minichatbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO để lấy lịch sử tin nhắn (messages) của một Conversation từ Facebook
 */
@Getter
@Setter
@Schema(description = "Request để lấy lịch sử tin nhắn theo Conversation từ Facebook")
public class GetConversationHistoryRequest {
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
     * Conversation ID từ Facebook (id trả về từ API conversations, ví dụ: t_1234567890)
     */
    @Schema(
        description = "Conversation ID từ Facebook (id từ API conversations, ví dụ: t_1234567890)",
        example = "t_1234567890",
        required = true
    )
    private String conversationId;

    /**
     * Số lượng messages muốn lấy (limit).
     * Optional, mặc định là 25, tối đa 100.
     */
    @Schema(
        description = "Số lượng messages muốn lấy. Mặc định 25, tối đa 100",
        example = "25",
        required = false
    )
    private Integer limit;

    /**
     * Cursor pagination từ Facebook (trường after trong paging).
     * Để trống nếu lấy trang đầu.
     */
    @Schema(
        description = "Cursor để lấy trang tiếp theo (after từ paging của lần gọi trước). Để trống nếu lấy trang đầu",
        required = false
    )
    private String after;
}
