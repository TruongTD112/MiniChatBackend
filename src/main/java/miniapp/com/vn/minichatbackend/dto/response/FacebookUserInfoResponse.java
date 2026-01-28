package miniapp.com.vn.minichatbackend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Response DTO chứa thông tin người dùng Facebook (tên, avatar)
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Thông tin người dùng Facebook (tên, avatar)")
public class FacebookUserInfoResponse {
    @Schema(description = "Page-Scoped User ID (PSID)", example = "1254459154682919")
    private String id;

    @Schema(description = "Tên người dùng", example = "Nguyễn Văn A")
    private String name;

    @Schema(description = "URL avatar/profile picture", example = "https://platform lookaside.fbsbx.com/...")
    private String avatarUrl;
}
