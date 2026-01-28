package miniapp.com.vn.minichatbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO để connect Facebook Page vào ứng dụng
 */
@Getter
@Setter
@Schema(description = "Request để connect Facebook Page vào ứng dụng")
public class ConnectFacebookPageRequest {
    /**
     * Facebook user access token từ client
     */
    @Schema(
        description = "Facebook user access token từ client sau khi đăng nhập Facebook",
        example = "EAABwzLix...",
        required = true
    )
    private String userToken;
    
    /**
     * Page ID của Facebook Page cần connect
     */
    @Schema(
        description = "Page ID của Facebook Page cần connect",
        example = "123456789012345",
        required = true
    )
    private String pageId;
    
    /**
     * Business ID để gán channel vào business
     */
    @Schema(
        description = "ID của Business để gán channel vào",
        example = "1",
        required = true
    )
    private Long businessId;
}
