package miniapp.com.vn.minichatbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO để lấy danh sách Channels/Pages đã connect
 */
@Getter
@Setter
@Schema(description = "Request để lấy danh sách Channels đã connect")
public class GetChannelsRequest {
    /**
     * ID của Business
     */
    @Schema(
        description = "ID của Business để lấy danh sách channels",
        example = "1",
        required = true
    )
    private Long businessId;
    
    /**
     * Platform type (FACEBOOK, ZALO, etc.)
     * Nếu null hoặc rỗng thì lấy tất cả platforms
     */
    @Schema(
        description = "Loại platform (FACEBOOK, ZALO, etc.). Để trống hoặc null để lấy tất cả",
        example = "FACEBOOK",
        required = false
    )
    private String type;
}
