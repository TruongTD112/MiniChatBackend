package miniapp.com.vn.minichatbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO để gửi tin nhắn từ Fanpage đến người dùng (chat với fanpage)
 */
@Getter
@Setter
@Schema(description = "Request gửi tin nhắn từ Fanpage đến người dùng (PSID)")
public class SendMessageRequest {
    @Schema(description = "Channel ID trong database (ID của Channel/Fanpage đã connect)", example = "1", required = true)
    private Long channelId;

    @Schema(description = "Page-Scoped User ID (PSID) - ID người nhận tin nhắn, lấy từ conversation participants/sender", example = "1254459154682919", required = true)
    private String recipientId;

    @Schema(description = "Nội dung tin nhắn text (Nếu gửi kèm ảnh, text sẽ được gửi trước)", example = "Xin chào! Chúng tôi đã nhận được tin nhắn của bạn.", required = false)
    private String text;

    @Schema(description = "URL ảnh để gửi (định dạng .jpg, .png, .gif, ...)", example = "https://example.com/image.jpg", required = false)
    private String imageUrl;
}
