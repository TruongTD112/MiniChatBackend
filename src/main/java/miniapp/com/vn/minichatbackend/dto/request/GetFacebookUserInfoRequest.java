package miniapp.com.vn.minichatbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO để lấy thông tin người dùng (tên, avatar) từ Facebook theo PSID
 */
@Getter
@Setter
@Schema(description = "Request lấy thông tin người dùng Facebook (tên, avatar) theo PSID")
public class GetFacebookUserInfoRequest {
    @Schema(description = "Channel ID trong database (ID của Channel/Fanpage đã connect)", example = "1", required = true)
    private Long channelId;

    @Schema(description = "Page-Scoped User ID (PSID) - ID người dùng cần lấy thông tin", example = "1254459154682919", required = true)
    private String psid;
}
