package miniapp.com.vn.minichatbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Response DTO sau khi gửi tin nhắn thành công qua Facebook Messenger
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response sau khi gửi tin nhắn thành công")
public class SendMessageResponse {
    @Schema(description = "Message ID từ Facebook", example = "m_AG5Hz2U...")
    private String messageId;

    @Schema(description = "Recipient ID (PSID) đã nhận tin nhắn", example = "1254459154682919")
    private String recipientId;
}
