package miniapp.com.vn.minichatbackend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO để lấy danh sách Facebook Pages
 */
@Getter
@Setter
@Schema(description = "Request để lấy danh sách Facebook Pages")
public class FacebookPageRequest {
    /**
     * Facebook user access token từ client
     */
    @Schema(
        description = "Facebook user access token từ client sau khi đăng nhập Facebook",
        example = "EAABwzLix...",
        required = true
    )
    private String userToken;
}
